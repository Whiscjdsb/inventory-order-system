package com.example.inventorypractice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {
    private final SysOrderMapper sysOrderMapper;
    private final SysUserMapper sysUserMapper;
    private final ProductMapper productMapper;
    private final ProductService productService;

    public OrderService(SysOrderMapper sysOrderMapper, SysUserMapper sysUserMapper, ProductMapper productMapper, ProductService productService) {
        this.sysOrderMapper = sysOrderMapper;
        this.sysUserMapper = sysUserMapper;
        this.productMapper = productMapper;
        this.productService = productService;
    }

    @Transactional
    public OrderVO createOrder(String username, CreateOrderRequest request) {
        SysUser user = getUserByUsername(username);
        Product product = productMapper.selectById(request.getProductId());

        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        BigDecimal unitPrice = product.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        productService.deductStock(product.getId(), request.getQuantity());

        SysOrder sysOrder = new SysOrder();


        sysOrder.setUserId(user.getId());
        sysOrder.setProductId(product.getId());
        sysOrder.setQuantity(request.getQuantity());
        sysOrder.setUnitPrice(unitPrice);
        sysOrder.setTotalPrice(totalPrice);
        sysOrder.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        sysOrder.setCreateTime(now);
        sysOrder.setUpdateTime(now);

        sysOrderMapper.insert(sysOrder);

        return OrderVO.fromEntity(sysOrder);

    }

    public PageResult<OrderVO> getMyOrders(String username, long pageNum, long pageSize) {
        if (pageNum < 1) {
            throw new BusinessException(400, "页码必须大于等于1");
        }

        if (pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "每页数量必须在1到100之间");
        }

        SysUser user = getUserByUsername(username);

        LambdaQueryWrapper<SysOrder> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(SysOrder::getUserId, user.getId());
        queryWrapper.orderByDesc(SysOrder::getId);

        Page<SysOrder> page = new Page<>(pageNum, pageSize);

        IPage<SysOrder> orderPage = sysOrderMapper.selectPage(page, queryWrapper);

        List<OrderVO> records = orderPage.getRecords().stream().map(OrderVO::fromEntity).toList();

        PageResult<OrderVO> result = new PageResult<>();
        result.setTotal(orderPage.getTotal());
        result.setCurrent(orderPage.getCurrent());
        result.setSize(orderPage.getSize());
        result.setRecords(records);

        return result;
    }

    @Transactional
    public OrderVO cancelOrder(String username, Long orderId) {
//        1. 检查orderId是否合法
        if (orderId == null || orderId < 1) {
            throw new BusinessException(400, "订单ID必须大于0");
        }
//        2. 根据username查询当前用户
        SysUser user = getUserByUsername(username);
//        3. 根据orderId查询订单
        SysOrder order = sysOrderMapper.selectById(orderId);
//        4. 判断订单是否属于当前用户
        if (order == null || !Objects.equals(order.getUserId(), user.getId())) {
            throw new BusinessException(404, "订单不存在");
        }
//        5. 判断订单是否已经取消
        if (order.getStatus() == 2) {
            throw new BusinessException(400, "订单已经取消");
        }
//        6. 执行原子取消SQL
        int affectedRows = sysOrderMapper.cancelOrder(orderId, user.getId());
        if (affectedRows == 0) {
            throw new BusinessException(400, "订单状态已变化，请重试");
        }
//        7. 只有影响行数为1，才退回库存
        productService.addStock(order.getProductId(), order.getQuantity());
//        8. 重新查询订单并返回OrderVO
        SysOrder updatedOrder = sysOrderMapper.selectById(orderId);
        return OrderVO.fromEntity(updatedOrder);
    }

    private SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(SysUser::getUsername, username);

        SysUser user = sysUserMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        return user;
    }
}
