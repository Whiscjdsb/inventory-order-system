# inventory-order-system 学习进度

更新时间：2026-09-02

> 本文件只维护当前状态和可验证证据，不再按天重复追加流水账。旧记录可通过 Git 历史查看。

## 当前状态面板

| 项目     | 当前结论                            |
| ------ | ------------------------------- |
| 当前真实水平 | 已达到基础 Java 后端实习项目展示与投递门槛        |
| 项目展示   | 达标：具备认证、缓存、事务、并发库存、测试、迁移和容器运行证据 |
| 是否可以投递 | 可以，应该边巩固边投递，不等待学完全部中间件          |
| 稳定技术面  | 尚未完全稳定，主要短板是独立排错、测试数据设计和复杂调用链表达 |
| 当前主线   | 从“能完成单点练习”升级为“能独立分析、修改、验证和讲解”   |

## 当前三个短板

1. **测试设计：** 本轮已改善 Arrange 自洽（分页测试 total 与 records.size() 一致）；仍需在多依赖、多分支场景下稳定检查数据一致性。
2. **独立排错：** 已完成 SQL 类（V3/V4 迁移 + 假阳性 200）和运行期类（Service 层 NPE、@Transactional 不回滚、IPage→PageResult 转换错误）两类排错；启动期 BeanCreationException 和认证类异常还需继续练。
3. **数据结构：** 链表反转和树最大深度已能独立写；BST 循环查找与插入已能完成；层序遍历（按层分组）已能写出，递归引用变化仍需巩固。

## 最近验证结果

- 2026-09-05：把商品详情缓存从 `@Cacheable` 改为全手动实现——`readCache`/`writeCache` 用 `ObjectMapper` 序列化 `ProductVO` 存 `product:id:`，30 分钟 TTL；缓存在 None 命中时用 `setIfAbsent` 抢分布式锁（10 秒 TTL），抢到后双检缓存再查库，`finally` 释放锁；7 个写操作改为手动 `evictProductCache(id)` 失效缓存。全量 `mvn test` 通过。

- 2026-09-02：完成六小时计划最后一项「工程与收口」——`mvn test` BUILD SUCCESS（35 用例全过），提交 [ProductService.java](src/main/java/com/example/inventorypractice/service/ProductService.java) Stream API 重构（`existingIds` 改为 `products.stream().map(Product::getId).collect(Collectors.toSet())`）。

- 2026-09-02：MVCC 掌握到位——能一句话讲原理（多版本并发控制 + undo log 维护旧版本 + Read View 选可见版本），区分快照读（普通 SELECT）和当前读（UPDATE/SELECT FOR UPDATE），明白防超卖靠原子 UPDATE + WHERE stock >= ?，不靠隔离级别。

- 2026-09-02：能区分 MySQL 默认 REPEATABLE READ 解决脏读、不可重复读，InnoDB 通过 Next-Key Lock 解决幻读；能区分幻读（行数变化）与不可重复读（同一行内容变化）。

- 2026-09-02：完成独立排错三类实战——Service 层 NPE（根因：查库后未判空直接转 VO，修复：判空抛 BusinessException(404)）、@Transactional 不回滚（根因：吞异常未重抛，修复：重抛或加 rollbackFor = Exception.class）、IPage→PageResult 转换错误（IPage 没有 stream()，应 getRecords() 再 stream）。

- 2026-09-02：完成 Java 算法三项——按层分组层序遍历（锁 size + for 循环）、原地翻转二叉树（交换左右子树 + 递归）、BST 循环查找与插入（比较大小缩小范围，插入返回根节点）。

- 独立新增订单空分页 Service 测试并通过。

- 能正确区分 Mockito 单元测试、MockMvc Controller 测试和真实数据库集成测试。

- 能按 AAA 识别 Arrange、Act、Assert，并解释 `when`、`assertEquals`、`verify` 的职责。

- 并发与事务集成测试共 2 个通过：`Failures: 0`、`Errors: 0`、`BUILD SUCCESS`。

- 两个 MySQL 会话真实观察到普通查询的 MVCC 读、并发更新等待、回滚和行锁释放。

- 链表反转隔天无提示完成；二叉树翻转在基础骨架下完成并运行正确。

- 独立完成排错四步法真实端到端复现：`Unknown column` 异常 → 定位 Mapper/SQL 段 → V3 加列修复 → V4 反向回退；过程中发现迁移文件漏 `.sql` 扩展名导致 Flyway 忽略、应用返回假阳性 200（Entity 删字段后 SELECT 不查该列，孤儿列残留不报错）。

