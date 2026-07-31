package com.example.inventorypractice.vo;

import com.example.inventorypractice.entity.Product;
import com.example.inventorypractice.enums.ProductStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO  implements Serializable {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ProductVO fromEntity(Product product) {
        ProductVO productVO = new ProductVO();

        productVO.setId(product.getId());
        productVO.setName(product.getName());
        productVO.setPrice(product.getPrice());
        productVO.setStock(product.getStock());
        productVO.setStatus(product.getStatus());
        productVO.setStatusText(
                ProductStatus.getText(product.getStatus()));
        productVO.setCreateTime(product.getCreateTime());
        productVO.setUpdateTime(product.getUpdateTime());
        return productVO;
    }
}
