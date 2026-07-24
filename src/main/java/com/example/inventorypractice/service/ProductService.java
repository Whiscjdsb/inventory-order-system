package com.example.inventorypractice.service;

import com.baomidou.mybatisplus.core.assist.ISqlRunner;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.inventorypractice.common.ApiResponse;
import com.example.inventorypractice.dto.CreateProductRequest;
import com.example.inventorypractice.dto.UpdateProductRequest;
import com.example.inventorypractice.entity.Product;
import com.example.inventorypractice.entity.StockOperation;
import com.example.inventorypractice.exception.BusinessException;
import com.example.inventorypractice.mapper.ProductMapper;
import com.example.inventorypractice.mapper.StockOperationMapper;
import com.example.inventorypractice.vo.ProductVO;
import com.example.inventorypractice.vo.StockOperationVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductService extends ServiceImpl <ProductMapper, Product>{
    private final ProductMapper productMapper;
    private final StockOperationService stockOperationService;
    private final StockOperationMapper stockOperationMapper;
    public ProductService(ProductMapper productMapper, StockOperationService stockOperationService, StockOperationMapper stockOperationMapper) {
        this.productMapper = productMapper;
        this.stockOperationService = stockOperationService;
        this.stockOperationMapper = stockOperationMapper;
    }

    public ProductVO createProduct(CreateProductRequest request){
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() == null ? 0 : request.getStock());
        product.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        product.setCreateTime(now);
        product.setUpdateTime(now);
        productMapper.insert(product);
        return ProductVO.fromEntity(product);
    }
    @Cacheable(cacheNames = "productById",key = "#id")
    public ProductVO getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return ProductVO.fromEntity(product);
    }
    @CacheEvict(cacheNames = "productById",key = "#id")
    public ProductVO updateProduct(Long id, UpdateProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        Product updatedProduct = productMapper.selectById(id);
        return ProductVO.fromEntity(updatedProduct);
    }
    @CacheEvict(cacheNames = "productById",key = "#id")
    public void  deleteProduct(Long id){
        Product product = productMapper.selectById(id);
        if(product == null){
            throw new BusinessException(404, "商品不存在");
        }
        productMapper.deleteById(id);
    }
    public List<ProductVO> getProductList(String name){
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()){
            queryWrapper.like(Product::getName, name.trim());
        }
        queryWrapper.orderByDesc(Product::getId);
        List<Product> products = productMapper.selectList(queryWrapper);
        return products.stream()
                .map(ProductVO::fromEntity)
                .toList();
    }
    public IPage<ProductVO> getProductPage(
            Long pageNum, Long pageSize, String name){
        if (pageNum == null || pageNum < 1) {
            throw new BusinessException(400, "页码必须大于等于1");
        }

        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "每页数量必须在1到100之间");
        }
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

        if (name != null && !name.isBlank()){
            queryWrapper.like(Product::getName, name.trim());
        }
        queryWrapper.orderByDesc(Product::getId);
        IPage<Product> productPage =
                productMapper.selectPage(page, queryWrapper);
        return productPage.convert(ProductVO::fromEntity);
    }
    @Transactional
    @CacheEvict(cacheNames = "productById", key = "#productId")
    public void deductStock(Long productId, Integer quantity) {
        int affectedRows = productMapper.deductStock(productId, quantity);
        if (affectedRows == 0) {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException(404, "商品不存在");
            }
            if (product.getStatus() == 0){
                throw new BusinessException(400, "商品已下架");
            }
            throw new BusinessException(400, "商品库存不足");
        }
        Product updatedProduct =
                productMapper.selectById(productId);

        Integer afterStock =
                updatedProduct.getStock();

        Integer beforeStock =
                afterStock + quantity;


        StockOperation stockOperation = new StockOperation();
        stockOperation.setProductId(productId);
        stockOperation.setOperationType(2);
        stockOperation.setQuantity(quantity);
        stockOperation.setBeforeStock(beforeStock);
        stockOperation.setAfterStock(afterStock);
        stockOperation.setCreateTime(LocalDateTime.now());

        stockOperationService.recordOperation(productId, 2, quantity, beforeStock, afterStock);

    }

    @Transactional
    @CacheEvict(cacheNames = "productById", key = "#productId")
    public ProductVO addStock(Long productId, Integer quantity){
        if (productId == null || productId < 1){
            throw new BusinessException(400, "商品ID必须大于等于1");
        }
        if (quantity == null || quantity < 1){
            throw new BusinessException(400, "商品数量必须大于等于1");
        }

        int affectedRows = productMapper.addStock(productId, quantity);
        if (affectedRows == 0){
                throw new BusinessException(404, "商品不存在");
            }


        Product updatedProduct = productMapper.selectById(productId);

        Integer afterStock = updatedProduct.getStock();
        Integer beforeStock = afterStock - quantity;

        StockOperation stockOperation = new StockOperation();
        stockOperation.setProductId(productId);
        stockOperation.setOperationType(1);
        stockOperation.setQuantity(quantity);
        stockOperation.setBeforeStock(beforeStock);
        stockOperation.setAfterStock(afterStock);
        stockOperation.setCreateTime(LocalDateTime.now());
        stockOperationService.recordOperation(productId, 1, quantity, beforeStock, afterStock);
        return ProductVO.fromEntity(updatedProduct);
    }
    @CacheEvict(cacheNames = "productById",key = "#id")
    public ProductVO updateProductStatus(Long id, Integer status){
        Product product = productMapper.selectById(id);
        if (product == null){
            throw new BusinessException(404, "商品不存在");
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        Product updatedProduct = productMapper.selectById(id);
        return ProductVO.fromEntity(updatedProduct);
    }

    public List<StockOperationVO> getStockOperationList(Long productId){
        if (productId == null || productId < 1){
            throw new BusinessException(400, "商品ID必须大于等于1");
        }
        Product product = productMapper.selectById(productId);
        if (product == null){
            throw new BusinessException(404, "商品不存在");
        }
        LambdaQueryWrapper<StockOperation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StockOperation::getProductId , productId);
        queryWrapper.orderByDesc(StockOperation::getId);

        List<StockOperation> stockOperations =
                stockOperationMapper.selectList(queryWrapper);
                return stockOperations.stream().map(StockOperationVO::fromEntity)
                        .toList();
    }

    public List<ProductVO> getLowStockProducts(Integer threshold){
        if(threshold == null || threshold < 0){
            throw new BusinessException(400, "库存阈值必须大于等于0");
        }
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(Product::getStock, threshold);
        queryWrapper.eq(Product::getStatus, 1);
        queryWrapper.orderByAsc(Product::getStock);
        List<Product> products =
                productMapper.selectList(queryWrapper);
                return products.stream().map(ProductVO::fromEntity).toList();


    }

    public List<ProductVO> getProductListByNameAndPrice(
            String name, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "最小价格必须大于等于0");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "最大价格必须大于等于0");
        }

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            queryWrapper.like(Product::getName, name.trim());
        }
        if (minPrice != null) {
            queryWrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le(Product::getPrice, maxPrice);
        }
        queryWrapper.orderByDesc(Product::getId);

        List<Product> products = productMapper.selectList(queryWrapper);
        return products.stream().map(ProductVO::fromEntity).toList();
    }
    @CacheEvict(cacheNames = "productById", allEntries = true)
    public void deleteProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "删除的ID列表不能为空");
        }
        for(Long id : ids) {
            if (id == null || id < 1){
                throw new BusinessException(400, "商品ID必须大于等于1");
            }
        }
        Set< Long> uniqueIds = new HashSet<>(ids);
        if (uniqueIds.size() != ids.size()) {
            throw new BusinessException(400, "删除的ID列表不能有重复");
        }

        // 先查是否存在（如果所有商品都存在才删，否则直接抛异常）
        List<Product> products = productMapper.selectBatchIds(ids);

        Set<Long> existingIds = new  HashSet<>();
        for(Product product : products) {
            existingIds.add(product.getId());
        }
        for (Long id : ids) {
            if (!existingIds.contains(id))
            {
                throw new BusinessException(404, "ID为"+ id +"商品不存在");
            }
        }

        // 批量删除
        productMapper.deleteBatchIds(ids);

    }
    @Transactional
    public List<ProductVO> createProducts(List<CreateProductRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(400, "创建商品列表不能为空");
        }

        List<ProductVO> result = new ArrayList<>();
        for (CreateProductRequest request : requests) {
            if (request.getName() == null || request.getName().isBlank()) {
                throw new BusinessException(400, "商品名称不能为空");
            }
            Product product = new Product();
            product.setName(request.getName());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock() == null ? 0 : request.getStock());
            product.setStatus(1);
            LocalDateTime now = LocalDateTime.now();
            product.setCreateTime(now);
            product.setUpdateTime(now);
            productMapper.insert(product);
            result.add(ProductVO.fromEntity(product));
        }
        return result;
    }
    @Transactional
    public List<ProductVO> createProductsBatch(List<CreateProductRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(400, "创建商品列表不能为空");
        }
        List<Product> products = new ArrayList<>();
        for (CreateProductRequest request : requests) {
            if (request.getName() == null || request.getName().isBlank()) {
                throw new BusinessException(400, "商品名称不能为空");
            }
            Product product = new Product();
            product.setName(request.getName());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock() == null ? 0 : request.getStock());
            product.setStatus(1);
            LocalDateTime now = LocalDateTime.now();
            product.setCreateTime(now);
            product.setUpdateTime(now);
            products.add(product);
        }
        saveBatch(products);
        return products.stream().map(ProductVO::fromEntity).toList();
    }

    @CacheEvict(cacheNames = "productById",key = "#id")
    public ProductVO restoreProduct(Long id) {
        if (id == null || id < 1) {
            throw new BusinessException(400, "商品ID必须大于等于1");
        }
        int affected = productMapper.restoreById(id);
        if (affected == 0) {
            throw new BusinessException(404, "商品不存在");
        }
        Product product = productMapper.selectById(id);
        return ProductVO.fromEntity(product);
    }
    public Long countProducts() {
        return productMapper.selectCount(null);
    }
    public Long countOnSaleProducts(){
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1);
        return productMapper.selectCount(queryWrapper);
        }

    public List<ProductVO> getOutOfStockProducts() {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1);
        queryWrapper.eq(Product::getStock, 0);
        queryWrapper.orderByDesc(Product::getId);
        List<Product> products = productMapper.selectList(queryWrapper);
        return products.stream().map(ProductVO::fromEntity).toList();
    }
    public BigDecimal calculateOnSaleStockValue(){
        return productMapper.calculateOnSaleStockValue();
    }
    }