- 2026-08-22：能沿真实代码解释 JWT 生成、过滤器校验、`SecurityContextHolder`、Controller 参数注入及 401/403；能判断公开路径的规则顺序和旧 Token 的时效问题。

- 2026-08-22：能区分原子 UPDATE 保护单条并发扣减、事务保护多条业务操作，并判断跨 Bean 调用、同类内部调用和吞异常对事务的影响。

- 2026-08-22：完成 BST 循环查找与插入；能解释为什么插入后返回根节点，以及平衡与退化情况下的复杂度。

- Git：本地领先 `origin/main` 6 个 commit，待用户自行推送。

## 项目能力矩阵

| 能力             | 已有实现或证据                                                         | 掌握等级                      |
| -------------- | --------------------------------------------------------------- | ------------------------- |
| Spring Boot 分层 | Controller → Service → Mapper → MySQL；Entity → VO → ApiResponse | 能解释                       |
| 商品管理           | CRUD、筛选、分页、逻辑删除、恢复、上下架、库存统计                                     | 能在提示下独立修改                 |
| 订单管理           | 创建、本人订单分页、取消并退库存                                                | 能解释，能完成简单测试               |
| 并发防超卖          | 条件原子 UPDATE、影响行数、真实双线程集成测试                                      | 能解释                       |
| 事务一致性          | 订单、库存和流水同事务；失败回滚集成测试                                            | 能解释                       |
| JWT 与权限        | 登录、过滤器、SecurityContext、401/403、角色限制                             | 能解释并判断常见配置                |
| Redis          | 手动缓存、写操作失效、不存在商品短期标记、分布式锁防击穿 | 能解释流程，能写缓存击穿互斥锁 |
| MySQL          | JOIN、聚合、索引、EXPLAIN、事务隔离、MVCC、行锁                                 | 能解释，MVCC 已达「一句话讲清原理」水平    |
| Flyway         | V1/V2 迁移、历史表、迁移日志和校验错误                                          | 能解释和基础操作                  |
| Docker         | 镜像、容器、端口、Volume、Compose、日志                                      | 能在模板帮助下操作                 |
| 自动化测试          | JUnit、Mockito、MockMvc、并发与事务集成测试                                 | 能写常见用例，分页测试 Arrange 自洽已稳定 |
| Git            | status、add、commit、push、分支和提交前检查                                 | 能独立操作                     |
| 项目表达           | 能讲订单、超卖、事务、缓存和迁移                                                | 内容正确，仍需压缩与稳定              |

## 已实现项目功能

### 商品与库存

- 商品新增、查询、修改、逻辑删除、恢复、批量新增和批量删除。

- 名称和价格筛选、分页、低库存、缺货和库存概览统计。

- 商品上下架、原子入库和原子出库。

- 入库、出库成功后保存库存流水。

- Entity、Request DTO、VO 和统一响应分离。

### 用户与安全

- 用户注册、用户名判重和 BCrypt 密码加密。

- JWT 登录认证，Token 保存用户名、用户 ID 和角色。

- Spring Security 无状态认证；公开登录、注册、Swagger 和健康检查。

- 普通受保护接口需要认证，管理员接口需要 `ADMIN` 角色。

- 能区分 `400/401/403/404/409/500/503` 的常见语义。

### 订单与一致性

- 根据当前登录用户创建订单，并保存下单时单价和总价。

- 分页查询当前用户订单。

- 取消订单时校验用户归属和订单状态。

- 原子取消只允许 `CREATED → CANCELLED`，防止重复退库存。

- `@Transactional` 保证扣库存、库存流水和订单保存一起提交或回滚。

- Mapper 原子 UPDATE 使用库存、上架和未删除条件防止并发超卖。

### 缓存、迁移与工程

- 手动 Redis 缓存商品详情（`product:id:` + JSON 序列化），写操作删除缓存 key 失效。

- 不存在商品保存 5 分钟短期标记，减少重复穿透 MySQL。

- 商品详情缓存用 Redis 分布式锁（`setIfAbsent` + 10 秒 TTL + `finally` 释放）防缓存击穿；抢到锁后双检缓存，避免重复查库。

- Flyway V1 创建表，V2 添加 `(status, create_time)` 订单索引。

- Swagger / OpenAPI、Actuator、统一异常处理和日志。

- Docker Compose 管理应用、MySQL 和 Redis，Volume 保存数据。

- 简单前端页面用于接口联调，不作为当前学习重点。

