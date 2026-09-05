package com.example.inventorypractice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.example.inventorypractice.vo.InventoryOverviewVO;
import com.example.inventorypractice.vo.ProductVO;
import com.example.inventorypractice.vo.StockOperationVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductService extends ServiceImpl <ProductMapper, Product>{
    private final ProductMapper productMapper;
    private final StockOperationService stockOperationService;
    private final StockOperationMapper stockOperationMapper;
    private static final String MISSING_PRODUCT_KEY_PREFIX =
            "product:not-found:";

    private static final String LOCK_KEY_PREFIX = "product:lock:";
    private static final long LOCK_EXPIRE_SECONDS = 10;

    private static final String PRODUCT_CACHE_KEY_PREFIX = "product:id:";
    private static final long PRODUCT_CACHE_TTL_MINUTES = 30;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public ProductService(
            ProductMapper productMapper,
            StockOperationService stockOperationService,
            StockOperationMapper stockOperationMapper,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper
    ) {
        this.productMapper = productMapper;
        this.stockOperationService = stockOperationService;
        this.stockOperationMapper = stockOperationMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }
    private void clearMissingProductMarker(Long id) {
        stringRedisTemplate.delete(
                MISSING_PRODUCT_KEY_PREFIX + id
        );
    }

    // 商品数据变化后，删掉手动缓存 key，避免读到旧的缓存值
    private void evictProductCache(Long id) {
        stringRedisTemplate.delete(
                PRODUCT_CACHE_KEY_PREFIX + id
        );
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
        clearMissingProductMarker(product.getId());
        return ProductVO.fromEntity(product);
    }
    public ProductVO getProductById(Long id) {
        String missingKey =
                MISSING_PRODUCT_KEY_PREFIX + id;
        String cacheKey =
                PRODUCT_CACHE_KEY_PREFIX + id;

        // 第 1 关：缓存穿透——商品不存在，空标记直接拦截
        if (Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(missingKey)
        )) {
            throw new BusinessException(404, "商品不存在");
        }

        // 第 2 关：缓存命中，直接返回，不查数据库
        ProductVO cached = readCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 第 3 关：缓存未命中——抢锁，只放一个请求去查库
        String lockKey = LOCK_KEY_PREFIX + id;
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(
                        lockKey,
                        "1",
                        LOCK_EXPIRE_SECONDS,
                        TimeUnit.SECONDS
                );

        if (Boolean.TRUE.equals(locked)) {
            try {
                // 双检：抢到锁后再看一次缓存（可能别人已回填），避免重复查库
                ProductVO recheck = readCache(cacheKey);
                if (recheck != null) {
                    return recheck;
                }

                Product product = productMapper.selectById(id);

                if (product == null) {
                    stringRedisTemplate.opsForValue().set(
                            missingKey,
                            "1",
                            5,
                            TimeUnit.MINUTES
                    );

                    throw new BusinessException(404, "商品不存在");
                }

                ProductVO vo = ProductVO.fromEntity(product);
                writeCache(cacheKey, vo);
                return vo;
            } finally {
                stringRedisTemplate.delete(lockKey);
            }
        }

        // 没抢到锁：别人正在查库。等 50ms 后去看缓存，不重复查库
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        ProductVO cachedAfterWait = readCache(cacheKey);
        if (cachedAfterWait != null) {
            return cachedAfterWait;
        }

        // 菜单还没有（极端情况）→ 才去查数据库兜底
        Product product = productMapper.selectById(id);
        if (product == null) {
            stringRedisTemplate.opsForValue().set(
                    missingKey,
                    "1",
                    5,
                    TimeUnit.MINUTES
            );
            throw new BusinessException(404, "商品不存在");
        }
        return ProductVO.fromEntity(product);
    }

    private ProductVO readCache(String cacheKey) {
        String json = stringRedisTemplate.opsForValue().get(cacheKey);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ProductVO.class);
        } catch (JsonProcessingException e) {
            // 缓存里是脏数据，忽略，当作未命中重新查库
            return null;
        }
    }

    private void writeCache(String cacheKey, ProductVO vo) {
        try {
            String json = objectMapper.writeValueAsString(vo);
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    json,
                    PRODUCT_CACHE_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            // 序列化失败时不写缓存，不影响查库结果
        }
    }
    public ProductVO updateProduct(Long id, UpdateProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        int affectedRows = productMapper.updateProductInfo(
                id,
                request.getName(),
                request.getPrice()
        );

        if (affectedRows == 0) {
            throw new BusinessException(
                    409,
                    "商品状态已变化，请重试"
            );
        }
        Product updatedProduct = productMapper.selectById(id);
        evictProductCache(id);
        return ProductVO.fromEntity(updatedProduct);
    }
    public void  deleteProduct(Long id){
        Product product = productMapper.selectById(id);
        if(product == null){
            throw new BusinessException(404, "商品不存在");
        }
        productMapper.deleteById(id);
        evictProductCache(id);
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
    public void deductStock(Long productId, Integer quantity) {
        if (productId == null || productId < 1){
            throw new BusinessException(400, "商品ID必须大于等于1");
        }
        if (quantity == null || quantity < 1){
            throw new BusinessException(400, "商品数量必须大于等于1");
        }

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

        stockOperationService.recordOperation(productId, 2, quantity, beforeStock, afterStock);
        evictProductCache(productId);

    }

    @Transactional
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

        stockOperationService.recordOperation(productId, 1, quantity, beforeStock, afterStock);
        evictProductCache(productId);
        return ProductVO.fromEntity(updatedProduct);
    }
    public ProductVO updateProductStatus(Long id, Integer status){
        Product product = productMapper.selectById(id);
        if (product == null){
            throw new BusinessException(404, "商品不存在");
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        Product updatedProduct = productMapper.selectById(id);
        evictProductCache(id);
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
        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException(
                    400,
                    "最小价格不能大于最大价格"
            );
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
        Set<Long> existingIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
        for (Long id : ids) {
            if (!existingIds.contains(id))
            {
                throw new BusinessException(404, "ID为"+ id +"商品不存在");
            }
        }

        // 批量删除
        productMapper.deleteBatchIds(ids);
        for (Long id : ids) {
            evictProductCache(id);
        }

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
            clearMissingProductMarker(product.getId());
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
        for (Product product : products) {
            clearMissingProductMarker(product.getId());
        }
        return products.stream().map(ProductVO::fromEntity).toList();
    }

    public ProductVO restoreProduct(Long id) {
        if (id == null || id < 1) {
            throw new BusinessException(400, "商品ID必须大于等于1");
        }
        int affected = productMapper.restoreById(id);
        if (affected == 0) {
            throw new BusinessException(404, "商品不存在");
        }

        clearMissingProductMarker(id);
        evictProductCache(id);

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
    public InventoryOverviewVO getInventoryOverview() {
        return productMapper.selectInventoryOverview();
    }
    }
