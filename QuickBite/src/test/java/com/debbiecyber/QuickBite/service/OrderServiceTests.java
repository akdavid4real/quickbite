package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.dto.resquest.DeliveryProofRequest;
import com.debbiecyber.QuickBite.entity.Order;
import com.debbiecyber.QuickBite.entity.Restaurant;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.enums.PaymentMethod;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.repository.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTests {
    @Test
    void customerCanCancelPendingOrder() {
        Fixture fixture = fixture(OrderStatus.PENDING);

        var response = fixture.service.cancelOrder(10L, "customer@test.local");

        assertEquals(OrderStatus.CANCELLED, response.getOrderStatus());
        verify(fixture.orderRepository).save(fixture.order);
    }

    @Test
    void deliveryProofCompletesAssignedDelivery() {
        Fixture fixture = fixture(OrderStatus.OUT_FOR_DELIVERY);
        DeliveryProofRequest request = new DeliveryProofRequest();
        request.setEvidenceUrl("https://example.test/proof.jpg"); request.setNotes("Handed to customer");
        when(fixture.deliveryProofRepository.existsByOrderId(10L)).thenReturn(false);
        when(fixture.deliveryProofRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(fixture.deliveryProofRepository.findByOrderId(10L)).thenReturn(Optional.empty());

        var response = fixture.service.submitDeliveryProof(10L, "rider@test.local", request);

        assertEquals(OrderStatus.DELIVERED, response.getOrderStatus());
        verify(fixture.deliveryProofRepository).save(any());
    }

    private Fixture fixture(OrderStatus status) {
        OrderRepository orders = mock(OrderRepository.class);
        OrderItemRepository items = mock(OrderItemRepository.class);
        CartRepository carts = mock(CartRepository.class);
        UserRepository users = mock(UserRepository.class);
        RestaurantRepository restaurants = mock(RestaurantRepository.class);
        MapService map = mock(MapService.class);
        DeliveryProofRepository proofs = mock(DeliveryProofRepository.class);
        OrderService service = new OrderService(orders, items, carts, users, restaurants, map, proofs);
        User customer = User.builder().id(1L).name("Customer").email("customer@test.local").phoneNumber("0801").role(UserRole.CUSTOMER).build();
        User owner = User.builder().id(2L).name("Owner").email("owner@test.local").phoneNumber("0802").role(UserRole.RESTAURANT_OWNER).build();
        User rider = User.builder().id(3L).name("Rider").email("rider@test.local").phoneNumber("0803").role(UserRole.RIDER).build();
        Restaurant restaurant = Restaurant.builder().id(4L).name("Kitchen").phoneNumber("0804").owner(owner).build();
        Order order = Order.builder().id(10L).customer(customer).restaurant(restaurant).rider(rider)
                .orderStatus(status).paymentMethod(PaymentMethod.CASH_ON_DELIVERY).deliveryAddress("10 Test Street")
                .subTotal(new BigDecimal("1000.00")).deliveryFee(new BigDecimal("500.00")).totalAmount(new BigDecimal("1500.00")).build();
        when(orders.findById(10L)).thenReturn(Optional.of(order));
        when(orders.save(order)).thenReturn(order);
        when(items.findByOrderId(10L)).thenReturn(List.of());
        when(proofs.findByOrderId(10L)).thenReturn(Optional.empty());
        return new Fixture(service, orders, proofs, order);
    }

    private record Fixture(OrderService service, OrderRepository orderRepository,
                           DeliveryProofRepository deliveryProofRepository, Order order) {}
}
