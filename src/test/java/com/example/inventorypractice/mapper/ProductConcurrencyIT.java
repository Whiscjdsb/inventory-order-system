package com.example.inventorypractice.mapper;

import com.example.inventorypractice.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
class ProductConcurrencyIT {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldPreventOversellingWhenTwoThreadsDeductStock()
            throws Exception {

        // 准备一个库存只有 1 的临时商品
        Product product = new Product();
        product.setName("并发扣减测试商品");
        product.setPrice(new BigDecimal("1.00"));
        product.setStock(1);
        product.setStatus(1);
        product.setIsDeleted(0);

        productMapper.insert(product);
        Long productId = product.getId();

        ExecutorService threadPool =
                Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Callable<Integer> deductTask = () -> {
                // 告诉主线程：当前线程已经准备好了
                readyLatch.countDown();

                // 等待主线程统一发出开始信号
                startLatch.await();

                return productMapper.deductStock(productId, 1);
            };

            Future<Integer> resultA =
                    threadPool.submit(deductTask);

            Future<Integer> resultB =
                    threadPool.submit(deductTask);

            // 等待两个线程都准备好
            readyLatch.await();

            // 同时放行两个线程
            startLatch.countDown();

            int successfulUpdates =
                    resultA.get() + resultB.get();

            Product updatedProduct =
                    productMapper.selectById(productId);

            System.out.println(
                    "成功扣减次数：" + successfulUpdates
            );
            System.out.println(
                    "最终库存：" + updatedProduct.getStock()
            );

            assertEquals(1, successfulUpdates);
            assertEquals(
                    0,
                    updatedProduct.getStock().intValue()
            );
        } finally {
            threadPool.shutdownNow();
            threadPool.awaitTermination(
                    5,
                    TimeUnit.SECONDS
            );

            // 物理删除临时数据，避免污染数据库
            jdbcTemplate.update(
                    "DELETE FROM product WHERE id = ?",
                    productId
            );
        }
    }
}