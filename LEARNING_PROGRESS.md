# inventory-order-system 学习进度

更新时间：2026-08-18（完成独立测试设计、算法复写、Security 调用链、行锁与集成测试日志）

## 当前真实状态（2026-08-18）

- **当前真实水平：** 已达到基础 Java 后端实习项目展示和投递门槛；能够从真实 Service 执行路线基本独立设计简单分页测试，能解释 JWT 的 `200/401/403`、MySQL 行锁等待和事务回滚日志；链表反转已能无提示独立写出。
- **当前三个短板：** ① 测试数据偶尔自相矛盾，例如“空分页”中仍放入一条空订单；② 树的层序遍历会混淆固定根节点 `root` 和每轮处理节点 `current`；③ Spring Security 能解释当前流程，但尚未独立从代码画出完整过滤链并定位安全配置问题。
- **当前下一步：** 从“能完成单点练习”转向一次完整独立排错：读取调用链、提出假设、用日志或测试验证；算法维持隔天复写，不继续增加相似测试数量。
- **最近一次验证：** 2026-08-18 独立新增并通过空分页 Service 测试；无提示完成链表反转，提示后完成树的层序遍历；两个 MySQL 会话观察到更新等待与回滚；并发与事务集成测试共 2 个全部通过。

## 当前技术栈

- Java 17
- Spring Boot 3.2.5
- MyBatis-Plus 3.5.5
- MySQL
- Lombok
- Jakarta Validation
- Postman
- JUnit 5、Mockito
- Swagger / OpenAPI（springdoc）
- Spring Security
- JJWT 0.13.0
- Spring Cache、Redis
- Git、GitHub

## 已完成

- 创建 `product` 商品表并理解主要字段类型和约束。
- 创建 `Product` Entity 和 `ProductMapper`。
- 完成商品新增、根据 ID 查询、修改、删除。
- 完成统一响应 `ApiResponse<T>`，理解泛型 `T` 的基本作用。
- 完成 Request DTO 参数校验和校验异常处理。
- 完成 `BusinessException` 与 `GlobalExceptionHandler`。
- 完成商品列表、名称模糊搜索和分页查询。
- 完成分页参数边界检查。
- 完成商品上架、下架状态修改。
- 完成基础扣库存，并改造成带 `stock >= quantity` 条件的原子 SQL。
- 扣库存 SQL 已增加 `status = 1`，下架商品不能扣库存。
- 完成 `ProductVO` 和 `ProductVO.fromEntity()`。
- 新增、单个查询、修改、列表、分页和状态接口已统一返回 `ProductVO`。
- VO 改造已通过 Maven 测试：`BUILD SUCCESS`，1 个测试通过。
- 创建 `stock_operation` 库存记录表。
- 创建 `StockOperation` Entity 和 `StockOperationMapper`。
- 已把 `StockOperationMapper` 通过构造方法注入 `ProductService`。
- 扣库存成功后会向 `stock_operation` 插入一条 `operation_type = 2` 的出库记录。
- `ProductService.deductStock()` 已添加 `@Transactional`，完成异常回滚测试。
- 已理解事务的基本规则：方法正常结束提交，抛出运行时异常回滚。
- 创建 `StockOperationVO` 和 `StockOperationVO.fromEntity()`，将操作类型转换为“入库/出库”文字。
- 完成根据商品 ID 查询库存操作记录，并按照记录 ID 倒序排列。
- 已理解 `queryWrapper` 保存查询条件，`List<StockOperation>` 保存查询结果。
- 创建 `AddStockRequest`，完成入库数量的 `@NotNull` 和 `@Positive` 校验。
- 在 `ProductMapper` 中完成原子增加库存 SQL：`stock = stock + quantity`。
- 完成商品入库 Service 和 Controller，入库成功后写入 `operation_type = 1` 的记录。
- 入库方法已添加 `@Transactional`，保证增加库存和保存操作记录一起提交或回滚。
- 已通过 Postman 验证：3 号商品库存从 1 增加到 6，同时生成入库记录。
- 已理解 MySQL 自增 ID 在事务回滚或记录删除后可能出现跳号，这是正常现象。
- 已独立复述 Controller → Service → Mapper → 数据库 的请求方向，以及 数据库 → Entity → VO → ApiResponse 的返回方向。
- 已理解 Service 层存在的意义：承载业务逻辑、管理事务、避免 Controller 直接调用 Mapper。
- 已理解 VO 的作用：字段过滤、命名转换、状态值翻译（status → statusText、operation_type → operationTypeText）、隔离数据库表结构。
- 已能独立分析扣库存 SQL 中 `affectedRows == 0` 的三种原因：商品不存在、库存不足、商品下架。
- 已能区分 `ApiResponse<T>` 中 T 的类型：单个对象是 `ProductVO`，列表是 `List<ProductVO>`，无返回是 `Void`。
- 已能用基础 for 循环写 `List<Entity>` 转 `List<VO>`，并理解 Stream 版本的等价逻辑。
- 已理解 for 循环版本的意义：是 Stream 的本质，复杂转换和调试时更灵活。
- 已理解 `RuntimeException` 是运行时异常父类，`@Transactional` 默认遇到运行时异常才回滚；`BusinessException` 继承 `RuntimeException` 是为了让事务回滚且不强制 try-catch。
- 已独立从空白编写 `searchProducts` 接口（Controller + Service），并修复了 `>` 括号缺失、`@RequestParam(required = false)` 缺失等问题。
- 已给 Service 里已有的 `getLowStockProducts` 补上 Controller 接口，并验证通过。
- 已识别并整理重复代码：删除 `searchProducts`，把排序合并到 `getProductList` 里；删除 Controller 的 `/search` 接口。
- 已往 product 表插入 10 条测试数据（3 个手机、3 个笔记本、耳机、键盘、鼠标、显示器）。
- 已通过 Postman 验证模糊查询和 low-stock 接口均能正常工作。
- 已完成查询参数 `trim()` 处理，理解后端不应信任前端输入的原则。
- 已完成多条件动态查询接口：按名称 + 价格区间查询，每个条件独立判空。
- 已正确使用 `BigDecimal.compareTo()` 比较价格范围合法性。
- 已完成批量删除接口，使用 `@RequestBody List<Long>` 接收参数、`deleteBatchIds` 执行批量删除。
- 已掌握删除前先查、查完再删的思路，避免"先删后查拿不到数据"。
- 已完成 `@RequestBody` 接收 JSON 数组的基本用法。
- 已完成 `saveBatch` 批量保存，理解与 for 循环 insert 的差异（攒一批、一次 SQL）。
- 已将 `ProductService` 继承 `ServiceImpl<ProductMapper, Product>`，获得 IService 通用方法。
- 已给批量新增方法添加 `@Transactional`，理解批量操作中部分失败应全部回滚。
- 已理解 `@Valid` 对 `List<@Valid X>` 中的元素不生效，需在 Service 层手动校验。
- 已在 `createProducts` 循环中增加 name 判空校验。
- 已学习 `@Transactional(rollbackFor = Exception.class)`，理解受检异常默认不回滚、加 rollbackFor 后强制回滚。
- 已完成商品逻辑删除与恢复验证：删除后数据库记录仍存在且 `is_deleted = 1`，恢复后改回 `0`。
- 已理解 `@TableLogic` 会让普通查询自动过滤已逻辑删除的数据，自定义恢复 SQL 可以直接修改删除标记。
- 已完成未删除商品总数接口，掌握 `productMapper.selectCount(null)`。
- 已完成上架商品数量接口，掌握通过 `LambdaQueryWrapper<Product>` 添加 `status = 1` 后执行 `selectCount(queryWrapper)`。
- 已通过商品 4 的下架、统计和重新上架验证：下架不影响商品总数，只影响上架数量。
- 已把批量删除前的逐个 `selectById` 查询优化为 `selectBatchIds(ids)`，减少数据库交互次数。
- 已学习 `List` 允许重复、`Set` 不允许重复，以及使用 `HashSet` 和 `size()` 检测重复 ID。
- 已为批量删除补充空列表、非法 ID、重复 ID 和不存在 ID 的校验。
- 已学习把查询到的 `Product` ID 放入 `Set<Long>`，再通过 `contains()` 精确找出不存在的请求 ID。
- 已通过商品 23、24 验证正常批量逻辑删除，通过商品 4 与 999999 验证缺失 ID 不会误删存在的商品。
- 已理解批量删除没有响应数据时应返回 `ApiResponse<Void>`；曾误写为 `ApiResponse<Null>`，现已改正。
- 已理解 Java 的 `||` 短路判断：`id == null || id < 1` 会先拦截 null，避免空指针异常。
- 已发现并修复 `if (...) ;` 多余分号导致异常无条件执行的问题。
- 已调整批量删除校验顺序为：空列表 → ID 合法性 → 重复 ID → 批量查询 → 存在性检查 → 批量删除。
- 已把批量删除 Controller 的错误类型 `ApiResponse<Null>` 修正为 `ApiResponse<Void>`。
- 已独立尝试“查询已上架且库存为 0 的商品”功能，并在纠错后完成 Service、Controller 和 Postman 验证。
- 已进一步区分单个对象与列表返回类型：单个商品用 `ProductVO`，多个商品用 `List<ProductVO>`。
- 已理解 MyBatis-Plus 的 `eq` 表示等于、`lt` 表示小于；查询库存为 0 应使用 `eq(Product::getStock, 0)`。
- 已理解 `selectList` 返回 `List<Product>`，需要通过 `fromEntity()` 转换为 `List<ProductVO>`。
- 已理解 `stream().map(ProductVO::fromEntity).toList()` 等价于遍历、转换和收集；Stream 目前以能看懂和会套用模板为目标。
- 已通过创建库存为 0 的测试商品验证 `/api/products/out-of-stock`；曾因 Postman 未从 POST 切换为 GET 导致误判。
- 已开始学习 MySQL `EXPLAIN`，完成缺货查询的首次执行计划分析：`type = index`、`key = PRIMARY`、`rows = 21`、`Using where; Backward index scan`。
- 已新建独立错误复盘文件 `LEARNING_ERRORS.md`，后续遇到高频错误继续追加。
- 已为缺货查询创建联合索引 `(is_deleted, status, stock, id)`，执行计划由扫描主键索引改善为使用联合索引 `idx_product_deleted_status_stock_id`。
- 已理解联合索引最左前缀：查询跳过最左列时可能无法有效利用索引；逻辑删除字段会影响实际 SQL 和索引设计。
- 已学习 B+Tree 的多叉、有序、叶子节点保存数据并形成链表等特点，以及 MySQL 使用它支持等值、范围和排序查询的原因。
- 已理解 InnoDB 是 MySQL 的存储引擎，支持事务、行锁、外键、崩溃恢复和 MVCC。
- 已学习 `REPEATABLE READ`、`START TRANSACTION`、`SELECT ... FOR UPDATE` 和行锁，并完成两连接锁等待实验。
- 已理解前缀模糊查询和 `%关键词%` 的差异；前导 `%` 可能让普通 B+Tree 索引失效并出现 `type = ALL`。
- 已为 `ProductService` 编写 Mockito 单元测试，覆盖缺货商品 Entity 转 VO 和库存不足时不写库存记录；测试运行成功。
- 已修复错误导入 JDK 内部 `Constraint`、JUnit 版本冲突和重复测试依赖，能从日志底部定位 `Caused by`。
- 已引入 Swagger / OpenAPI，能通过 `/swagger-ui.html` 查看和调试接口，理解 Swagger 与 Postman 的用途差异。
- 已创建 `sys_user` 表、`SysUser` Entity、`SysUserMapper`、注册请求 DTO 和密码编码配置。
- 已完成用户注册：用户名判重、BCrypt 密码加密、默认角色与状态设置；注册和重复用户名验证成功。
- 已理解敏感配置使用 `${环境变量名}`，JWT 密钥和数据库密码不应写入源码或提交 Git。
- 已引入 JJWT，完成 `JwtTokenProvider`：生成 Token、设置用户名/用户 ID/角色、过期时间和签名，并能解析、校验 Token 和取得用户名。
- 已完成登录 Service 与 Controller：用户查询、统一用户名或密码错误、密码匹配、禁用状态判断和 `LoginVO` 返回。
- 已通过正确密码和错误密码测试：正确登录返回 JWT，错误密码返回 401。
- 已引入 Spring Security，理解 `OncePerRequestFilter`、`Authorization: Bearer`、`SecurityContextHolder` 和无状态认证流程。
- 已完成 JWT 过滤器与安全规则：注册、登录、Swagger 公开，其他接口必须认证；商品 4 已验证“带 Token 返回 200、不带 Token 返回 401”。
- 已完成受保护的 `/api/auth/me` 接口，能够从 Spring Security 的 `Authentication` 中取得当前登录用户名，并返回 `backend_intern`。
- 已把 JWT 中的角色转换为 Spring Security 权限，理解认证解决“你是谁”，授权解决“你能做什么”。
- 已测试普通认证和管理员权限，能够区分 401（未认证或 Token 无效）与 403（已经认证但权限不足）。
- 已完成商品按 ID 查询的 Redis 缓存：第一次查询 MySQL 并写入 Redis，后续查询命中缓存，不再执行商品查询 SQL。
- 已使用 `@Cacheable`、`@CacheEvict` 和 TTL；修改或删除商品后清除对应缓存，避免返回旧数据。
- 已理解 `ProductVO` 实现 `Serializable` 是为了让对象能够被 Redis 序列化保存。
- 已理解当前项目使用的是 Cache-Aside 模式，也能说明“先查缓存、未命中查数据库、回填缓存、写操作删除缓存”的完整流程。
- 已纠正项目表述：当前完成了商品查询缓存和缓存失效，但没有真正实现缓存穿透方案，不能在简历中写“解决缓存穿透”。
- 已完成 Git 本地仓库初始化、`.gitignore`、查看状态、暂存、提交、查看日志和分支切换等基础操作。
- 已把默认分支整理为 `main`，关联并推送到 GitHub：`https://github.com/Whiscjdsb/inventory-practice`。
- 已完成 `feature/git-practice` 功能分支练习：修改、提交、切回主分支、Fast-forward 合并、推送并删除分支。
- 已整理 README、项目技术栈、核心功能和后续计划，并能理解工作区、暂存区、本地仓库和远程仓库的区别。
- 已能用自己的话讲解项目：原子 SQL 防止超卖、事务保证库存与操作记录一致、JWT 保护接口、Redis 提高热点查询速度。
- 已开始准备后端实习：确认简历基础信息与项目描述，当前策略是边学习边投递，优先积累线上面试经验。
- 已开始 Java 算法练习：完成 HashSet 判断数组重复元素，理解集合去重、`add()` 返回值、增强 for 循环和平均 `O(n)` 复杂度。
- 已完成两数之和暴力解法并在 `main` 中运行成功，理解双重循环、数组下标、`new int[]{i, j}`、`O(n²)` 时间复杂度和 `O(1)` 额外空间。
- 已在模板帮助下完成两数之和 HashMap 解法并运行成功；知道它使用“数字 → 下标”查找补数，但目前仍不能独立写出和完整解释代码。
- 已为登录补充密码错误测试，验证返回 401 且不生成 JWT；Mockito、`assertThrows` 和 `verify(..., never())` 目前属于会读、会改模板，不要求独立手写。
- 已引入 Actuator 并公开 `/actuator/health`，能够通过 `{"status":"UP"}` 判断 Spring Boot 应用已启动；相关配置属于可复制模板。
- 已在 Git Bash 中接触 `pwd`、`ls -la`、`cd`、`find` 和 `cat`，能在提示下完成目录导航和文件查看，尚未形成独立使用习惯。
- 已理解 `mvn test` 用于测试、`mvn package` 用于生成 JAR、`java -jar` 用于运行 JAR，并在提示下从终端完成测试、打包和启动。
- 已理解环境变量用于把数据库密码、JWT 密钥和端口放在源码之外，并验证同一 JAR 可通过 `SERVER_PORT` 运行在不同端口；变量配置命令仍需查模板。
- 已新增 `sql/schema.sql`，包含 `product`、`stock_operation` 和 `sys_user` 三张表，并通过 MySQL 容器验证三张表能够自动创建。
- 已补充 README 的环境要求、本地构建、JAR 运行、健康检查、数据库初始化和 Docker Compose 说明。
- 已在指导下创建 Dockerfile、`.dockerignore`、`.env.example` 和 `compose.yaml`，完成 Spring Boot、MySQL、Redis 三容器启动。
- 已实际验证容器环境中的健康检查、用户注册、商品新增和 Redis 缓存：连续查询两次商品时 MySQL 只执行一次商品查询 SQL。
- 已完成 Docker 配置的 Git 提交和推送，但 Dockerfile、Compose、镜像、容器、数据卷和容器网络目前只是首次接触，不能视为独立掌握。
- 已通过实际命令区分镜像与容器：镜像是创建容器的模板，容器是镜像运行后的实例；执行 `docker compose down` 后容器消失但镜像仍存在。
- 已练习 `docker run`、`docker ps`、`docker ps -a`、`docker stop`、`docker start` 和 `docker rm`，知道停止容器不会删除容器，删除容器不会自动删除镜像。
- 已理解 `--filter` 只筛选显示结果，不会改变容器状态；目前命令参数仍需参考示例。
- 已练习端口映射 `-p 6381:6379`，知道格式是“宿主机端口:容器端口”，同一个宿主机端口不能被两个运行中的容器同时占用。
- 已使用 `docker exec` 在 Redis 容器中执行 `redis-cli ping`、`set`、`get` 和 `save`；理解 `PONG` 表示 Redis 已启动并能处理命令。
- 已验证同一容器 `stop/start` 后数据仍在；删除无 Volume 的容器并重建后数据消失。
- 已创建命名 Volume `docker-learning-redis-data` 并挂载到 `/data`，通过 `docker inspect`、`dump.rdb`、删除容器后重建和再次读取，验证 Volume 可独立保存数据。
- 已阅读 Redis 容器日志，能在提示下识别版本、内部端口、RDB 加载、加载键数量和 `Ready to accept connections`。
- 已重新理解 Compose：`docker compose up -d` 读取 `compose.yaml`，在后台创建网络并启动 `app`、`mysql`、`redis`；`docker compose down` 删除容器和网络但默认保留 Volume。
- 已验证 Compose 重建 MySQL 容器后 `docker_user` 仍能登录，证明 `mysql_data` Volume 保留了数据库数据。
- 已检查 `inventory-practice_default` 网络，确认 `inventory-app`、`inventory-mysql`、`inventory-redis` 位于同一 bridge 网络中。
- 已通过 `printenv` 验证 Compose 把 `DB_URL`、`REDIS_HOST`、`REDIS_PORT` 和 `SERVER_PORT` 传入应用容器。
- 已纠正关键误解：容器中的 `localhost` 指当前容器自己；应用访问其他 Compose 服务应使用 `mysql`、`redis` 等服务名。
- 已完成 Dockerfile 的 `FROM`、`WORKDIR`、`COPY`、`EXPOSE`、`ENTRYPOINT` 理解检查，能够说明本机 JAR 被复制为镜像中的 `/app/app.jar`，源码修改后需要重新打包和构建镜像。
- 已结合真实 `compose.yaml` 理解 `services`、`image/build`、`ports`、`environment`、`volumes`、`healthcheck` 和 `depends_on`；知道 Navicat 通过 `localhost:3307` 访问容器 MySQL，而 app 容器通过 `mysql:3306` 访问。
- 已结合真实 `application.properties` 理解 `${变量名:默认值}`、数据库与 Redis 地址切换、JWT 必填密钥、MyBatis 下划线转驼峰、SQL 日志、Redis 超时和缓存 TTL。
- 已能使用 `docker compose ps` 判断三个服务状态，使用 `docker compose logs app --tail 30` 从日志确认 Java 17、`/app/app.jar`、Tomcat 8080 和 Spring Boot 启动成功。
- 已完成“统计上架商品库存总价值”功能：先亲自使用 `BigDecimal` 和 for 循环实现 `价格 × 库存` 求和，再优化为 Mapper 中的 `SUM(price * stock)` 聚合 SQL。
- 已理解 `COALESCE(SUM(...), 0)` 用于在没有符合条件的商品时返回 0，自定义 SQL 中显式添加 `status = 1` 和 `is_deleted = 0`。
- 已通过接口与 SQL 交叉验证库存总价值：Windows 本机数据库曾得到 `1644063.70`，Docker MySQL 当前得到 `990.00`；两套数据库的数据和账号互相独立，不能混用验证结果。
- 已从 IDEA 运行整个 `ProductServiceTest`，3 个测试通过；运行 `mvn package` 时进一步发现并修复完整 Spring 上下文测试的 JWT 测试配置问题，最终全部 6 个测试通过。
- 已在 Windows 用户环境变量中永久配置 `JAVA_HOME`、`MAVEN_HOME` 和 `Path`，新 PowerShell 中执行 `mvn -v` 能识别 Maven 3.9.11 和 Java 17。
- 已执行 `docker compose up -d --build`，把 MySQL `SUM` 聚合版本更新到 app 容器；接口与 Docker MySQL 直接查询均返回 `990.00`，确认新镜像和 SQL 已生效。
- 已创建 `InventoryOverviewVO` 和 `/api/products/overview`，组合商品总数、上架数量、缺货数量和上架库存总价值；Docker 环境真实返回 `1、1、0、990.00`。
- 编写库存概览时曾误把缺货数量也设置成 `countOnSaleProducts()`，随后改为带 `status = 1`、`stock = 0` 条件的独立 `selectCount`。
- 已创建独立的 `JAVA_ALGORITHM_ERRORS.md`，后续只记录 Java/力扣中的题意、语法、数据结构、复杂度和重做结果，与后端项目错误分开。
- 已重做热题 100 的“两数之和”暴力解法，修正 `nums.length`、中文逗号和返回下标理解；能够说明 `return new int[]{i, j}` 与空数组兜底的用途。
- 已完成“283. 移动零”的两次遍历原地写法，理解 `void` 方法通过修改原数组产生结果、`writeIndex` 的用途，以及两个前后循环仍为 `O(n)`、额外空间为 `O(1)`。
- 已独立完成统计数组正数、查找数组最大值和筛选正数练习，进一步理解方法返回值、`static`、数组遍历和 `ArrayList`。
- 已理解 `ArrayList` 是长度可以动态增长的列表，能够使用 `add()` 保存筛选结果，并知道 `List<Integer>` 中的 `Integer` 是整数包装类型。
- 已独立使用 `HashMap<Integer, Integer>` 统计数字出现次数，理解 `key → value`、`put()` 新增或更新、`get()` 取值和 `containsKey()` 判断键是否存在。
- 已理解增强 for 循环 `for (int num : nums)` 会依次取得数组元素，不需要下标时比普通 for 循环更简洁。
- 已能使用 `Map.Entry`、`entrySet()`、`getKey()` 和 `getValue()` 遍历 Map，并独立完成“找出出现次数最多的数字”。
- 已重新独立写出两数之和 HashMap 解法并运行成功，能够说明 Map 保存“数字 → 下标”、`complement` 是补数，以及为什么必须先查询再保存当前数字。
- 已通过 `ProductItem` 综合练习复习对象创建、构造方法、Getter、成员方法和封装；当前定位不是 Java 零基础，而是已有 Spring 使用经验、Java 细节和独立组织能力仍需补强。
- 已理解字符串内容比较应使用 `equals()`，可能为 `null` 时可使用 `Objects.equals()`；在商品名称查询练习中修正了把整个 `ProductItem` 对象与字符串直接比较的问题。
- 已在纯 Java 代码中使用 `IllegalArgumentException`、`IllegalStateException`、`throw` 和 `try-catch`，能够从异常类型、消息、栈帧和退出码判断主动抛出的业务异常。
- 已新增 `sys_order` 订单表、`SysOrder` Entity、`SysOrderMapper`、`CreateOrderRequest`、`OrderVO`、`OrderService` 和 `OrderController`。
- 已完成创建订单业务：从 JWT 认证信息取得用户名，根据用户名查询用户 ID，根据商品 ID 查询真实单价，计算总价，调用已有原子扣库存业务，保存订单并返回 `OrderVO`。
- 已理解下单请求不能由前端传 `userId`、单价、总价和状态；这些字段必须由认证信息、数据库和后端业务生成，避免伪造身份与价格。
- 已通过真实接口验证订单 1：用户 1 购买商品 4 共 2 件，单价 `7999.00`，总价 `15998.00`；同时商品库存减少并生成出库记录。
- 已完成 `/api/orders/my`，根据当前登录用户名取得用户 ID，只查询当前用户自己的订单，并将 `List<SysOrder>` 转成 `List<OrderVO>`。
- 已在 `SysOrderMapper` 编写带 `id`、`user_id`、`status = 1` 条件的原子取消 SQL，防止同一订单被并发重复取消和重复退库存。
- 已完成取消订单业务：验证订单归属和状态，原子更新为已取消，调用入库方法恢复库存并保存入库记录；订单状态、库存和记录由外层事务保证一致。
- 已通过真实接口取消订单 1，返回 `status = 2`、`statusText = 已取消`，并验证库存增加 2、最新库存操作为入库记录。
- 已把 `sys_order` DDL 补入 `sql/schema.sql`，并核对 `compose.yaml` 会把它只读挂载到 MySQL 初始化目录；理解初始化 SQL 只在全新 MySQL 数据目录首次创建时自动执行。
- 已发现并清理 `ProductService` 扣库存与入库方法中“创建了 `StockOperation` 对象但未插入”的重复代码；真正保存记录的位置是 `StockOperationService.recordOperation()` 中的 Mapper `insert()`。
- 已再次请求取消订单 1，验证已取消订单会返回 `400` 和“订单已经取消”，不会进入退库存逻辑。
- 已新增 `OrderServiceTest`，使用 Mockito 验证已取消订单再次取消时，不调用原子取消 Mapper，也不调用 `productService.addStock()`；单独测试和完整 `mvn test` 均通过。
- 已把订单业务中重复的“根据用户名查询用户并判空”提取为 `getUserByUsername()` 私有方法，理解小范围重复逻辑优先提取为私有方法，独立业务职责才考虑单独建立 Service。
- 已复习重载、重写、继承、接口、抽象类和多态；能够判断 `Map` 是接口、`HashMap` 是实现类，并能说明“编译看左边，运行看右边”。
- 已理解 `==` 与 `equals()` 的区别，知道基本类型比较数值可用 `==`，对象内容比较优先使用 `equals()` 或 `Objects.equals()`。
- 已理解基本类型与包装类、自动装箱和拆箱、对象引用、Java 值传递及 `String` 不可变；能够说明修改对象内容与在方法中重新给参数赋值的区别。
- 已复习运行时异常与受检异常、`throw`、`throws`、`try-catch-finally`，知道 `RuntimeException` 的 `throws` 声明可以省略，但异常本身并不会消失。
- 已理解常用注解需要由编译器或框架处理，能说明 `@Override`、`@Data`、`@Service`、`@GetMapping`、`@Transactional` 和校验注解的基本作用。
- 已理解 Lambda 是可作为参数传递的一小段处理逻辑；能够区分 Stream 的 `filter`、`map` 和 `toList`，并与普通 for 循环对应。
- 已理解 Spring Service 默认通常是单例 Bean，Mapper 等固定依赖适合作为 `final` 成员变量，用户名、商品 ID 等单次请求数据应放在方法参数或局部变量中。
- 已新增通用 `PageResult<T>`，把“我的订单”改为 MyBatis-Plus 分页查询；能够说明 `Page` 保存分页条件、`IPage` 保存查询结果、`PageResult<OrderVO>` 面向接口响应。
- 已通过 `mvn test` 验证 7 个测试全部通过，并通过真实接口验证订单分页结果：`total = 1`、`current = 1`、`size = 10`，当前页返回订单 1。
- 已不看代码复述本人订单分页链路，能够区分 `total` 是全部符合条件的记录数、`records` 是当前页列表；仍需注意 Controller 最初传入的是 `username`，Service 再查询 `userId`。
- 已再次复述订单创建和原子取消流程，能够说明 `user_id` 防止越权取消、订单 `status = 1` 防止重复取消、`affectedRows = 1` 才允许退库存。
- 已核对并修复原子扣库存 SQL 缺少 `is_deleted = 0` 的问题，`mvn test` 通过并已提交；理解这用于防止并发情况下继续修改刚被逻辑删除的商品。
- 已完成 Redis 复盘：第一次未命中查询 MySQL 并回填，后续命中直接返回；修改后删除缓存；当前未缓存空结果，因此没有解决缓存穿透。
- 已完成 JWT 复盘：Token 保存用户名、用户 ID、角色、签发时间和过期时间；过滤器实际读取用户名和角色写入 `SecurityContext`，订单 Service 仍根据用户名查询用户 ID。
- 已掌握常见 HTTP 状态码的基础含义：`200` 成功、`400` 请求或业务条件错误、`401` 未认证、`403` 无权限、`404` 资源不存在、`405` 请求方式错误、`500` 服务器内部错误。
- 已能按“项目用途 → 技术栈 → 核心功能 → 原子 SQL、事务、Redis 三个亮点”完成一分钟项目介绍，仍需继续提高术语准确性和表达流畅度。
- 已详细学习 JDBC、MySQL Driver、HikariCP、MyBatis 和 MyBatis-Plus 的分层关系，能够说明 Mapper 调用最终仍通过 JDBC 访问 MySQL。
- 已复习 Spring 与 Spring Boot、IoC、DI、Bean、AOP、Spring MVC、Starter、自动配置、内嵌 Tomcat、Actuator 和可执行 JAR 的作用。
- 已核对当前项目没有自定义 `@Aspect`，但 `@Transactional`、`@Cacheable` 使用了 Spring 代理思想；旧项目中的日志、耗时、权限和限流切面暂不视为已掌握。
- 已复习事务 ACID、隔离级别、MVCC、InnoDB、Redo Log、Undo Log、聚簇索引、二级索引、覆盖索引、回表、最左前缀、`EXPLAIN`、悲观锁和条件更新。
- 已能够区分 Dockerfile 与 Compose：Dockerfile 构建应用镜像，Compose 组合 app、MySQL、Redis 服务；能够说明容器使用 `mysql:3306`、Windows 使用 `localhost:3307`。
- 已核对当前 4 个测试文件中共有 7 个 `@Test` 方法；理解 JUnit 负责运行和断言，Mockito 负责模拟依赖，现有单元测试不能证明真实 SQL、Redis 和完整 HTTP 链路。
- 已分析三类福州 Java 岗位：大型企业加分技术较多、小公司职责不透明、初级岗位与当前项目高度匹配；能够区分岗位必需项与加分项，不再因为未学完全部框架而停止投递。

