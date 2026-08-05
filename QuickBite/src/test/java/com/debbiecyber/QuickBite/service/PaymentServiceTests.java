package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.entity.Order;
import com.debbiecyber.QuickBite.entity.Payment;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.dto.resquest.PaymentRequest;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.enums.PaymentMethod;
import com.debbiecyber.QuickBite.enums.PaymentStatus;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.repository.OrderRepository;
import com.debbiecyber.QuickBite.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTests {

    private static final String SECRET = "test_paystack_secret";

    @Test
    void confirmsOrderForAuthenticMatchingWebhook() throws Exception {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentService paymentService = paymentService(paymentRepository, orderRepository);
        Order order = Order.builder().orderStatus(OrderStatus.PENDING).build();
        Payment payment = Payment.builder()
                .order(order)
                .reference("QB_TEST")
                .amount(new BigDecimal("1500.00"))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.PAYSTACK)
                .build();
        String payload = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"QB_TEST\",\"amount\":150000,\"currency\":\"NGN\"}}";
        when(paymentRepository.findByReference("QB_TEST")).thenReturn(Optional.of(payment));

        paymentService.handleWebHook(payload.getBytes(StandardCharsets.UTF_8), signature(payload));

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
    }

    @Test
    void preservesTwoDecimalAmountsWhenValidatingPaystackKobo() throws Exception {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentService paymentService = paymentService(paymentRepository, orderRepository);
        Order order = Order.builder().orderStatus(OrderStatus.PENDING).build();
        Payment payment = Payment.builder()
                .order(order)
                .reference("QB_PRECISE")
                .amount(new BigDecimal("1500.01"))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.PAYSTACK)
                .build();
        String payload = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"QB_PRECISE\",\"amount\":150001,\"currency\":\"NGN\"}}";
        when(paymentRepository.findByReference("QB_PRECISE")).thenReturn(Optional.of(payment));

        paymentService.handleWebHook(payload.getBytes(StandardCharsets.UTF_8), signature(payload));

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
    }

    @Test
    void retryReturnsThePersistedAuthorizationUrlForAPendingPayment() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentService paymentService = paymentService(paymentRepository, orderRepository);
        User customer = User.builder().email("customer@quickbite.local").build();
        Order order = Order.builder()
                .id(42L)
                .customer(customer)
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.PAYSTACK)
                .totalAmount(new BigDecimal("2500.00"))
                .build();
        Payment payment = Payment.builder()
                .id(9L)
                .order(order)
                .reference("QB_RETRY")
                .amount(new BigDecimal("2500.00"))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.PAYSTACK)
                .authorizationUrl("https://checkout.example/QB_RETRY")
                .build();
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(42L);
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(42L)).thenReturn(Optional.of(payment));

        assertEquals(
                "https://checkout.example/QB_RETRY",
                paymentService.initializePayment(request, customer.getEmail()).getPaymentURL()
        );
    }

    @Test
    void rejectsWebhookWithInvalidSignature() {
        PaymentService paymentService = paymentService(
                mock(PaymentRepository.class),
                mock(OrderRepository.class)
        );

        assertThrows(
                BadRequestException.class,
                () -> paymentService.handleWebHook("{}".getBytes(StandardCharsets.UTF_8), "invalid")
        );
    }

    private PaymentService paymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        PaymentService paymentService = new PaymentService(
                paymentRepository,
                orderRepository,
                new RestTemplate()
        );
        ReflectionTestUtils.setField(paymentService, "paystackSecretKey", SECRET);
        return paymentService;
    }

    private String signature(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
