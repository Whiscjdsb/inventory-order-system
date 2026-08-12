package com.example.inventorypractice.service;

import com.example.inventorypractice.dto.CreateOrderRequest;
import com.example.inventorypractice.entity.Product;
import com.example.inventorypractice.entity.SysOrder;
import com.example.inventorypractice.entity.SysUser;
import com.example.inventorypractice.mapper.ProductMapper;
import com.example.inventorypractice.mapper.SysOrderMapper;
import com.example.inventorypractice.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "spring.cache.type=none"
})
class OrderTransactionRollbackIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private SysOrderMapper sysOrderMapper;

    @Test
    void shouldRollbackStockWhenOrderInsertFails() {
        LocalDateTime now = LocalDateTime.now();

        // 准备临时用户
        SysUser user = new SysUser();
        user.setUsername("transaction_test_user");
        user.setPassword("test-password");
        user.setRole("USER");
        user.setStatus(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        sysUserMapper.insert(user);

        // 准备库存为 5 的临时商品
        Product product = new Product();
        product.setName("事务回滚测试商品");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(5);
        product.setStatus(1);
        product.setIsDeleted(0);
        product.setCreateTime(now);
        product.setUpdateTime(now);
        productMapper.insert(product);

        Long userId = user.getId();
        Long productId = product.getId();

        try {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setProductId(productId);
            request.setQuantity(2);

            // 故意让“保存订单”这一步抛出异常
            doThrow(new RuntimeException("模拟订单保存失败"))
                    .when(sysOrderMapper)
                    .insert(any(SysOrder.class));

            assertThrows(
                    RuntimeException.class,
                    () -> orderService.createOrder(
                            user.getUsername(),
                            request
                    )
            );

            // 重新查询数据库
            Product productAfterFailure =
                    productMapper.selectById(productId);

            Integer operationCount =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM stock_operation
                            WHERE product_id = ?
                            """,
                            Integer.class,
                            productId
                    );

            Integer orderCount =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM sys_order
                            WHERE product_id = ?
                            """,
                            Integer.class,
                            productId
                    );

            // 库存应该从 3 回滚到原来的 5
            assertEquals(5, productAfterFailure.getStock());

            // 库存流水也应该回滚，没有留下记录
            assertEquals(0, operationCount);

            // 订单保存失败，所以订单数量是 0
            assertEquals(0, orderCount);
        } finally {
            // 清理测试产生的数据
            jdbcTemplate.update(
                    "DELETE FROM stock_operation WHERE product_id = ?",
                    productId
            );
            jdbcTemplate.update(
                    "DELETE FROM sys_order WHERE product_id = ?",
                    productId
            );
            jdbcTemplate.update(
                    "DELETE FROM product WHERE id = ?",
                    productId
            );
            jdbcTemplate.update(
                    "DELETE FROM sys_user WHERE id = ?",
                    userId
            );
        }
    }
}