## 当前正在学习

目标：以尽快达到 Java 后端开发实习要求为导向，在 `inventory-practice` 中真正掌握核心知识，而不是只跟着代码过一遍。

学习定位：

- `inventory-practice` 是当前主线练习项目，Controller、Service 和核心业务判断优先由学习者手写。
- `springboot-practice` 中的 JWT、Redis、RabbitMQ、AOP 和微服务等内容以前只跟着做过一遍，暂不视为已经掌握，只作为旧代码参考。
- 不再反复堆相似 CRUD，也不过度研究低价值边界细节；每个功能做到理解原理、写出核心代码、验证成功并能用于面试说明。
- Java 语法出现实际错误时再针对性讲解，重点补集合、异常、泛型和基础代码组织能力。

当前下一步（按优先级从高到低）：

1. 从当前四张表开始进行 SQL 多表查询实战，先掌握 `JOIN`、`GROUP BY`、`HAVING` 和子查询。
2. 每天保持 Java 主动编码和 1～2 道简单算法题，减少只听概念、不写代码的时间。
3. 通过现有项目练习单元测试、错误定位、项目讲解和 README 对接文档，不再增加普通 CRUD。
4. 从现在开始每天投递和沟通 Java 后端实习岗位，同时记录岗位要求，按真实 JD 调整加分技术学习顺序。

## 当前达标判断（2026-07-27）

- **项目展示门槛：已达到。** 项目已有商品、库存、认证、Redis、订单、并发控制、测试、Swagger、Git 和 Docker 部署。
- **开始投递门槛：已达到。** 可以立即投递初级 Java 后端和实习岗位，不需要等待 Nacos、RabbitMQ、Spring Cloud 全部学完。
- **稳定通过技术面门槛：尚未达到。** 主要短板是 Java 和 SQL 独立编写、框架知识主动表达、真实 Bug 排查和多表查询。
- **微服务熟练门槛：未达到。** 旧项目中的 AOP、RabbitMQ、Nacos 和微服务只是接触过，不应写成“熟练掌握”。

## 后续十四天计划（每天约六小时）

每天固定保留：

- Java/算法主动编码：1.5～2 小时。
- 当日主线实战：2～3 小时。
- 项目复盘、测试或面试表达：1 小时。
- 岗位筛选、沟通和投递：1 小时。

### 第 1 天：SQL JOIN

- 使用 `sys_order`、`sys_user`、`product` 写不少于 8 条多表查询。
- 独立完成 1 道数组题和 1 道 HashMap 题。
- 输出：能查询订单 ID、用户名、商品名、数量、单价和总价。

### 第 2 天：SQL 统计

- 练习 `GROUP BY`、聚合函数、`HAVING` 和子查询，不少于 8 条 SQL。
- 复习集合、异常和泛型，独立写 2 个小方法。
- 输出：能统计用户订单数、消费总额和商品销量。

### 第 3 天：MySQL 与 Java

- 使用 `EXPLAIN` 对 3 条真实 SQL 查看执行计划，说明 `type`、`key`、`rows`。
- 复习 Java 面向对象、Lambda、Stream，并完成 2 道简单题。
- 输出：写一份索引是否有效的简短判断。

### 第 4 天：Spring 与排错

- 不看文档复述一次完整 HTTP 请求链路、IoC/DI、事务和 JWT。
- 人为制造并修复 2 个安全的小错误，例如错误方法参数或错误 Mapper 返回值。
- 输出：`mvn test` 最终通过，并能说明错误定位过程。

### 第 5 天：Redis 与测试

- 练习 Redis 五种常用数据结构及 TTL；复述缓存穿透、击穿和雪崩。
- 阅读现有测试，独立补充 1 个有业务价值的 Service 单元测试。
- 输出：测试通过，并能说明它证明和没有证明什么。

### 第 6 天：项目对接与简历

- 补充 README 的项目结构、认证流程、接口概览、启动步骤和常见错误。
- 完善一页后端实习简历和 GitHub 项目描述。
- 输出：另一位开发者能根据 README 启动项目。

### 第 7 天：第一轮验收

- 不看代码完成项目一分钟介绍和三个核心难点说明。
- 独立完成 5 道 SQL、小型 Java 方法和一次完整测试。
- 输出：根据结果重新判断技术面差距，不达标的部分进入下一周复习清单。

### 第 8～10 天：RabbitMQ

- 第 8 天：Producer、Consumer、Exchange、Queue、Routing Key。
- 第 9 天：确认、重试、死信队列、重复消费和幂等。
- 第 10 天：完成一个最小 Spring Boot 消息案例并写运行说明。

### 第 11 天：Nginx

- 学习反向代理、静态资源、端口转发和基础负载均衡。
- 输出：通过 Nginx 转发到 Spring Boot 健康检查或接口。

### 第 12～14 天：Spring Cloud 与 Nacos

- 第 12 天：理解单体与微服务，建立两个最小服务，学习 OpenFeign 调用。
- 第 13 天：使用 Nacos 完成服务注册、发现和基础配置管理。
- 第 14 天：学习 Gateway 基础路由，完成调用链复盘和简历技术栈校准。

## 达标后的长期每日安排

当第 7 天验收达到“能独立写基础 Java、SQL 和解释项目”后，每天仍按六小时执行：

- 1.5 小时：Java 与算法，保持编码手感。
- 1 小时：SQL/MySQL 或 Redis 轮换练习。
- 1 小时：项目测试、Bug 修复、文档或部署维护。
- 1 小时：针对当天岗位要求复习面试题。
- 1.5 小时：岗位筛选、沟通、投递和面试复盘。

已经进入面试流程时，优先复习对应 JD 中明确要求的技术，不为了增加技术名词临时堆新框架。

## 当前真实掌握程度（2026-07-26）

- 能理解 `Controller → Service → Mapper → 数据库` 的基本调用方向。
- 能在提示下完成基础 CRUD、条件查询和统一响应类型。
- 已接触分页、事务、逻辑删除、批量操作和原子库存更新，但分页中的 `Page`、`IPage` 和 `selectPage()` 仍属于会理解、会查模板，尚不能独立默写。
- Java 集合和语法细节还不稳定，需要结合真实业务继续练习。
- 已能从需求开始尝试组织完整的小功能，但返回类型、查询条件和 Entity/VO 转换仍需要提示纠正。
- 已开始接触 `EXPLAIN`，能在讲解下认识 `type`、`key`、`rows` 和 `Extra`，尚未独立掌握索引设计。
- 已在讲解下完成联合索引、事务、行锁和 B+Tree 学习，能理解核心用途，但仍需独立复述和再次实战巩固。
- 已在讲解下完成注册、登录、JWT 生成与校验、过滤器和接口保护的完整链路。
- 能说明 `Authorization: Bearer`、JWT 校验、`SecurityContext`、角色权限、401 和 403 的基本流程；Spring Security 固定 API 目前以理解和会修改模板为目标，不要求默写。
- 能说明 Redis Cache-Aside 查询和缓存删除流程，并通过控制台 SQL 日志判断是否命中缓存；注解参数和序列化配置仍允许查资料。
- 已能独立完成 Git 的常用本地操作和一次功能分支合并，但发生冲突、回退提交和多人协作仍需后续练习。
- 已能较完整地复述项目难点和解决方案，但表达中的技术名词仍需准确，例如“原子 SQL”而不是含义不明的词。
- 已进入算法入门阶段，能够独立完成数组基础遍历、HashSet 去重、暴力两数之和和 HashMap 两数之和；已经理解 HashMap 中的键值关系及常用操作，但 `Map.Entry` 遍历刚接触，仍需后续复习。
- Maven/JAR 流程目前能说出“测试、打包、运行”三步；具体环境变量和命令路径仍需提示。
- Actuator、数据库初始化和 Docker Compose 均已完成真实验证；Docker 已从纯复制配置进展到能理解镜像/容器、生命周期、端口、Volume、日志和网络的基础用途，但命令拼写、独立操作和完整复述仍不稳定，不能写成“熟练掌握 Docker”。
- 能在提示下独立写出 `BigDecimal` 循环求和的核心业务方法，并理解金额不能用普通浮点数、`BigDecimal.add()` 需要重新赋值。
- 能理解 MySQL 聚合比查询全部 Entity 后在 Java 计算减少数据传输，但 `@Select`、`SUM` 和 `COALESCE` 目前仍允许参考模板。
- 已理解测试的预期结果应来自需求或人工计算；当 Mapper 被 Mockito 模拟时，固定返回值只验证 Service 的调用和传递，不能证明真实 SQL 正确。
- 能根据报告最底层 `Caused by` 依次定位缺少 JWT 测试配置和 Base64 解码后长度不足，但 Spring 测试配置仍属于可复制模板。
- 能在提示下使用一个 VO 组合多个统计值，但独立组织新业务前仍需要先明确用途和字段含义；相近统计方法容易调用混淆。
- 已理解数组使用 `nums.length`、`return` 返回并结束方法、`void` 可直接修改传入数组；这些基础概念刚完成首次复盘，仍需通过独立重做巩固。
- 能在具体代码中判断单循环和前后两个循环为 `O(n)`、嵌套双循环为 `O(n²)`，复杂度分析目前处于入门阶段。
- 当前最明显的问题不是项目功能不足，而是推进速度超过理解速度；下一阶段必须减少配置复制，增加基础 Java 和完整小功能的亲自编写。
- 能从长日志最底层 `Caused by` 定位环境变量缺失和临时目录权限问题，但本地运行配置仍需继续熟悉。
- 能在明确业务流程和少量关键提示下独立组织订单 DTO、VO、Service、Controller 和 Mapper SQL，并能复述“认证用户 → 查询商品 → 扣库存 → 保存记录和订单 → 返回 VO”的完整链路。
- 已能说明订单事务用于保证订单状态、商品库存和库存记录一起提交或回滚；已完成重复取消实测和“不重复退库存”的 Mockito 单元测试。
- Java 面向对象、引用、异常、注解、Lambda 和并发基础已完成一轮复习；可以回答基础判断题，但仍需要在后续项目修改中独立使用，不能仅凭本轮听懂就视为熟练掌握。
- 当前项目功能已经足够支撑实习项目展示，下一阶段的主要短板是独立编码、项目讲解、错误排查和对接文档，而不是功能数量。

## 后端实习导向

优先掌握：

- Java 面向对象、集合、异常、泛型和常用并发基础。
- Spring Boot 分层、依赖注入、参数校验和全局异常处理。
- MyBatis-Plus、SQL、分页、批量操作和动态查询。
- MySQL 索引、事务、锁和执行计划。
- JWT、Redis、接口文档、单元测试和 Git。
- 能清楚讲解请求链路、事务回滚、原子扣库存和项目难点。

暂时降低优先级：

- 前端页面和 JSP。
- 冷门 Java 语法和复杂框架源码。
- 过度复杂的分布式架构。
- 价值较低的重复 CRUD 和过多边界测试。

## 当前掌握要求

需要逐渐做到能自己手写：

- Entity、Request DTO、VO 的基本结构。
- Controller 接收参数并调用 Service。
- Service 中的查询、判空、业务判断、更新和异常抛出。
- 基础 MyBatis-Plus CRUD 调用。
- 简单 SQL，以及原子扣库存中 `stock >= quantity` 的意义。
- `fromEntity()` 的基础字段转换。
- 根据入库或出库计算 `beforeStock` 和 `afterStock`。
- 根据 Mapper SQL 条件分析 `affectedRows` 的含义。
- 使用 `queryWrapper` 组织基础筛选和排序条件。

可以查资料或复制模板：

- 框架配置类。
- 全局异常处理器的固定结构。
- `@Update`、`@Param` 等 Mapper 注解的包名和固定格式。
- Stream、方法引用和分页 VO 转换。
- `@Transactional` 的 import 和固定用法，但必须理解事务为什么存在。

## 本轮复盘结论

已经理解：

- `Controller -> Service -> Mapper -> 数据库` 的请求方向。
- `数据库 -> Entity -> VO -> ApiResponse` 的返回方向。
- `@PathVariable` 从请求路径取值，`@RequestBody` 从 JSON 请求体取值。
- DTO 注解检查请求参数，数据库记录是否存在需要由 Service 查询判断。
- `affectedRows` 只表示 SQL 影响的行数，不包含最新商品库存。
- `ProductMapper` 操作 `product` 表，`StockOperationMapper` 操作 `stock_operation` 表。
- Entity 面向数据库，VO 面向前端响应。

## 常见错误汇总（2026-07-19 更新）

> 以下错误均为实际编码中出现的，下次遇到时优先检查这几项。

### 1. 泛型括号不匹配

**示例：** `ApiResponse<List<ProductVO>`（少了一个 `>`）
**改正：** `ApiResponse<List<ProductVO>>`
**规律：** `List<X>` 外面再套一层泛型，关闭括号数量是 `<` 数量的两倍。

### 2. 方法名拼写错误

**示例：** `deletePoducts`（少了一个 r）
**改正：** `deleteProducts`
**原则：** 写完方法名后看一眼。

### 3. private vs public

**示例：** Controller 方法写成 `private`
**改正：** Controller 方法必须是 `public`
**原因：** Spring 通过反射调用 Controller 方法，`private` 方法外部不可见，会报错。

### 4. 先删后查，查不到数据

**场景：** 批量删除时先 `deleteById` 逐个删，再去查，当然查不到。
**改正：** 先查出来再用 `deleteBatchIds` 删。

### 5. 查询条件没判空就加到 queryWrapper

**示例：**
```java
queryWrapper.like(Product::getName, name);   // name 可能是 null
queryWrapper.ge(Product::getPrice, minPrice); // minPrice 可能是 null
```
**改正：** 每个可选参数都要 `if (... != null)` 判断后再加条件。

### 6. 查询参数没有 trim

**示例：** name 参数前后有空格，导致 `LIKE '% 手机%'` 匹配不到
**改正：** 字符串参数进查询前做 `name.trim()`

### 7. Variable name conflict

**示例：**
```java
List<ProductVO> productVO = ...;    // 外面的列表
ProductVO productVO = new ...;      // 里面的单个对象——冲突！
```
**改正：** 列表用 `productVOList` 或 `vos`，单个用 `vo`

### 8. VO 里多加了不存在的方法/字段

**示例：** `productVO.setStockOperationList(getStockOperationList(...))`——ProductVO 里根本没有这个方法
**改正：** 写 setter 前确认 VO 类里确实有这个字段

### 9. @RequestParam 忘写 required = false

**示例：** `@RequestParam String name` — 不传 name 时直接 400 报错
**改正：** 可选参数要加 `@RequestParam(required = false)` 或者设 `defaultValue`

### 10. isBlank 和 isEmpty 的区分

- `isEmpty()`：字符串长度为 0 时返回 true（`""`）
- `isBlank()`：字符串为空或全是空白时返回 true（`""`、`"  "`、`"\t"`）
- 参数 trim 前后：如果用了 `trim()` 则 `isEmpty()` 和 `isBlank()` 效果一样
- **推荐用 `isBlank()`**，因为你在 trim 之前就可能需要判断

### 11. BigDecimal 不能用 `>` `<` 比较

**示例：** `minPrice > 0` — 编译通过但逻辑不对
**改正：** `minPrice.compareTo(BigDecimal.ZERO) > 0`

### 12. 查询方法不需要 @Transactional

