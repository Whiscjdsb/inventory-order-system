package com.example.inventorypractice.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ProductStatus
{
    ON_SALE(1,"上架") ,
    OFF_SALE(0,"下架");

    private final int code;
    private final String text;

    ProductStatus(int code , String text){
        this.code = code;
        this.text = text;
    }
    public static String getText(Integer code){
        for (ProductStatus status : values()){
            if (Objects.equals(status.getCode(),code)){
                return status.getText();
            }

        }
        return "未知状态";
    }
}
