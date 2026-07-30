package com.example.inventorypractice.service;

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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private StockOperationService stockOperationService;

    @Mock
    private StockOperationMapper stockOperationMapper;

    @InjectMocks
    private ProductService productService;

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
}
