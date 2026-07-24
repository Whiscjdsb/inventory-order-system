package com.example.inventorypractice.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryOverviewVO {
    private Long totalProducts;
    private Long onSaleProducts;
    private Long outOfStockProducts;
    private BigDecimal onSaleStockValue;
}