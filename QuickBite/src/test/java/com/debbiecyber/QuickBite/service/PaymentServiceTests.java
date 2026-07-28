package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.entity.Order;
import com.debbiecyber.QuickBite.entity.Payment;
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
                .amount(1500.0)
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
