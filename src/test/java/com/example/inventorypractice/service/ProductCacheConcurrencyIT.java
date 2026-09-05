package com.example.inventorypractice.service;

import com.example.inventorypractice.entity.Product;
import com.example.inventorypractice.mapper.ProductMapper;
import com.example.inventorypractice.vo.ProductVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 缓存击穿集成测试：
 * 一个热点商品缓存未命中时，多个线程并发调用 getProductById，
 * 分布式锁应保证只有极少数请求真正查数据库，其余请求等待后从缓存取。
 */
@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "REDIS_HOST=localhost",
        "REDIS_PORT=6380"
})
@Import(ProductCacheConcurrencyIT.CountingProductMapperConfig.class)
class ProductCacheConcurrencyIT {

    // 统计真实数据库 selectById 被调用的次数
    private static final AtomicInteger SELECT_BY_ID_COUNT =
            new AtomicInteger();

    @TestConfiguration
    static class CountingProductMapperConfig {

        @Bean
        @Primary
        ProductMapper countingProductMapper(
                @org.springframework.beans.factory.annotation.Qualifier(
                        "productMapper"
                ) ProductMapper realMapper) {
            // 用 JDK 动态代理包一层真实 Mapper：方法全转发，只数 selectById
            return (ProductMapper) Proxy.newProxyInstance(
                    ProductMapper.class.getClassLoader(),
                    new Class<?>[]{ProductMapper.class},
                    (proxy, method, args) -> {
                        if ("selectById".equals(method.getName())) {
                            SELECT_BY_ID_COUNT.incrementAndGet();
                        }
                        return method.invoke(realMapper, args);
                    });
        }
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldOnlyQueryDatabaseOnceWhenManyThreadsHitSameProduct()
            throws Exception {

        // ===== Arrange：准备一个数据库里真实存在、但缓存未命中的商品 =====
        Product product = new Product();
        product.setName("热点并发缓存测试商品");
        product.setPrice(new BigDecimal("999.00"));
        product.setStock(100);
        product.setStatus(1);
        product.setIsDeleted(0);

        productMapper.insert(product);
        Long productId = product.getId();

        String cacheKey = "product:id:" + productId;
        String missingKey = "product:not-found:" + productId;
        String lockKey = "product:lock:" + productId;

        // 清掉可能残留的缓存、空标记和锁，确保从"缓存未命中"开始
        stringRedisTemplate.delete(List.of(cacheKey, missingKey, lockKey));
        SELECT_BY_ID_COUNT.set(0);

        try {
            // ===== Act：10 个线程同时查同一个商品 =====
            int threadCount = 10;
            ExecutorService threadPool =
                    Executors.newFixedThreadPool(threadCount);

            CountDownLatch readyLatch =
                    new CountDownLatch(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);

            Callable<String> queryTask = () -> {
                // 告诉主线程：本线程已就绪，等统一发令
                readyLatch.countDown();
                startLatch.await();

                ProductVO vo = productService.getProductById(productId);
                return vo.getName();
            };

            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                results.add(threadPool.submit(queryTask));
            }

            // 等 10 个线程都就绪后，同时放行
            readyLatch.await();
            startLatch.countDown();

            // 收集所有线程的结果
            List<String> names = new ArrayList<>();
            for (Future<String> result : results) {
                names.add(result.get(10, TimeUnit.SECONDS));
            }

            // ===== Assert =====
            // 1. 所有线程都拿到了同一个商品（锁没有让任何人丢结果）
            for (String name : names) {
                assertNotNull(name);
                assertEquals("热点并发缓存测试商品", name);
            }

            // 2. 10 个并发，真正查数据库的次数应远小于 10
            //    理想是 1 次（抢到锁的人查库），允许极端情况下多 2 次兜底
            int dbQueryCount = SELECT_BY_ID_COUNT.get();
            System.out.println(
                    "并发线程数：" + threadCount
                            + "，实际查库次数：" + dbQueryCount
            );
            assertTrue(
                    dbQueryCount <= 3,
                    "查库次数 " + dbQueryCount
                            + " 应远小于并发线程数 " + threadCount
            );

            // 3. 锁用完了必须被删除，不能有死锁残留
            assertFalse(
                    Boolean.TRUE.equals(
                            stringRedisTemplate.hasKey(lockKey)
                    ),
                    "锁 key 应已被释放"
            );

            threadPool.shutdownNow();
        } finally {
            // 物理删除临时商品，并清掉 Redis 里的测试数据
            jdbcTemplate.update(
                    "DELETE FROM product WHERE id = ?",
                    productId
            );
            stringRedisTemplate.delete(
                    List.of(cacheKey, missingKey, lockKey)
            );
        }
    }

    @Autowired
    private ProductMapper productMapper;
}