## 测试能力

### 已理解的选择原则

```text
单个类的业务分支和依赖调用 → Mockito 单元测试
HTTP 参数绑定、校验、状态码和 JSON → MockMvc 测试
真实 SQL、事务、锁、并发和容器依赖 → 集成测试
```

### 已有测试证据

- `AuthServiceTest`：登录成功、密码错误、禁用用户等分支。

- `ProductServiceTest`：参数校验、缓存标记、库存和查询逻辑。

- `OrderServiceTest`：创建、取消、异常、分页和空分页。

- `ProductControllerTest`：请求校验和库存概览 JSON。

- `ProductConcurrencyIT`：两个线程竞争库存 1，只有一个更新成功。

- `OrderTransactionRollbackIT`：订单插入失败后库存与流水回滚。

### 当前测试规则

- 写测试前先读真实执行路线。

- Arrange 数据必须自洽。

- `when()` 只模拟依赖行为，不能证明真实 SQL 正确。

- `assertEquals()` 验证结果，`verify()` 验证调用过程。

- 验证回滚必须在事务结束后重新查询数据库，不能依赖事务内旧 Java 对象。

## Java 与算法状态

| 主题                | 当前状态                                   |
| ----------------- | -------------------------------------- |
| 数组、双指针            | 移动零、删除元素、合并有序数组可完成                     |
| HashMap / HashSet | 计数、补数、最近下标、去重可完成，边界需检查                 |
| 滑动窗口              | 固定窗口可写；可变窗口理解正整数前提                     |
| 前缀和               | 区间和、中心下标能解释并完成                         |
| 栈与队列              | 有效括号、相邻去重能完成；Queue 层序遍历已能写出（按层分组）      |
| 链表                | 遍历、查找、反转已能独立写；环和中点能解释                  |
| 二叉树               | 节点计数、最大深度、原地翻转能写；按层分组层序遍历已能写出          |
| 二叉搜索树             | 循环查找与插入已能完成；能解释根节点返回和查找方向              |
| 递归                | 能理解返回值逐层传回和共享结果对象；引用变化仍需巩固             |
| 复杂度               | 常见题能判断 `O(n)`、`O(n²)`、`O(1)`、`O(n)` 空间 |

## 当前项目表达（约 45 秒）

我的项目是基于 Spring Boot 3 的库存与订单管理后端，使用 MyBatis-Plus、MySQL、Redis、Spring Security、JWT、Flyway 和 Docker。项目支持商品库存管理、用户认证、订单创建与取消。创建订单时使用带库存和商品状态条件的原子 UPDATE，通过影响行数判断扣减是否成功，避免并发超卖；再用事务保证库存、流水和订单要么全部成功，要么一起回滚。商品详情使用 Redis 缓存，并为不存在商品设置短期标记来减少缓存穿透。数据库结构由 Flyway 管理，也编写了 Mockito、MockMvc、并发和事务回滚测试。

## 当前下一步（已完成六小时计划，待选下一轮主题）

六小时计划已于 2026-09-02 完成全部 6 项。可选下一轮主题（按优先级）：

1. **Redis 缓存进阶：** Token 版本化、黑名单即时禁用旧 JWT、缓存击穿互斥锁方案。
2. **Spring Security 进阶：** 即时禁用旧 JWT、刷新 Token、角色继承。
3. **独立排错扩展：** 启动期 BeanCreationException 真实复现 + 四步法；认证类异常（401/403 边界）排错。
4. **数据结构巩固：** 递归引用变化、层序遍历隔天无提示复写、可变滑动窗口。
5. **项目表达压缩：** 45 秒讲清并发防超卖 / 事务回滚 / 缓存穿透，无卡壳、能回答追问。
6. **工程与投递：** 推送本地 6 个 commit 到远程；简历一轮；岗位沟通准备。

> 建议先推送本地 commit，避免积累过多未推送变更。

## 暂不扩展

- 不继续增加低价值 CRUD 接口。

- Nacos、OpenFeign、Gateway、RabbitMQ 只算接触过，未在本项目落地，不写成“熟练掌握”。

- 暂不深入复杂分布式架构、框架源码、前端工程化和冷门 Java 语法。

- Redis Token 版本、即时禁用旧 JWT 属于扩展设计，当前项目尚未实现。

## 历史记录

2026-07 至 2026-08 的逐日长记录已经提交到 Git，可通过以下命令查看优化前版本：

```powershell
git show 8d58397:LEARNING_PROGRESS.md
```