**原则：** 只有写操作（insert、update、delete）才加 `@Transactional`。纯查询方法加了反而占连接资源。

### 13. Controller 路径不能冲突

**示例：** 项目中已有 `@GetMapping`（无路径），又加了一个 `@GetMapping("/list")`，两个接口功能类似容易混淆。
**原则：** 每个路径唯一，语义清晰。

### 14. List 类型声明错误

**示例：** `List<ProductVO> products = new ArrayList<>();` 然后 `products.add(product)`（product 是 Product 类型）
**改正：** `List<Product> products = new ArrayList<>();`
**原则：** `List<>` 里的类型要和 `add()` 的元素类型一致。

### 15. 硬编码数据替代请求数据

**示例：** 批量新增时用了 `product.setName("商品" + id)` 而不是 `request.getName()`
**改正：** 永远从请求参数取值，不要自己硬编码。

### 16. 方法调用错误

**示例：** 批量新增接口里调了 `productService.deleteProducts(ids)`（调成了删除）
**改正：** 写方法调用前确认方法名正确。

### 17. @Valid 对 List 元素不生效

**场景：** `@RequestBody List<@Valid CreateProductRequest>` 不会校验列表里每个元素的字段
**改正：** 在 Service 循环中手动判断参数合法性
**原则：** 不能依赖 `@Valid` 拦截 List 元素，Service 层必须自己校验。

### 18. 不要在循环里对 null 字段 insert

**场景：** MyBatis 默认跳过 null 字段，如果数据库字段是 NOT NULL 且无默认值，会报 DataIntegrityViolationException
**改正：** 入参校验在前端拦住 null，或在 Entity 设值阶段给默认值。

## 仍需加强（旧记录）

- 目前可以在提示下完成业务代码，还需要练习从空白独立组织完整方法。
- 不要直接复制另一项业务的异常判断；入库和扣库存的失败条件不同。
- 区分集合类型和元素类型，例如 `List<StockOperationVO>` 与 `StockOperationVO`。
- 继续熟悉泛型的完整类型，例如 `ApiResponse<List<StockOperationVO>>` 中的 `T` 是整个 `List<StockOperationVO>`。
- Stream 和方法引用目前以能看懂、会调用为目标，不要求脱离提示手写。

## 已知非阻塞警告

- `pom.xml` 中重复声明了 `spring-boot-starter-test`。
- 用户目录下的 `.m2/settings.xml` 根元素格式不规范。
- 这两个警告目前不影响项目编译，后续可以单独清理。

## 新任务继续方式

新任务开始时先说明：

> 先读取 AGENTS.md、LEARNING_PROGRESS.md 和 LEARNING_ERRORS.md，然后从“当前下一步”继续。

## 2026-07-28：简历、项目改名与投递策略

### 已完成

- 已完成一页式 Java 后端实习简历初稿，PDF 文字能够正常提取，没有文字重叠或截断。
- 教育经历已确认：福建农林大学、数据科学与大数据技术、本科、2024 年入学、预计 2028 年毕业、CET-4。
- 简历中的个人项目统一命名为“库存与订单管理系统”，角色写作“Java 后端开发｜个人项目”，不写“项目负责人”。
- 专业技能从八条压缩为五条：Java 基础、Spring Boot 分层、MySQL、Redis/Security/JWT、工程与测试工具；技能栏负责说明“会什么”，项目经历负责证明“在哪里使用、解决了什么问题”。
- 项目经历已突出三项核心证据：带库存条件的原子扣减 SQL、`@Transactional` 保证订单与库存一致性、订单状态条件更新防止重复取消和重复退库存。
- 已确认简历当前需要修正：增加“Java 后端开发实习”求职方向、教育时间写到预计毕业年份、项目角色补充“Java 后端开发”、删除出生年月、将 PDF 改成正式文件名。
- 推荐简历文件名：`万晖-Java后端开发实习-福建农林大学-2028届.pdf`。
- 项目文件夹已由 `inventory-practice` 改为 `inventory-order-system`，Git 远程地址已更新到同名 GitHub 仓库。
- 文件夹改名后已通过 Docker Compose 重新构建并启动 app、MySQL、Redis；MySQL 和 Redis 显示 Healthy，应用容器显示 Started。
- 已在 `compose.yaml` 使用固定 Compose 项目名 `inventory-practice`，避免文件夹改名后创建新的空数据卷。

### 本轮概念结论

- SSM 指 Spring、Spring MVC、MyBatis；当前项目使用的是 Spring Boot、Spring MVC、MyBatis-Plus，可理解为更现代、配置更简化的组合。
- Spring Boot 实习阶段重点不是框架源码，而是启动与配置、IoC/DI、Controller/Service/Mapper 分层、RESTful API、参数校验、异常处理、事务、测试、Maven 打包及请求链路表达。
- 当前项目已经覆盖多数 Spring Boot 实际用法；主要短板是脱离参考独立组织业务、Java/SQL 基础、真实错误定位和主动表达。
- 当前招聘中确实有岗位列出 Spring Cloud、Nacos 和 MQ，但需要区分核心要求与加分项。缺少一两个加分技术时仍应投递，不能等待技术全部学完。
- 微服务补学目标暂定为“能够运行和解释基础案例”：Nacos 服务注册与发现、OpenFeign 服务调用、Gateway 路由、RabbitMQ 基础消息收发；达到这一程度后才可在简历中写“了解”。

### 当前真实达标判断

- **项目展示门槛：已达到。** 项目内容足够支撑一段 Java 后端个人项目经历。
- **开始投递门槛：已达到。** 简历完成必要修改后应立即开始投递，不等待 Spring Cloud 和 MQ 全部学完。
- **稳定技术面门槛：尚未达到。** 继续加强 Java 主动编码、SQL 多表查询、项目讲解和错误排查。
- **Spring Cloud/MQ：尚未掌握。** 旧项目中接触过不等于掌握，当前不能在简历中写“熟练”。

### 当前下一步

1. 修改并导出正式 PDF 简历，当天沟通或投递不少于 3 个匹配度较高的 Java 后端实习岗位，优先确认是否支持线上面试。
2. 使用 `sys_user`、`sys_order`、`product` 独立完成不少于 5 条 `JOIN` 查询，至少输出订单 ID、用户名、商品名、数量、单价和总价。
3. 独立完成 1 道数组简单题和 1 道 HashMap 简单题，先自行定位错误，再请求提示。
4. 脱离代码复述创建订单、防止超卖、事务回滚、JWT 请求链路和 Redis 缓存更新策略。
5. 在持续投递的同时，用约 7 天补 Spring Cloud、Nacos、OpenFeign、Gateway 和 RabbitMQ 基础，不中断 Java、SQL 与项目复盘。

### 焦虑时的执行规则

- 不再同时展开 Java、SQL、算法、Nacos、MQ、Spring Cloud、简历和投递全部任务。
- 焦虑明显时，当天只保留一个关键结果：优先完成简历并投递 3 个岗位。
- 已达到投递门槛不等于已经稳定通过技术面；两者可以同时推进，不需要等到“全部学会”才投递。

## 2026-07-28：微服务、RabbitMQ、Java 与面试表达实战

### Spring Cloud 与 RabbitMQ

- 使用旧 `springboot-practice` 项目完成基础案例运行，明确该项目仍是参考项目，不替代主线项目。
- 启动 Nacos 容器并在服务列表中确认 `gateway-service`、`springboot-practice` 和 `notification-service` 的注册关系。
- 通过 Gateway 路由访问通知服务，理解 `Path`、`StripPrefix=1` 和 `lb://服务名` 的作用。
- 使用 JWT 登录后，经 Gateway 成功访问通知服务；能够区分 Gateway 负责入口和路由、Nacos 负责查找服务地址、OpenFeign 负责发送服务间 HTTP 请求。
- 验证完整调用链：`Gateway → springboot-practice → OpenFeign → notification-service`。
- 停止通知服务后实际得到 Fallback 降级响应，重新启动后调用恢复。
- 通过创建任务触发 RabbitMQ，日志中同时确认 Producer 发送和 Consumer 接收处理。
- 已理解基础消息流：`Producer → Exchange → Queue → Consumer`，以及异步处理和业务解耦的意义。
- 已接触持久化队列、生产者确认、ACK、重试、死信队列和幂等等可靠性概念，但没有在当前案例中完整实现。
- 当前掌握程度：能够运行基础案例并在提示下解释核心流程，简历最多写“了解 Spring Cloud Gateway、Nacos、OpenFeign 和 RabbitMQ”，不能写“熟练掌握”或“实现可靠消息投递”。

### Java 主动编码

- 独立使用 `HashMap` 完成“商品 ID → 累计购买数量”，输入 `{4,38,4,39,38}` 与 `{2,3,1,2,4}`，运行得到 `{4=3, 38=7, 39=2}`。
- 独立遍历 `Map.Entry` 找出购买数量最多的商品 ID，理解 `entry.getKey()` 和 `entry.getValue()`。
- 独立使用 `List<ProductItem>`、Getter、增强 for 循环筛选低库存商品，运行得到 `[机械键盘, 显示器]`。
- 独立筛选累计购买数量大于等于阈值的商品 ID，修正 `>` 与 `>=` 的边界错误后运行得到 `[4, 38]`。
- 使用 Lambda 和 `Integer.compare()` 对 `List<ProductItem>` 按库存升序、降序排序并验证成功。
- 已能把一个方法返回的 `Map` 继续传给另一个方法，理解基础方法组合和数据流转。

### 项目面试表达

- 能脱离代码说明创建订单流程：前端只提交商品 ID 和数量，后端从认证信息取得当前用户，从数据库取得商品价格，计算总价、扣库存、记录出库并新增订单。
- 能说明原子扣库存 SQL 把库存判断和扣减放在同一条 `UPDATE` 中，并根据 `affectedRows` 判断成功或失败。
- 能说明取消订单通过 `status = 1` 条件更新和 `affectedRows == 1` 后才退库存，防止重复取消和重复退库存。
- 一分钟项目介绍已补充订单业务、事务一致性和 Docker 的准确职责；需要继续避免把 JWT 说成注册校验、把多个容器说成一个镜像。

### 错误定位与测试

- 能识别普通 Java 堆栈中的异常类型，并知道优先查看第一条属于自己项目的文件与行号。
- 能从 Spring 多层启动错误的最底层 `Caused by` 定位缺失环境变量。
- 已理解编译信息中 `required` 是方法要求的参数，`found` 是实际传入的参数；数量不一致时同步修改调用位置。
- 已理解 `List<Product>` 不能直接赋值给 `List<ProductVO>`，需要逐个完成 Entity 到 VO 的转换。
- 在 `AuthServiceTest` 新增“用户不存在时登录失败”测试：Mapper 返回 `null` 时断言 401，并验证密码组件和 JWT 组件没有被调用；整个测试类运行成功。

### 当前达标判断

- **项目展示门槛：已达到。**
- **开始投递门槛：已达到，应边学习边投递。**
- **稳定技术面门槛：尚未达到。** 仍需提高 Java 独立组织代码、错误定位速度和项目表达稳定性。
- **Spring Cloud/MQ 入门目标：已达到。** 当前属于“运行过并了解基本流程”，不是熟练掌握。

### 当前下一步

1. 保持每天 1～2 个 Java 主动编码输出，下一次优先练习集合、对象和方法组合，不继续堆框架术语。
2. 使用现有项目继续练习测试阅读和安全的小型错误定位，不增加普通 CRUD。
3. 复述一次 JWT、Redis 缓存更新和 Docker 部署链路，补齐一分钟项目介绍中的表达短板。
4. 完成正式 PDF 简历，并沟通或投递不少于 3 个匹配的 Java 后端实习岗位，优先确认线上面试。
5. 处理旧 `springboot-practice` 中硬编码的邮件授权凭据：先撤销或轮换，再改为环境变量；不得在聊天、文档或提交中保留真实值。

## 2026-07-28 补充：Docker 排错与登录测试

### Docker 部署与排错

- 能区分 `Dockerfile` 与 `compose.yaml`：前者负责把 Spring Boot JAR 构建成应用镜像，后者负责统一配置和管理 app、MySQL、Redis 多个容器。
- 能正确解释端口映射：Windows 上的 Navicat 使用 `localhost:3307` 访问容器 MySQL，app 容器使用 `mysql:3306`；Windows 使用 `localhost:6380` 访问 Redis，app 容器使用 `redis:6379`。
- 已理解容器中的 `localhost` 只表示当前容器自己，容器之间通过 Compose 服务名访问。
- 已理解 `environment` 用于把数据库地址、用户名、密码、JWT 密钥和端口等运行配置传入容器；`${DB_PASSWORD}`、`${JWT_SECRET}` 从外部环境读取，避免把敏感值提交到 Git。
- 能说明修改 Java 源码后仅执行 `docker compose restart app` 仍会运行旧镜像中的旧 JAR；正确更新流程是先执行 `mvn package`，再执行 `docker compose up -d --build`。
- 能根据现象选择排错命令：使用 `docker compose ps` 查看服务状态，使用 `docker compose logs app --tail 100` 查看应用启动失败原因。
- 能识别 `port is already allocated` 表示宿主机端口被占用，并优先检查 IDEA 中是否仍运行着占用 8080 的 Spring Boot 进程。

### AuthService 单元测试

- 独立补充了“用户已禁用时登录失败”的 Mockito 单元测试：模拟用户存在、密码正确但 `status = 0`，断言抛出 403 和“用户已禁用”，并验证 JWT 未生成。
- 已纠正测试数据表达：请求密码使用明文，数据库用户密码使用模拟密文，`passwordEncoder.matches(明文, 密文)` 返回 `true`。
- 当前 `AuthServiceTest` 已覆盖登录成功、密码错误、用户不存在和用户禁用四条主要分支。
- 能说明 Mockito 单元测试只验证模拟依赖条件下的 Service 业务逻辑，不能证明真实 SQL、MySQL 数据、Controller、Spring Security、HTTP 请求和真实 JWT 链路全部正确。
- 能初步区分：Mockito 单元测试验证单个 Service 逻辑，Spring 集成测试验证多个组件协作，Postman 接口测试验证真实 HTTP 运行链路。

### 本轮达标判断

- **Docker 基础表达：达到实习入门要求。** 能解释镜像、容器、端口、服务名、环境变量、重新构建和常见启动排错；仍不表述为“熟练掌握 Docker”。
- **测试基础：达到模板应用要求。** 能参考现有测试独立补充分支，并理解单元测试的证明范围；暂不要求脱离参考默写全部 Mockito API。
- **下一次继续：** 优先进行 Java 集合、对象和方法组合的主动编码，再回到现有项目做一个安全的小型错误定位，不增加重复 CRUD。

## 2026-07-29 学习记录

### Java 主动编码

- 独立编写 `createRestockPlan(List<ProductItem>, int)`：遍历商品对象列表，把库存低于目标值的商品转换为“商品名称 → 需要补充数量”的 `Map`。
- 独立编写 `applyRestockPlan(List<ProductItem>, Map<String, Integer>)`：使用 `containsKey()` 和 `get()` 读取补货数量，并调用 `ProductItem.addStock()` 修改对象库存。
- 运行结果正确：补货计划为 `{显示器=3, 机械键盘=2}`，执行计划后机械键盘和显示器库存均变为 5，苹果手机保持 10。
- 已理解 `HashMap` 不保证输出顺序，键值对顺序不同不影响结果正确性。
- 使用 IDEA `Shift + F6` 完成变量安全重命名，知道 Rename 重构会统一修改同一变量的引用。

### 项目错误定位与测试

- 发现并修正注册逻辑中的用户名处理不一致：查重时使用 `request.getUsername().trim()`，保存时也应使用处理后的 `username`，避免带空格用户名导致登录失败或绕过查重。
- 修改后运行完整 Maven 测试：`Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`，最终 `BUILD SUCCESS`。
- 能说明该结果证明现有测试没有被破坏，但现有测试未专门覆盖用户名空格场景，因此不能把回归测试通过等同于该场景已被完整验证。

### JWT、Redis 与项目表达

- 能说明 JWT 请求主线：过滤器读取 `Authorization: Bearer ...`，校验签名、格式和有效期，解析用户名与角色，创建 `Authentication` 放入 `SecurityContext`，再由 Spring Security 完成认证和授权。
- 已纠正“JWT 过滤器重新判断用户名和密码”的误解：当前过滤器验证的是 Token，没有重新查询数据库校验账号密码。
- 能说明 Redis Cache-Aside 流程：首次查询未命中后查询 MySQL 并回填缓存，第二次查询直接命中 Redis；修改商品、库存或状态后删除缓存，下次查询重新加载最新数据。
- 一分钟项目介绍已进一步修正：订单总价是单价乘数量，接口返回 `OrderVO` 而不是 URL；原子 SQL 使用 `stock >= quantity` 和 `status = 1`；Dockerfile 构建应用镜像，Compose 编排 app、MySQL、Redis 多个容器。

### SQL 主动练习

- 完成“按日期统计有效订单”查询，综合使用 `DATE()`、`COUNT()`、`SUM()`、`WHERE`、`GROUP BY` 和 `ORDER BY`。
- 能区分 `COUNT(o.id)` 统计订单条数，`SUM(o.quantity)` 统计售出商品总件数，单个订单可以购买多件商品。
- 已通过真实数据验证：2026-07-23 有 1 个有效订单、购买 2 件商品、总金额 2598.00。
- 理解 `ONLY_FULL_GROUP_BY` 规则：`SELECT` 中未聚合的普通字段必须与 `GROUP BY` 的分组表达式一致；按 `DATE(create_time)` 分组时也应查询 `DATE(create_time)`，不能直接选择完整 `create_time`。

### 当前判断与下一步

- **今日技术输出已达标：** 完成 2 个 Java 方法、1 个真实项目 Bug 修正、9 个测试回归、JWT/Redis/Docker 复述和 1 条统计 SQL。
- **项目展示和开始投递门槛继续保持达标；稳定技术面仍需训练。**
- 下一次优先：独立完成 1 个包含空值或边界判断的 Java 方法；阅读 1 个现有 Service 测试并说明 Mock、执行、断言三部分；完成正式 PDF 简历并开始匹配岗位投递。

## 2026-07-30 学习记录

### Java 对象返回与空值边界

- 编写 `findLowestStockProduct(List<ProductItem>)`，能够处理列表本身为 `null`、空列表、列表中存在 `null` 元素和全部元素为 `null` 的情况。
- 最初使用 `int lowestStock` 保存最小库存，但方法返回类型是 `ProductItem`；已改为使用 `ProductItem lowest` 保存并返回完整商品对象。
- 已使用 `continue` 跳过列表中的 `null` 元素，避免调用 `product.getStock()` 时出现空指针异常。
- 已理解不应使用 `999999999` 作为人为最大值；通过 `lowest == null` 让第一个非空商品成为初始候选更安全。
- 使用 `Arrays.asList()` 构造包含 `null` 的测试列表；知道 `List.of()` 不允许包含 `null`。
- 运行验证成功：正常列表返回“显示器：2”，全部为 `null` 的列表返回 `null`。

### Spring IoC、DI 与 Bean

- 能说明 IoC（Inversion of Control，控制反转）：把对象创建、依赖组装和生命周期管理交给 Spring 容器。
- 能说明 DI（Dependency Injection，依赖注入）：Spring 通过构造方法把对象需要的依赖传进去，是实现 IoC 的方式之一。
- 能结合项目说明：Spring 创建 `AuthService`，并注入 `SysUserMapper`、`PasswordEncoder` 和 `JwtTokenProvider`。
- 已纠正职责混淆：MyBatis 负责生成 Mapper 代理对象，最终由 Spring 管理并注入 Service。
- 已理解 `@Service`、`@Component`、`@Mapper`、`@Configuration` 和 `@Bean` 在当前项目中注册 Bean 的不同方式。
- 能说明通过 `@Bean` 提供 `PasswordEncoder` 的好处：统一交给 Spring 管理、面向接口依赖，并方便 Mockito 注入 Mock 实现。
- 能从 Spring 启动错误中定位：`AuthService` 缺少 `PasswordEncoder` 时，应检查 `PasswordConfig`、`@Configuration` 和 `@Bean`。

### AOP、事务和测试边界

- 已了解 AOP（Aspect-Oriented Programming，面向切面编程）适合日志、耗时、事务、缓存等横切逻辑，不应把订单创建、库存扣减等核心业务隐藏在自定义切面中。
- 知道当前主项目没有自定义 `Aspect`，这不影响项目完整性；`@Transactional`、`@Cacheable` 和 `@CacheEvict` 的底层行为与 Spring 代理/AOP 机制有关。
- 能说明订单事务边界：`createOrder()` 的事务覆盖扣库存、库存记录和订单插入；默认 `REQUIRED` 传播下，`deductStock()` 会加入外层事务。
- 能分析缺少外层事务的后果：扣库存事务先提交后，订单保存失败无法回滚已提交库存，造成“库存减少但订单不存在”的数据不一致。
- 能区分 Mockito 单元测试不能证明真实数据库回滚；验证 SQL 和事务需要 Spring 集成测试、测试 MySQL/Testcontainers，或通过 Postman 与数据库人工验证。

### 扣库存安全修复

- 代码审查发现 `deductStock()` 只依赖 Controller DTO 校验，Service 本身未拦截非法 `productId` 和 `quantity`。
- 已在 `ProductService.deductStock()` 的 Mapper 调用前增加 ID 和数量非空、必须大于等于 1 的校验，防止负数进入 `stock = stock - quantity` 后反向增加库存。
- 新增 `shouldRejectNegativeDeductQuantity()` 单元测试：传入 `-1` 时断言 400 和错误消息，并通过 `verifyNoInteractions` 确认 Mapper 与库存记录服务均未调用。
- `ProductServiceTest` 和完整测试运行成功，项目测试总数增加到 10。
- 已完成独立 Git 提交，提交主题为“增加扣库存参数校验”。
- 能说明 Service 仍需校验的原因：Service 可能被其他 Service、定时任务、测试或未来入口直接调用，不能只依赖某个 Controller 的 `@Valid`。

### 统一异常处理

- 已结合真实代码理解 `BusinessException → GlobalExceptionHandler → ResponseEntity<ApiResponse<Void>>` 的处理链路。
- 能区分 `BusinessException` 是异常类型，`message` 是错误说明，`code` 是错误码，错误响应的 `data` 为 `null`。
- 知道 `@RestControllerAdvice` 提供全局 Controller 异常处理，`@ExceptionHandler` 按异常类型选择处理方法。
- 能区分真实 HTTP 状态码和 JSON 中的 `code`；`ResponseEntity` 控制 HTTP 状态，`ApiResponse` 统一 Body 结构。
- 了解当前处理器明确处理业务异常和参数校验异常；未匹配异常通常按 500 处理，生产项目可增加记录日志的通用兜底，但暂未要求立即实现。

### HashSet 自定义对象去重入门

