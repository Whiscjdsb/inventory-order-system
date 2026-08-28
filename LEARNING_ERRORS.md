# 项目错误复盘

更新时间：2026-08-18

> 只记录项目开发、数据库、测试、Docker 和 Git 中高价值且容易重复的错误。Java 算法错误记录在 `JAVA_ALGORITHM_ERRORS.md`。

## 一分钟排错路线

先判断错误发生在哪个阶段：

```text
编译阶段 → Java 语法、类型、导入、方法签名
测试启动阶段 → Spring Bean、环境变量、测试配置
请求进入前 → 端口、进程、Docker、URL、HTTP 方法
Controller → JSON、@Valid、参数绑定、权限
Service → 业务校验、执行顺序、事务
Mapper / SQL → 字段、表结构、条件、索引、连接库
Redis / MySQL → 服务状态、地址、端口、密码、网络
Docker → 镜像是否最新、容器状态、日志、Volume
```

遇到多层异常时，从最底层 `Caused by` 开始，不要只看最外层 `ApplicationContext`、`MyBatisSystemException` 或 `BUILD FAILURE`。

## 当前优先检查清单

1. 当前终端是否位于 `inventory-order-system` 仓库目录。
2. 文件是否保存，运行的是 IDEA 新代码还是旧 Docker 镜像。
3. HTTP 方法、URL、请求体和 JWT 是否正确。
4. 返回类型是单个对象、列表、分页还是 `Void`。
5. Mapper 返回 Entity 后是否转换成 VO。
6. `null` 校验是否放在解引用之前，短路条件是否正确。
7. MySQL 和 Redis 使用的是本机端口还是容器内部端口。
8. 环境变量是否进入当前启动方式或测试进程。
9. Flyway 迁移是否执行，真实数据库是否具有对应字段和索引。
10. Mockito 数据是否自洽，`when()` 是否真的能执行到。

## Java 与类型

### `if` 后误加分号

错误：

```java
if (condition);
{
    throw new BusinessException(400, "错误");
}
```

分号已经结束 `if`，代码块会无条件执行。右括号后应直接写 `{`。

### 单个、列表和无响应类型混淆

```text
单个商品 → ProductVO
多个商品 → List<ProductVO>
分页结果 → PageResult<ProductVO> 或 IPage<ProductVO>
没有 data → ApiResponse<Void>
```

不能使用不存在的 `Null` 泛型，也不能用 `ProductVO` 接收 `selectList()`。

### Mapper 返回 Entity，Controller 返回 VO

```text
数据库 → Product Entity → ProductVO → ApiResponse
```

`ProductMapper.selectList()` 返回 `List<Product>`，不能直接赋给 `List<ProductVO>`。

### 空值判断顺序

正确：

```java
if (id == null || id < 1) {
    // ...
}
```

`||` 会短路；先拦截 `null` 才能避免拆箱或方法调用触发空指针。

### BigDecimal 比较

不能使用 `>` 或 `<`：

```java
minPrice.compareTo(BigDecimal.ZERO) < 0
maxPrice.compareTo(minPrice) < 0
```

金额优先使用字符串或 `BigDecimal.valueOf()` 创建，避免 `new BigDecimal(0.1)` 的二进制浮点误差。

## Spring、认证与接口

### 修改代码后仍返回旧结果

依次确认：

```text
保存文件
→ 停止旧 IDEA 进程或容器
→ mvn package
→ docker compose up -d --build
→ 查看应用启动日志
```

源码修改不会自动进入已经构建好的 JAR 和镜像。

### 401、403 与业务异常

- `401 Unauthorized`：没有有效认证，例如缺少、无效或过期 JWT。
- `403 Forbidden`：已经认证，但角色权限不足。
- `400`：请求或业务参数不合法。
- `404`：当前用户可见范围内资源不存在。
- `409`：资源状态冲突，可用于重复或并发冲突场景。

### JWT 配置缺失

典型根因：

```text
Could not resolve placeholder 'JWT_SECRET'
```

重启本身不是修复。应先给当前 IDEA、Maven 或 Docker 启动环境提供 `JWT_SECRET`，再重新执行原失败操作。

### DTO、Entity、VO 混淆

- Request DTO：接收前端数据和校验注解。
- Entity：映射数据库表，包含后端持久化字段。
- VO：控制返回给前端的字段和文字表示。

`@Valid` 校验的是请求 DTO，不是 VO。

## Mockito 与测试

### 把 `when()` 理解成读取参数

```java
when(mapper.selectOne(any())).thenReturn(user);
```

含义是预设模拟对象的返回行为，不会执行真实 SQL，也不能证明数据库正确。

### 用断言代替调用验证

- `assertEquals()`：检查返回值或异常内容。
- `verify()`：检查依赖方法是否调用。
- `verifyNoInteractions()`：检查指定依赖完全没有调用。

不能通过再次调用 Mapper 并 `assertEquals()` 来证明 Service 调用了 Mapper。

### `assertThrows()` 调错对象

要测试 Service 的业务异常，必须调用 Service：

```java
assertThrows(
        BusinessException.class,
        () -> orderService.cancelOrder(username, orderId)
);
```

直接调用 Mapper 只是在测试 Mapper，不会执行 Service 的异常判断。

### 不必要的 `when()`

如果参数校验在第一步就抛异常，后续 Mapper 不会调用，因此不需要准备用户或商品 Mock；应验证依赖没有交互。

### 模拟数据不自洽

空分页必须同时满足：

