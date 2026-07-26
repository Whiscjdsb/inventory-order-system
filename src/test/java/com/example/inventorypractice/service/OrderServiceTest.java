package com.example.inventorypractice.service;

import com.example.inventorypractice.entity.SysOrder;
import com.example.inventorypractice.entity.SysUser;
import com.example.inventorypractice.exception.BusinessException;
import com.example.inventorypractice.mapper.ProductMapper;
import com.example.inventorypractice.mapper.SysOrderMapper;
import com.example.inventorypractice.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private SysOrderMapper sysOrderMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldNotRestoreStockWhenOrderAlreadyCancelled() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");

        SysOrder order = new SysOrder();
        order.setId(1L);
        order.setUserId(1L);
        order.setProductId(4L);
        order.setQuantity(2);
        order.setStatus(2);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(sysOrderMapper.selectById(1L))
                .thenReturn(order);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancelOrder(
                        "backend_intern",
                        1L
                )
        );

        assertEquals(400, exception.getCode());
        assertEquals("订单已经取消", exception.getMessage());

        verify(sysOrderMapper, never())
                .cancelOrder(anyLong(), anyLong());

        verify(productService, never())
                .addStock(anyLong(), anyInt());
    }
}