- 已判断两个字段相同但分别 `new` 出来的 `ProductItem` 在未重写 `equals()`、`hashCode()` 时会同时进入 `HashSet`，集合大小为 2。
- 初步理解 `hashCode()` 用于哈希定位，`equals()` 用于判断业务相等；相等对象必须具有相同的 hashCode。
- 知道 IDEA 可通过 `Alt + Insert → equals() and hashCode()` 生成方法；可变库存不适合作为哈希身份字段，避免对象修改后集合定位异常。

### 当前达标判断与新对话下一步

- **项目展示门槛：已达到。**
- **开始投递门槛：已达到。** 用户当前明确暂不投递，后续由用户主动通知开始投递，不再反复催促。
- **稳定技术面门槛：尚未达到。** IoC/DI、事务、异常处理已经能理解和表达，仍需通过主动编码和真实排错提高稳定性。
- 新对话先读取 `AGENTS.md`、`LEARNING_PROGRESS.md`、`LEARNING_ERRORS.md` 和 `JAVA_ALGORITHM_ERRORS.md`。
- 下一步优先完成 `equals()`/`hashCode()` 的一个小型对象去重实验，然后回到现有项目做一次不增加 CRUD 的代码阅读或错误定位。

## 2026-07-31 学习记录

### 商品价格区间校验与单元测试

- 在 `ProductService.getProductListByNameAndPrice()` 中增加价格区间校验：当最小价格和最大价格都不为空，并且最小价格大于最大价格时，抛出 400 业务异常。
- 正确使用 `BigDecimal.compareTo()` 比较金额，理解 `minPrice.compareTo(maxPrice) > 0` 表示最小价格大于最大价格。
- 新增 `shouldRejectInvalidPriceRange()` 单元测试，使用最小价格 `1000.00`、最大价格 `100.00` 触发异常，断言错误码和错误消息。
- 使用 `verifyNoInteractions()` 验证参数不合法时不会继续调用 Mapper 或库存相关组件。
- `ProductServiceTest` 通过，项目完整测试总数增加到 11；已提交“增加商品价格范围校验”。

### Java 比较、排序与参数校验

- 理解 `Long` 是包装类型：`==` 可能比较对象引用，业务 ID 推荐使用 `.equals()`；允许空值时可使用 `Objects.equals()`。
- 理解 `BigDecimal.equals()` 会比较数值和小数位，`compareTo()` 主要比较数值大小；金额大小判断优先使用 `compareTo()`。
- 完成商品两级排序：先按库存升序；库存相同时，再按名称字典顺序排序。
- 已理解比较器返回负数、零、正数分别表示第一个元素排前、两者当前条件相同、第一个元素排后；复杂 Comparator 目前仍需要模板提示。
- 理解 `Integer` 可表示 `null`，适合 DTO 配合 `@NotNull`；`int` 是基本类型，未赋值时默认为 0，无法区分“未传”和“传了 0”。
- 理解 `@NotBlank` 可以拦截 `null`、空字符串和纯空格；`@NotNull` 只限制不能为空，具体范围还需要 `@Positive` 等注解。
- 理解 `final` 修饰引用变量后不能让变量重新指向其他对象，但仍可修改该对象内部允许变化的内容。

### Maven 与 IDEA Debug

- 阅读真实 `pom.xml`，知道 POM 是 Project Object Model（项目对象模型），用于描述项目、依赖和构建配置。
- 能区分 Maven Dependency（依赖）与 Plugin（插件）：`spring-boot-starter-test` 为测试提供 JUnit、Mockito 等依赖；`spring-boot-maven-plugin` 参与 Spring Boot 打包。
- 初步理解 Maven Scope（依赖作用域）中的 `compile`、`runtime` 和 `test`，以及 `mvn test`、`mvn package`、`mvn clean package` 的用途。
- 开始使用 IDEA Debug（调试）：已在价格区间判断处设置 Breakpoint（断点），通过 Debug 运行测试并观察 `minPrice`、`maxPrice`。
- 已接触 `F8` Step Over（单步跳过），用于执行当前行但不进入该行调用的方法内部；本次 Debug 流程尚未完整练完，暂不算独立掌握。

### MySQL 掌握边界与当前判断

- MySQL 基础不是当前新手内容：已经学习过 JOIN、GROUP BY、HAVING、子查询、聚合、索引、EXPLAIN、事务、锁和 `FOR UPDATE`。
- 后续 MySQL 不再重复 `KEY` 等基础定义，优先结合真实项目练习 SQL、执行计划和索引优化。
- **项目展示门槛：已达到。**
- **开始投递门槛：已达到，但按当前决定暂不投递。**
- **稳定技术面门槛：尚未达到。** 当前主要短板是脱离提示组织代码、读懂较复杂 Java 表达式和独立排错。
- 下一步：完成一次完整 Debug 流程，观察异常抛出和测试通过；随后做一次真实项目错误定位，不继续增加低价值 CRUD。

### IDEA Debug 与测试失败定位（已完成）

- 已在 `ProductService` 价格区间判断处设置断点，观察 `minPrice=1000.00`、`maxPrice=100.00`，使用 `F8` 执行到抛出异常，并使用 `F9` 继续运行到测试结束。
- 理解 `assertThrows()` 期望目标代码抛出指定异常，因此业务代码执行 `throw` 后测试仍然可以成功。
- 能阅读简单调用栈：异常名称说明发生了什么，`ProductService.java:行号` 是异常产生位置，`ProductServiceTest.java:行号` 是调用位置。
- 能根据 JUnit 的 `Expected` 与 `Actual` 判断测试命中了错误的校验分支；能根据 Mockito 的 `No interactions wanted` 判断非法参数没有在数据库访问前被拦截。
- 当前已完成一次完整 Debug 流程；常用断点、Variables、`F8` 和 `F9` 已理解，仍需在后续真实错误中继续使用才能达到熟练。

### Controller 切片测试

- 新增 `ProductControllerTest`，使用 `@WebMvcTest` 和 `MockMvc` 验证扣库存接口的 HTTP 请求、JSON 转换、参数校验和统一响应。
- 失败路径：`quantity=-1` 返回 HTTP 400，错误消息正确，并通过 `verifyNoInteractions(productService)` 证明请求未进入业务层。
- 成功路径：合法请求返回 200，并通过 `verify(productService).deductStock(4L, 2)` 证明路径 ID 和 JSON 数量正确传入 Service。
- `@WebMvcTest` 初次启动时缺少 `JwtTokenProvider`；增加 `@MockBean JwtTokenProvider` 后成功加载精简 Web 环境。
- 能区分 Service 单元测试、Controller 切片测试和 Spring 上下文测试；当前 Controller 测试关闭 Security 过滤器，因此不用于证明 JWT 正确。

### 库存概览 SQL 聚合优化

- 将库存概览接口从 4 条独立统计 SQL 优化为 1 条条件聚合 SQL，一次返回商品总数、上架商品数、缺货商品数和上架库存总价值。
- 使用 `COUNT(CASE WHEN ... THEN 1 END)` 完成条件计数，使用 `SUM(CASE WHEN ... THEN price * stock ELSE 0 END)` 计算库存价值，使用 `COALESCE(..., 0)` 防止空表返回 `null`。
- 真实排错时根据 MyBatis 日志发现仍执行 4 条 SQL，读取源码后确认 Mapper 已增加新方法，但 Service 仍调用旧逻辑；修改 Service 后日志只剩 1 条统计 SQL。
- 通过接口结果和 MyBatis 日志验证四项统计正确；`EXPLAIN` 显示小表约扫描 25 行并选择 `ALL`，由于约 92% 数据符合逻辑删除条件且聚合需要读取大部分数据，目前全表扫描合理。

### 注册并发、日志与状态枚举

- 理解“先 `selectCount` 再插入”不能单独解决并发注册：两个请求可能同时查询到用户名不存在；数据库唯一索引负责最终防止重复数据。
- 在 `AuthService.register()` 捕获 `DuplicateKeyException`，把数据库重复键错误转换成 400“用户名已存在”，并通过 Mockito 模拟插入冲突验证异常转换。
- 能说明 Service 预查询用于改善普通请求反馈，数据库唯一索引用于并发情况下的最终保证。
- 在 `OrderService` 使用 SLF4J `log.info` 记录订单创建成功的订单 ID、用户 ID、商品 ID 和数量；已通过真实创建订单请求观察到日志，未记录密码或 JWT。
- 创建 `OrderStatus` 和 `ProductStatus` 枚举，集中维护状态码和中文文本；数据库与 Entity 继续使用整数，Service 和 VO 不再直接依赖部分魔法数字。
- 理解枚举的 `values()` 返回全部枚举值，增强 `for` 循环逐个比较 code，匹配后返回 text，未匹配时返回“未知状态”。

### Maven 编译排错与并发机制复盘

- 创建 `ProductStatus` 后遇到测试编译阶段找不到主类和 `NoSuchFileException`；确认源码存在后使用 `mvn clean test` 删除旧 `target` 并完整重建，问题解决。
- 能区分日常使用 `mvn test`，编译缓存明显异常时使用 `mvn clean test`；不需要每次都执行 `clean`。
- 能说明 `synchronized` 主要约束单个 JVM；多实例共享库存应由数据库原子 SQL 提供最终并发保证。
- 能区分原子 SQL 与事务：原子 SQL 防止库存扣成负数，`@Transactional` 保证库存、库存流水和订单保存作为整体提交或回滚。

### 微服务调用链重新梳理

- 已按真实代码区分两个调用路径：Gateway 可直接通过 `lb://notification-service` 转发到通知服务；也可先进入主服务，再由 `NotificationClient` 通过 OpenFeign 调用通知服务。
- 能说明 Gateway 负责统一入口、JWT 校验和路由；Nacos 保存服务名与实例地址；OpenFeign 把跨服务 HTTP 请求封装成 Java 接口调用。
- 当前属于“结合真实代码理解调用链”，尚未达到能独立搭建 Nacos、OpenFeign、Gateway 的程度；后续应从现有三个模块的运行和故障验证继续，不新增第二套示例。

### 今日达标判断与下一步

- **项目展示门槛：已达到并进一步增强。** 新增 Controller 测试、统计 SQL 优化、并发注册兜底、业务日志和状态枚举。
- **开始投递门槛：已达到。** 按用户当前决定暂不投递，不反复催促。
- **稳定技术面门槛：仍未完全达到，但真实排错能力有明显进步。** 今天能够根据日志发现 Service 未调用新 Mapper，并使用 `mvn clean test` 处理编译缓存异常。
- 下一步：继续运行并解释现有微服务的完整请求链与故障降级；Java 练习继续要求从方法签名独立组织代码，不增加低价值 CRUD。

### 微服务环境实际运行与当前掌握程度

- 实际启动了 `springboot-practice` 中的 Nacos、主服务、通知服务、Gateway、MySQL、Redis 和 RabbitMQ 容器。
- 验证了通知服务直连地址：`8081/api/notifications/ping` 返回 200。
- 验证了主服务通过 OpenFeign 调用通知服务：`8080/api/microservices/notification/ping` 返回 200。
- 验证了不携带 JWT 访问 Gateway 完整路径时返回 401，说明请求在网关鉴权阶段被拦截。
- 注册测试用户时遇到 500，通过日志定位到 `UnknownHostException: mysql`，确认不是 JSON 写错，而是主服务容器无法解析 MySQL 服务名。
- 重新创建 MySQL、Redis、主服务和 Gateway 容器后，MySQL 正确加入 `springboot-practice_default` 网络，并获得 `mysql` 网络别名。
- 当前能够在指导下完成容器启动、接口请求和日志验证，但对完整微服务调用链仍然看不懂，**不能记录为已经掌握 Nacos、Gateway 或 OpenFeign**。

当前只需要先记住一条主线：

```text
Postman -> Gateway -> 主服务 -> 通知服务 -> 返回结果
```

- Gateway：统一接收请求并检查 JWT，然后把请求转发给目标服务。
- Nacos：保存“服务名对应哪些运行地址”，帮助 Gateway 和 OpenFeign寻找服务。
- OpenFeign：主服务调用通知服务时使用的 Java 接口，底层仍然发送 HTTP 请求。
- Docker 网络：让容器能够使用 `mysql`、`redis` 等服务名互相访问。

#### 本次真实掌握判断

- **完成程度：** 在帮助下运行成功并验证了部分链路。
- **理解程度：** 只知道组件的大致职责，完整请求过程暂时无法独立复述。
- **独立能力：** 暂时不能独立搭建或排查这套微服务环境。
- **简历表述：** 目前最多写“了解 Nacos、OpenFeign、Gateway 的基本作用”，不能写“熟练掌握 Spring Cloud 微服务”。

#### 下一步

- 暂停增加新的微服务术语和配置。
- 只选一次成功请求，根据真实 Controller、Feign Client 和 Gateway 配置逐段追踪，不要求背配置。
- 验收标准：能够用自己的话说明请求先到哪里、谁调用谁、Nacos在其中提供什么信息。

## 2026-08-01 学习记录

### Gateway、Nacos 与 OpenFeign 真实调用链

- 根据 `springboot-practice` 的真实 Gateway 配置、`MicroserviceController`、`NotificationClient` 和通知服务 Controller，逐段追踪了一次请求。
- 能理解访问 `9000` 端口时，请求首先进入 Gateway；Gateway 根据 `/user/**` 路由选择主服务，并通过 `StripPrefix=1` 删除只用于网关识别服务的 `/user` 前缀。
- 能理解主服务的类级 `@RequestMapping` 与方法级 `@GetMapping` 会拼成完整接口路径。
- 已修正调用顺序：不是 Gateway 调用 OpenFeign，而是 Gateway 先把请求转发给主服务，再由主服务的 `MicroserviceController` 调用 `notificationClient.ping()`。
- 能说明 OpenFeign 负责发送服务之间的 HTTP 请求，Nacos 只负责根据服务名提供实际服务地址，不参与最终业务数据返回。
- 能说明返回值遵循“谁调用，结果先返回给谁”：通知服务结果先回主服务，再回 Gateway，最后回 Postman。

当前能够复述的请求与返回路线：

```text
请求：Postman -> Gateway -> 主服务 -> 通知服务
返回：通知服务 -> 主服务 -> Gateway -> Postman
```

### OpenFeign Fallback 降级

- 阅读真实 `NotificationClientFallback`，理解 `fallbackFactory` 把 OpenFeign 接口与备用实现关联起来。
- 理解 `FallbackFactory.create(Throwable cause)` 会在调用失败时创建备用的 `NotificationClient`；`cause` 保存连接失败、超时等原因。
- 能把 Lambda `return () -> "通知服务暂时不可用，请稍后再试"` 对应到“实现 `NotificationClient.ping()` 并直接返回备用文本”。
- 实际停止通知服务后请求主服务，成功获得“通知服务暂时不可用，请稍后再试”，随后重新启动通知服务。
- 能说明 Fallback 不会自动修复或重启通知服务，它只提供备用结果，避免依赖故障直接演变成主服务 500。

### RabbitMQ 概念、代码与实际日志

- 能区分 OpenFeign 与 RabbitMQ：需要立即取得另一个服务的结果时使用 OpenFeign；不需要立即结果、希望稍后异步处理时可使用 RabbitMQ。
- 阅读真实 `TaskNotificationProducer`，理解 `RabbitTemplate` 由 Spring 注入，`convertAndSend(交换机, 路由键, 消息)` 负责发送消息。
- 阅读真实 `TaskNotificationConsumer`，理解 `@RabbitListener` 会持续监听指定队列；队列出现消息后由 Spring 自动把消息传给方法参数，不需要 `TaskService` 手动调用消费者。
- 阅读真实 `RabbitConfig`，理解配置类创建 Queue、TopicExchange，并通过 Binding 和 Routing Key 建立交换机到队列的路由规则。
- 能解释 Spring AMQP、RabbitMQ 的关系：AMQP 是消息协议，RabbitMQ 是消息中间件，Spring AMQP 提供 Java 操作封装。
- 通过真实创建任务接口观察到生产者发送、请求完成、消费者收到消息并处理的日志，验证了基础异步消息链路。
- 能独立复述：生产者发送消息，交换机根据 Routing Key 路由，队列保存消息，消费者取得并处理。

### AOP 真实代码阅读

- 阅读 `PerformanceAspect` 和 `LogAspect`，理解 AOP（Aspect-Oriented Programming，面向切面编程）用于集中处理日志、耗时统计等公共逻辑，避免在每个业务方法中重复编写。
- 能说明 `@Aspect` 标记切面，`@Pointcut` 选择目标方法，`@Around` 在目标方法前后执行公共代码。
- 理解 `joinPoint.proceed()` 会继续执行被拦截的原业务方法并取得返回值；删除它会导致真正的 Service 方法不执行。
- 能说明 `PerformanceAspect` 统计 Service 方法耗时，`LogAspect` 记录 Controller 请求方式、路径、用户、IP、耗时和异常。
- 能读懂 `execution(* com.example.springbootpractice.service.*.*(..))` 的整体意思：匹配 Service 包中任意类、任意返回类型、任意参数的方法；当前不要求默写表达式。

### 今日真实掌握程度

- **已达到：** 能结合项目真实代码解释 Gateway、OpenFeign、Nacos、Fallback、RabbitMQ 和 AOP 的核心职责；能读懂最基础的 Producer、Consumer 和切面代码。
- **尚未达到：** 不能从空白独立搭建微服务、RabbitMQ 配置或 AOP 切面；复杂配置和固定 API 仍需查资料或使用模板。
- **学习方式修正：** 框架学习不能只停留在概念流程；建立概念后必须回到真实代码，至少读懂入口、核心调用和验证日志，才能算“接触并理解”。
- **简历边界：** 当前可以表述为“了解 Nacos、OpenFeign、Gateway、RabbitMQ 和 AOP 的基本使用与调用流程”，不能写“熟练掌握 Spring Cloud 微服务”。

### 达标判断与下一学习日六小时安排

- **项目展示门槛：已达到。** 库存订单主项目仍是简历核心，微服务练习只作为扩展认知。
- **开始投递门槛：已达到。** 按当前决定暂不投递，等待用户主动开始。
- **稳定技术面门槛：尚未达到。** 当前框架流程能在提示下解释，但独立编码、稳定复述和错误定位仍需练习。

下一学习日不继续增加 Sentinel、死信队列等新技术，安排如下：

1. 1 小时：不看文档画出并口述 Gateway、主服务、OpenFeign、Nacos、通知服务的请求与返回路线，验收为连续讲清一次。
2. 1 小时：阅读 Producer、Consumer 和 RabbitConfig，各自用一句话说明作用，验收为能解释 `convertAndSend()` 三个参数和 `@RabbitListener`。
3. 1.5 小时：回到 `inventory-order-system`，独立阅读一个 Controller -> Service -> Mapper 完整调用链，不增加新接口，输出调用步骤。
4. 1.5 小时：完成一个 Java 集合或对象练习，要求从方法签名独立组织代码并运行成功。
5. 1 小时：复盘项目高频面试题，验收为独立回答事务、原子扣库存、Redis 缓存一致性以及 OpenFeign/RabbitMQ 的区别。

### 事务边界、MQ 幂等与真实代码补充

- 能区分订单核心操作与辅助操作：扣减库存、保存库存操作记录和保存订单应处于同一个数据库事务；欢迎邮件、订单通知不应因为发送失败而回滚已经成功的订单。
- 阅读 `springboot-practice` 的真实 `TaskService.createTask()` 后确认：当前代码先执行 `taskMapper.insert(task)`，再调用 Producer 发送 RabbitMQ 消息，并且该方法当前没有 `@Transactional`。
- 能分析当前演示代码的风险：任务写入 MySQL 后若 MQ 发送抛出异常，接口可能返回 500，但任务已经存在；用户再次提交可能产生重复任务。
- 理解 Idempotency（幂等性）的核心含义：同一请求或同一消息重复到达时，只产生一次有效业务结果。消费者也需要防止重复消息造成重复短信、重复库存操作等副作用。
- 已明确普通 `@Transactional` 默认保证的是数据库操作，不能单独保证 MySQL 与 RabbitMQ 一定同时成功；本地消息表、消息确认等可靠消息方案属于后续扩展，目前不要求实现。
- 进一步对应真实消费者代码：`@RabbitListener` 监听队列，消息到达后由 Spring AMQP 自动调用 `handleTaskCreated(String message)`，它不是由 Controller 或 TaskService 手动调用。

### 订单 Service 单元测试补充

- 阅读并解释 `shouldNotRestoreStockWhenOrderAlreadyCancelled()`：`assertThrows()` 验证异常结果，两个 `verify(..., never())` 分别保证取消 SQL 和退库存方法没有被错误调用。
- 能说明只验证异常还不够：方法可能先错误修改库存再抛异常，因此还需要验证关键依赖的调用行为。
- 完成并运行 `shouldCreateOrderSuccessfully()`，验证订单总价为 `200.00`、`deductStock(4L, 2)` 被调用、`sysOrderMapper.insert(any(SysOrder.class))` 被调用，运行结果为 `exit code 0`。
- 已理解一次 `verify(productService).deductStock(4L, 2)` 会同时验证商品 ID 和数量，不需要为两个参数重复写两次相同验证。
- 能区分 Mockito 单元测试与集成测试：当前测试验证 Service 计算、分支和依赖调用，不能证明真实 SQL、Spring 配置或数据库事务回滚一定正确。

### 本轮达标判断与下一学习日六小时安排

- **项目展示门槛：已达到。** 能展示库存订单核心业务、事务、原子扣库存、Redis、JWT、Docker，并能把 MQ 作为扩展学习内容说明。
- **开始投递门槛：已达到。** 当前按本人决定暂不投递；需要投递时再进入岗位筛选与沟通阶段。
- **稳定技术面门槛：尚未达到。** 在真实代码和提示下能够正确解释，但从空白独立写测试、稳定复述 MQ 可靠性和快速定位异常仍需练习。

下一学习日安排：

1. 1.5 小时：从空白完成 1 个包含正常分支和异常分支的 Java 方法，运行至少 3 组输入。
2. 1 小时：不看答案复述创建订单、取消订单的事务边界，验收为说明哪些操作必须共同回滚。
3. 1 小时：独立阅读 1 个现有 Service 单元测试，指出准备数据、执行方法、结果断言和行为验证四部分。
4. 1 小时：完成 2 道数组、Map、Set 或栈的简单题，每题写出时间复杂度。
5. 1 小时：复盘 RabbitMQ 生产者、交换机、队列、消费者和幂等，验收为结合真实类名连续说明一次。
6. 0.5 小时：运行主项目测试并检查 `git diff --check`，只整理学习产生的真实改动。

### 后端巩固与前端开始时间

- 当前求职定位继续保持 Java 后端，前端只作为接口联调和项目展示能力，不转为前端学习路线。
- 接下来先用 5～7 个学习日巩固 Java 独立编码、订单业务、MySQL、事务、测试、排错和现有微服务调用链。
- 达到以下三个条件后开始前端：能独立讲清订单与事务；能独立修改一个简单 Service 方法并补测试；能判断常见编译、启动和 HTTP 状态码错误。
- 前端开始后采用“每天后端 4 小时、前端 2 小时”，先学习 HTML、CSS、JavaScript、Vue、Axios、JWT 请求和简单列表页面，不深入复杂样式与前端底层原理。

