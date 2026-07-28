package com.debbiecyber.QuickBite.controller;


import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.PaymentResponse;
import com.debbiecyber.QuickBite.dto.resquest.PaymentRequest;
import com.debbiecyber.QuickBite.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/payments")
@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping("/initialize_payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<PaymentResponse>> initializePayment(@Valid @RequestBody PaymentRequest paymentRequest, @AuthenticationPrincipal UserDetails userDetails) {
        PaymentResponse paymentResponse = paymentService.initializePayment(paymentRequest, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Payment initialized successfully", paymentResponse));
    }


    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody byte[] payload, @RequestHeader("x-paystack-signature") String paystackSignature) {
        paymentService.handleWebHook(payload, paystackSignature);

        return ResponseEntity.ok().build();
    }


    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Long orderId, @AuthenticationPrincipal UserDetails userDetails) {
        PaymentResponse paymentResponse = paymentService.getPaymentByOrderId(orderId, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Payment fetched successfully", paymentResponse));
    }
}
