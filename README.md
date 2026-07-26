# inventory-practice

基于 Spring Boot 开发的库存管理练习项目。

## 已实现功能

- 商品增删改查
- 库存增加与扣减
- JWT 登录认证
- 用户权限控制
- Redis 商品缓存
- 统一响应与异常处理
- Swagger / OpenAPI 接口文档
- Actuator 健康检查
- 用户创建订单与查询本人订单
- 原子取消订单并自动退回库存
- 订单、商品库存和库存记录的事务一致性

## 技术栈

- Java 17
- Spring Boot
- MyBatis-Plus
- MySQL
- Redis
- Spring Security
- JWT

## 环境要求

- Java 17
- Maven 3.9+
- MySQL 8
- Redis

MySQL 8 启动后，执行 [sql/schema.sql](sql/schema.sql) 创建 `inventory` 数据库和所需数据表。Redis 默认连接 `127.0.0.1:6379`。

## 环境变量

启动项目前需要配置：

| 变量名 | 作用 | 是否必填 |
| --- | --- | --- |
| `DB_PASSWORD` | MySQL 密码 | 是 |
| `JWT_SECRET` | Base64 格式的 JWT 密钥，解码后至少 32 字节 | 是 |
| `SERVER_PORT` | 服务端口 | 否，默认 `8080` |
| `DB_URL` | MySQL 地址 | 否 |
| `DB_USERNAME` | MySQL 用户名 | 否，默认 `root` |

Git Bash 可以使用下面的命令生成临时 JWT 密钥：

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
```

不要把真实密码或 JWT 密钥提交到 Git。

## 构建与运行

运行测试：

```bash
mvn test
```

打包项目：

```bash
mvn package
```

运行生成的 JAR：

```bash
java -jar target/inventory-practice-0.0.1-SNAPSHOT.jar
```

启动成功后可以访问：

- Swagger：`http://localhost:8080/swagger-ui.html`
- 健康检查：`http://localhost:8080/actuator/health`


## Docker Compose 运行

先打包最新代码：

```bash
mvn package
```

复制环境变量示例文件：

```powershell
Copy-Item .env.example .env
```

修改 `.env` 中的数据库密码和 JWT 密钥后，构建并启动全部服务：

```bash
docker compose up -d --build
```

查看容器状态：

```bash
docker compose ps
```

查看应用日志：

```bash
docker compose logs app --tail 100
```

停止并删除容器和网络：

```bash
docker compose down
```

默认端口：

- Spring Boot：`8080`
- MySQL：宿主机 `3307` → 容器 `3306`
- Redis：宿主机 `6380` → 容器 `6379`

`docker compose down` 会保留数据卷；不要随意使用 `docker compose down -v`，因为它会删除 MySQL 和 Redis 数据。

## 后续计划

- 补充订单核心自动化测试
- 完善项目部署说明
- 继续巩固 Java 与 Spring Boot 核心知识