未来 7 天主线：

1. Java 集合、栈和数组，独立完成 2 道简单题并说明复杂度。
2. 复盘 `OrderService` 创建、查询、取消订单，独立阅读 1 个单元测试。
3. 独立完成 5 条包含 JOIN、分组或索引分析的业务 SQL。
4. 复盘事务、原子扣库存、并发超卖和幂等，连续回答 4 个项目问题。
5. 走通 JWT、Redis、Docker 启动与一次真实日志排错。
6. 根据真实代码复述 Gateway、Nacos、OpenFeign、RabbitMQ 的调用链，不增加新中间件。
7. 独立修改一个小方法、补测试并构建成功；若验收通过，当天开始 2 小时前端基础。

## 2026-08-02 学习记录

### Java 栈独立编码

- 不看昨日完整答案，从空白独立写出 `ValidParenthesesReview.isValid()`，正确使用 `ArrayDeque<Character>`、`push()`、`pop()`、`isEmpty()` 和三组括号配对判断。
- 通过 5 组输入验证：正常连续括号、交叉错误、嵌套括号、只含左括号和只含右括号，结果全部正确。
- 能说明 `text.toCharArray()` 把字符串转换为字符数组，增强 `for` 每轮把一个字符赋给 `current`。
- 能解释 `char left = stack.pop()`：从栈顶删除最近的左括号，并把该字符保存到局部变量 `left`。
- 完成第二道栈题“删除字符串中的相邻重复项”，独立写出 `peek()` 比较栈顶、相同则 `pop()`、不同则 `push()` 的核心循环，测试运行成功。
- 能判断两道题时间复杂度均为 `O(n)`、最坏空间复杂度均为 `O(n)`。

### 创建与取消订单事务复盘

- 已纠正事务操作对象：创建订单需要共同保证的是扣减 `product` 库存、插入 `stock_operation` 流水和插入 `sys_order` 订单；`sys_user` 在该流程中只查询，不是需要回滚的修改。
- 能说明 Spring 默认根据方法是否抛出运行时异常判断事务回滚；`return null` 属于正常返回，不能作为可靠的失败信号。当前项目没有使用该错误写法。
- 能复述取消订单的核心校验：查询当前用户与订单并比较 `userId`，原子 SQL 只有满足订单 ID、用户 ID、`status = 1` 时才修改成功。
- 能说明 `affectedRows = 0` 表示数据库没有被修改，不能退库存；只有影响一行后才调用 `addStock()`，避免并发重复取消造成重复退库。
- 能说明 `cancelOrder()` 使用 `@Transactional` 的原因：订单状态、商品库存和入库流水必须一起成功或回滚。

### OrderService 单元测试

- 能分辨测试的 Arrange（准备数据与 Mock）、Act（调用 Service）、Assert（检查返回值或异常）和 Verify（检查依赖调用）四部分。
- 理解 `when(...).thenReturn(...)` 不会调用 Service，也不会查询数据库；它提前规定 Mock Mapper 将来被 Service 调用时返回什么对象。
- 能找到真正执行 Service 的 `orderService.createOrder(username, request)`，并理解 Mock 返回值会进入 Service 内部对应的 Mapper 调用位置。
- 能区分正常测试和异常测试：正常分支直接调用方法，意外抛异常会自动失败；异常分支使用 `assertThrows()`，未抛出预期异常则测试失败。
- 新增并通过 `shouldCreateOrderSuccessfully()`：验证总价 `200.00`、扣减商品 4 的 2 件库存、保存 `SysOrder`。
- 新增并通过 `shouldThrowWhenProductDoesNotExist()`：商品 `999999` 返回空时断言 404，并验证既不扣库存也不保存订单。
- 单独测试和全部 `mvn test` 均成功，`git diff --check` 通过。

### MySQL 五条业务 SQL 与执行计划

- 完成商品有效订单销售汇总：使用 `LEFT JOIN`、`COUNT(o.id)`、`SUM()`、`COALESCE()`、`GROUP BY` 和 `ORDER BY`，无订单商品仍显示 0。
- 使用 `HAVING` 只保留有效售出件数大于等于 2 的商品，能区分 `WHERE` 在分组前过滤、`HAVING` 在分组后过滤聚合结果。
- 使用 `NOT EXISTS` 查询没有有效订单的商品；首次误写 `o.id = p.id`，已改为正确关联 `o.product_id = p.id` 并验证结果。
- 编写库存流水一致性查询，使用入库加法和出库减法检查异常；首次使用 `=` 查到正常记录，已改为 `<>` 查询错误记录，结果为空。
- 编写订单金额一致性查询，检查 `total_price <> unit_price * quantity`，结果为空。
- 能解释 `COALESCE(SUM(...), 0)` 把没有数据时的 `NULL` 替换为 0，而 `COUNT(o.id)` 本身不统计 `NULL`。
- 能区分三种写法：不限制订单状态会统计全部订单；把状态放在 `ON` 中会保留全部商品但只连接有效订单；把右表状态放在 `WHERE` 中会过滤没有有效订单的商品。
- 阅读销售汇总 `EXPLAIN`：当前 `product` 约 25 行、`sys_order` 约 8 行，优化器选择全表扫描和哈希连接属于合理结果；理解 `type = ALL` 不等于 SQL 必然慢，需要结合数据量和查询目的。

### 今日达标判断

- **项目展示门槛：已达到。** 今天能够根据真实代码解释创建、取消订单和事务边界，并用 SQL 验证订单、库存流水一致性。
- **开始投递门槛：已达到。** 继续遵循本人暂不投递的决定，需要投递时再启动岗位沟通。
- **稳定技术面门槛：尚未完全达到。** 栈题和基础测试已有独立完成证据，但 Mapper 选择、SQL 关联字段和状态码仍会在首次编写时混淆。
- **前端开始条件：正在接近。** 已达到独立修改测试并完成全量回归；还需稳定完成一次 JWT/Redis/Docker 排错复盘后开始每天 2 小时前端。

### 下一学习日六小时安排

1. 1 小时：独立完成 2 道数组、Map 或 Set 简单题，运行至少 3 组边界输入并写复杂度。
2. 1.5 小时：读取主项目 Redis 缓存真实代码，独立讲清首次查询、缓存命中和修改后失效三条路径。
3. 1 小时：读取 JWT 过滤器真实代码，不看总结复述 Token 校验和 `SecurityContext` 写入流程。
4. 1 小时：启动主项目 Docker Compose，验证 health、登录和一个商品查询，并根据日志区分缓存命中与数据库查询。
5. 1 小时：连续回答事务、原子扣库存、订单价格快照、`LEFT JOIN` 和 Mockito 五个项目问题。
6. 0.5 小时：清理两个测试文件的多余空行与注释缩进，运行 `mvn test`、`git diff --check`，只提交确认过的改动。

## 2026-08-03 学习记录

### Java Map 题

- 完成“有效的字母异位词”：先检查两个字符串长度，再使用 `Map<Character, Integer>` 对第一个字符串计数、对第二个字符串减数，最后遍历 `map.values()` 检查所有次数是否为 0。
- 首次把字符 Map 当作数组使用 `map.get(i)`，已理解 Map 应通过真实 key 查值；只需要全部 value 时应遍历 `values()`。
- 完成“多数元素”的 HashMap 解法：遍历数组累计每个数字的出现次数，当次数大于 `nums.length / 2` 时返回该数字。
- 首次把多数元素条件写成大于数组总长度，已纠正为严格大于数组长度的一半。
- 两题均完成多组输入验证；HashMap 解法的时间复杂度为 `O(n)`，空间复杂度为 `O(n)`。

### Redis 真实代码与运行验证

- 核对主项目真实代码：`@Cacheable(cacheNames = "productById", key = "#id")` 使用方法参数 `id` 生成商品缓存 key；命中缓存时跳过方法体和 MySQL 查询。
- 理解 `@CacheEvict(key = "#id")` 只删除一个商品缓存，`allEntries = true` 清空 `productById` 缓存区域，适用于批量修改或删除。
- 核对真实配置：商品缓存 TTL 为 10 分钟，且 `cache-null-values=false`，不存在商品不会缓存。
- 明确项目现状：当前实现了商品缓存和修改后失效，但没有实现空值缓存或布隆过滤器，因此不能声称已经解决缓存穿透。
- 启动 Docker Compose 后确认 `inventory-app` 正常运行，`inventory-mysql` 与 `inventory-redis` 均为 healthy。
- 发现容器 MySQL 与 Windows 本地 MySQL 是两套独立数据；容器中有效测试商品 ID 为 1，使用 ID 4 查询得到 404 属于数据不存在，不是 JWT 或缓存故障。
- 清除 `productById::1` 后连续查询商品 1，日志只出现一次 MySQL `SELECT`，验证首次未命中查询数据库并回填、第二次命中 Redis 不再执行 SQL。

### JWT 与项目问题复盘

- 根据真实 `JwtAuthenticationFilter`、`JwtTokenProvider` 和 `SecurityConfig` 复述请求链路：读取 Authorization、截取 Bearer Token、校验签名与过期时间、提取 username 和 role、构造 Authentication、写入 SecurityContext、继续过滤器链。
- 理解 `filterChain.doFilter()` 让请求继续经过后续过滤器并进入 Controller；没有这一步，请求会停在当前过滤器。
- 连续回答事务、原子扣库存、订单价格快照、LEFT JOIN 和 Mockito 五个问题；事务、原子 SQL、价格快照和 JOIN 已基本正确。
- 修正 Mockito 概念：`when(...).thenReturn(...)` 是预设 Mock 方法将来的返回行为，不是读取参数；`assertEquals()` 比较结果，`verify()` 检查依赖方法是否按预期调用。
- 整理测试格式后运行全量测试：`Tests run: 16, Failures: 0, Errors: 0, Skipped: 0`，结果为 `BUILD SUCCESS`；`git diff --check` 无格式错误，只有正常的 LF/CRLF 提示。

### 今日达标判断

- **项目展示门槛：已达到。** 能用真实日志演示 JWT 保护接口、Redis 缓存命中、Docker 容器运行，并能说明订单事务和原子扣库存。
- **开始投递门槛：已达到。** 继续遵循本人暂不投递的决定，需要投递时再开始岗位筛选与沟通。
- **稳定技术面门槛：正在接近但尚未完全达到。** 核心项目问题已能回答，但 Mockito 的 Mock 行为、容器与本地数据边界仍需要更多独立排错练习。
- **前端开始条件：已达到基础启动条件。** 已能解释订单事务、完成测试修改并识别常见 404、401 和运行环境问题；后续可以按“后端 4 小时 + 前端 2 小时”开始接口联调所需的前端基础，不改变 Java 后端求职方向。

### 下一学习日六小时安排

1. 2 小时：前端基础第一天，完成一个包含标题、商品表格和查询按钮的静态 HTML 页面，能解释标签、属性和基本 CSS，不接后端。
2. 1 小时：从空白完成 1 道数组或 Map 简单题，至少运行正常、空数据和边界数据 3 组输入，并说明复杂度。
3. 1 小时：独立阅读一个现有 Service 方法和对应测试，用 Arrange、Act、Assert、Verify 标出四部分，不新增业务接口。
4. 1 小时：独立写 3 条订单业务 SQL，至少包含一次 JOIN、一次 GROUP BY/HAVING 和一次数据一致性检查。
5. 1 小时：不看文档连续复述 JWT、Redis、事务、原子扣库存和 Docker 五个项目问题，并运行一次 `mvn test` 验证项目状态。
## 2026-08-03 学习记录

### Java Map 练习

- 完成“有效的字母异位词”：使用 `Map<Character, Integer>` 对第一个字符串计数、对第二个字符串减数，并通过 `map.values()` 判断计数是否全部归零。
- 修正了把 `Map<Character, Integer>` 当成数字下标访问的错误：`map.get(i)` 中的 `i` 是整数，不能用于读取字符键；遍历值应使用 `map.values()`。
- 完成“多数元素”的 Map 解法：遍历数组累计次数，当次数大于 `nums.length / 2` 时返回该数字。
- 两道题均能说明时间复杂度为 `O(n)`，Map 解法的空间复杂度为 `O(n)`。

### Redis、JWT 与 Docker 真实验证

- 根据主项目真实代码复习 `@Cacheable(cacheNames = "productById", key = "#id")`：`#id` 使用当前方法的 `id` 参数作为缓存键，不同商品使用不同缓存条目。
- 能区分单个缓存失效与批量失效：修改单个商品使用 `key = "#id"`，批量操作使用 `allEntries = true` 清空该缓存区域。
- 确认当前配置 `spring.cache.redis.cache-null-values=false`，不存在的商品不会缓存；重复查询不存在的 ID 仍会访问 MySQL，因此当前项目不能表述为已经解决缓存穿透。
- 读取真实 `JwtAuthenticationFilter`、`JwtTokenProvider` 和 `SecurityConfig`，复习了读取 Bearer Token、校验签名和过期时间、提取用户名与角色、创建 `Authentication`、写入 `SecurityContext` 以及继续过滤器链的流程。
- 启动并确认 Docker Compose 中应用、MySQL、Redis 三个容器正常，MySQL 和 Redis 均为 healthy。
- 使用 Docker 数据库中的真实商品 ID `1` 连续查询两次：第一次日志出现 MyBatis 查询 SQL，第二次没有重复 SQL，验证 Redis 缓存命中成功。
- 能判断 Docker MySQL 与 Windows 本地 MySQL 是两套数据；请求商品 ID `4` 返回 404 后，通过容器数据库确认可用商品 ID 为 `1`。

### 项目面试题与测试收尾

- 能回答订单事务、原子扣库存、订单价格快照和 `LEFT JOIN` 条件位置的核心作用。
- 进一步修正 Mockito 术语：`when(...).thenReturn(...)` 是预设 Mock 行为，不是读取参数；`assertEquals()` 比较预期与实际结果，`verify()` 验证依赖方法及参数。
- 全量执行 `mvn test`：共 16 个测试，Failures 0、Errors 0、Skipped 0，结果 `BUILD SUCCESS`。
- `git diff --check` 通过，仅存在 Windows 下正常的 LF/CRLF 提示。

### 今日达标判断

- **项目展示门槛：已达到。** 能用真实日志展示 JWT 认证后的商品查询、Redis 首次查询与缓存命中，并能解释项目核心事务与并发控制。
- **开始投递门槛：已达到。** 继续遵循本人暂不投递的决定，准备投递时再进行岗位筛选和沟通。
- **稳定技术面门槛：继续接近但尚未完全达到。** 核心流程已能回答，仍需减少对提示的依赖，并稳定区分 Mock 预设行为、真实数据库行为和缓存行为。
- **前端开始条件：基本达到。** 已完成独立测试修改、全量回归以及 JWT/Redis/Docker 真实验证；下一学习日完成一次独立 Service 小修改与测试后，可开始每天 2 小时前端基础。

### 下一学习日六小时安排

1. 1 小时：从空白完成 2 道数组、Map 或 Set 简单题，每题运行 3 组输入并写出复杂度。
2. 1.5 小时：独立阅读一个现有 Service 方法，画出 Controller → Service → Mapper/Redis 的调用链，不新增低价值接口。
3. 1.5 小时：独立完成一个小型 Service 校验修改，并补充正常或异常分支单元测试。
4. 1 小时：复盘 MySQL 索引、事务、行锁和 2 条业务 SQL，要求能解释执行结果。
5. 0.5 小时：连续口述 JWT、Redis 缓存一致性、原子扣库存三个项目问题。
6. 0.5 小时：运行 `mvn test`、`git diff --check` 并整理改动；验收通过后开始安排后端 4 小时、前端 2 小时的学习节奏。
## 2026-08-04 学习记录

### Service 校验与全量测试

- 在 `OrderService.createOrder()` 最前面增加购买数量校验：`quantity == null` 或小于 `1` 时抛出 400，避免错误输入进入用户查询、商品查询和库存扣减流程。
- 独立完成 `shouldRejectInvalidOrderQuantity()`，使用 `assertThrows()` 检查异常，并使用 `verifyNoInteractions()` 验证 `sysUserMapper`、`productMapper`、`sysOrderMapper` 和 `productService` 均未被调用。
- 单独执行 `OrderServiceTest`：4 个测试全部通过。
- 执行全量 `mvn test`：17 个测试，Failures 0、Errors 0、Skipped 0，结果 `BUILD SUCCESS`；`git diff --check` 通过。

### 前端基础与真实接口联调

- 在主项目中新建 `frontend/index.html`、`style.css` 和 `app.js`，使用 IDEA 静态服务器成功打开页面。
- 掌握 HTML 的页面结构以及 `h1`、`p`、`label`、`input`、`button`、`div`、`link`、`script` 的基本用途。
- 使用 CSS 完成输入框、按钮、鼠标悬停、结果卡片和商品列表的基础样式；理解标签选择器、ID 选择器、类选择器、内外边距、圆角和阴影。
- 使用 `getElementById()`、`addEventListener()`、`value`、`textContent` 和 `innerHTML` 完成页面交互，能区分普通文本显示和 HTML 解析。
- 从单个模拟商品过渡到商品数组，使用 `find()` 查找商品，使用 `map()` 和 `join("")` 渲染商品列表。
- 新增开发环境 CORS 配置，允许 `http://localhost:*` 的前端页面访问后端常用 HTTP 方法和请求头；重新打包并构建 Docker 后，通过前端 `fetch()` 成功读取 `/actuator/health`。
- 完成真实登录：前端 POST JSON 到 `/api/auth/login`，读取 `responseData.data.token`，并在后续商品请求中添加 `Authorization: Bearer ...`。
- 完成真实单个商品查询、商品列表查询和新增商品；数据来自 Docker MySQL，不再使用模拟商品数组。
- 使用 `createProductHtml()` 提取重复的商品 HTML，理解函数用于封装重复逻辑以及 DRY 原则。
- 使用 `sessionStorage` 保存 JWT，验证刷新页面后仍可查询；新增退出登录和 401 清理 Token 的逻辑。
- 能说明 `fetch()` 发起异步请求，第一层 `then()` 处理 HTTP 响应并解析 JSON，第二层 `then()` 使用响应数据，`catch()` 处理请求或解析失败。
- 已明确前端校验用于改善用户体验，后端 `@Valid` 和 Service 校验才是无法绕过的业务防线。

### MySQL 索引与事务锁

- 对商品名称和价格筛选 SQL 执行 `EXPLAIN`，读到 `type=index`、`key=PRIMARY`、`rows=25`、`Using where; Backward index scan`，能结合小表数据量判断主键倒序扫描是合理选择，而不是看到扫描就盲目加索引。
- 对当前用户订单分页 SQL 执行 `EXPLAIN`，首次发现运行中的 `sys_order` 只有主键索引，与 `schema.sql` 声明不一致。
- 理解 Schema Drift（数据库结构漂移）：修改 `CREATE TABLE IF NOT EXISTS` 不会更新 Docker 数据卷中已存在的旧表。
- 实际添加 `idx_order_user_id(user_id)` 后，执行计划从主键索引扫描变为 `type=ref`、`key=idx_order_user_id`、`ref=const`、`filtered=100%`，完成一次真实索引修复和前后对比。
- 已提出补充 `idx_order_product_id(product_id)` 以对齐 `schema.sql`，但本轮没有收到执行结果，暂不标记为已完成。
- 结合真实原子扣库存 SQL理解 InnoDB 行锁：同一商品的并发更新需要排队，后执行请求会基于最新库存重新判断 `stock >= quantity`。
- 理解事务提交和回滚都会释放行锁；订单保存异常时库存回滚到原值，随后等待中的其他事务可以继续执行。

### 今日达标判断

- **项目展示门槛：已达到。** 除后端核心业务外，已经有一个能够真实登录、查询和新增商品的简单前端联调页面。
- **开始投递门槛：已达到。** 继续遵循本人暂不投递的决定，准备投递时再开始岗位筛选。
- **稳定技术面门槛：尚未完全达到。** 已能根据真实执行计划和代码说明索引、事务与缓存，但对事务回滚后释放锁、浏览器存储位置等细节仍需复述巩固。
- **前端阶段：已经正式开始。** 后续采用每天后端 4 小时、前端 2 小时；前端用于接口联调和项目展示，不改变 Java 后端求职方向。

### 下一学习日六小时安排

1. 1 小时：从空白完成“删除有序数组重复项”和另一道数组/Map 简单题，各运行 3 组输入并写复杂度。
2. 1 小时：复盘 `OrderService` 创建与取消订单，独立指出事务开始、数据库修改、异常和锁释放位置。
3. 1 小时：核对运行数据库和 `schema.sql` 的索引差异，确认 `idx_order_product_id`，再完成一条 JOIN 查询的 `EXPLAIN`。
4. 1 小时：连续回答原子扣库存、事务回滚、Redis 一致性、JWT 和 Mockito 五个项目问题。
5. 1 小时：整理当前原生 JavaScript，掌握函数、数组、`fetch`、Promise、`sessionStorage` 和错误处理，不继续堆叠页面功能。
6. 1 小时：了解 Node.js、npm、Vite 和 Vue 的职责；在原生页面逻辑能够复述后，再决定是否创建 Vue 练习项目。
## 2026-08-04：Java 后端实习竞争力八周计划

### 计划目标

- 学习目标从“达到基础投递线”调整为“提高中国 Java 后端实习筛选和技术面的竞争力”。
- 每天学习 6 小时，计划周期 8 周；完成后再进行集中投递准备验收。
- 不再每天重复事务、JWT、Redis 等已经能够解释的内容；旧知识使用第 1、3、7 天间隔复习。
- 学习时间中约 70% 推进新内容、20% 用于独立编码和排错、10% 用于间隔复习。
- 暂停深入前端，现有前端页面只用于接口联调和项目展示，当前主线保持 Java 后端。

### 每日六小时固定结构

1. 1.5 小时：Java 基础与算法。
2. 1.5 小时：MySQL、JVM、并发、网络或操作系统。
3. 2.5 小时：`inventory-order-system` 项目实战。
4. 0.5 小时：面试表达、错误复盘、Git 与学习记录。

### 第 1 周：Java 核心基础和独立编码

- 学习数组、字符串、List、Set、Map、Deque、面向对象、泛型、异常、`equals()`、`hashCode()` 和基础 IO。
- 练习双指针、HashMap、栈和二分查找。
- 新完成 10 道简单题，其中至少 5 道能够从空白独立完成；每题运行 3 组输入并说明复杂度。
- 独立完成一个包含对象、集合、参数校验和异常处理的小程序。
- 当前进度：已完成删除有序数组重复项、删除指定元素和移动零三道双指针练习。

### 第 2 周：多线程与 JVM

