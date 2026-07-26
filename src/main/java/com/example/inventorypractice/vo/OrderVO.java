package com.example.inventorypractice.vo;

import com.example.inventorypractice.entity.SysOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static OrderVO fromEntity(SysOrder order){
        OrderVO orderVO = new OrderVO();

        orderVO.setId(order.getId());
        orderVO.setUserId(order.getUserId());
        orderVO.setProductId(order.getProductId());
        orderVO.setQuantity(order.getQuantity());
        orderVO.setUnitPrice(order.getUnitPrice());
        orderVO.setTotalPrice(order.getTotalPrice());
        orderVO.setStatus(order.getStatus());
        orderVO.setStatusText(order.getStatus() == 1 ? "已创建" : "已取消");
        orderVO.setCreateTime(order.getCreateTime());
        orderVO.setUpdateTime(order.getUpdateTime());
        return orderVO;
    }
}
