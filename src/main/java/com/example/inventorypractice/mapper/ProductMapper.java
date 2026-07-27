package com.example.inventorypractice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.inventorypractice.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    @Update("""
        UPDATE product
        SET stock = stock - #{quantity},
            update_time = CURRENT_TIMESTAMP
        WHERE id = #{productId}
          AND stock >= #{quantity}
        AND status = 1
        AND is_deleted = 0
       """)
    int deductStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity
    );
    @Update("""
        UPDATE product
        SET stock = stock + #{quantity},
            update_time = CURRENT_TIMESTAMP
        WHERE id = #{productId}
       """)
    int addStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity
    );

    @Update("UPDATE product SET is_deleted = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);

    @Select("""
    SELECT COALESCE(SUM(price * stock), 0)
    FROM product
    WHERE status = 1
      AND is_deleted = 0
    """)
    BigDecimal calculateOnSaleStockValue();
}