- 学习进程、线程、并发、并行、`Thread`、`Runnable`、`synchronized`、`volatile` 和线程池。
- 学习竞态条件、死锁基础、JVM 内存区域、对象创建、类加载和垃圾回收基础。
- 编写内存库存并发扣减实验，对比未加锁和加锁后的结果，并对应项目中的 MySQL 原子扣库存。
- 验收为能够解释竞态条件、编写基础线程池任务，并说明堆、栈和垃圾回收的用途。

### 第 3 周：Spring 原理与自动化测试

- 学习 IOC、DI、Bean、Spring MVC 请求流程、Filter、Interceptor、AOP、事务传播和隔离级别。
- 深化参数校验、统一异常、JUnit、Mockito、MockMvc、单元测试和集成测试。
- 为订单 Controller 和 Service 补充正常及异常测试。
- 项目全量测试从当前 17 个提升到至少 25 个。
- 验收为能够独立完成一个小型 Service 改动和对应测试，并能根据错误定位 Controller、Service 或 Mapper。

### 第 4 周：MySQL 与 Redis 深化

- 学习 B+Tree、聚簇索引、联合索引、最左前缀、覆盖索引、回表和 `EXPLAIN`。
- 学习事务隔离、行锁、间隙锁基础、JOIN、聚合、慢 SQL 与索引设计。
- 学习 Redis 数据类型、缓存穿透、击穿、雪崩和缓存一致性。
- 使用数据库迁移工具记录表结构变化，检查并补齐订单索引。
- 在项目中实现一个真实的缓存异常治理方案，不能把“使用缓存”夸大为“已经解决缓存穿透”。

### 第 5 周：RabbitMQ 与幂等

- 学习 Producer、Exchange、Routing Key、Queue、Consumer、ACK、重试、死信队列和重复消费。
- 在订单创建后发送异步通知消息，并使用业务唯一标识避免重复消费。
- 验证消费者失败与重试场景，补充日志和测试。
- 能够解释 MySQL 本地事务为什么不能天然保证数据库与 RabbitMQ 同时成功。

### 第 6 周：微服务基础落地

- 学习单体与微服务的区别，以及 Nacos、OpenFeign、Gateway、超时和 Fallback 的职责。
- 从当前项目中拆出一个小型通知服务，通过 Nacos 注册，使用 Gateway 作为统一入口，并完成一次 OpenFeign 调用。
- 使用 Docker Compose 启动整条调用链。
- 验收为能够画出请求调用路线，并通过日志判断请求停止在哪一个服务。

### 第 7 周：Linux、部署与工程能力

- 学习 Linux 文件、进程、端口和日志命令，以及 Maven 生命周期、Git 分支和冲突基础。
- 深化 Dockerfile、Docker Compose、Nginx 反向代理和 CI 基础。
- 完成 Docker 一键启动、健康检查、GitHub Actions 自动测试、数据库初始化和部署说明。
- 完善 README、接口文档和项目架构说明，使其他人能够根据文档启动项目。

### 第 8 周：面试与投递准备

- 集中训练 Java、JVM、并发、Spring、MySQL、Redis、MQ、微服务、项目问题和算法手写。
- 累计完成约 40～50 道适合实习面试的算法题，其中至少 15 道能够隔天从空白重写。
- 准备 10 分钟完整项目介绍和 3 分钟精简介绍。
- 准备至少 50 个核心面试问题，并完成一页真实、可信、可以接受追问的简历。

### 集中投递前验收条件

1. 能在 45 分钟内独立完成一个小型后端需求。
2. 能独立完成一道常见简单算法题。
3. 能解释 Java、Spring、MySQL、Redis 和项目核心设计。
4. 能根据日志独立定位常见启动或接口错误。
5. 项目能够一键启动、测试并演示。
6. 简历中的每一个技术点都能回答至少三个追问。

### 执行原则

- 每周最后一天进行一次验收；未通过的项目只补对应缺口，不整周重学。
- 新技术必须经过“理解用途、阅读真实代码、自己修改、运行验证、能够讲解”后，才可以写入简历。
- 计划用于提高竞争力，不代表完成后一定获得 Offer；实际投递时仍需根据岗位、届别、城市和实习时间进行筛选。

## 2026-08-04：八周计划第 1 周第 1 天补充记录

### Java 数组与字符串

- 完成“删除有序数组中的重复项”，能够使用 `readIndex` 读取元素、使用 `writeIndex` 记录有效写入位置，并返回去重后的有效长度。
- 完成“删除指定元素”，能够把不等于目标值的元素原地写到数组前部，并正确返回有效长度。
- 从已有经验独立完成“移动零”核心代码：先把非零元素写到前部，再从 `writeIndex` 开始把剩余位置填成 0。
- 完成“验证回文字符串”，使用左右双指针、`Character.isLetterOrDigit()` 和 `Character.toLowerCase()` 忽略标点、空格与大小写。
- 已理解 `readIndex`、`writeIndex`、`left` 和 `right` 都是示例自行命名的变量，不是 Java 自带方法。

### HashMap 字符映射

- 完成“同构字符串”，使用两个 `Map<Character, Character>` 同时维护正向和反向映射。
- 能理解两个 Map 分别保证“同一个原字符不能映射到不同字符”和“不同原字符不能映射到同一个字符”。
- 四组输入 `egg/add`、`foo/bar`、`paper/title`、`ab/aa` 运行结果符合预期。
- 时间复杂度为 `O(n)`，最坏空间复杂度为 `O(n)`。

### Java 泛型

- 学习泛型 `T` 的作用：它是方法声明的类型参数，用于让参数类型与返回类型保持一致，不是 Java 固定类型或普通变量。
- 完成通用方法 `getFirst(List<T> items)`，能够同时处理 `List<String>` 和 `List<Integer>`。
- 完成 `countOccurrences(List<T> items, T target)`，使用增强 `for` 和 `Objects.equals()` 统计任意类型目标元素的出现次数。
- 能说明 `T`、`E`、`K`、`V` 是泛型命名习惯，并理解泛型使用对象类型，因此 `List<int>` 不合法、`List<Integer>` 合法。

### Java IO 与异常

- 使用 `Path.of()`、`Files.writeString()` 和 `Files.readString()` 完成字符串文件写入与读取。
- 将 `throws IOException` 改为 `try-catch`，能够区分“把异常继续交给上层”和“在当前方法处理异常”。
- 使用不存在的路径触发文件读取失败，控制台正确输出 `文件操作失败：missing-directory\\message.txt`，证明异常被 `catch` 处理。
- 核对项目真实代码后，能区分 `IOException` 是受检异常，项目自定义 `BusinessException` 继承 `RuntimeException`，因此不强制写 `throws` 或 `try-catch`。
- 理解 `GlobalExceptionHandler` 使用 Spring 的 `@RestControllerAdvice` 和 `@ExceptionHandler` 统一处理业务异常。
- 完成商品库存文件练习：使用 `Files.write()` 写入多行数据、`Files.readAllLines()` 读取、`split(",")` 拆分，并使用 `Integer.parseInt()` 累加库存，运行结果为 `总库存：17`。

### 本轮真实掌握判断

- **已达到：** 能独立补全基础双指针、双向字符映射、简单泛型方法和文件库存累加核心代码，并完成运行验证。
- **仍需巩固：** 新算法首次编写时容易遗漏指针移动后的流程控制；泛型和受检异常刚接触，当前能解释和使用基础模板，但还不能要求默写全部 IO API。
- **学习路线调整：** 不再以“达到最低投递线”为目标，已建立八周竞争力提升计划；暂停深入前端，当前优先补 Java、并发/JVM、项目深度和计算机基础。

### 下一次继续位置

1. 处理商品文件中库存缺失、非数字或格式错误的输入，学习 `NumberFormatException` 与数据校验。
2. 完成本周剩余字符串、集合和异常练习，继续要求从核心方法独立组织代码。
3. 第 1 周结束时验收 10 道新题、至少 5 道从空白完成，并完成一个对象与集合综合小程序。

## 2026-08-05：Java 异常、多线程、JVM 与并发扣库存集成测试

### Java 文件数据校验

- 在商品库存文件中把库存改为 `abc`，真实触发 `NumberFormatException`，能够从堆栈中的 `ProductFilePractice.java:29` 定位到自己的代码行。
- 在逐行解析时使用 `try-catch` 捕获单行数字格式错误；坏数据被跳过，其他行继续累计，运行结果为“库存不是数字，跳过：显示器,abc”和“总库存：15”。
- 能区分 `IOException` 负责文件读写错误，`NumberFormatException` 负责字符串无法转换成数字；二者不能使用同一个具体 `catch` 互相替代。

### Java 多线程基础

- 能区分 Process（进程）与 Thread（线程），并理解同一个后端进程会使用多个线程并发处理请求。
- 运行两个 `Thread`，观察到输出顺序交错；将 `start()` 改成 `run()` 后，验证 `run()` 只是当前线程中的普通方法调用。
- 使用 `join()` 等待两个工作线程执行结束后再读取最终结果。
- 使用两个线程各执行一百万次 `count++`，实际得到 `1518341`，真实观察到共享变量的丢失更新。
- 使用 `static synchronized` 后结果稳定为 `2000000`；使用 `AtomicInteger.incrementAndGet()` 后同样得到正确结果。
- 使用 `Executors.newFixedThreadPool(2)` 让两个工作线程复用处理五个任务，理解 `submit()` 是把任务交给线程池执行。
- 已理解 `volatile` 主要保证可见性，不能把 `count++` 变成原子操作；理解死锁是多个线程互相等待对方持有的资源。
- 已接触 `ThreadLocal`：同一变量为每个线程保存独立数据；能够联系 Spring Security 当前请求认证信息理解其用途。

### 真实 MySQL 并发集成测试

- 新增 `ProductConcurrencyIT`，使用 `@SpringBootTest`、真实 `ProductMapper` 和 Docker MySQL，而不是 Mockito 模拟数据库结果。
- 测试创建库存为 1 的临时商品，使用两个线程同时各扣减 1，最终验证成功更新次数为 1、库存为 0，并在 `finally` 中物理删除临时数据。
- 能说明 `CountDownLatch` 的作用：先等待两个线程准备完成，再统一放行，以更接近同时请求的场景。
- 能说明 `Callable<Integer>` 返回 SQL 影响行数，`Future.get()` 等待任务结束并取得结果，两次 `submit()` 模拟两个用户同时扣库存。
- 已通过真实数据库验证：InnoDB 对同一行的并发更新进行控制，同时 `WHERE stock >= quantity` 执行业务条件判断；只有二者配合才能防止库存变为负数。
- 能区分单元测试与集成测试：Mockito 单元测试验证 Service 逻辑和调用行为；本次集成测试验证 Spring、MyBatis-Plus、JDBC 与 MySQL 的真实协作。
- 将类名从 `ProductConcurrencyIntegrationTest` 改为 `ProductConcurrencyIT`，避免普通 `mvn test` 自动运行数据库集成测试；使用 `mvn clean test` 清除旧 `.class` 后，普通测试构建成功。

### JVM 与事务基础

- 理解每个线程拥有独立栈，堆中的对象和类级共享数据可能被多个线程访问；判断线程安全的关键是多个线程是否修改同一份数据。
- 理解 `synchronized` 实例方法锁当前对象，`static synchronized` 方法锁类对象；二者都只在当前 JVM 内有效，不能替代多实例下的数据库并发控制。
- 理解堆、栈、GC、可回收对象和内存泄漏的基础含义；能够区分 `StackOverflowError` 与 `OutOfMemoryError` 的常见原因。
- 能把 `.java → .class → .jar → JVM` 与 `javac`、`mvn package`、`java -jar` 联系起来。
- 理解 Spring 默认事务传播 `REQUIRED`：已有事务时加入当前事务；订单保存失败时，库存和库存流水可以一起回滚。
- 理解 `BusinessException` 继承 `RuntimeException`，默认可以触发事务回滚；异常被 `catch` 后不再抛出时，Spring 可能把方法视为正常结束并提交事务。

### 本轮真实掌握判断

- **已经实操通过：** 线程交替执行、`start()`/`run()`、`join()`、丢失更新、`synchronized`、`AtomicInteger`、固定线程池，以及连接真实 MySQL 的双线程库存扣减集成测试。
- **能够解释但暂不能要求从空白默写：** `ExecutorService`、`Callable`、`Future`、`CountDownLatch`、`JdbcTemplate` 清理模板、`volatile`、`ThreadLocal` 和完整 Spring 集成测试结构。
- **项目展示是否达标：** 达标。项目已有 CRUD、认证、Redis、订单、事务、Docker 和真实并发防超卖验证，可以作为简历项目展示。
- **是否可以开始投递：** 已达到基础投递线；竞争力提升八周计划仍在进行。按当前用户选择暂不主动投递，用户明确提出开始投递时再进入岗位筛选和沟通阶段。
- **是否达到稳定技术面水平：** 尚未。Java 并发和 JVM 刚完成第一轮，需要继续补算法独立编码、Spring 原理、MySQL/Redis 深化，并训练不看提示的项目表达与排错。

### 下一学习日六小时安排

1. **1.5 小时 Java/算法：** 从空白完成二分查找及一个字符串或 HashMap 题；每题运行至少 3 组输入并说明时间、空间复杂度。
2. **1.5 小时并发/JVM：** 从空白重写一个双线程安全计数器；口头解释 `start()`、`run()`、`join()`、`synchronized`、`volatile`、堆和栈的区别。
3. **2.5 小时项目实战：** 设计并运行一个真实订单事务回滚集成测试，验证订单保存失败时库存与库存流水不会留下不一致数据；测试必须自行清理临时记录。
4. **0.5 小时复盘：** 根据真实日志区分缺少环境变量、账号密码错误、端口无服务和旧编译产物四类问题，并完成 Git 状态检查，不自动提交。

## 2026-08-07：二分查找、线程安全复写与订单事务回滚验证

### Java 与算法

- 从空白写出二分查找：使用 `left`、`right` 和 `mid` 缩小有序数组的搜索范围，三组输出为 `3、0、-1`。
- 能说明二分查找每轮排除约一半数据，时间复杂度为 `O(log n)`，额外空间复杂度为 `O(1)`。
- 完成最长公共前缀：从第一个单词取得候选前缀，其他单词不匹配时使用 `substring()` 从末尾逐个缩短。
- 补充空数组检查，并认识到健壮写法还应先判断 `words == null`。
- 完成合并两个有序数组：从末尾比较并写入 `nums1`，避免覆盖尚未读取的数据；三组输入运行成功，复杂度为 `O(m+n)` 和 `O(1)`。
- 完成最大子数组和：使用 `currentSum` 在“延续前一段”和“从当前元素重新开始”之间取较大值，再更新全局 `maxSum`；三组输入运行正确，复杂度为 `O(n)` 和 `O(1)`。

### 并发复写验收

- 从空白写出两个线程各执行一百万次累加的安全计数器。
- 使用 `static synchronized` 保护共享的 `count++`，最终结果稳定为 `2000000`。
- 能说明 `join()` 用于等待工作线程结束，`throws InterruptedException` 表示等待过程可能被中断。
- 当前已能独立组织线程创建、启动、等待和同步累加的基础结构；线程池、`Future`、`CountDownLatch` 仍以理解和使用模板为主。

### 真实订单事务回滚集成测试

- 新增 `OrderTransactionRollbackIT`，连接真实 Spring、MyBatis-Plus 和 Docker MySQL。
- 测试准备临时用户和库存为 5 的商品，使用 `@SpyBean` 与 Mockito 让订单 `insert()` 在最后一步故意抛出异常。
- 验证异常发生后商品库存恢复为 5、库存流水数量为 0、订单数量为 0，证明库存扣减、流水和订单属于同一个事务。
- 能说明默认事务传播 `REQUIRED`：`OrderService.createOrder()` 创建事务后，`ProductService.deductStock()` 加入同一个事务；SQL 已执行但尚未提交时仍可整体回滚。
- 能区分单元测试与集成测试：前者使用 Mock 隔离依赖，后者连接多个真实组件和数据库验证协作结果。
- 单独事务集成测试成功，随后执行普通 `mvn test`，全部单元测试通过。

### Docker 数据库结构问题

- 根据 Surefire 报告中的最底层 `Caused by`，先后定位数据库端口/密码错误和 `sys_order` 表缺失。
- 实际检查 Docker `inventory` 数据库时只有 `product`、`stock_operation`、`sys_user` 三张表；手动补建 `sys_order` 后测试通过。
- 理解 `/docker-entrypoint-initdb.d/` 中的初始化 SQL 只在 MySQL 数据卷首次初始化时执行；后来修改 `schema.sql` 不会自动升级已经存在的数据卷。
- 当前练习项目采用“更新完整 `schema.sql` + 对现有数据库手动执行增量 SQL”的方式；后续可学习 Flyway 或 Liquibase 管理数据库版本。

### Spring 与 MySQL 复盘

- 能说明 `new` 出来的 Service 没有被 Spring IOC 容器接管，也没有事务代理，因此不能依赖 `@Transactional` 自动生效。
- 能说明同类中的 `this.method()` 内部调用没有重新经过代理，方法上的独立事务增强可能不生效；事务入口通常放在 Service 的 `public` 方法上。
- 能说明异常被 `catch` 后不再抛出时，Spring 可能把方法视为正常完成并提交；`BusinessException` 继承 `RuntimeException`，默认可以触发回滚。
- 完成 6 条订单统计 SQL：条件聚合、商品销售汇总、`HAVING`、无有效订单商品、金额高于平均值的订单和按日期汇总。
- 对条件聚合和 `LEFT JOIN ... IS NULL` 查询执行 `EXPLAIN`；能关注 `type`、`key`、`rows` 和 `Extra`，并理解小表出现 `ALL` 不代表必须增加索引。

### 集成测试运行脚本

- 新增 `scripts/run-integration-tests.ps1`，从被 Git 忽略的 `.env` 读取数据库密码，设置 Windows 连接 Docker MySQL 所需的 `localhost:3307`，然后运行两个真实数据库 IT。
- 脚本运行成功，以后新开 PowerShell 后只需执行 `./scripts/run-integration-tests.ps1`，不再逐条设置临时环境变量。
- 普通 `mvn test` 与两个数据库集成测试均已分别验证成功。

### 本轮真实掌握判断

- **已经实操通过：** 二分查找、最长公共前缀、安全计数器复写、真实订单事务回滚集成测试及全量单元测试。
- **需要理解但无需默写：** `@SpyBean`、`doThrow()`、`JdbcTemplate` 和完整 Spring 集成测试模板。
- **项目展示是否达标：** 达标。项目现在不仅实现事务，还能用真实数据库集成测试证明订单失败时库存和流水会回滚。
- **是否可以开始投递：** 已达到基础投递门槛；按当前约定暂不主动投递，等学习者明确提出后再开始。
- **是否达到稳定技术面水平：** 尚未完全达到。事务和并发项目表达明显改善，仍需继续补 MySQL、Spring 原理、算法独立编码和系统化排错。

### 下一学习日六小时安排

1. **1.5 小时 Java/算法：** 完成 2 道数组或字符串题，其中至少 1 道从空白写出；每题运行 3 组输入并说明复杂度。
2. **1.5 小时 Spring：** 结合真实代码复盘 IOC、DI、AOP 代理和 `@Transactional` 生效条件，完成 5 个口头追问。
3. **2 小时 MySQL：** 编写并执行 6 条包含 JOIN、GROUP BY、子查询或索引分析的 SQL，使用 `EXPLAIN` 解释至少 2 条。
4. **1 小时项目工程：** 整理数据库增量 SQL 的使用说明，运行普通单元测试和两个真实数据库 IT，并根据日志独立判断失败层级。

## 2026-08-08：算法复写、Spring MVC 请求链与订单测试

### Java 与算法

- 完成固定长度滑动窗口最大和：先计算前 `k` 个元素之和，窗口右移时加入新元素并减去离开窗口的元素，运行成功。
- 再次从空白复写二分查找，修正了闭区间循环应使用 `left <= right`、找到目标应返回下标 `mid` 两个问题。
- 复写最大子数组和时最初混淆 `currentSum` 和 `maxSum`，修正后理解“当前连续和”与“历史最大和”必须分别更新。

### Spring MVC 与认证请求链

- 结合实际项目代码梳理请求流程：Tomcat 接收 HTTP 请求，JWT 过滤器尝试认证，Spring Security 判断权限，`DispatcherServlet` 分发到 Controller，Controller 调用 Service，Service 通过 Mapper 访问 MySQL，最后返回 JSON。
- 理解 `SecurityContextHolder` 保存当前请求的认证信息，Controller 中的 `Authentication` 可以从中取得用户名。
- 理解 `filterChain.doFilter()` 的作用是把请求继续交给过滤器链；缺少 JWT 时过滤器本身不一定直接抛异常，而是后续访问受保护接口时由 Spring Security 返回 401。
- 能区分 401 的两个来源：进入 Spring MVC 前由 Security 产生的未认证响应，以及 Service 抛出 `BusinessException(401)` 后由全局异常处理转换的响应。

### 订单 Service 单元测试

- 补充并运行订单取消成功、订单不存在、订单不属于当前用户、并发情况下订单状态已变化四类测试。
- 进一步理解单元测试的四段结构：`when()` 准备 Mock 行为，调用真实 `orderService` 方法，`assert` 检查结果或异常，`verify` 检查依赖方法是否被调用。
- 修正了把 `assertEquals()` 当作调用验证、在 `assertThrows()` 中直接调用 Mapper、以及 Mock 的订单 ID 与 Service 查询 ID 不一致等问题。
- 普通测试共 21 个通过；随后两个真实数据库集成测试也通过，结果为 `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` 和 `BUILD SUCCESS`。

### 集成测试一键脚本

- 修复 `scripts/run-integration-tests.ps1`，脚本现在会切换到项目目录、从 `.env` 读取数据库密码、设置 Docker MySQL 的连接信息、启动 MySQL，并运行两个集成测试。
- 中文错误字符串曾因文件编码损坏为乱码，导致 PowerShell 把后面的 Maven 参数误判为语法错误；脚本已改为纯 ASCII 提示并通过 PowerShell 语法解析检查。
- Docker Desktop 未启动时，`docker compose` 无法连接 `dockerDesktopLinuxEngine`；启动 Docker Desktop 后重新执行同一脚本，两个集成测试全部成功。

### 本轮真实掌握判断

- **已经实操通过：** 滑动窗口、二分查找、最大子数组和修正、四类订单取消单元测试、两个真实数据库集成测试。
- **仍需加强：** 目前能看懂并修改 Mockito 测试，但从业务分支独立决定“准备什么数据、Mock 哪些依赖、断言和验证什么”仍不稳定。
- **项目展示是否达标：** 达标。项目同时具备单元测试、并发防超卖集成测试和事务回滚集成测试证据。
- **是否可以开始投递：** 已达到基础投递门槛；继续遵守当前暂不投递的决定，等学习者主动提出时再开始。
- **是否达到稳定技术面水平：** 尚未完全达到。需要继续训练测试设计、Spring 请求链复述和不依赖提示的算法实现。

### 下一学习日六小时安排

