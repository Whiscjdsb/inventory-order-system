package com.example.inventorypractice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.inventorypractice.common.ApiResponse;
import com.example.inventorypractice.dto.*;
import com.example.inventorypractice.service.ProductService;
import com.example.inventorypractice.vo.InventoryOverviewVO;
import com.example.inventorypractice.vo.ProductVO;
import com.example.inventorypractice.vo.StockOperationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.ibatis.jdbc.Null;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "商品管理", description = "商品、库存和状态相关接口")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<ProductVO> createProduct(
            @Valid @RequestBody CreateProductRequest request)  {
        return ApiResponse.success(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductVO> getProductById(@PathVariable Long id) {
        ProductVO productVO = productService.getProductById(id);
        return ApiResponse.success(productVO);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductVO> updateProduct(
            @PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.success(
                productService.updateProduct(id, request));
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<ProductVO>> getProductList(
            @RequestParam(required = false) String name) {
        return ApiResponse.success(productService.getProductList(name));
    }

    @GetMapping("/page")
    public ApiResponse<IPage<ProductVO>> getProductPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String name) {
        return ApiResponse.success(
                productService.getProductPage(pageNum, pageSize, name));
    }

    @PatchMapping("/{id}/stock/deduct")
    public ApiResponse<Void> deductStock(
            @PathVariable Long id, @Valid @RequestBody DeductStockRequest request) {
        productService.deductStock(id, request.getQuantity());
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ProductVO> updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductStatusRequest request) {
        return ApiResponse.success(
                productService.updateProductStatus(id, request.getStatus()));
    }
    @GetMapping("/{id}/stock-operations")
    public ApiResponse<List<StockOperationVO>> getStockOperationList(@PathVariable Long id) {
        return ApiResponse.success(
                productService.getStockOperationList(id));
    }
    @PatchMapping("/{id}/stock/add")
    public ApiResponse<ProductVO> addStock(
            @PathVariable Long id, @Valid @RequestBody AddStockRequest request
    ){
        return ApiResponse.success(
                productService.addStock(id, request.getQuantity()));
    }

    @GetMapping("/low-stock")
    public ApiResponse<List<ProductVO>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold){
        return ApiResponse.success(
                productService.getLowStockProducts(threshold));
    }


    @GetMapping("/filter")
    public ApiResponse<List<ProductVO>> getProductListByNameAndPrice(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ){
        return  ApiResponse.success(
                productService.getProductListByNameAndPrice(name, minPrice, maxPrice)
        );
    }
    @PostMapping("/batch-delete")
    public ApiResponse<Void> deleteProducts(@RequestBody List<Long> ids){
        productService.deleteProducts(ids);
        return ApiResponse.success(null);
    }
    @PostMapping("/batch")
    public ApiResponse<List<ProductVO>> createProducts(@RequestBody List<CreateProductRequest> requests){
         return ApiResponse.success(productService.createProducts(requests));
    }
    @PostMapping("/batch-v2")
    public ApiResponse<List<ProductVO>> createProductsBatch(@RequestBody List<CreateProductRequest> requests){
        return ApiResponse.success(productService.createProductsBatch(requests));
    }
    @PatchMapping("/{id}/restore")
    public ApiResponse<ProductVO> restoreProduct(@PathVariable Long id){
        return ApiResponse.success(productService.restoreProduct(id));
    }
    @GetMapping("/count")
    public ApiResponse<Long> countProducts(){
        return ApiResponse.success(productService.countProducts());
    }
    @GetMapping("/count/on-sale")
    public ApiResponse<Long> countOnSaleProducts(){
        return ApiResponse.success(productService.countOnSaleProducts());
    }

    @Operation(
            summary = "查询缺货商品",
            description = "查询未删除、已上架且库存为0的商品"
    )
    @GetMapping("/out-of-stock")
    public ApiResponse<List<ProductVO>> getOutOfStockProducts(){
        return ApiResponse.success(productService.getOutOfStockProducts());
    }
    @GetMapping("/stock-value/on-sale")
    public ApiResponse<BigDecimal> calculateOnSaleStockValue(){
        return ApiResponse.success(productService.calculateOnSaleStockValue());
    }
    @GetMapping("/overview")
    public ApiResponse<InventoryOverviewVO> getInventoryOverview(){
        return ApiResponse.success(productService.getInventoryOverview());
    }
}
