package com.example.inventorypractice.controller;

import com.example.inventorypractice.common.ApiResponse;
import com.example.inventorypractice.dto.CreateOrderRequest;
import com.example.inventorypractice.service.OrderService;
import com.example.inventorypractice.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderVO> createOrder(
            Authentication  authentication, @Valid @RequestBody CreateOrderRequest request) {
        String username = authentication.getName();
        OrderVO orderVO = orderService.createOrder(username, request);
        return ApiResponse.success(orderVO);
    }
    @GetMapping("/my")
    public ApiResponse<List<OrderVO>> getMyOrders(
            Authentication authentication
    ){
        String username = authentication.getName();
        List<OrderVO> orderVOs = orderService.getMyOrders(username);
        return ApiResponse.success(orderVOs);

    }
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderVO> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ){
        String username = authentication.getName();
        OrderVO orderVO = orderService.cancelOrder(username, orderId);
        return ApiResponse.success(orderVO);
    }

}