1. **1.5 小时 Java/算法：** 从空白完成 2 道数组或字符串题，每题运行 3 组输入并说明复杂度。
2. **1.5 小时 Spring：** 不看笔记复述一次完整 HTTP 请求链，并结合项目指出 JWT 过滤器、Security、Controller 和全局异常处理的位置。
3. **2 小时测试设计：** 先用中文为订单创建或取消列出 3 个业务场景，再独立写出其中 2 个 Mockito 测试，重点训练 Given-When-Then。
4. **1 小时项目工程：** 分别运行普通测试和集成测试脚本，根据输出判断是代码失败、数据库未启动还是环境配置失败。

## 2026-08-10：Flyway、Redis 缓存穿透、日志排错与 Controller 测试

### Flyway 数据库版本管理

- 项目已经接入 Flyway 10.20.1 和 `flyway-mysql`，通过 `V1__init_schema.sql` 初始化数据库结构。
- Docker 日志已验证：Flyway 创建 `flyway_schema_history`、成功执行 V1；再次启动显示 `Schema is up to date. No migration necessary.`，说明迁移已执行且数据库版本最新。
- 能解释 `flyway_schema_history` 用于保存迁移版本、描述和执行结果。
- 能说明已经执行的 V1 不应继续修改；新增字段应创建 `V2__add_product_description.sql`，避免新旧数据库结构不一致。
- 能按“启动日志 → `flyway_schema_history` → 文件目录和命名 → SQL 内容”的顺序排查 V2 未执行问题。
- 已处理 Navicat 连接 Docker MySQL 时的端口和密码问题，理解修改 `.env` 不会自动修改已有 MySQL 数据卷中的账号密码。

### Redis 缓存穿透处理

- 在真实 `ProductService.getProductById()` 中加入短期空值标记：不存在商品首次查询 MySQL 后写入 `product:not-found:{id}`，有效期 5 分钟。
- 后续查询先检查 Redis 标记；标记存在时直接返回 404，不再重复访问 MySQL。
- 创建、批量创建和恢复商品后清除对应空值标记，避免商品已经存在但旧标记仍造成错误 404。
- 使用 Mockito 测试验证“标记存在时不调用 Mapper”和“首次不存在时写入带 TTL 的标记”；`ProductServiceTest` 7 个测试通过。
- Docker 中真实验证 `GET` 返回标记值、`TTL` 约为 290 秒，并通过日志确认重复查询只有第一次访问 MySQL。
- **真实掌握程度：** 已能解释完整业务流程和标记清理原因；嵌套 Mock、`StringRedisTemplate` 固定 API 仍允许参考模板，不要求从空白默写。

### JVM、并发与事务补充

- 能区分栈、堆、元空间、对象引用、类初始化和 GC 的基础作用。
- 能说明 `volatile` 主要解决可见性，`AtomicInteger` 可完成单 JVM 中的原子计数，但二者不能替代数据库原子 SQL 和事务。
- 结合 `ProductConcurrencyIT` 理解 `readyLatch` 等待两个线程就绪、`startLatch` 同时放行、`Future.get()` 等待并取得 SQL 影响行数；线程 A、B 的先后顺序不固定。
- 结合真实订单代码理解默认事务传播 `REQUIRED`：订单、库存和库存流水加入同一事务；运行时异常向外抛出时一起回滚。

### 后端日志排错

- 能区分 `Failures` 是断言结果不符合预期，`Errors` 是测试执行过程中发生异常，`Skipped` 是测试未执行。
- 遇到 `BUILD FAILURE` 时会向上寻找异常类型和最底层 `Caused by`，不再只根据 Maven 最后一行修改代码。
- 能判断 `BadSqlGrammarException` 通常需要检查具体 SQL、表字段、实际连接数据库和 Flyway 记录。
- 能判断 `Connection refused: localhost:6379` 属于 Redis 服务状态或连接地址问题；Windows 运行使用 `localhost:6380`，Docker 容器运行使用 `redis:6379`。

### 测试分层与 Controller 测试

- 能区分三类验证：Mockito 单元测试验证 Service 业务分支，MockMvc Controller 测试验证 HTTP/JSON/参数校验，真实数据库 IT 验证 SQL、锁和事务。
- 能根据业务执行路线决定测试数据：经过且需要返回值的依赖使用 `when()`；结果使用 `assert`；不应执行的依赖使用 `verify(..., never())` 或 `verifyNoInteractions()`。
- 结合真实 `ProductControllerTest` 理解 `@WebMvcTest`、`MockMvc`、`@MockBean` 和 `addFilters = false` 的用途；这些固定框架配置允许查资料和复制。
- 独立完成库存概览 Controller 测试：模拟 `InventoryOverviewVO`，发送 `GET /api/products/overview`，验证响应 JSON 和 Service 调用。
- 实际运行结果：`ProductControllerTest` 共 3 个测试，Failures 0、Errors 0，`BUILD SUCCESS`。

### 本轮真实掌握判断

- **已经实操通过：** Flyway V1 执行与重复启动检查、Redis 不存在商品短期标记、Redis TTL/日志验证、Controller 返回数据测试。
- **已经能够解释：** Flyway 为什么新增 V2、缓存穿透流程、单元测试/Controller 测试/集成测试区别、常见构建日志的第一层分类。
- **仍需加强：** 从完全空白设计复杂 Mockito 测试仍不稳定；Flyway 目前只完成 V1 和 V2 命名练习，尚未在主项目实际执行新的 V2；复杂 MockMvc Security/JWT 测试暂不要求。
- **项目展示是否达标：** 达标。项目已有认证、缓存、并发防超卖、订单事务、Flyway、Docker、单元测试和真实数据库集成测试证据。
- **是否可以开始投递：** 已达到 Java 后端基础投递门槛；继续遵守当前“暂不投递”的决定，学习者明确开始投递时再执行岗位筛选与沟通。
- **是否达到稳定技术面水平：** 尚未完全达到。项目主线较完整，但 Java/算法独立编码、测试场景设计和不看提示的项目表达还需继续稳定。

### 下一学习日六小时安排

1. **1.5 小时 Java/算法：** 从空白完成 2 道新题，其中至少 1 道使用前缀和、滑动窗口或 HashMap；每题运行 3 组输入并说明复杂度。
2. **1.5 小时测试设计：** 先为一个 Service 方法画执行路线，再独立列出成功、参数非法、数据不存在 3 个场景；从空白完成其中 1 个 Mockito 测试。
3. **1.5 小时 MySQL/Flyway：** 新建临时练习 V2，执行一次字段变更并查询 `flyway_schema_history`；确认成功后说明为什么不能修改 V1。
4. **1 小时项目复盘：** 不看笔记完整讲解订单创建、并发防超卖、事务回滚、Redis 缓存和 Flyway 五条主线。
5. **0.5 小时工程验证：** 运行普通单元测试和集成测试脚本，根据日志独立判断失败属于断言、代码异常、数据库或 Docker 环境中的哪一层。
## 2026-08-12：事务隔离、联合索引、异常体系、链表与运行环境

### Git 提交整理

- 将积累的修改按功能拆分为 7 个提交，分别覆盖 Flyway、Redis 防缓存穿透、订单校验与测试、集成测试、前端与跨域、Controller 测试和学习文档。
- 所有提交已推送到 `origin/main`，最终状态为本地与远程同步、工作区干净。
- 进一步理解一个提交应尽量只表达一个完整目的，业务代码和对应测试可以放在同一提交，学习文档应与功能代码分开。

### MySQL 事务隔离与锁

- 查询当前隔离级别得到 `REPEATABLE-READ`，理解它让同一事务中的普通重复查询通常看到一致的数据版本。
- 能区分脏读、不可重复读和幻读；理解 MVCC 通过数据版本提高并发读取能力。
- 理解普通 `SELECT` 通常是快照读，`SELECT ... FOR UPDATE` 是读取当前数据并锁定记录。
- 能说明防超卖不能只依赖隔离级别：项目依靠条件原子 `UPDATE`、InnoDB 行锁、SQL 影响行数判断和事务共同保证。
- 能判断最后一件库存被两个事务同时扣减时，第二个事务等待后重新判断条件，影响行数为 `0`。
- 理解死锁是多个事务形成循环等待；多商品更新保持统一顺序、缩短事务时间和使用合适索引可以降低风险。

### 联合索引

- 结合实际索引 `(status, create_time)` 理解最左前缀原则：只用 `status` 或使用 `status + create_time` 较合适，只使用 `create_time` 通常不能有效利用该联合索引。
- 理解 SQL 中条件的书写顺序不等于联合索引顺序，优化器会分析条件；真正关键的是索引字段排列和查询模式。
- 理解等值查询字段通常放在前面，范围或排序字段放在后面，并应使用 `EXPLAIN` 验证。
- 能说明低区分度的 `status` 单列索引可能价值有限，但 `(status, create_time)` 对“按状态查询最新订单并限制条数”仍有价值。

### Java 异常与 Spring 事务

- 能区分受检异常和非受检异常；确认项目的 `BusinessException` 继承 `RuntimeException`，属于非受检异常。
- 能说明 `RuntimeException` 不强制每层声明或捕获，适合交给全局异常处理器统一返回。
- 能说明 Spring 默认对向外传播的 `RuntimeException` 和 `Error` 回滚；普通受检异常通常需要配置 `rollbackFor = Exception.class`。
- 理解事务方法中捕获异常后不再抛出，可能使 Spring 认为方法正常结束并提交事务。

### 链表基础

- 创建了自定义 `ListNode` 节点类，理解节点由数据字段 `value` 和指向下一个节点的引用字段 `next` 组成。
- 独立完成链表节点计数和目标值查找，运行结果正确；查找方法首次遗漏 `current = current.next`，会导致第一个节点不匹配时死循环。
- 理解 `.next` 不是 Java 固定方法，而是示例类中自己定义的字段；`current = current.next` 是让变量移动到下一个节点，`current.next = previous` 才会修改链表结构。
- 初步理解反转链表的四步：保存下一个节点、改变当前节点指向、移动 `previous`、移动 `current`。
- **当前掌握程度：** 创建、连接、遍历和查找已经实操；反转链表只能在图示辅助下理解，暂不要求独立默写。

### 配置、终端与运行环境

- 读取实际 `application.properties` 和 `compose.yaml`，确认项目目前使用环境变量覆盖配置，尚未使用 Spring Profile 文件。
- 理解 `${DB_URL:默认值}` 是 Spring 配置占位符：优先读取环境变量，不存在时使用冒号后的默认值；密码和 JWT 密钥不设置公开默认值。
- 能区分 IDEA 运行时通过 `localhost:3307`、`localhost:6380` 访问容器，以及 Docker 应用通过 `mysql:3306`、`redis:6379` 访问服务。
- 理解端口映射 `3307:3306` 是“宿主机端口:容器端口”；容器间访问不依赖宿主机端口映射，Windows 程序访问容器服务才需要映射。
- 能区分 Terminal 是命令窗口，PowerShell、CMD、Git Bash、WSL Bash 是不同命令解释环境；当前统一使用 IDEA Terminal + PowerShell。
- 实际执行 `docker compose up -d mysql redis`，确认两个服务均为 `healthy`，随后从 IDEA 成功启动 Spring Boot 并验证健康接口。
- 理解开发环境、测试环境、预发布环境和生产环境；当前项目属于本地开发环境，并已完成本地容器化部署，不能描述为生产部署。

### 日志与部署表达

- 理解 `TRACE → DEBUG → INFO → WARN → ERROR` 的级别顺序；设置为 `WARN` 时不会显示 `INFO`。
- 能说明开发环境输出详细 SQL 便于排错，生产环境通常减少 SQL 日志以控制日志量、性能影响和敏感信息风险。
- 能准确表达：项目使用 Dockerfile 和 Docker Compose 完成本地容器化部署，可启动 Spring Boot、MySQL 和 Redis。

### 本轮真实掌握判断

- **已经实操通过：** Git 分组提交与推送、链表计数和查找、MySQL/Redis 依赖启动、IDEA 后端启动和健康检查。
- **已经能够解释：** 可重复读的作用、原子 SQL 与事务的区别、联合索引最左前缀、运行时异常回滚、配置占位符、宿主机与容器端口。
- **仍需加强：** 链表反转尚不能独立编写；MVCC、当前读和死锁目前以概念理解为主；生产环境配置尚未真实部署验证。
- **项目展示是否达标：** 达标。代码、测试、Flyway、Redis、Docker 和前端演示均已分类提交并推送，仓库状态整洁。
- **是否可以开始投递：** 已达到基础 Java 后端实习投递门槛；继续遵守当前暂不投递的决定，学习者明确开始时再执行投递。
- **是否达到稳定技术面水平：** 尚未完全达到。项目核心问题能够解释，但 Java 数据结构、无提示编码和数据库并发细节仍需稳定。

### 下一学习日六小时安排

1. **1.5 小时 Java 数据结构：** 画出 3 条链表并独立完成遍历、查找和末尾添加；反转只完成一遍逐轮变量表，不要求背模板。
2. **1.5 小时算法：** 完成 2 道链表基础题或一道链表加一道数组题，每题运行至少 3 组输入并说明时间、空间复杂度。
3. **1.5 小时 MySQL：** 用两个 Navicat 会话实际观察事务提交前后读取结果，并写出脏读、不可重复读、快照读各自含义。
4. **1 小时项目复盘：** 不看笔记讲清 IDEA 与 Docker 两种启动方式、环境变量占位符、端口映射和健康检查。
5. **0.5 小时面试表达：** 分别用 30 秒回答防超卖、异常回滚、联合索引和本地容器化部署四个问题。
## 2026-08-13：字符串、DTO 校验、Optional、Map 综合与项目表达

### Java 字符串处理

- 理解 `String` 不可变：`replace()`、`toUpperCase()` 等方法返回新字符串，不会原地修改原对象，必须接住返回值。
- 独立使用 `StringBuilder` 完成多个名称的逗号拼接，理解 `append()` 用于追加内容，`toString()` 用于得到最终 `String`。
- 能区分 `isEmpty()` 和 `isBlank()`：空格字符串不是 empty，但属于 blank。
- 理解 `strip()` 删除字符串两端空白并返回新字符串，不删除中间空格。
- 理解 `split()` 返回 `String[]`；能说明 `split("\\s+")` 用于按一个或多个空白字符拆分文本。
- 独立完成数字字符统计和删除数字字符两个方法，正确使用 `toCharArray()`、`Character.isDigit()` 和 `StringBuilder`。

### DTO 校验与请求链

- 结合项目真实代码确认 `CreateProductRequest`、`UpdateProductRequest` 使用 `@NotBlank`、`@Size`、`@NotNull`、`@Positive` 和 `@Min` 定义规则。
- 能区分 `@RequestBody` 负责把 JSON 转换为 DTO，`@Valid` 负责触发 DTO 字段校验。
- 理解校验失败发生在 Controller 方法体执行前，因此 Service、Mapper 和数据库操作都不应发生。
- 理解 Mockito 的 `verifyNoInteractions(productService)` 用于证明参数校验失败后 Service 没有任何方法被调用。
- 项目请求链口述基本正确，已修正把 DTO 误说成 VO：Request DTO 接收并校验输入，Entity 对应数据库，VO 返回前端，`ApiResponse` 统一包装响应。

### Optional、record 与对象引用

- 理解 `Optional<T>` 表达“可能有值，也可能没有值”；能区分 `of()`、`ofNullable()` 和 `empty()`。
- 能说明 `orElseGet()` 只在没有值时调用默认值生成逻辑，`orElse()` 的参数会先被计算。
- 当前项目继续使用清晰的 `null + BusinessException`，`Optional` 只要求看懂，不强行改写。
- 理解 Java `record` 会自动生成构造方法、访问方法、`equals()`、`hashCode()` 和 `toString()`，适合固定的数据载体；项目 Entity 继续使用普通类。
- 理解引用赋值不会复制对象，`first == second` 为真表示两个变量指向同一个对象；能区分引用赋值、浅拷贝和深拷贝的基本含义。

### Map 综合练习

- 完成按单词统计次数：字符串经 `strip()` 和 `split("\\s+")` 得到单词数组，再用 `Map<String, Integer>` 和 `getOrDefault()` 累加。
- 完成最高频单词查找，理解遍历 `entrySet()` 时 `Map.Entry` 表示当前键值对，`getKey()` 取得单词，`getValue()` 取得次数。
- 修正寻找最大项时只更新单词、没有更新最高次数的问题；理解最大值和对应对象必须一起更新。
- 独立完成低库存商品名称筛选，正确使用 `Map.Entry<String, Integer>`、`ArrayList` 和阈值判断。
- 理解多结果查询应优先返回空集合而不是 `null`，方便调用者直接遍历、调用 `size()` 或 `isEmpty()`。

### 项目表达

- 完成一次四句话以内的项目介绍，顺序为“项目是什么 → 核心功能 → 解决的核心问题 → 工程化能力”。
- 修正语音表达中的术语：原子 `UPDATE`、SQL 影响行数、事务、缓存穿透、Flyway、本地容器化运行。
- 明确当前 Docker Compose 属于本地多容器运行，不能描述为集群或生产部署。

### 本轮真实掌握判断

- **已经实操通过：** `StringBuilder` 拼接、数字字符统计与过滤、单词频次统计、最高频单词、低库存 Map 筛选。
- **已经能够解释：** String 不可变、`isBlank()` 与 `isEmpty()`、DTO/Entity/VO、`@RequestBody` 与 `@Valid`、`Map.Entry`、空集合返回习惯。
- **接触但不要求独立使用：** `Optional`、`record`、通用深拷贝。
- **仍需加强：** 首次组织稍复杂集合题时，容易混淆字符与单词、遗漏首次写入；项目口述仍会因语速出现术语错误。
- **项目展示是否达标：** 达标。项目技术链、核心问题和工程化验证均可用于简历与面试展示。
- **是否可以开始投递：** 已达到基础 Java 后端实习投递门槛；继续遵守学习者当前暂不投递的选择。
- **是否达到稳定技术面水平：** 尚未完全达到。基础概念理解明显提升，但无提示集合编码、数据结构和稳定口述仍需训练。

### 下一学习日六小时安排

1. **1.5 小时 Java：** 从空白完成 2 个集合方法，其中一个使用 `Map.Entry`、一个使用对象列表；每题至少运行 3 组输入。
2. **1.5 小时数据结构：** 复写链表节点创建、遍历、查找和末尾添加；画出每次 `current` 移动，不要求默写反转。
3. **1.5 小时 MySQL：** 使用两个会话实际执行一次事务可见性实验，记录提交前后查询结果，并解释快照读与当前读。
4. **1 小时项目复盘：** 独立讲解新增商品请求链、订单创建、防超卖和缓存穿透，每条控制在 40 秒。
5. **0.5 小时工程验证：** 检查 Git 状态，运行普通测试；只有环境依赖需要时再启动 Docker MySQL/Redis。
## 2026-08-14：HTTP 语义、幂等性、日志定位与全局异常处理

### HTTP 状态码与接口语义

- 能准确区分 `400` 参数错误、`401` 未认证、`403` 权限不足、`404` 资源不存在和 `409` 资源状态冲突。
- 能判断“再次取消已取消订单”更适合返回 `409 Conflict`，因为订单存在、参数合法，但请求与订单当前状态冲突。
- 理解 HTTP 状态码的选择不仅取决于“失败了”，还取决于失败发生在哪一层以及资源当前状态。

### 幂等性

- 理解幂等性关注的是同一请求执行多次后，系统最终状态是否与执行一次一致。
- 能说明取消订单具备幂等性：第一次取消后状态变为已取消，后续请求不会再次退回库存。
- 能判断库存增加和普通订单创建默认不幂等，因为重复请求会重复增加库存、扣减库存或创建订单。
- 初步了解幂等键可以用于避免重复下单，但目前只要求理解设计目的，不要求立即加入主项目。

### 日志与错误阶段定位

- 能沿“`Caused by` → 第一条项目包名 → 文件与行号”定位异常，不再从长日志第一行逐字阅读。
- 能判断 Mapper 位置的 `BadSqlGrammarException` 应继续检查日志中的 SQL、参数、表结构和实际数据库。
- 能区分编译错误、测试断言失败、测试执行异常和应用运行时错误。
- 能说明 `Failures` 表示实际结果与断言不一致，`Errors` 表示测试执行过程中发生异常。
- 能根据 `Unknown column`、Redis `Connection refused` 和端口占用日志分别定位到表结构、Redis 环境配置和运行进程。

### 全局异常处理

- 结合实际 `GlobalExceptionHandler` 理解：`BusinessException` 处理预期业务错误，`MethodArgumentNotValidException` 处理 DTO 校验错误。
- 能判断 `NullPointerException` 不会进入现有两个专用处理器，需要由更宽泛的 `Exception` 兜底处理器处理。
- 理解 Spring 会优先匹配更具体的异常处理器，业务异常不会因为存在 `Exception.class` 兜底而全部变成 `500`。
- 理解未知异常的真实堆栈应记录在服务端日志中，前端只返回通用 `500` 信息，避免泄露 SQL、表结构、路径和账号等内部细节。

### 本轮真实掌握判断

- **已经能够独立判断：** 常见 HTTP 状态码、取消订单与创建订单的幂等性、编译失败与测试 Failure/Error 的区别、Redis和端口类环境错误。
- **已经能够解释：** 如何从堆栈定位项目行号、专用异常处理器与兜底处理器的关系、为什么内部异常不能直接返回前端。
- **仍需加强：** 面对一段未经整理的完整真实日志时，还需要继续练习从多层 `Caused by` 中选出最底层根因；幂等键尚未实际实现。
- **项目展示是否达标：** 达标，现有项目主线和工程化证据足够展示。
- **是否可以开始投递：** 已达到基础 Java 后端实习投递门槛；继续遵守当前暂不投递的选择。
- **是否达到稳定技术面水平：** 接近但尚未完全稳定，主要短板仍是无提示编码、复杂测试设计和完整故障排查表达。

### 下一学习日六小时安排

1. **1.5 小时 Java/算法：** 从空白完成 2 道中等基础题，至少一道使用链表或滑动窗口，每题运行 3 组输入并说明复杂度。
2. **1.5 小时真实排错：** 分析 3 段完整日志，分别定位编译、数据库和环境问题，并写出证据链与第一步处理动作。
3. **1 小时 Spring Boot：** 复盘全局异常处理、DTO 校验和 HTTP 状态码，设计 3 个有业务冲突的接口响应场景。
4. **1 小时 MySQL：** 复习事务、行锁和联合索引，使用 `EXPLAIN` 对比一条使用索引和一条未有效使用索引的 SQL。
5. **0.5 小时项目表达：** 用 60 秒讲清订单创建、防超卖、事务和幂等取消。
6. **0.5 小时工程验证：** 检查 Git 状态并运行普通测试；只有集成测试需要时才启动 Docker MySQL 和 Redis。

## 2026-08-15：滑动窗口、链表、工程排错、Swagger、索引与事务传播

### Java 与数据结构

