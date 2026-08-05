package com.debbiecyber.QuickBite.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.debbiecyber.QuickBite.dto.response.PaymentResponse;
import com.debbiecyber.QuickBite.dto.resquest.PaymentRequest;
import com.debbiecyber.QuickBite.entity.Order;
import com.debbiecyber.QuickBite.entity.Payment;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.enums.PaymentMethod;
import com.debbiecyber.QuickBite.enums.PaymentStatus;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.repository.OrderRepository;
import com.debbiecyber.QuickBite.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository  orderRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${paystack.secret.key}")
    private String paystackSecretKey;

    @Value("${paystack.base.url}")
    private String paystackBaseUrl;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;


    @Transactional
    public PaymentResponse initializePayment(PaymentRequest paymentRequest, String customerEmail) {
        Order order = orderRepository.findById(paymentRequest.getOrderId()).orElseThrow(()-> new ResourceNotFoundException("Order not found"));
        if (!order.getCustomer().getEmail().equals(customerEmail)) {
            throw new BadRequestException("This order doesn't belong to you ");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("This order has been processed");
        }
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            throw new BadRequestException("This order has been set to payment on delivery. No online payment needed");
        }
        Payment existingPayment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("This order has already been paid");
        }
        if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.PENDING
                && existingPayment.getAuthorizationUrl() != null) {
            return mapToResponse(existingPayment, existingPayment.getAuthorizationUrl());
        }

        String reference = "QB_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        Payment payment = existingPayment == null
                ? Payment.builder()
                    .order(order)
                    .reference(reference)
                    .amount(order.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.PAYSTACK)
                    .build()
                : existingPayment;
        payment.setReference(reference);
        payment.setStatus(PaymentStatus.PENDING);

        String paymentUrl = callPaystackInitialize(
                reference,
                order.getTotalAmount(),
                order.getCustomer().getEmail()
        );
        payment.setAuthorizationUrl(paymentUrl);
        paymentRepository.save(payment);
        return mapToResponse(payment, paymentUrl);
    }


    @Transactional
    public void handleWebHook(byte[] rawPayload, String paystackSignature) {
        verifyWebHookSignature(rawPayload, paystackSignature);

        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(rawPayload, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new BadRequestException("Invalid webhook payload");
        }

        String event = (String) payload.get("event");
        if (!"charge.success".equals(event) && !"charge.failed".equals(event)) {
            return;
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) {
            throw new BadRequestException("Webhook payment data is missing");
        }
        String reference = (String) data.get("reference");

        Payment payment = paymentRepository.findByReference(reference).orElseThrow(()->new ResourceNotFoundException("Payment not found with reference: " + reference));
        if ("charge.failed".equals(event)) {
            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
            return;
        }
        Number paidAmount = (Number) data.get("amount");
        long expectedAmountInKobo = payment.getAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
        if (paidAmount == null || paidAmount.longValue() != expectedAmountInKobo) {
            throw new BadRequestException("Webhook payment amount does not match the order amount");
        }
        Object currency = data.get("currency");
        if (currency != null && !"NGN".equalsIgnoreCase(currency.toString())) {
            throw new BadRequestException("Webhook payment currency is not supported");
        }
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }


    public PaymentResponse getPaymentByOrderId(Long orderId, String customerEmail) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(()-> new ResourceNotFoundException("Payment not found for order " + orderId));
        if (!payment.getOrder().getCustomer().getEmail().equals(customerEmail)) {
            throw new BadRequestException("This payment does not belong to you");
        }

        return mapToResponse(payment, null);
    }


    private String callPaystackInitialize(String reference, BigDecimal amount, String customerEmail) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + paystackSecretKey);

            Map<String, Object> body = new HashMap<>();
            body.put("email", customerEmail);
            body.put("amount", amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact());
            body.put("reference", reference);
            body.put("callback_url", frontendBaseUrl.replaceAll("/$", "") + "/orders?payment=return");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(paystackBaseUrl + "/transaction/initialize", entity, Map.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() !=null) {
                Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                return (String) data.get("authorization_url");
            }
            throw new BadRequestException("Paystack initialization failed");
        } catch (Exception e) {
            throw new BadRequestException("Paystack initialization failed: " + e.getMessage());
        }
    }


    private void verifyWebHookSignature(byte[] rawPayload, String paystackSignature) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(paystackSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(rawPayload);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            boolean signaturesMatch = paystackSignature != null && MessageDigest.isEqual(
                    hexString.toString().getBytes(StandardCharsets.US_ASCII),
                    paystackSignature.getBytes(StandardCharsets.US_ASCII)
            );
            if (!signaturesMatch) {
                throw new BadRequestException("Paystack signature verification failed");
            }
        } catch (BadRequestException e) {
            throw new BadRequestException("Webhook verification failed: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private PaymentResponse mapToResponse(Payment payment, String paymentUrl) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .reference(payment.getReference())
                .amount(payment.getAmount())
                .paymentStatus(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paymentURL(paymentUrl)
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
