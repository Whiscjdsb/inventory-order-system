package com.example.inventorypractice.controller;

import com.example.inventorypractice.common.ApiResponse;
import com.example.inventorypractice.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("管理员接口访问成功");
    }

    @PostMapping("/users/{id}/disable")
    public ApiResponse<Void> disableUser(@PathVariable Long id) {
        authService.disableUser(id);
        return ApiResponse.success();
    }
}