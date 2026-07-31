# inventory-order-system 学习进度

更新时间：2026-07-30（完成 Java 空值边界、Spring IoC/DI、事务与统一异常处理复盘）

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