- 从空白完成正整数数组“和大于等于目标值的最短连续子数组”，正确使用可变长度滑动窗口、哨兵初始值和 `Math.min()`。
- 能解释外层 `for` 扩大右边界、内层 `while` 连续缩小左边界，两个指针各自最多移动 `n` 次，因此整体为 `O(n)`。
- 通过负数反例理解滑动窗口依赖单调性，当前模板不能直接处理含负数数组。
- 完成链表中间节点方法，修正 `fast.next.next` 带来的空指针和偶数链表错误。
- 能根据 `fast` 的初始位置推导偶数链表返回前一个还是后一个中点。

### 三段真实日志排错

- 能从多层异常中定位 Flyway V1 checksum 不匹配，并选择恢复历史文件而不是修改历史表或删除数据。
- 能结合 `Unknown column`、Flyway 当前版本和 JAR 内容判断 Docker 运行的是缺少新迁移的旧构建产物。
- 能区分 MySQL 健康状态与应用认证成功，理解 `.env` 修改不会覆盖已有数据卷中的真实密码。
- 已形成“最底层根因 → 项目或环境证据 → 第一处理动作”的排错表达方式。

### Swagger/OpenAPI 与 JWT

- 核对并理解项目已有 springdoc 依赖和 Swagger 路径放行；新增并提交 `OpenApiConfig`，Swagger 页面成功出现 `Authorize`。
- 使用 Swagger 完成登录、JWT 授权、普通受保护接口和管理员接口验证，确认认证成功与角色授权失败分别对应 `200` 和 `403`。
- 结合实际代码确认 JWT 保存 `username`、`userId` 和 `role`，过滤器读取 token 角色但不重新查询数据库。
- 能判断数据库角色或用户状态变化不会自动修改旧 JWT；了解令牌撤销可以通过短有效期、数据库检查或 Redis 状态/黑名单设计解决，但暂不要求实现。

### MySQL 索引设计

- 使用 `EXPLAIN UPDATE` 验证取消订单 SQL 在多个候选索引中选择 `PRIMARY(id)`，预计只定位一行，其他条件继续保证归属和状态正确性。
- 能判断无需增加 `(id, user_id, status)` 重复联合索引，因为唯一主键已经足够定位。
- 分析“某用户某状态的最新订单”查询，理解潜在联合索引 `(user_id, status, create_time)` 的字段顺序。
- 当前数据量小时优化器选择 `(status, create_time)` 并反向扫描；能说明只有在真实数据量和性能证据出现后才新增索引，避免过早优化。

### 项目表达与事务传播

- 重新组织订单创建与取消的面试回答，能够串联条件原子 UPDATE、影响行数、事务回滚和防重复退库存。
- 理解默认 `Propagation.REQUIRED`：`cancelOrder()` 开启事务后，`addStock()` 加入同一事务，而不是独立提交。
- 理解 `REQUIRES_NEW` 会独立提交，若用于当前退库存流程可能导致外层订单回滚但库存已增加的数据不一致。
- 确认没有 `@Transactional` 的 `recordOperation()` 在同一线程和现有事务中调用时，其 Mapper INSERT 仍参与外层事务。
- 能判断 Spring 本地事务通常绑定当前线程，自建新线程不会自动继承原事务。
- 初步理解内层事务异常可能把共享事务标记为 `rollback-only`，外层 catch 不会自动恢复事务可提交状态。

### 工程验证

- 新增 Swagger JWT 配置后运行普通测试，结果全部成功。
- Swagger 配置以独立提交 `a82acfc 配置Swagger JWT认证` 保存；功能代码与学习文档保持分开。

### 本轮真实掌握判断

- **已经实操通过：** 可变滑动窗口、链表中点、三段日志分析、Swagger JWT 授权、两个真实 EXPLAIN、Maven 普通测试和功能代码提交。
- **已经能够解释：** JWT 角色时效、索引选择与写入成本、REQUIRED/REQUIRES_NEW、线程与事务边界、rollback-only 基本现象。
- **仍需加强：** 含负数子数组算法只理解当前模板为何失效；事务传播与 rollback-only 属于首次接触，尚不要求独立配置；完整项目口述仍有状态数字和术语口误。
- **项目展示是否达标：** 达标。Swagger JWT 调试、Flyway、Redis、并发防超卖、事务与 Docker 均有实际代码或验证证据。
- **是否可以开始投递：** 已达到基础 Java 后端实习投递门槛；继续遵守当前暂不投递的选择，但开始投递时无需等待学习更多框架。
- **是否达到稳定技术面水平：** 尚未完全稳定，但已能处理常见项目追问；主要继续加强无提示编码、完整日志证据链和简洁准确表达。

### 下一学习日六小时安排

1. **1.5 小时 Java/算法：** 从空白完成 2 道新题，其中一道链表、一道数组或 HashMap；每题运行 3 组输入并说明复杂度。
2. **1 小时 Spring：** 用调用链图区分同类自调用、跨 Bean 调用、REQUIRED 和新线程四种事务情况，完成 3 个故障推导。
3. **1 小时 MySQL：** 编写并 EXPLAIN 3 条项目查询，分别观察主键、联合索引和全表扫描，并说明是否值得新增索引。
4. **1 小时项目排错：** 分析一段真实完整 Maven/Spring 日志，写出根因、证据、修复和验证命令。
5. **1 小时项目表达：** 各用 45 秒讲解 JWT 认证授权、订单防超卖、Flyway 和本地 Docker Compose。
6. **0.5 小时工程整理：** 更新学习文档、检查 Git diff，按文档目的单独提交并确认工作区状态。

## 2026-08-16：事务边界、链表环、HashMap 最近下标、EXPLAIN 与项目表达

### Spring 事务

- 能区分同类自调用和新线程两种不同问题：同类自调用绕过 Spring 代理，方法上的事务注解可能不生效；新线程仍可经过代理创建新事务，但不能加入原线程事务。
- 能解释跨 Bean 默认 `REQUIRED` 加入外层事务，订单失败时库存一起回滚。
- 修正了“`REQUIRES_NEW` 仍与外层处于同一事务”的误解：它会暂停外层事务并独立提交，因此不适合当前订单扣库存流程。
- 当前掌握等级为“能解释”；尚未要求独立设计复杂传播配置。

### Java 算法

- 在 IDEA 可运行骨架中完成快慢指针判断链表环，通过普通无环、普通有环和单节点自环三组测试。
- 理解到达 `null` 用于证明无环，快慢指针相遇用于证明有环；否则有环输入会无限循环。
- 完成“存在距离不超过 k 的重复元素”：使用 `HashMap<数字, 最近下标>`，通过三组测试。
- 能解释为什么必须更新最近下标；时间复杂度 `O(n)`，空间复杂度 `O(n)`。
- 链表环复杂度为时间 `O(n)`、额外空间 `O(1)`；曾混淆遍历节点数量和额外存储数量，修正后理解判断方法。
- 加练二叉树基础：从 IDEA 骨架完成节点统计、最大深度和前序遍历，三组测试均运行成功。
- 能跟随具体节点解释递归返回值由子调用逐层送回父调用，并理解不同调用的 `root` 不同、前序遍历共享同一个结果列表。
- 初步理解递归辅助空间为 `O(h)`；前序遍历包含返回列表时总空间为 `O(n)`。当前属于“能在提示下写和解释”，尚未达到无提示独立编写树题。
- 下一学习日提前完成 Java 数据结构部分：独立写出递归查找 `containsValue()`，并在 API 提示后写出基于 `Queue<TreeNode>` 的层序遍历。
- 层序遍历通过普通树、单节点子树和空树三类测试；能够区分结果列表 `add()` 与待处理队列 `offer()`，但 FIFO 顺序首次判断错误，修正后能够用排队模型解释。

### MySQL 与日志

- 对用户订单查询、状态时间查询和原子取消订单执行 3 条 `EXPLAIN`。
- 能识别小表可能选择主键倒序扫描或全表扫描，即使 `possible_keys` 中存在索引；不会仅凭 `ALL` 就盲目增加索引。
- 能说明取消订单通过唯一主键先定位一行，`user_id` 和 `status` 继续作为安全条件。
- 从 Flyway 启动失败日志中定位 `Duplicate column name 'description'`，理解数据库真实结构与 `flyway_schema_history` 不一致会导致重复迁移。

### 项目表达与工程状态

- 能覆盖项目、订单创建、防超卖、事务一致性和工程工具五段内容，但仍出现 `MyBatis-Java`、`stock > quantity`、删除状态矛盾和 Docker 打包数据库等口误。
- 已修正为真实表达：Java 17 + MyBatis-Plus；原子 SQL 条件为 `stock >= quantity`、已上架、未删除；Docker Compose 编排应用、MySQL 和 Redis。
- 工作区检查结果：业务代码无新增修改；学习文档存在待提交修改，最近功能提交为 `a82acfc 配置Swagger JWT认证`。

### 本轮真实掌握判断

- **项目展示是否达标：** 达标，功能与工程化证据完整。
- **是否可以开始投递：** 已达到基础 Java 后端实习投递门槛；继续遵守当前暂不投递的选择。
- **是否达到稳定技术面水平：** 尚未完全达到。事务基本推导已进步，但复杂度、无提示编码和项目术语仍需稳定。
- **算法训练节奏调整：** 不把刷题作为全天主线；接下来2～3周每天约1～1.5小时，达到常见题可独立完成后降到每天30～45分钟或每周3～4次，不以“刷完算法”作为投递前置条件。

### 下一学习日六小时安排

1. **1.5 小时 Java 数据结构：** 使用 IDEA 完整骨架学习二叉树节点、前序遍历和最大深度；每个方法运行空树、单节点和普通树三类输入。
2. **1 小时 Spring：** 把 4 种事务调用场景整理成调用链表格，并从项目测试中找出一个能证明整体回滚的断言。
3. **1 小时 MySQL：** 使用 `EXPLAIN ANALYZE` 对比预计行数与实际行数，分析现有两个订单查询，不新增索引。
4. **1 小时真实排错：** 独立分析一段包含多层 `Caused by` 的测试或启动日志，写出根因、证据、第一处理动作和验证方式。
5. **1 小时项目表达：** 不背全文，分别用 30 秒回答订单创建、防超卖和 Redis 缓存穿透，并接受两层追问。
6. **0.5 小时工程整理：** 运行普通测试、检查 Git diff，并将学习文档单独提交。

## 2026-08-17：事务回滚证据、执行计划、环境排错与树题独立复写

### 事务集成测试

- 读取真实 `OrderTransactionRollbackIT`，能够指出订单保存失败后验证整体回滚的两个关键结果：商品库存恢复为 `5`，库存流水数量为 `0`。
- 能推导移除 `OrderService.createOrder()` 外层事务后的后果：扣库存方法自己的事务可能先提交，最终库存为 `3`、库存流水为 `1`、订单为 `0`，形成数据不一致。
- 进一步区分“库存变化量”和“库存流水记录数量”，修正把 `operationCount` 理解为 `-2` 的错误。

### EXPLAIN ANALYZE 与环境排错

- 对用户订单查询和按状态查询读取 `EXPLAIN ANALYZE`，能够区分预计行数与实际行数，并理解 `actual time` 才是实际耗时、`cost` 不是毫秒。
- 理解小数据量且 `user_id` 区分度低时，优化器可能选择主键倒序扫描，而不是为了使用已有索引强行走二级索引。
- 从 `UnknownHostException: redis` 定位 IDEA 与 Docker Compose 网络边界：容器内使用服务名 `redis:6379`，Windows 上 IDEA 访问端口映射 `127.0.0.1:6380`。
- 能说明容器中的 `127.0.0.1` 指向当前容器自身，不指向 Redis 容器。

### Java 独立编码验证

- 在只提供类、节点结构、方法签名和三组测试数据的情况下，独立完成二叉树最大深度：空节点返回 `0`，递归取得左右子树深度，返回 `1 + Math.max(leftDepth, rightDepth)`。
- 普通树、单节点和空树三类输入运行成功，预期结果分别为 `3`、`1`、`0`。
- `maxDepth()` 掌握等级由“能在提示下写”提升为“能独立写”；时间复杂度 `O(n)`、递归辅助空间 `O(h)` 的判断仍需后续隔天复述。

### 工程验证与范围判断

- 运行 `ProductServiceTest`、`OrderServiceTest`、`ProductControllerTest` 和 `AuthServiceTest`，共 26 个测试全部通过。
- 本日没有修改项目业务代码或新增测试，因此这次执行属于已有功能回归检查，不是验证当天新增测试；在仅修改独立算法练习和文档时，该检查不是每次都必需。
- 语音转文字中能够从上下文确认的 `Mapper`、`OrderVO` 等术语偏差按正确含义理解，不再作为技术掌握错误。

### 本轮真实掌握判断

- **项目展示是否达标：** 达标。事务、防超卖、Redis、Flyway、Docker、测试和 SQL 分析都有真实代码或验证证据。
- **是否可以开始投递：** 已达到基础 Java 后端实习投递门槛；继续尊重当前暂不投递的安排，但技术上不需要等待 Nacos、MQ 等内容全部学完。
- **是否达到稳定技术面水平：** 尚未完全稳定。事务和环境排错已能解释，下一重点是独立设计测试、隔天独立编码和更简洁的项目表达。

### 下一学习日六小时安排

1. **1.5 小时测试设计：** 读取一个真实 Service 方法，独立画出 3 条执行分支，并从空白完成其中 1 条单元测试；验收为测试通过且能解释每个 Mock 和断言的来源。
2. **1 小时 Java/算法：** 从 IDEA 基础骨架独立复写 1 道 HashMap 题和 1 道链表题，各运行正常、边界、无结果三组输入。
3. **1 小时 Spring：** 根据真实代码画出创建订单的事务调用链，推导扣库存、流水、订单插入三个位置分别失败时的最终数据库状态。
4. **1 小时环境排错：** 分别写出 IDEA 和 Docker 运行时 MySQL、Redis 的主机与端口，并用 2 条实际命令验证连接目标。
5. **1 小时 MySQL：** 对两条项目 SQL 执行 `EXPLAIN ANALYZE`，说明访问方式、预计/实际行数和是否值得加索引。
6. **0.5 小时工程整理：** 只在代码发生变化时运行对应测试；检查 Git diff，并将学习文档与功能代码分开处理。

### 2026-08-17 追加训练结果

- 从真实 `getMyOrders()` 执行路线设计分页正常流程测试：准备 `SysUser`、`SysOrder` 和 `Page<SysOrder>`，配置用户查询与分页查询的 Mock，断言 `total/current/size/records` 和 `statusText`。
- 理解 `selectPage()` 返回的是分页容器 `IPage<SysOrder>`，订单列表位于分页对象的 `records` 中；最终 Service 返回的是 `PageResult<OrderVO>`。
- 单独运行 `OrderServiceTest#shouldGetMyOrdersSuccessfully` 成功；该测试属于“能在提示下完成”，尚未达到独立准备 MyBatis-Plus 分页对象。
- 独立复写 HashMap 最近重复元素，三组结果 `true/true/false`；能够解释 `put()` 必须在条件分支外执行，时间与空间均为 `O(n)`。
- 在箭头变化和临时保存后继节点的提示下完成链表反转，三组输入运行成功；时间 `O(n)`、额外空间 `O(1)`。
- 推导订单事务三个失败位置的结果均为库存 `5`、流水 `0`、订单 `0`；能够进一步判断流水若使用 `REQUIRES_NEW`，订单失败后会形成 `5/1/0` 的不一致状态。
- 实际启动 MySQL、Redis 容器，并通过 `Test-NetConnection` 验证 Windows 映射端口 `3307`、`6380` 可访问。
- 使用 `EXPLAIN ANALYZE` 对比两条订单查询：`(status, create_time)` 索引可完成条件定位和倒序读取；`total_price` 查询当前执行全表扫描、过滤和额外排序。
- 修正“主键一定优先”的索引判断：索引要根据 SQL 的过滤、范围和排序字段设计；`status = ? AND total_price > ? ORDER BY total_price` 更匹配 `(status, total_price)`。

### 新的下一学习日六小时安排

1. **1 小时测试独立设计：** 不提供测试代码，根据一个真实 Service 分支自行列出输入、依赖返回、预期结果和禁止调用，再独立完成 1 条测试。
2. **1 小时算法隔天验收：** 无提示复写链表反转，并从 IDEA 骨架独立完成 1 道树或 HashMap 题；每题验证三类输入并说明复杂度。
3. **1.5 小时 Spring Security：** 读取真实过滤器和安全配置，画出有效 JWT、缺少 JWT、角色不足三条调用链，并分别解释 `200/401/403` 的产生位置。
4. **1 小时 MySQL 事务实操：** 使用两个数据库会话观察同一商品行的更新等待，记录事务开始、更新、提交前后两个会话看到的结果。
5. **1 小时项目排错与表达：** 分析一段真实启动或测试日志，并用 60 秒讲清项目亮点、一个一致性问题和验证证据。
6. **0.5 小时工程整理：** 运行当天改动直接相关的测试，检查 Git diff；测试代码与学习文档分开提交。

## 2026-08-18：分页测试、算法复写、Security、行锁与真实日志

### 测试独立设计

- 根据 `getMyOrders()` 真实执行路线设计“用户存在但没有订单”的正常空分页测试。
- 能确定合法输入、用户 Mock、分页 Mock、最终分页断言，以及 `productMapper`、`productService` 不应被调用。
- 独立完成测试主体并运行通过；首次把一条空 `SysOrder` 放入 `records`，造成 `total=0` 但当前页有一条记录的数据矛盾，修正为 `List.of()`。
- 当前测试掌握等级为“基本能独立完成简单 Service 测试”，MyBatis-Plus 分页等框架对象仍允许查 API。

### Java 算法隔天验收

- 无提示复写链表反转，正确使用 `previous/current/nextNode`，三类输入运行成功；掌握等级提升为“能独立写”，时间 `O(n)`、额外空间 `O(1)`。
- 完成二叉树层序遍历：空树返回空列表，根节点先入队，循环 `poll()` 当前节点并依次 `offer()` 左右子节点。
- 首次错误使用 `root.left/root.right`，会反复把根节点孩子加入队列；修正为随循环变化的 `current.left/current.right` 后三组输入通过。
- 能解释 `poll()` 只移除队列中的等待记录，不会删除树节点；取出的节点仍由 `current` 引用。

### Spring Security 调用链

- 根据真实 `SecurityConfig`、`JwtAuthenticationFilter` 和 `JwtTokenProvider` 区分三条路径：有效 JWT 访问普通接口通常 `200`，缺少或无效 JWT 访问受保护接口返回 `401`，有效 USER JWT 访问管理员接口返回 `403`。
- 理解 JWT 无效时过滤器没有直接返回 `401`，而是未建立认证并继续过滤链；是否返回 `401` 取决于目标路径是否要求认证。
- 能推导过期 JWT 访问 `permitAll()` 的健康检查仍可进入接口。
- 能判断用户数据库角色从 ADMIN 改为 USER 后，未过期旧 JWT 仍携带 ADMIN 权限，因为当前过滤器不重新查询数据库。
- 对当前小项目可先通过缩短 JWT 有效期降低旧权限风险，但能判断该方案不能满足“立即禁用”。
- 对必须立即失效的场景，能够设计 Redis 用户状态或 Token 版本：JWT 版本与 Redis 当前版本不一致时拒绝建立认证。
- 对库存修改、订单取消和管理员接口选择 `fail-closed`；Redis 状态服务不可用时禁止敏感操作，并理解依赖故障返回 `503` 通常比把 Token 误判为无效的 `401` 更准确。
- 上述 Redis Token 版本属于扩展设计，当前项目尚未实现，不能写成项目已完成功能。

### MySQL 双会话行锁实操

- 通过 `CONNECTION_ID()` 确认 Navicat 两个查询窗口是独立会话 `8` 和 `9`。
- 会话8把商品4库存从49临时更新为48但不提交；会话9普通查询仍看到已提交版本49。
- 会话9更新同一商品行时实际进入等待；会话8回滚释放行锁后，会话9继续执行成功。
- 会话9回滚后库存从自己看到的48恢复为49，实验未改变最终业务数据。
- 能结合实际现象解释：普通一致性读可通过 MVCC 读取已提交版本，而并发 UPDATE 同一行需要等待行锁。

### 集成测试日志与项目表达

- 读取真实 `ProductConcurrencyIT`，确认库存为1时两个线程同时扣1，断言成功更新总数为1、最终库存为0；该测试证明 Mapper 原子 SQL 防止并发超卖。
- 运行 `run-integration-tests.ps1`，并发与事务回滚集成测试共2个全部通过，结果 `Failures: 0`、`Errors: 0`、`BUILD SUCCESS`。
- 从事务日志识别库存先由5更新为3、流水插入成功，但订单保存被模拟失败后事务外查询库存恢复为5。
- 能解释 `Updates: 1` 只代表 SQL 在当前事务中修改了一行，事务未结束前不等于最终提交成功。
- 项目防超卖表达已能覆盖行锁、原子条件 UPDATE、影响行数和事务；补充验证证据后可用于面试回答。
- 加练历史真实 `ApplicationContext` 启动失败日志，能够从多层 Bean 创建异常继续追到最底层 `Could not resolve placeholder 'JWT_SECRET'`。
- 理解“重新启动”不是修复；需要先为对应运行环境提供 JWT 配置，再重新运行原失败测试，并以 `Errors: 0`、`BUILD SUCCESS` 验证。

### 工程整理

- 两条订单分页测试已通过提交前的 `git diff --cached --stat` 和 `git diff --cached --check`。
- 测试代码以独立提交 `fe74784 补充订单分页查询测试` 保存，未与学习文档混在同一提交。

### 本轮真实掌握判断

- **项目展示是否达标：** 达标，并发测试、事务回滚日志和数据库行锁实验提供了可展示的真实证据。
- **是否可以开始投递：** 可以。当前能力已满足基础 Java 后端实习投递门槛，不需要等待学完更多中间件。
- **是否达到稳定技术面水平：** 接近但尚未完全稳定；主要继续加强独立测试数据设计、树遍历对象变化和从安全代码独立推导调用链。

### 下一学习日六小时安排

1. **1 小时独立排错：** 给出一段真实项目异常日志，不提供选项，独立写出最底层根因、证据、第一处理动作和验证命令。
2. **1 小时 Java：** 无提示复写层序遍历，并完成一道使用栈或队列的中等基础题，验证三类输入和复杂度。
3. **1.5 小时 Spring Security：** 从空白画 JWT 过滤链，并设计“用户被禁用但旧 JWT 未过期”的解决方案，不要求立即实现。
4. **1 小时 MySQL：** 比较普通 `SELECT`、`SELECT ... FOR UPDATE` 和 `UPDATE` 在两个会话中的锁行为，完成一张结果表。
5. **1 小时项目表达：** 各用45秒讲清并发防超卖、事务回滚和 JWT 权限时效，并回答一层追问。
6. **0.5 小时工程整理：** 只运行改动直接相关的测试；检查并分开提交测试代码与学习文档。
