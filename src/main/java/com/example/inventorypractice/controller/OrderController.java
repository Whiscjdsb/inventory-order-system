package com.example.inventorypractice.controller;

import com.example.inventorypractice.common.ApiResponse;
import com.example.inventorypractice.dto.CreateOrderRequest;
import com.example.inventorypractice.service.OrderService;
import com.example.inventorypractice.vo.OrderVO;
import com.example.inventorypractice.vo.PageResult;
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
    public ApiResponse<PageResult<OrderVO>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        String username = authentication.getName();

        PageResult<OrderVO> result =
                orderService.getMyOrders(username, pageNum, pageSize);

        return ApiResponse.success(result);
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