package com.example.inventorypractice.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum OrderStatus {

    CREATED(1, "已创建"),
    CANCELLED(2, "已取消");

    private final int code;
    private final String text;

    OrderStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static String getText(Integer code) {
        for (OrderStatus status : values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status.getText();
            }
        }

        return "未知状态";
    }
}