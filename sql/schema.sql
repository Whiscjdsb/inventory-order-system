CREATE DATABASE IF NOT EXISTS `inventory`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `inventory`;

CREATE TABLE IF NOT EXISTS `product` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL,
    `price` decimal(10, 2) NOT NULL,
    `stock` int NOT NULL DEFAULT '0',
    `status` tinyint NOT NULL DEFAULT '1',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_product_deleted_status_stock_id` (`is_deleted`, `status`, `stock`, `id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `stock_operation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `product_id` bigint NOT NULL,
    `operation_type` tinyint NOT NULL COMMENT '1-入库，2-出库',
    `quantity` int NOT NULL,
    `before_stock` int NOT NULL,
    `after_stock` int NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '加密后的密码',
    `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '角色',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用，0禁用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS `sys_order` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
   `user_id` bigint NOT NULL COMMENT '用户ID',
   `product_id` bigint NOT NULL COMMENT '商品ID',
   `quantity` int NOT NULL COMMENT '购买数量',
   `unit_price` decimal(10, 2) NOT NULL COMMENT '下单时商品单价',
    `total_price` decimal(12, 2) NOT NULL COMMENT '订单总价',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1已创建，2已取消',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_user_id` (`user_id`),
    KEY `idx_order_product_id` (`product_id`)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci
    COMMENT='订单表';