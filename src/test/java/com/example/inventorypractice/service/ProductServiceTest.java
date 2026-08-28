package com.example.inventorypractice.service;

import com.example.inventorypractice.dto.UpdateProductRequest;
import com.example.inventorypractice.entity.Product;
import com.example.inventorypractice.exception.BusinessException;
import com.example.inventorypractice.mapper.ProductMapper;
import com.example.inventorypractice.mapper.StockOperationMapper;
import com.example.inventorypractice.vo.ProductVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private StockOperationService stockOperationService;

    @Mock
    private StockOperationMapper stockOperationMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private ProductService productService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void shouldKeepStockWhenUpdatingProductInformation() {
        Product existingProduct = new Product();
        existingProduct.setId(4L);
        existingProduct.setName("旧商品名称");
        existingProduct.setPrice(new BigDecimal("100.00"));
        existingProduct.setStock(8);

        Product updatedProduct = new Product();
        updatedProduct.setId(4L);
        updatedProduct.setName("新商品名称");
        updatedProduct.setPrice(new BigDecimal("120.00"));
        updatedProduct.setStock(8);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("新商品名称");
        request.setPrice(new BigDecimal("120.00"));

        when(productMapper.selectById(4L))
                .thenReturn(existingProduct, updatedProduct);
        when(productMapper.updateProductInfo(
                4L,
                "新商品名称",
                new BigDecimal("120.00")
        )).thenReturn(1);

        ProductVO result = productService.updateProduct(4L, request);

        assertEquals("新商品名称", result.getName());
        assertEquals(new BigDecimal("120.00"), result.getPrice());
        assertEquals(8, result.getStock());
        verify(productMapper).updateProductInfo(
                4L,
                "新商品名称",
                new BigDecimal("120.00")
        );
        verify(productMapper, never()).updateById(any(Product.class));
    }

    @Test
    void shouldRejectProductInformationUpdateWhenStateHasChanged() {
        Product existingProduct = new Product();
        existingProduct.setId(4L);
        existingProduct.setStock(8);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("新商品名称");
        request.setPrice(new BigDecimal("120.00"));

        when(productMapper.selectById(4L))
                .thenReturn(existingProduct);
        when(productMapper.updateProductInfo(
                4L,
                "新商品名称",
                new BigDecimal("120.00")
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.updateProduct(4L, request)
        );

        assertEquals(409, exception.getCode());
        assertEquals("商品状态已变化，请重试", exception.getMessage());
        verify(productMapper, times(1)).selectById(4L);
        verify(productMapper, never()).updateById(any(Product.class));
    }

    @Test
    void shouldReturnOutOfStockProducts() {
        Product product = new Product();
        product.setId(100L);
        product.setName("缺货测试商品");
        product.setPrice(new BigDecimal("99.00"));
        product.setStock(0);
        product.setStatus(1);

        when(productMapper.selectList(any()))
                .thenReturn(List.of(product));

        List<ProductVO> result =
                productService.getOutOfStockProducts();

        assertEquals(1, result.size());
        assertEquals("缺货测试商品", result.get(0).getName());
        assertEquals(0, result.get(0).getStock());
        assertEquals("上架", result.get(0).getStatusText());
    }

    @Test
    void shouldThrowWhenStockIsInsufficient() {
        Product product = new Product();
        product.setId(4L);
        product.setStock(2);
        product.setStatus(1);

        when(productMapper.deductStock(4L, 8))
                .thenReturn(0);

        when(productMapper.selectById(4L))
                .thenReturn(product);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.deductStock(4L, 8)
        );

        assertEquals(400, exception.getCode());
        assertEquals("商品库存不足", exception.getMessage());

        verifyNoInteractions(stockOperationService);
    }

    @Test
    void shouldRejectNegativeDeductQuantity() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.deductStock(4L, -1)
        );
        assertEquals(400, exception.getCode());
        assertEquals("商品数量必须大于等于1", exception.getMessage());
        verifyNoInteractions(
                stockOperationService,
                productMapper,
                stockOperationMapper
                );
        }


    @Test
    void shouldCalculateOnSaleStockValue() {

        when(productMapper.calculateOnSaleStockValue())
                .thenReturn(new BigDecimal("81.00"));

        BigDecimal result =
                productService.calculateOnSaleStockValue();

        assertEquals(new BigDecimal("81.00"), result);
    }
    @Test
    void shouldRejectInvalidPriceRange() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.getProductListByNameAndPrice(
                        "测试商品",
                        new BigDecimal("1000.00"),
                        new BigDecimal("100.00")
                )
        );

        assertEquals(400, exception.getCode());
        assertEquals(
                "最小价格不能大于最大价格",
                exception.getMessage()
        );

        verifyNoInteractions(
                productMapper,
                stockOperationService,
                stockOperationMapper
        );
    }
    @Test
    void shouldNotQueryDatabaseWhenMissingMarkerExists() {
        Long productId = 999999L;
        String missingKey = "product:not-found:" + productId;

        when(stringRedisTemplate.hasKey(missingKey))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.getProductById(productId)
        );

        assertEquals(404, exception.getCode());
        assertEquals("商品不存在", exception.getMessage());

        verifyNoInteractions(productMapper);
    }
    @Test
    void shouldCacheMissingMarkerWhenProductDoesNotExist() {
        Long productId = 999999L;
        String missingKey = "product:not-found:" + productId;

        when(stringRedisTemplate.hasKey(missingKey))
                .thenReturn(false);

        when(productMapper.selectById(productId))
                .thenReturn(null);

        when(stringRedisTemplate.opsForValue())
                .thenReturn(valueOperations);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.getProductById(productId)
        );

        assertEquals(404, exception.getCode());
        assertEquals("商品不存在", exception.getMessage());

        verify(productMapper).selectById(productId);

        verify(valueOperations).set(
                missingKey,
                "1",
                5,
                TimeUnit.MINUTES
        );
    }
}
