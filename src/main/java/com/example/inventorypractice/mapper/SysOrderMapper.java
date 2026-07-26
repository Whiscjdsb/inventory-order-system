package com.example.inventorypractice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.inventorypractice.entity.SysOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysOrderMapper extends BaseMapper<SysOrder> {
    @Update("""
    UPDATE sys_order
    SET status = 2,
        update_time = CURRENT_TIMESTAMP
    WHERE id = #{orderId}
      AND user_id = #{userId}
      AND status = 1
    """)
    int cancelOrder(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );

}
