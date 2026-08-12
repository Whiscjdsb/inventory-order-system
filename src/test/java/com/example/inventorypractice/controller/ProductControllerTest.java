package com.example.inventorypractice.controller;

import com.example.inventorypractice.security.JwtTokenProvider;
import com.example.inventorypractice.service.ProductService;
import com.example.inventorypractice.vo.InventoryOverviewVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;


    @Test
    void shouldRejectNegativeDeductQuantity() throws Exception {
        mockMvc.perform(
                        patch("/api/products/4/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quantity": -1
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message")
                        .value("扣减数量必须大于0"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(productService);
    }

    @Test
    void shouldDeductStockSuccessfully() throws Exception {
        mockMvc.perform(
                        patch("/api/products/4/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quantity": 2
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(productService).deductStock(4L, 2);
    }

    @Test
    void shouldGetInventoryOverviewSuccessfully() throws Exception {
        InventoryOverviewVO overview = new InventoryOverviewVO();
        overview.setTotalProducts(23L);
        overview.setOnSaleProducts(20L);
        overview.setOutOfStockProducts(2L);
        overview.setOnSaleStockValue(new BigDecimal("990.00"));


        when(productService.getInventoryOverview())
                .thenReturn(overview);

        mockMvc.perform(
                        get("/api/products/overview")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data.totalProducts").value(23))
                .andExpect(jsonPath("$.data.onSaleProducts").value(20))
                .andExpect(jsonPath("$.data.outOfStockProducts").value(2))
                .andExpect(jsonPath("$.data.onSaleStockValue").value(990.00));

        verify(productService).getInventoryOverview();
    }

}