package com.example.inventorypractice.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private Long total;
    private Long current;
    private Long size;
    private List<T> records;
}