```java
orderPage.setTotal(0);
orderPage.setRecords(List.of());
```

`total = 0` 但 `records` 有一条数据，会制造真实查询通常不会返回的矛盾状态。

### 单元测试不能证明真实事务和并发

```java
when(productMapper.deductStock(...)).thenReturn(1);
```

只证明“Mapper 返回 1 时 Service 如何处理”。真实 SQL 条件、MySQL 行锁、并发竞争和事务回滚必须使用集成测试验证。

## MySQL、SQL 与 Flyway

### 本机与容器端口混淆

```text
Windows → Docker MySQL：localhost:3307
应用容器 → MySQL 容器：mysql:3306
Windows → Docker Redis：localhost:6380
应用容器 → Redis 容器：redis:6379
```

容器之间通过服务名通信，不使用 Windows 的映射端口。
- IDEA 开发可能连本机 MySQL（`localhost:3306`，MySQL 8.0），Docker app 容器连 3307（MySQL 8.4）。看启动日志 Flyway `Database: jdbc:mysql://localhost:3306...` 那行可确认实际连的是哪个库。

### 条件和关联字段写错

- 商品与订单关联：`o.product_id = p.id`，不是 `o.id = p.id`。
- 用户与订单关联：`o.user_id = u.id`。
- 查询库存等于 0 使用 `eq` 或 `= 0`，不是 `lt`。
- 日统计选择 `DATE(create_time)` 时，`GROUP BY` 也应使用相同表达式。
- `AND`、`OR` 混合时使用括号明确优先级。

### LEFT JOIN 条件位置

- 条件放 `ON`：保留左表全部行，不匹配的右表字段为 `NULL`。
- 条件放 `WHERE`：连接完成后过滤，可能把没有匹配的左表行一起去掉。

### 只看影响行数，不看事务结果

日志中的 `Updates: 1` 只表示当前事务中修改了一行，不代表已经提交。后续异常仍可能让整个事务回滚。

验证回滚要等事务结束后，从事务外重新查询数据库；事务内已有 Java 对象不会自动恢复字段值。

### Flyway 迁移文件

- 文件名格式：`V2__description.sql`，版本号后是两个下划线。
- 已成功执行的 V1 不再修改；结构变化新增 V2、V3。
- `checksum mismatch` 表示历史中记录的脚本与当前文件内容不一致。
- `FlywayMigrateException` 只是外层错误，要继续查看真正失败的 SQL。
- 修改 `.env` 的 MySQL 密码不会自动改变已有 Volume 中数据库账号的密码。
- 迁移文件必须带 `.sql` 扩展名，否则 Flyway 直接忽略；日志会显示 `up to date. No migration necessary`，但该版本实际没跑。
- 撤销已执行的迁移只能新建反向版本（如 V4 `DROP COLUMN`）；删除已执行的 V3 文件会触发 missing/checksum mismatch。
- 应用返回 200 不等于数据库一致：Entity 删字段后 SELECT 不再查该列，孤儿列残留也不报错。迁移类修复必须同时验证表结构 + `flyway_schema_history`，不能只看 HTTP。

### 索引判断

- 不因为某列是主键就默认它最适合查询。
- 先看 `WHERE`、`ORDER BY`、数据量和 `EXPLAIN ANALYZE`。
- 联合索引通常把稳定的等值条件放在左侧，但最终以真实执行计划和性能证据为准。

## Docker、Maven 与 PowerShell

### Docker 命令存在但引擎未启动

`docker --version` 只证明客户端存在。出现 Docker API pipe 错误时，先启动 Docker Desktop，再执行 `docker compose ps`。

### 镜像、容器和 Volume

- 镜像：创建容器的只读模板。
- 容器：镜像的运行实例。
- Volume：独立于容器保存数据。

停止或删除容器不会自动删除镜像；`docker compose down` 默认保留 Volume，`down -v` 会删除数据卷。

### Maven 命令

- `mvn test`：编译并运行测试。
- `mvn package`：测试通过后生成 JAR。
- `java -jar ...`：运行已生成的 JAR。

新终端找不到 `mvn` 时，检查 Maven `bin` 和 JDK `bin` 是否已加入用户 `Path`。

### PowerShell 脚本乱码

中文字符串编码损坏可能破坏引号并引发一串 ParserError。脚本交付前使用 PowerShell Parser 检查，运行提示优先使用 ASCII。

## Git

### `AM` 的含义

`AM file` 表示文件已暂存过一次，暂存后又继续修改。需要重新执行 `git add file`，暂存区才包含最新版。

### LF / CRLF 提示

这是换行符转换警告，不等于 `git add` 或提交失败。使用 `git diff --cached --check` 检查真正的空白问题。

### 仓库目录

出现 `not a git repository` 时先执行：

```powershell
git rev-parse --show-toplevel
```

确认当前目录位于真实仓库内，再执行 remote、commit 或 push。

## 修复后的验证要求

```text
编译错误 → 原命令重新执行且 BUILD SUCCESS
单元测试 → 对应测试 Failures: 0、Errors: 0
接口错误 → 用相同请求重新验证状态码和响应体
SQL 错误 → 在实际连接的数据库重新执行并检查数据
迁移错误 → 检查 Flyway 日志和 flyway_schema_history
Docker 错误 → docker compose ps + 对应服务日志
```

## 历史记录

优化前的逐条错误记录已保存在 Git 中：

```powershell
git show 8d58397:LEARNING_ERRORS.md
```
