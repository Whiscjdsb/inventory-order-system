package com.example.inventorypractice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.inventorypractice.dto.CreateOrderRequest;
import com.example.inventorypractice.entity.Product;
import com.example.inventorypractice.entity.SysOrder;
import com.example.inventorypractice.entity.SysUser;
import com.example.inventorypractice.exception.BusinessException;
import com.example.inventorypractice.mapper.ProductMapper;
import com.example.inventorypractice.mapper.SysOrderMapper;
import com.example.inventorypractice.mapper.SysUserMapper;
import com.example.inventorypractice.vo.OrderVO;
import com.example.inventorypractice.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(4L);
        request.setQuantity(2);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");

        Product product = new Product();
        product.setId(4L);
        product.setPrice(new BigDecimal("100.00"));

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(productMapper.selectById(4L))
                .thenReturn(product);

        OrderVO result = orderService.createOrder(
                "backend_intern",
                request
        );

        assertEquals(
                new BigDecimal("200.00"),
                result.getTotalPrice()
        );
// 验证扣减商品 4 的 2 件库存
        verify(productService).deductStock(4L, 2);

// 验证保存订单，并且传入的是 SysOrder 对象
        verify(sysOrderMapper).insert(any(SysOrder.class));
    }
    @Test
    void shouldThrowWhenProductDoesNotExist(){
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(999999L);
        request.setQuantity(2);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(productMapper.selectById(999999L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(
                        "backend_intern",
                        request
                )
        );

        assertEquals(404, exception.getCode());
        assertEquals("商品不存在", exception.getMessage());

        verify(sysOrderMapper, never())
                .insert(any(SysOrder.class));

        verify(productService, never())
                .deductStock(anyLong(), anyInt());
    }

    @Test
    void shouldRejectInvalidOrderQuantity() {
        // 你来完成
//        创建 CreateOrderRequest。
        CreateOrderRequest request = new CreateOrderRequest();
//        productId 设置为 4L。
        request.setProductId(4L);
//        quantity 设置为 0。
        request.setQuantity(0);
//        使用 assertThrows() 调用 orderService.createOrder()。
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(
                        "backend_intern",
                        request
                )
        );
//        断言 code 为 400。
        assertEquals(400, exception.getCode());
//        断言 message 为 "购买数量必须大于等于1"。
        assertEquals("购买数量必须大于等于1", exception.getMessage());
//        使用 verifyNoInteractions() 验证以下对象都没有被调用：
//        sysUserMapper
//                productMapper
//        sysOrderMapper
//                productService
        verifyNoInteractions(
                sysUserMapper,
                productMapper,
                sysOrderMapper,
                productService
        );
    }
    @Test
    void shouldCancelOrderSuccessfully() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");

        SysOrder orderBeforeCancel = new SysOrder();
        orderBeforeCancel.setId(1L);
        orderBeforeCancel.setUserId(1L);
        orderBeforeCancel.setProductId(4L);
        orderBeforeCancel.setQuantity(2);
        orderBeforeCancel.setStatus(1);

        SysOrder orderAfterCancel = new SysOrder();
        orderAfterCancel.setId(1L);
        orderAfterCancel.setUserId(1L);
        orderAfterCancel.setProductId(4L);
        orderAfterCancel.setQuantity(2);
        orderAfterCancel.setStatus(2);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(sysOrderMapper.selectById(1L))
                .thenReturn(
                        orderBeforeCancel,
                        orderAfterCancel
                );

        when(sysOrderMapper.cancelOrder(1L, 1L))
                .thenReturn(1);

        // 你从这里完成：
        // 1. 调用 orderService.cancelOrder()
        OrderVO result = orderService.cancelOrder("backend_intern", 1L);
        // 2. 断言返回状态为 2
        assertEquals(2, result.getStatus());
        // 3. 验证 cancelOrder SQL 被调用
        verify(sysOrderMapper)
                .cancelOrder(1L, 1L);
        // 4. 验证商品 4 的 2 件库存被退回
        verify(productService)
                .addStock(4L, 2);
    }

    @Test
    void shouldThrowWhenOrderDoesNotExist() {
        // 你来写
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");

        SysOrder order = new SysOrder();
        order.setId(999999L);
        order.setUserId(1L);
        order.setProductId(1L);
        order.setQuantity(2);
        order.setStatus(1);


        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);
        when(sysOrderMapper.selectById(999999L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancelOrder("backend_intern", 999999L)
        );
        assertEquals(404, exception.getCode());
        assertEquals("订单不存在", exception.getMessage());

        verify(sysOrderMapper, never())
                .cancelOrder(anyLong(), anyLong());

        verify(productService, never())
                .addStock(anyLong(), anyInt());
    }

    @Test
    void shouldThrowWhenOrderBelongsToAnotherUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");

        SysOrder order = new SysOrder();
        order.setId(1L);
        order.setUserId(2L);
        order.setStatus(1);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(sysOrderMapper.selectById(1L))
                .thenReturn(order);

        // 你完成：
        // 使用 assertThrows 调用 cancelOrder()
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancelOrder("backend_intern", 1L)
        );
        // 断言 code = 404
        assertEquals(404, exception.getCode());
        // 断言 message = "订单不存在"
        assertEquals("订单不存在", exception.getMessage());
        verify(sysOrderMapper, never())
                .cancelOrder(anyLong(), anyLong());

        verify(productService, never())
                .addStock(anyLong(), anyInt());
    }
    @Test
    void shouldNotRestoreStockWhenOrderStatusChanged() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");

        SysOrder order = new SysOrder();
        order.setId(1L);
        order.setUserId(1L);
        order.setProductId(4L);
        order.setQuantity(2);
        order.setStatus(1);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(sysOrderMapper.selectById(1L))
                .thenReturn(order);

        when(sysOrderMapper.cancelOrder(1L, 1L))
                .thenReturn(0);

        // 你完成：
        // 1. assertThrows 调用 cancelOrder()
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancelOrder("backend_intern", 1L)
        );
        // 2. 断言 code = 400
        assertEquals(400, exception.getCode());
        // 3. 断言 message = "订单状态已变化，请重试"
        assertEquals("订单状态已变化，请重试", exception.getMessage());
        // 4. 验证 addStock() 从未调用
        verify(productService, never()).addStock(anyLong(), anyInt());
    }


    @Test
    void shouldThrowWhenCreatingOrderForMissingUser() {
        // 你从空白完成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(4L);
        request.setQuantity(2);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder("backend_intern", request)
        );

        assertEquals(401, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());

        verify(sysUserMapper).selectOne(any());

        verifyNoInteractions(
                productMapper,
                productService,
                sysOrderMapper
        );
    }

    @Test
    void shouldRejectInvalidProductId() {
        // 你从空白完成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(0L);
        request.setQuantity(2);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder("backend_intern", request)
        );

        assertEquals(400, exception.getCode());
        assertEquals("商品ID必须大于等于1", exception.getMessage());

        verifyNoInteractions(
                sysUserMapper,
                productMapper,
                sysOrderMapper,
                productService
        );

    }
    @Test
    void shouldRejectInvalidPageNumber() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.getMyOrders(
                        "backend_intern",
                        0,
                        10
                )
        );

        assertEquals(400, exception.getCode());
        assertEquals(
                "页码必须大于等于1",
                exception.getMessage()
        );

        verifyNoInteractions(
                sysUserMapper,
                sysOrderMapper,
                productMapper,
                productService
        );
    }
    @Test
    void shouldGetMyOrdersSuccessfully() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");

        SysOrder order = new SysOrder();
        order.setId(10L);
        order.setUserId(1L);
        order.setProductId(4L);
        order.setQuantity(2);
        order.setUnitPrice(new BigDecimal("100.00"));
        order.setTotalPrice(new BigDecimal("200.00"));
        order.setStatus(1);

        Page<SysOrder> orderPage = new Page<>(1, 10);
        orderPage.setRecords(List.of(order));
        orderPage.setTotal(1);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(sysOrderMapper.selectPage(any(Page.class), any()))
                .thenReturn(orderPage);


        PageResult<OrderVO> result = orderService.getMyOrders(
                "backend_intern",
                1,
                10
        );

        assertEquals(1L, result.getTotal());
        assertEquals(1L, result.getCurrent());
        assertEquals(10L, result.getSize());
        assertEquals(1, result.getRecords().size());
        assertEquals(10L, result.getRecords().get(0).getId());
        assertEquals(
                "已创建",
                result.getRecords().get(0).getStatusText()
        );
    }

    @Test
    void shouldReturnEmptyPageWhenUserHasNoOrders() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");


        Page<SysOrder> orderPage = new Page<>(1, 10);
        orderPage.setRecords(List.of());
        orderPage.setTotal(0);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(sysOrderMapper.selectPage(any(Page.class), any()))
                .thenReturn(orderPage);

        PageResult<OrderVO> result = orderService.getMyOrders(
                "backend_intern",
                1,
                10
        );

        assertEquals(0L, result.getTotal());
        assertEquals(1L, result.getCurrent());
        assertEquals(10L, result.getSize());
        assertEquals(0, result.getRecords().size());


        verifyNoInteractions(
                productMapper,
                productService
        );
    }
      @Test
    void shouldReturnThreeOrdersOnFirstPage() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");

        SysOrder order1 = new SysOrder();
        order1.setId(1L);
        order1.setUserId(1L);
        order1.setStatus(1);
        order1.setQuantity(1);
        order1.setUnitPrice(new BigDecimal("7999"));
        order1.setTotalPrice(new BigDecimal("7999"));

        SysOrder order2 = new SysOrder();
        order2.setId(2L);
        order2.setUserId(1L);
        order2.setStatus(1);
        order2.setQuantity(2);
        order2.setUnitPrice(new BigDecimal("99"));
        order2.setTotalPrice(new BigDecimal("198"));

        SysOrder order3 = new SysOrder();
        order3.setId(3L);
        order3.setUserId(1L);
        order3.setStatus(1);
        order3.setQuantity(1);
        order3.setUnitPrice(new BigDecimal("50"));
        order3.setTotalPrice(new BigDecimal("50"));

        Page<SysOrder> orderPage = new Page<>(1, 10);
        orderPage.setRecords(List.of(order1, order2, order3));
        orderPage.setTotal(3);

        when(sysUserMapper.selectOne(any())).thenReturn(user);
        when(sysOrderMapper.selectPage(any(Page.class), any())).thenReturn(orderPage);

        PageResult<OrderVO> result = orderService.getMyOrders("backend_intern", 1, 10);

        assertEquals(3L, result.getTotal());
        assertEquals(3, result.getRecords().size());
        assertEquals(1L, result.getCurrent());
        assertEquals(10L, result.getSize());
    }
}