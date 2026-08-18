# inventory-practice 错误复盘

更新时间：2026-07-23

用途：记录实际编码和接口测试中出现过的错误。下次出现编译错误、接口结果异常或数据不符合预期时，先按本文件检查。

## 使用方式

1. 先看错误属于 Java 语法、类型、业务条件、Spring 接口还是 Postman 操作。
2. 对照“错误写法”和“正确写法”。
3. 修正后保存文件并重启正确的 Spring Boot 启动类。
4. 只重新验证发生错误的功能，不重复测试无关接口。

## 当前最需要优先检查的 8 项

1. `if (...)` 后是否误写了分号。
2. 方法返回的是单个对象还是 `List`。
3. Mapper 返回的是 Entity，接口是否需要转换成 VO。
4. 查询条件应该使用 `eq`、`lt`、`le`、`gt` 还是 `ge`。
5. `ApiResponse<T>` 中的 `T` 是否与真实 `data` 一致。
6. 泛型的 `>` 是否完整闭合。
7. Postman 的 GET、POST、PUT、PATCH、DELETE 是否选对。
8. 修改代码后是否保存、停止旧进程并启动了正确项目。

## 一、Java 语法错误

### 1. `if` 后误加分号

错误写法：

```java
if (!existingIds.contains(id));
{
    throw new BusinessException(404, "商品不存在");
}
```

原因：分号已经结束 `if`，后面的代码块会无条件执行，所以存在的商品也会被判断为不存在。

正确写法：

```java
if (!existingIds.contains(id)) {
    throw new BusinessException(
            404, "ID为 " + id + " 的商品不存在");
}
```

检查方法：看到 `if` 时，确认右括号后是 `{`，不是 `;`。

### 2. 抛出异常后缺少分号

错误写法：

```java
throw new BusinessException(404, "部分商品不存在")
```

正确写法：

```java
throw new BusinessException(404, "部分商品不存在");
```

规律：普通 Java 语句通常以分号结尾，但 `if (...)` 本身后面不能随意加分号。

### 3. 方法名首字母大写

错误写法：

```java
public Long ProductCount()
```

正确写法：

```java
public Long countProducts()
```

原因：Java 方法名使用小驼峰，首字母小写；类名才通常首字母大写。

### 4. 泛型括号没有闭合

错误写法：

```java
ApiResponse<List<ProductVO>
```

正确写法：

```java
ApiResponse<List<ProductVO>>
```

检查方法：数清楚左尖括号 `<` 和右尖括号 `>` 的数量。

### 5. Markdown 星号被误认为源码

聊天中可能显示：

```text
*success*
**null**
```

Java 和 JSON 中实际应该是：

```java
ApiResponse.success(...)
```

```json
"data": null
```

原则：星号可能只是聊天排版；判断代码是否有星号要看 IDEA 中的真实源码。

## 二、返回类型和泛型错误

### 6. 多条商品却返回单个 `ProductVO`

错误写法：

```java
public ProductVO getOutOfStockProducts()
```

原因：查询可能返回 0 条、1 条或多条商品，必须使用列表。

正确写法：

```java
public List<ProductVO> getOutOfStockProducts()
```

记忆：

```text
单个对象 → ProductVO
多个对象 → List<ProductVO>
```

### 7. 用 `List<ProductVO>` 接收 Mapper 查询结果

错误写法：

```java
List<ProductVO> products = productMapper.selectList(queryWrapper);
```

原因：`ProductMapper` 操作的是 `Product` Entity，因此 `selectList` 返回 `List<Product>`。

正确写法：

```java
List<Product> products = productMapper.selectList(queryWrapper);

return products.stream()
        .map(ProductVO::fromEntity)
        .toList();
```

数据方向：

```text
数据库 → Product Entity → ProductVO → ApiResponse
```

### 8. 无响应数据却使用列表泛型

错误写法：

```java
public ApiResponse<List<ProductVO>> deleteProducts(...)
```

但实际返回：

```java
return ApiResponse.success(null);
```

正确写法：

```java
public ApiResponse<Void> deleteProducts(...)
```

### 9. 把 `Null` 当成泛型类型

错误写法：

```java
ApiResponse<Null>
```

正确写法：

```java
ApiResponse<Void>
```

原因：Java 没有用于此场景的 `Null` 类型；没有返回数据时使用 `Void`。

## 三、MyBatis-Plus 和业务条件错误

### 10. 把 `eq()` 放入 `if` 条件

错误写法：

```java
if (queryWrapper.eq(Product::getStatus, 1)) {
    return productMapper.selectCount(queryWrapper);
}
```

原因：`eq()` 的作用是添加查询条件，返回的仍然是 Wrapper，不是 `boolean`。

正确写法：

```java
queryWrapper.eq(Product::getStatus, 1);
return productMapper.selectCount(queryWrapper);
```

### 11. 查询库存为 0 时误用 `lt`

错误写法：

```java
queryWrapper.lt(Product::getStock, 0);
```

含义：查询库存小于 0，也就是负库存。

正确写法：

```java
queryWrapper.eq(Product::getStock, 0);
```

常见操作符：

```text
eq = 等于
lt = 小于
le = 小于等于
gt = 大于
ge = 大于等于
```

### 12. 批量删除逐个查询，产生多条 SQL

旧写法：在 `for` 循环中反复调用 `selectById`。

问题：删除 100 个 ID 会产生 100 条查询 SQL。

改进：

```java
List<Product> products = productMapper.selectBatchIds(ids);
```

再把查询到的 ID 放入 `Set<Long>`，通过 `contains()` 找出具体缺失 ID。

### 13. 重复 ID 被误判为商品不存在

场景：请求 `[4, 4]`，List 大小为 2，但数据库只能查到一条商品。

正确处理：

```java
Set<Long> uniqueIds = new HashSet<>(ids);
if (uniqueIds.size() != ids.size()) {
    throw new BusinessException(400, "删除的ID列表不能有重复");
}
```

### 14. 校验顺序不合理

不推荐：

```text
空列表 → 重复 ID → ID 合法性
```

推荐：

```text
空列表 → ID 合法性 → 重复 ID → 数据库存在性
```

原因：对于 `[null, null]`，应优先提示 ID 不合法，而不是提示重复。

### 15. null 判断顺序错误可能导致空指针

正确写法：

```java
if (id == null || id < 1) {
    throw new BusinessException(400, "商品ID必须大于等于1");
}
```

原因：`||` 会短路。`id == null` 为 true 后，不再执行 `id < 1`。

## 四、Postman 和运行环境错误

### 16. 请求方式没有切换

实际场景：创建缺货商品时使用 POST，随后查询 `/out-of-stock` 时忘记切换为 GET，误以为查询没有结果。

检查顺序：

```text
请求方式 → 地址 → Body → 请求头 → 后端日志
```

### 17. 批量删除 ID 写进 URL

批量删除正确接口：

```text
POST /api/products/batch-delete
```

ID 列表由请求体接收：

```json
[
  23,
  24
]
```

不要把多个 ID 拼进 URL，因为 Controller 使用的是 `@RequestBody List<Long>`。

### 18. 修改代码后仍返回旧提示

原因通常是：

- 文件没有保存。
- Spring Boot 没有重新启动。
- 旧进程仍占用 8080 端口。
- 启动了错误项目、模块或启动类。

处理步骤：

```text
Ctrl + S
→ 停止旧进程
→ 确认 InventoryPracticeApplication
→ 重新启动
→ 再发请求
```

### 19. HTTP 方法不支持

日志示例：

```text
Request method 'POST' is not supported
```

含义：请求已经到达 Spring，但当前地址没有匹配的 POST 映射。优先检查 Postman 方法、完整地址和 Controller 注解。

### 20. 把对象方法当成静态方法调用

错误写法：

```java
LoginVO.setExpiresInSeconds(seconds);
```

编译错误：

```text
无法从静态上下文中引用非静态方法
```

正确写法：

```java
LoginVO loginVO = new LoginVO();
loginVO.setExpiresInSeconds(seconds);
```

记忆：`LoginVO` 是类名，`loginVO` 是具体对象；Lombok `@Data` 生成的 setter 是对象方法。

### 21. 登录状态判断写反

错误写法：

```java
if (user.getStatus() != 0) {
    throw new BusinessException(403, "用户已被禁用");
}
```

这样会把状态为 `1` 的正常用户拒绝。正确写法：

```java
if (user.getStatus() == 0) {
    throw new BusinessException(403, "用户已被禁用");
}
```

### 22. JWT 环境变量没有传给启动配置

底层异常：

```text
Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"
```

含义：`application.properties` 要求从环境变量读取 JWT 密钥，但当前 IDEA 启动配置没有提供名称完全匹配的变量。

检查：

```text
变量名必须是 JWT_SECRET
不能只填密钥值
名称前后不能有空格
真实密钥不能提交 Git 或发到聊天中
```

本次实际出现过 `JWT_SECRET` 名称末尾带隐藏空格，导致看起来相同但 Spring 无法识别。

### 23. 取消系统环境变量后临时目录丢失

底层异常：

```text
Unable to create tempDir
AccessDeniedException: C:\WINDOWS\tomcat...
```

原因：IDEA 未继承系统环境变量后，Java 读不到正常的 `TEMP/TMP`，退回到无写入权限的 `C:\WINDOWS`。

当前本机运行配置通过下面的 VM option 指定临时目录：

```text
-Djava.io.tmpdir=C:\tmp
```

这属于本机运行环境问题，不是登录或 JWT 业务代码错误。

### 24. 登录代码中的局部变量和 setter 调用错误

本轮出现过：

- 使用未声明的 `loginVO`。
- 把 `LoginVO.SetExpiresInSeconds(...)` 写成类调用且首字母大写。
- 查询不到用户时，在判空前调用 `user.getPassword()` 会产生空指针风险。

正确顺序：

```text
查询用户 → user 判空 → 校验密码 → 校验状态
→ 生成 JWT → new LoginVO → 调用对象 setter → return
```

### 25. 测试依赖版本与 Spring Boot 不兼容

本轮曾手动加入 JUnit 6 并重复声明 `spring-boot-starter-test`，导致运行测试出现 `NoClassDefFoundError: CancellationToken`。

处理原则：

- 优先使用 Spring Boot 管理的测试依赖版本。
- 不要重复声明 `spring-boot-starter-test`。
- 遇到 `NoClassDefFoundError` 时检查依赖树和版本兼容性，而不是只修改测试业务代码。

### 26. IDEA 自动导入了 JDK 内部类

测试中曾错误导入：

```java
jdk.jfr.internal.jfc.model.Constraint
```

出现“不是 public，无法从包外访问”。Mockito 参数匹配应使用正确的静态导入，例如：

```java
import static org.mockito.ArgumentMatchers.any;
```

选择 IDEA 自动导入时必须确认包名，不要使用 `jdk.internal` 或类似内部包。

## 五、Git、PowerShell 与跨平台命令错误

### 27. 把 `.gitignore` 当成 PowerShell 命令执行

出现过：

```text
.gitignore : 无法将“.gitignore”项识别为 cmdlet、函数、脚本文件或可运行程序的名称
```

原因：`.gitignore` 是文件名，不是终端命令。

改正：在 IDEA 中使用 `New → File` 创建和编辑 `.gitignore`；在终端中执行的是 `git status`、`git add`、`git commit` 等 Git 命令。

### 28. 把 Git 的 LF/CRLF 提示当成失败

`git add .` 时出现过 `LF will be replaced by CRLF`。这是 Windows 与 Linux 换行符转换警告，不代表暂存失败。

处理方式：继续用 `git status` 检查文件是否进入暂存区；只有出现 `fatal` 或 `error` 才需要进一步处理。

### 29. GitHub 克隆时 TLS 握手失败

出现过：

```text
schannel: failed to receive handshake, SSL/TLS connection failed
```

原因：当时代理或网络节点连接 GitHub 超时，不是仓库地址或 Git 命令语法错误。

处理顺序：切换可用代理节点 → 浏览器确认 GitHub 可访问 → 重试；必要时用：

```powershell
git -c http.version=HTTP/1.1 clone 仓库地址
```

不要通过关闭 `sslVerify` 绕过证书校验。

### 30. 在 Windows PowerShell 中复制 Linux/macOS 虚拟环境命令

错误命令和现象：

```text
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
```

Windows 中 `.venv/bin/python` 不存在；同时 `python3` 可能只是 Microsoft Store 的应用执行别名，因此没有真正创建 `.venv`。

正确做法：先进入包含 `requirements.txt` 的项目目录，再执行：

```powershell
py --version
py -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m playwright install chromium
```

每一步都可用 `Test-Path .\.venv` 或 `Get-ChildItem .\.venv` 检查结果，不要连续复制多条命令后才看报错。

### 31. 在错误目录执行复制 Skill 的命令

在 `Easy-Job-Tutor` 项目内部执行 `cp -R Easy-Job-Tutor ...`，会寻找一个并不存在的同名子目录；`mkdir -p` 也是从 Unix 文档复制来的写法，目录已存在时 PowerShell 会提示错误。

改正：先确认当前目录和源路径，再使用 PowerShell 命令：

```powershell
Get-Location
Test-Path .\Easy-Job-Tutor
Copy-Item -Recurse .\Easy-Job-Tutor "$env:USERPROFILE\.codex\skills\easy-job-tutor"
```

原则：复制安装文档前先判断自己使用的是 Windows PowerShell 还是 Linux/macOS 终端，并确认当前工作目录。

## 六、Maven、Docker 和文件操作错误

### 32. 在 PowerShell 中执行 Git Bash 命令

本轮把 Git Bash 路径 `/c/Users/...` 和 `ls -la` 输入 PowerShell，分别出现路径不存在和参数无法识别；查找 Java 文件时还把英文点 `.` 输入成了中文句号 `。`。

检查提示符：

- `PS C:\...>` 表示 PowerShell。
- `MINGW64 ... $` 表示 Git Bash。
- `.` 必须使用英文输入法，表示当前目录。

### 33. Maven 命令和 `JAVA_HOME` 未正确配置

Git Bash 先出现 `mvn: command not found`，随后 Maven 又提示 `JAVA_HOME environment variable is not defined correctly`。原系统变量中还混入了隐藏字符。

处理原则：

- `PATH` 决定终端能否找到 `mvn` 和 `java`。
- `JAVA_HOME` 必须指向真实 JDK 根目录，不能指向 `bin`，也不能带隐藏字符。
- 临时 `export` 只对当前 Git Bash 窗口生效，换终端后不会自动继承。

### 34. Docker 客户端存在不等于 Docker 引擎已运行

`docker --version` 成功后，构建仍出现：

```text
failed to connect to the docker API ... dockerDesktopLinuxEngine
```

原因是 Docker 命令已安装，但 Docker Desktop 后台引擎尚未启动。应先打开 Docker Desktop，再用 `docker info` 检查服务端状态。

本轮还把 `docker` 两次拼成 `dockder`。出现“无法识别为命令”时，先检查拼写，不要立刻修改配置。

### 35. 文件名、目录层级和自动格式化错误

本轮真实出现：

- 把 `sql/schema.sql` 创建成 `sql/sql/schema.sql`。
- 把 `.env.example` 命名成带中文顿号的 `.env.example、`。
- IDEA 自动格式化把 SQL 的关键字、字段类型和括号拆成大量单独行。

创建文件后要用 `git status --short` 检查真实路径；文件名必须使用英文标点。自动格式化后必须重新阅读结果，不能看到“格式化完成”就默认正确。

### 36. PowerShell、Docker 和容器 Shell 的多层引号冲突

直接执行带 `mysql -e "SHOW TABLES ..."` 的 `docker exec` 时，PowerShell 和 Docker 处理引号后让 MySQL 收到了不完整 SQL，出现 1064。

最终通过管道传入 SQL：

```powershell
'SHOW TABLES FROM inventory;' | docker exec -i inventory-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"'
```

遇到多层引号问题时，优先减少嵌套，把 SQL 通过标准输入传给容器，而不是不断增加转义符。

### 37. Docker 名称和镜像标签连续拼写错误

本轮出现过：

- 把 `redis:7.4-alpine` 写成 `redis:7.4-apline`，导致镜像筛选为空。
- 把 `docker-learning-redis` 写成 `docker-laerning-redis` 创建了拼错名称的容器。
- 停止时又输入 `docker-laening-redis`，因此提示容器不存在。
- 正确名称的新容器启动时，拼错名称的旧容器仍占用 `6381`，导致 `port is already allocated`。

出现“查不到容器”或“端口已占用”时，先执行：

```powershell
docker ps -a
```

从真实列表复制容器名，不要继续凭记忆手打。

### 38. Redis 命令缺少操作关键字

本轮执行：

```powershell
docker exec docker-learning-redis redis-cli course docker
```

Redis 把 `course` 当成命令，返回 `ERR unknown command`。正确结构是：

```text
set 键 值
get 键
```

例如 `redis-cli set course docker`。容器命令成功执行不代表内部 Redis 命令语法正确，需要继续查看返回值。

### 39. 第一次 Volume 持久化验证没有成功

删除并重建容器后第一次读取 `course` 返回空值，不能因为之前步骤显示“成功”就断言 Volume 已生效。由于当时证据不足，没有猜测唯一原因。

重新验证时依次确认：

1. `docker inspect` 显示 `docker-learning-redis-data -> /data`。
2. `set` 和 `save` 都返回 `OK`。
3. `/data` 中存在 `dump.rdb`。
4. 删除容器、使用同一 Volume 重建后仍能读取 `course=volume`。

只有完整证据链成立，才能确认持久化成功。

### 40. 把容器中的 `localhost` 误认为 Windows 主机

理解检查时曾认为 `inventory-app` 容器中的 `localhost` 指 Windows。正确规则是：

```text
Windows 中的 localhost       = Windows 自己
inventory-app 中的 localhost = app 容器自己
mysql 容器中的 localhost     = MySQL 容器自己
```

Compose 容器访问其他服务应使用服务名，例如 `mysql:3306`、`redis:6379`；容器需要访问 Windows 主机时通常使用 `host.docker.internal`。

### 41. 没有核对项目文件就讲解不存在的配置

本轮讲解 Compose 时，未先读取实际 `compose.yaml` 就声称项目使用了：

```yaml
restart: unless-stopped
```

实际文件中并不存在该配置，造成学习混乱。后续讲解“当前项目已有内容”前必须先读取相关文件；通用但尚未写入项目的内容只能明确标注为“扩展知识”。

### 42. IDEA 只运行了一个测试方法，却误以为运行了整个测试类

IDEA 命令末尾出现：

```text
ProductServiceTest,shouldCalculateOnSaleStockValue
```

表示只运行了该方法。运行整个类时，末尾只有：

```text
ProductServiceTest
```

应点击类名左侧的绿色三角，或执行 `mvn -Dtest=ProductServiceTest test`。

### 43. 新终端找不到 Maven

新 PowerShell 和新 Git Bash 中出现：

```text
mvn: command not found
mvn : 无法将“mvn”项识别为 cmdlet
```

原因是之前的 `export` 只对单个 Git Bash 窗口有效，且 Maven 目录没有永久加入 Windows `Path`。本轮已设置：

```text
JAVA_HOME
MAVEN_HOME
%JAVA_HOME%\bin
%MAVEN_HOME%\bin
```

新终端中通过 `mvn -v` 验证 Maven 3.9.11 与 Java 17。`JAVA_HOME` 指向 JDK 根目录，不能包含 `bin`。

### 44. 完整 Spring 测试缺少 JWT 配置

Mockito Service 测试能够通过，但 `mvn package` 会运行 `@SpringBootTest` 的 `contextLoads()`，它会启动完整 Spring，因此创建 `JwtTokenProvider` 时需要 JWT 密钥。

第一次错误：

```text
Could not resolve placeholder 'JWT_SECRET'
```

在测试注解中加入公开的假测试密钥后，又出现：

```text
JWT密钥解码后不能少于32字节
```

原因是项目先执行 Base64 解码，原来的 32 个文字字符不等于解码后的 32 字节。最终使用“32 字节测试内容的 Base64 字符串”作为 `app.jwt.secret`，全部 6 个测试通过。

原则：

- Mockito 单元测试不启动完整 Spring，可能暴露不了环境配置问题。
- `@SpringBootTest` 会加载完整上下文，需要提供启动所需的测试配置。
- 测试密钥必须是假值，不能把真实 JWT 密钥写入测试或提交 Git。
- 看异常时继续找到最底层 `Caused by`，修完一层后如果仍失败，再读取新的根因。

### 45. 混淆 Windows 本机 MySQL 与 Docker MySQL

重新构建容器后，使用 `backend_intern` 登录返回 401。只读查询 Docker MySQL 后确认容器数据库当时只有 `docker_user`，`backend_intern` 原先属于另一套数据库。

两套连接：

```text
Windows 本机 MySQL：localhost:3306
Docker MySQL：      localhost:3307
app 容器访问：      mysql:3306
```

它们的账号、商品和统计结果互相独立。本轮库存总价值在本机数据库曾为 `1644063.70`，Docker MySQL 为 `990.00`。接口验证前必须确认当前应用连接的是哪套数据库，不能直接复用另一套数据库的账号和预期结果。

### 46. 库存概览中复用了错误的统计方法

初次组装 `InventoryOverviewVO` 时写成：

```java
overview.setOutOfStockProducts(countOnSaleProducts());
```

这会让“上架商品数”和“缺货商品数”来自同一个统计条件。正确做法是为缺货数量单独添加：

```text
status = 1
stock = 0
```

再执行 `selectCount`。组装多个相近统计字段时，要逐项核对“字段名称 → 查询条件 → setter”，不能因为返回类型相同就复用错误方法。

### 47. 把商品 ID 设置成订单主键

创建订单时曾写成：

```java
sysOrder.setId(product.getId());
```

商品 ID 和订单 ID 属于不同表、不同业务含义。订单 `id` 使用 `AUTO_INCREMENT`，插入前不应手动设置；MyBatis-Plus 插入成功后会把数据库生成的订单 ID 回填到 Entity。否则多个订单购买同一商品时会发生订单主键冲突。

### 48. 订单错误码和状态文字不一致

创建订单时曾把“商品不存在”写成 `401`，应使用 `404`；`401` 表示未认证。订单状态 `1` 在表中定义为“已创建”，VO 中曾先后写成“待创建”和错别字“乙创建”，最终统一为“已创建”。

原则：新增状态字段时同步核对“数据库注释 → Entity 数值 → VO 状态文字 → 接口响应”，异常状态码也必须与错误类型一致。

2026-07-27 复盘取消订单时，曾把订单的 `status = 1` 解释成“商品处于上架状态”。同一个数字在不同表中的含义可能完全不同：

```text
product.status = 1：商品上架
sys_order.status = 1：订单已创建、尚未取消
```

解释条件前必须先确认当前操作的是哪张表、哪个 Entity，不能只根据数值猜业务含义。

### 49. 取消订单时遗漏用户判空和安全对象比较

取消订单根据用户名查询用户后，最初没有判断 `user == null`，后续 `user.getId()` 存在空指针风险；订单归属最初使用 `order.getUserId().equals(user.getId())`，后改为：

```java
Objects.equals(order.getUserId(), user.getId())
```

同时把错误消息“订单状态以变化请重试”修正为“订单状态已变化，请重试”。涉及当前用户的 Service 代码要先完成“查询 → 判空 → 使用 ID”的顺序。

### 50. 重复创建库存操作对象但没有保存

`ProductService.deductStock()` 和入库方法中曾先创建并填充一个 `StockOperation` 对象，但没有调用 Mapper 插入，随后又调用：

```java
stockOperationService.recordOperation(...)
```

真正的保存发生在 `StockOperationService` 的：

```java
stockOperationMapper.insert(operation);
```

未使用的前一个对象属于重复死代码，应删除。判断“是否保存数据库”要继续追踪到 `insert`、`update` 或实际 Mapper SQL，不能只看到 `new Entity()` 和 setter 就认为已经持久化。

### 51. 分页时导入了错误的 `Page`，Controller 仍调用旧方法

订单分页首次编译时，IDEA 自动导入了：

```java
org.springframework.data.domain.Page
```

它是 Spring Data 的分页接口，属于抽象类型，不能通过 `new Page<>(...)` 创建对象，也不符合 MyBatis-Plus `selectPage()` 要求的 `IPage` 类型。正确导入是：

```java
com.baomidou.mybatisplus.extension.plugins.pagination.Page
```

同时 Service 的方法已经从：

```java
getMyOrders(String username)
```

改为接收 `username`、`pageNum`、`pageSize`，但 Controller 一开始仍然只传了 `username`，因此出现“实际参数列表和形式参数列表长度不同”。

以后修改方法签名时要同步检查：

- 方法的所有调用位置。
- Controller 和 Service 的返回类型。
- 同名类的完整包名。
- 修改后运行 `mvn test`，以最后一次输出的 `BUILD SUCCESS` 为准。

### 52. 混淆 Dockerfile、Compose 和端口所在位置

复盘部署时曾把 Dockerfile 说成“引入 JAR 的使用方法”，并把 Compose 误认为 Java 的 Service、API 和数据层组合。

正确职责是：

```text
Dockerfile：定义如何使用 JRE 和 JAR 构建、启动一个应用镜像
compose.yaml：定义并组合 app、mysql、redis 等多个容器服务
```

端口映射：

```text
应用容器访问 MySQL：mysql:3306
Windows Navicat 访问容器 MySQL：localhost:3307
compose 中 3307:3306：宿主机端口映射到容器端口
```

`3306` 是 MySQL 的默认端口，不是绝对不能修改的固定值；`3307` 是当前项目选择的 Windows 宿主机端口。

## 七、当前阶段的防错清单

写完 Service 后检查：

- 返回单个还是列表？
- Mapper 返回 Entity 还是 VO？
- 查询条件操作符是否正确？
- 所有执行路径是否都有 return？
- `if` 后是否误加分号？

写完 Controller 后检查：

- HTTP 注解是否正确？
- 路径是否与已有接口冲突？
- `ApiResponse<T>` 的 T 是否与 Service 返回类型一致？
- 没有 data 时是否使用 `Void`？

Postman 测试前检查：

- GET/POST/PUT/PATCH/DELETE 是否正确？
- 地址是否完整？
- ID 应放路径还是 JSON 请求体？
- 修改代码后是否保存并重启？

启动失败时检查：

- 从最后一个 `Caused by` 开始看，不被前面的连锁异常干扰。
- `${NAME}` 是否存在对应环境变量，变量名是否有隐藏空格？
- IDEA 当前运行的是不是正确的 `InventoryPracticeApplication` 配置？
- `TEMP/TMP` 或 `java.io.tmpdir` 是否指向有写入权限的目录？
- 是否误把密钥、数据库密码或完整 JWT 放进截图、日志、源码或 Git？

JWT / Security 检查：

- 登录和注册是否公开，其他接口是否要求认证？
- 请求头格式是否为 `Authorization: Bearer token`？
- `substring(7)` 是否只去掉 `Bearer ` 前缀？
- JWT 过滤器最后是否调用了 `filterChain.doFilter()`？
- 无 Token 是否返回 401，带有效 Token 是否正常进入 Controller？

## 后续记录规则

- 同一个错误再次出现时，在对应条目下记录日期和场景，不重复创建新条目。
- 只记录真实发生过、值得下次优先检查的错误。
- 已经连续多次独立避免的错误，可以标记为“基本掌握”，但暂不删除历史记录。

### 53. 在 Git 仓库的父目录执行仓库命令

**场景：**

在下面的父目录执行：

```powershell
PS C:\Users\Whiscjdsb\Documents\SpringBoot> git remote set-url origin ...
```

出现：

```text
fatal: not a git repository (or any of the parent directories): .git
```

**原因：** 当前终端不在真正的项目仓库目录内。

**改正：**

```powershell
cd C:\Users\Whiscjdsb\Documents\SpringBoot\inventory-order-system
git remote set-url origin https://github.com/Whiscjdsb/inventory-order-system.git
git remote -v
```

**规律：** 执行 `git status`、`git add`、`git commit`、`git remote` 前，先看终端提示符是否位于包含 `.git` 的项目目录。

### 54. 项目文件夹改名会改变 Docker Compose 默认项目名

**场景：** 项目文件夹从 `inventory-practice` 改为 `inventory-order-system`。Docker Compose 默认根据文件夹名生成网络和数据卷名称，如果直接重新启动，可能创建一套新的空 MySQL、Redis 数据卷，让人误以为原数据丢失。

**处理：** 在 `compose.yaml` 顶层固定原项目名：

```yaml
name: inventory-practice

services:
```

**规律：** `docker compose down` 默认保留数据卷；文件夹改名不会删除旧数据卷，但可能导致 Compose 不再自动使用它。先固定项目名，再执行 `docker compose up -d`。

### 55. 修改 Git remote 不等于自动修改 GitHub 仓库名

`git remote set-url` 只修改本地仓库保存的远程地址。GitHub 网站上的仓库必须先在仓库 `Settings` 中完成改名，再更新本地 remote，并通过下面的命令确认：

```powershell
git remote -v
```

### 56. 简历文件名和信息优先级不清晰

**场景：** PDF 使用了 `新建简历 ad228c.pdf`，顶部展示出生年月，但没有直接展示求职方向。

**改正：**

- 文件名使用 `姓名-岗位-学校-毕业届别.pdf`。
- 顶部优先展示姓名、求职方向、联系方式、学校和 GitHub。
- 出生年月对 Java 后端能力帮助较小，可以删除。
- 课程、技术和项目描述只能写真实学过、能够解释的内容。

推荐文件名：

```text
万晖-Java后端开发实习-福建农林大学-2028届.pdf
```

### 57. 普通异常堆栈与 Spring 多层 `Caused by` 的定位方式混淆

**场景：**

看到下面的普通 Java 空指针堆栈时，最初认为应该从最下面的 `AuthController` 开始查看：

```text
NullPointerException: user is null
at AuthService.login(AuthService.java:45)
at AuthController.login(AuthController.java:31)
```

**正确判断：**

- 普通 Java 异常先看异常类型和 message，再找第一条属于自己项目的文件与行号；本例应先打开 `AuthService.java:45`。
- 后续的 `AuthController.java:31` 表示调用来源，不是空指针直接发生的位置。
- Spring 启动失败存在多层包装异常时，先找最底层 `Caused by`，然后在对应堆栈中继续找自己的代码或具体配置。

**规律：**

```text
普通运行异常：异常类型/message → 第一条自己的代码
Spring启动异常：最底层Caused by → 具体配置或第一条自己的代码
```

### 58. 混淆 Spring、MyBatis、Nacos、Gateway 和 OpenFeign 的职责

**场景：**

- 把 `ProductService` 对象说成由 MyBatis 创建。
- 把 Gateway、Nacos 和 OpenFeign 混成“Gateway 调用 OpenFeign 寻找服务地址”。

**正确区分：**

```text
Spring：创建和管理Service等Bean，并通过构造方法完成依赖注入
MyBatis：为Mapper接口生成数据库访问代理对象，再交给Spring管理
Gateway：统一入口、认证过滤和路由转发
Nacos：服务注册与发现，提供目标服务地址
OpenFeign：根据接口定义发送服务间HTTP请求
```

**规律：** 解释框架调用链时，每个组件只说自己的直接职责，先画出请求顺序，再补充实现细节。

### 59. Docker 项目介绍中混淆镜像与多容器编排

**场景：**

项目介绍时把 Docker 说成“将 Service、Redis 和 MySQL 层打包成一个镜像”。

**正确表达：**

```text
Dockerfile：把Spring Boot JAR构建为应用镜像
MySQL和Redis：分别使用自己的官方镜像
Docker Compose：统一配置、启动和连接app、mysql、redis多个容器
```

**规律：** 镜像描述单个容器如何创建；Compose 描述多个容器如何组合运行。

### 60. 按日期分组却选择完整时间导致 `ONLY_FULL_GROUP_BY` 报错

**场景：**

查询每天的订单统计时写成：

```sql
SELECT o.create_time
FROM sys_order o
GROUP BY DATE(o.create_time);
```

MySQL 返回：

```text
Expression #1 of SELECT list is not in GROUP BY clause
```

**原因：** `GROUP BY DATE(o.create_time)` 把同一天的多条记录分为一组，但 `SELECT o.create_time` 要求显示包含时分秒的某个具体时间。组内可能存在多个不同时间，MySQL 无法确定应该返回哪一个。

**正确写法：**

```sql
SELECT
    DATE(o.create_time) AS order_date,
    COUNT(o.id) AS order_count,
    SUM(o.quantity) AS total_quantity,
    SUM(o.total_price) AS total_amount
FROM sys_order o
WHERE o.status = 1
GROUP BY DATE(o.create_time)
ORDER BY order_date DESC;
```

**规律：** `SELECT` 中没有经过聚合函数处理的字段或表达式，应与 `GROUP BY` 的分组内容保持一致。SQL 代码继续使用英文逗号和英文标点，避免中文逗号造成语法错误。

### 61. 扣库存 Service 只依赖 DTO 校验，负数可能让 SQL 反向增加库存

**场景：**

`DeductStockRequest` 虽然使用 `@Positive`，但 `ProductService.deductStock()` 最初没有再次校验 `productId` 和 `quantity`，直接调用：

```sql
SET stock = stock - #{quantity}
WHERE stock >= #{quantity}
```

如果未来有其他 Service、定时任务或测试绕过 Controller，错误传入 `quantity = -2`，SQL 的计算会变成：

```text
stock - (-2) = stock + 2
```

并且 `stock >= -2` 通常成立，可能把“扣库存”变成“加库存”。

**修正：**

在 Mapper 调用前校验：

```java
if (productId == null || productId < 1) {
    throw new BusinessException(400, "商品ID必须大于等于1");
}
if (quantity == null || quantity < 1) {
    throw new BusinessException(400, "商品数量必须大于等于1");
}
```

新增单元测试传入负数，并使用 `verifyNoInteractions` 确认 Mapper 和库存记录组件均未执行；完整测试通过。

**规律：**

- DTO 校验保护 HTTP 请求入口，Service 校验保护核心业务边界，两者职责不同。
- 对会直接参与加减乘除或 SQL 更新条件的数量，必须在最接近业务操作的位置再次验证范围。
- 单元测试用于证明校验有效；真正阻止非法 SQL 的是 Service 中的判断，不是测试本身。

### 62. 价格区间测试使用了错误的测试数据

**场景：**

测试“最小价格不能大于最大价格”时，最初传入的最小价格是负数。方法会先命中“最小价格不能小于 0”的校验，无法执行到本次真正想测试的价格区间判断。

**修正：**

使用两个本身都合法、但顺序不合法的价格：

```java
new BigDecimal("1000.00"),
new BigDecimal("100.00")
```

这样前面的非负校验能够通过，随后准确触发“最小价格不能大于最大价格”。

**规律：**

- 一个测试方法尽量只验证一个规则。
- 测试后面的校验分支时，测试数据必须先通过前面的所有校验。
- `assertThrows()` 捕获到异常并不代表测试目标正确，还要断言异常码和消息，确认命中了预期分支。

### 63. `@WebMvcTest` 缺少 JWT 依赖导致上下文启动失败

**场景：** 新建 `ProductControllerTest` 后出现 `Failed to load ApplicationContext`，最底层原因为 `JwtAuthenticationFilter` 找不到 `JwtTokenProvider`。

**修正：** 在 Controller 测试中增加 `@MockBean JwtTokenProvider`。本次测试使用 `addFilters=false` 关闭 Security 过滤器，只验证 Controller、参数校验和响应，不需要创建真实 JWT Provider。

**规律：** 阅读 Spring 启动失败时应继续找到最后一层 `Caused by`；`Failed to load ApplicationContext` 只是表面结果，缺少哪个 Bean 才是实际原因。

### 64. Mapper 新 SQL 已写好，但 Service 仍调用旧逻辑

**场景：** 库存概览接口已增加单条聚合 SQL，但请求后 MyBatis 日志仍显示 4 次数据库查询。

**原因：** `ProductMapper.selectInventoryOverview()` 已存在，但 `ProductService.getInventoryOverview()` 仍分别调用 4 个旧统计方法，新 Mapper 方法从未被调用。

**修正：** Service 改为直接返回 `productMapper.selectInventoryOverview()`；重启后通过日志确认只执行一条统计 SQL。

**规律：** 新代码存在不代表运行路径会经过它。优化是否生效应同时检查调用链、运行日志和接口结果。

### 65. Maven 增量编译结果异常导致主类无法访问

**场景：** `testCompile` 报告 `ProductVO` 找不到、`OrderService.class` 出现 `NoSuchFileException`，但对应源码实际存在。

**修正：** 保存文件、停止正在运行的测试，执行 `mvn clean test` 删除 `target` 后从头编译，构建恢复成功。

**规律：** 源码存在且包名正确，但错误指向 `target/classes` 中的旧类或缺失类时，优先怀疑增量编译产物；正常测试使用 `mvn test`，缓存异常时才使用 `mvn clean test`。

### 66. 商品下架枚举状态码误写为订单取消状态码

**场景：** 创建 `ProductStatus` 时把 `OFF_SALE` 写成 `2`，混入了订单状态“已取消”的编码。

**修正：** 根据商品业务规则改为 `OFF_SALE(0, "下架")`，并通过测试和商品查询验证 `statusText` 正确。

**规律：** 枚举消除了散落的魔法数字，但枚举定义本身仍必须与数据库真实编码一致；创建枚举时应先核对表字段约定和现有接口数据。

### 67. 两套 Docker Compose 项目使用相同宿主机端口

**场景：** 启动 `springboot-practice` 后，`8081` 和 `9000` 无法访问，Compose 启动过程提示：

```text
Bind for 0.0.0.0:6380 failed: port is already allocated
```

**原因：** `inventory-order-system` 的容器已经占用了宿主机的 `8080`、`3307` 和 `6380`，而 `springboot-practice` 也配置了相同端口。一个宿主机端口同一时间只能被一个容器或程序绑定。

**处理：** 暂时停止库存项目的 `inventory-app`、`inventory-mysql`、`inventory-redis` 容器，再启动微服务项目。只停止容器，没有删除容器和数据卷。

**规律：**

- 接口完全无法连接时，先用 `docker compose ps` 和端口检查确认服务是否真正运行。
- 出现 `port is already allocated` 时先查占用者，不要反复重启。
- 两套本地项目可以修改宿主机端口错开，也可以学习其中一套时先停止另一套。

### 68. MySQL 容器运行但没有加入 Compose 网络

**场景：** 注册接口返回 500；主服务日志最底层出现：

```text
java.net.UnknownHostException: mysql
```

**原因：** 主服务容器位于 `springboot-practice_default` 网络，但旧 MySQL 容器的网络列表为空。主服务因此无法把 `mysql` 解析成 MySQL 容器地址。

**处理：** 使用 Compose 重新创建 MySQL、Redis、主服务和 Gateway 容器。MySQL 数据保存在命名数据卷中，重建容器没有删除数据。重建后 MySQL 加入同一网络，并获得 `mysql` 网络别名。

**规律：**

- 容器显示 `Up` 只说明进程在运行，不等于容器间一定可以通信。
- `UnknownHostException: mysql` 重点检查 Docker 网络和服务别名，不是先修改 Java SQL。
- `docker inspect` 可查看容器连接的网络；同一 Compose 网络中的服务可以用服务名互相访问。
- 排错要找到最后一层 `Caused by`；接口的 500 只是结果，`UnknownHostException` 才是本次根因。

### 69. 混淆 Gateway、OpenFeign、Nacos 与返回链路

**场景：** 复述微服务请求时，最初认为 Gateway 会直接调用 OpenFeign，并认为通知服务结果会先返回 Gateway；同时把 Nacos 误读成其他名称。

**正确关系：**

```text
请求：Postman -> Gateway -> 主服务 -> OpenFeign -> 通知服务
返回：通知服务 -> 主服务 -> Gateway -> Postman
```

- Gateway 负责接收外部请求并把请求转发给主服务。
- 主服务中的 Controller 才会调用 OpenFeign 接口。
- OpenFeign 负责向通知服务发送 HTTP 请求。
- Nacos 只提供“服务名对应的实际地址”，不是业务结果返回链路的一层。
- 通知服务由主服务调用，因此结果必须先回到主服务中的 `result`，再逐层返回。

**规律：**

- 判断返回方向时使用“谁调用，结果先返回给谁”。
- 阅读微服务调用链时先只画运行程序和端口，再补 Gateway、OpenFeign、Nacos 等框架角色。
- 不应只背组件定义，必须在真实代码中找到 Gateway 路由、Controller 调用和 Feign Client 三个位置。

### 70. RabbitMQ 只会复述概念但尚未对应代码

**场景：** 能说出“生产者、交换机、队列、消费者”，但主动指出自己还没有看懂项目中的实现代码。

**处理：** 回到真实 `TaskNotificationProducer`、`TaskNotificationConsumer` 和 `RabbitConfig`，把概念逐一对应到：

```text
RabbitTemplate.convertAndSend() -> 生产者发送
TopicExchange                 -> 交换机路由
Binding + Routing Key         -> 交换机与队列的匹配规则
Queue                         -> 保存消息
@RabbitListener               -> 消费者监听并自动执行
```

**规律：**

- 框架学习先建立概念，但不能以“知道流程”作为结束；还要读懂项目中的入口代码和核心 API。
- Producer 和 Consumer 当前要求能读懂、能在提示下编写；RabbitConfig 的固定构建 API 可以查资料，不要求默写。
- 看到日志中的生产者发送和消费者处理，只能证明链路运行成功；能解释代码如何触发才算开始理解。

### 71. 混淆数据库事务、MQ 发送失败与接口重试

**场景：** 最初把“订单数据库操作需要共同回滚”和“通知发送失败是否回滚订单”混在一起，没有先区分核心数据和辅助通知，也没有意识到接口返回 500 时数据库可能已经写入成功。

**真实代码证据：** `springboot-practice` 的 `TaskService.createTask()` 当前先执行 `taskMapper.insert(task)`，再调用 `sendTaskCreatedMessage()`，并且方法没有 `@Transactional`。因此 MQ 发送抛出异常时，任务可能已经保存；用户重试可能再次插入任务。

**正确区分：**

- 扣库存、库存记录、订单保存属于同一数据库核心业务，应一起成功或回滚。
- 邮件、短信和普通通知属于辅助业务，失败不应取消已经成功的订单，通常记录失败并重试。
- 普通 `@Transactional` 默认管理数据库事务，不能自动把 MySQL 和 RabbitMQ 变成一个绝对同时成功的事务。
- 同一请求或消息可能重复到达，生产端和消费端都需要根据业务唯一标识考虑幂等。

**规律：**

- 接口返回失败不等于数据库一定没有变化，重试前要考虑请求是否已经产生结果。
- 分析事务时先列出所有操作，再划分“核心数据操作”和“可异步辅助操作”。
- 当前阶段掌握风险和基本幂等思路即可；本地消息表、事务消息等复杂方案暂不要求实现。

### 72. SQL 关联字段和异常筛选运算符选择错误

**场景一：** 使用 `NOT EXISTS` 查询没有有效订单的商品时写成 `o.id = p.id`，把订单主键与商品主键直接比较，导致实际存在有效订单的商品仍被错误返回。

**修正：** 订单与商品的业务关联字段是 `sys_order.product_id`，正确条件为：

```sql
o.product_id = p.id
```

**场景二：** 检查库存流水计算错误时使用 `after_stock = ...`，实际查询出了计算正确的数据；需求是找异常记录，应使用不等于：

```sql
after_stock <> before_stock + quantity
after_stock <> before_stock - quantity
```

**规律：**

- 写 JOIN 或相关子查询前，先用中文说清两个字段各自代表什么；同样叫 `id` 不代表可以直接关联。
- 把需求中的“符合规则”翻译成 `=`，把“不符合、异常、不一致”翻译成 `<>`。
- 数据质量检查正常返回空集是合理结果，不要为了看到数据而把判断方向写反。

### 73. 创建订单异常测试使用了错误 Mapper 和状态码

**场景：** 编写“商品不存在”测试时，把商品查询配置成 `sysOrderMapper.selectById(999999L)`，并断言状态码 400。

**修正：** `OrderService.createOrder()` 根据商品 ID 调用的是：

```java
productMapper.selectById(request.getProductId())
```

因此测试应配置 `productMapper.selectById(999999L)` 返回 `null`，并断言 404“商品不存在”。

**规律：**

- `ProductMapper` 操作商品，`SysOrderMapper` 操作订单，`SysUserMapper` 操作用户；写 Mock 前先沿真实 Service 调用链确认被调用对象。
- 400 表示请求参数本身不合法；合法 ID 查询不到对应资源通常使用 404。
- `when(...).thenReturn(...)` 只是预设 Mock 行为，真正触发它的是后续 `orderService.createOrder()` 中的 Mapper 调用。

### 74. 把 `when()` 误解为读取参数

**场景：** 项目复盘时把 Mockito 的 `when()` 解释为“读取参数”。

**修正：** `when(mock.method(...)).thenReturn(value)` 是为 Mock 对象预设行为：以后 Service 调用这个模拟方法时，直接返回指定值，不会访问真实数据库。

**规律：**

- `when()` 准备依赖行为，`assertEquals()` 检查结果，`verify()` 检查依赖调用。
- 判断测试代码作用时，先区分“准备数据”“真正执行 Service”“结果断言”“行为验证”。

### 75. 混淆容器数据库与 Windows 本地数据库

**场景：** Docker 项目运行正常，但按本地数据库中常用的商品 ID 4 请求时返回 404，容易误判为 JWT、Redis 或接口故障。

**真实原因：** 容器中的 MySQL 使用独立端口和数据卷，与 Windows 本地 MySQL 不是同一份数据；本次容器数据库中的有效商品 ID 是 1。

**规律：**

- 接口返回“商品不存在”时，先确认应用当前连接的是哪一个 MySQL，再核对该数据库中的真实 ID。
- `localhost:3307` 是 Windows 访问容器 MySQL 的映射端口；容器之间使用服务名和容器端口 3306。
- 不要仅凭 Navicat 中看到的数据推断 Docker 应用一定能读取到相同记录。

### 76. 缓存功能不等于解决缓存穿透

**场景：** 项目已经使用 `@Cacheable`，容易把“有 Redis 缓存”表述成“解决了缓存穿透”。

**真实代码证据：** 当前配置为 `spring.cache.redis.cache-null-values=false`。查询不存在的商品时方法抛出 404，不会把空结果写入 Redis；重复请求同一个不存在 ID 仍会反复访问 MySQL。

**规律：**

- 当前项目可以表述为“实现商品查询缓存及修改后的缓存失效”。
- 在没有空值缓存、布隆过滤器等实际方案前，不能在简历或面试中声称已经解决缓存穿透。
## 2026-08-03 补充：缓存与测试概念

### 1. 把“没有缓存空值”误认为解决了缓存穿透

- **错误理解：** 商品不存在时返回 404，因此认为不存在的商品不会造成缓存穿透。
- **真实情况：** 当前配置 `spring.cache.redis.cache-null-values=false`，空结果不会写入 Redis；同一个不存在 ID 被重复请求时，每次仍会访问 MySQL。
- **正确结论：** 当前项目实现了商品缓存和修改后失效，但没有完整解决缓存穿透，简历和面试不能夸大。

### 2. 把 Mockito 的 `when()` 理解成读取参数

- **错误理解：** `when()` 用来读取传入的参数。
- **正确理解：** `when(mock.method(...)).thenReturn(value)` 是预设 Mock 对象的行为；以后 Service 调用这个模拟方法时，Mockito 返回预先指定的值，不会访问真实数据库。
- **快速区分：** `when` 安排依赖，`assert` 检查结果，`verify` 检查调用行为。

### 3. Docker 数据与本地数据混淆

- **现象：** 以前常用的商品 ID `4` 在容器接口中返回 404。
- **原因：** Docker MySQL 使用自己的数据卷，与 Windows 本地 MySQL 不是同一个数据库实例；容器数据库当前可用商品 ID 是 `1`。
- **排查顺序：** 先确认应用连接的是哪个 MySQL，再查询该数据库中的真实 ID，不要仅凭本地 Navicat 中的数据判断。
## 2026-08-04 补充：前端存储、数据库结构与事务锁

### 1. `sessionStorage.setItem()` 放在错误位置

- **错误写法：** 页面加载时读取 `authToken` 后立即把它重新写入 `sessionStorage`，但登录成功取得新 Token 后没有保存。
- **后果：** 第一次加载保存的是空字符串，刷新页面仍无法恢复新登录状态。
- **正确位置：** 文件开始只调用 `getItem()`；登录成功并给 `authToken` 赋值后，再调用 `setItem()`。

### 2. 登录状态初始化代码重复

- `if (authToken !== "")` 被写了两次，功能不会立刻出错，但属于重复逻辑。
- 初始化页面登录状态只需要执行一次；401 时还需要同时清空内存变量并调用 `sessionStorage.removeItem()`。

### 3. 只修改 `schema.sql`，误以为旧数据库会自动增加索引

- **现象：** `schema.sql` 声明了 `idx_order_user_id`，但 `SHOW INDEX FROM sys_order` 只显示主键。
- **原因：** Docker 数据卷中的表早已存在，`CREATE TABLE IF NOT EXISTS` 不会修改旧表结构。
- **修复：** 使用 `ALTER TABLE ... ADD INDEX ...` 修改实际数据库；长期项目应通过 Flyway 或 Liquibase 记录数据库迁移。

### 4. 误以为事务回滚后行锁不会释放

- **错误回答：** 订单保存异常后库存回滚为原值，但行锁不会释放。
- **正确结论：** 事务无论提交还是回滚，结束时都会释放它持有的锁；否则其他事务会永久等待。
- **正确流程：** 扣库存成功 → 保存订单异常 → 回滚库存和流水 → 释放行锁 → 等待中的请求继续执行。

### 5. 前端文件处于 `AM` 状态

- `AM` 表示文件已经进入暂存区，但暂存之后又继续被修改。
- 这不是代码运行错误；准备提交时需要重新执行 `git add frontend`，才能把最新内容放入暂存区。

## 2026-08-05 补充：文件解析、集成测试配置与 Maven 旧产物

### 1. 把非数字库存直接交给 `Integer.parseInt()`

- **现象：** `Integer.parseInt("abc")` 抛出 `NumberFormatException`，堆栈最后指向 `ProductFilePractice.java:29`。
- **原因：** `abc` 不是合法整数；原来的 `catch (IOException)` 只处理文件读写异常，不能捕获这个数字格式异常。
- **修复：** 在逐行处理内部捕获 `NumberFormatException`，输出坏数据并 `continue`，保证一条错误记录不会终止整个文件。
- **规律：** 读堆栈先找异常类型和第一条属于自己源文件的行，不从 `java.base` 内部调用开始排查。

### 2. 集成测试文件物理目录与 `package` 不一致

- **现象：** 文件最初位于 `src/test/java/mapper`，但声明为 `package com.example.inventorypractice.mapper;`。
- **修复：** 使用 IDEA `Refactor → Move` 将文件移动到 `src/test/java/com/example/inventorypractice/mapper`。
- **规律：** Java 文件目录应与 `package` 层级对应；移动包优先使用 IDEA Refactor，避免只改一边。

### 3. `@SpringBootTest` 启动失败时只看最外层错误

- **现象：** 最上层显示 `Failed to load ApplicationContext`，实际最底层是 `Could not resolve placeholder 'JWT_SECRET'`。
- **原因：** 集成测试会启动完整 Spring 上下文；即使只测试 Mapper，Security 和 `JwtTokenProvider` 也需要创建。
- **修复：** 复用已有测试中的测试用 `app.jwt.secret` 属性，不使用生产密钥。
- **规律：** `Failed to load ApplicationContext` 只是汇总结果，应沿 `Caused by` 向下查找最底层根因。

### 4. IDEA 环境变量被错误填成一整行

- **现象：** 配置后仍连接默认数据库，并出现 `Access denied for user 'root'@'localhost'`。
- **真实原因：** 把 `DB_URL`、URL、`DB_USERNAME` 和 `DB_PASSWORD` 全部填进一个 Name；IDEA 因此只创建了一个名称错误的环境变量。
- **修复：** 在 Environment Variables 表格中分别创建 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 三行，Name 列只放变量名，Value 列放对应值。
- **规律：** 环境变量不是命令文本；表格模式必须一行一个变量，不使用空格把多组名称和值串联起来。

### 5. 把“密码拒绝”和“连接拒绝”混为一类

- `Access denied` 表示已经连接到某个 MySQL，但用户名或密码不被接受；本次还暴露出测试可能落到了默认 `localhost:3306`。
- `Connection refused` 表示目标地址和端口没有服务接收连接；本次检查发现 Compose 容器列表为空，`localhost:3307` 没有 MySQL。
- **修复顺序：** 先确认 `DB_URL`，再用 `docker compose ps` 确认 MySQL 是否 `healthy`，最后核对用户名和密码。

### 6. 重命名测试后旧测试仍被 Maven 执行

- **现象：** 源文件已经改为 `ProductConcurrencyIT.java`，但 `mvn test` 仍运行旧的 `ProductConcurrencyIntegrationTest`。
- **原因：** `target/test-classes` 同时残留旧、新两个 `.class` 文件；普通 `mvn test` 不会主动删除旧编译产物。
- **修复：** 执行 `mvn clean test`，先删除 `target`，再重新编译和运行普通测试。
- **规律：** 源文件重命名后出现旧类名、旧行为或重复测试，优先检查 `target` 等生成目录；`clean` 删除的是可重新生成的构建产物，不会删除 `src` 源码。

## 2026-08-04 补充：Java 流程控制与异常分类

### 1. 指针写入后遗漏位置更新

- **场景：** 删除指定元素时能够把 `nums[readIndex]` 写入 `nums[writeIndex]`，但首次遗漏 `writeIndex++`。
- **后果：** 后续所有有效元素会继续覆盖同一个数组位置，返回的有效长度也不会增长。
- **规律：** 使用写指针时，每成功写入一个有效元素，就检查写指针是否应移动到下一个位置。

### 2. 跳过无效字符后仍继续执行本轮比较

- **场景：** 回文判断中遇到空格或标点后执行 `left++` 或 `right--`，但没有 `continue`，随后仍执行字符比较。
- **后果：** 连续标点或新位置尚未验证时可能产生错误比较。
- **规律：** 指针移动后若需要从头重新验证新位置，应立即 `continue`；不要机械移动后继续使用未经检查的新字符。

### 3. 受检异常与运行时异常的区别

- `IOException` 是受检异常，Java 编译器要求使用 `try-catch` 或在方法上声明 `throws`。
- 项目的 `BusinessException` 继承 `RuntimeException`，属于运行时异常，编译器不强制当场处理；异常会向外传播并由 `GlobalExceptionHandler` 转换成 HTTP 响应。
- **规律：** 是否必须捕获主要由异常继承体系决定，不是看到所有 `Exception` 都必须写 `try-catch`。

## 2026-08-07 补充：事务集成测试的数据库连接与旧表结构

### 1. 只看 `MyBatisSystemException`，无法判断真实原因

- **现象：** Maven 只汇总显示 `OrderTransactionRollbackIT... MyBatisSystem`，最初无法判断是哪段代码出错。
- **排查：** 打开 `target/surefire-reports` 中对应报告，从上往下找到最后一层 `Caused by`。
- **规律：** `MyBatisSystemException`、`Failed to load ApplicationContext` 等通常是外层包装；真正原因常在最底层数据库异常、连接异常或配置异常中。

### 2. 本地测试连接了错误端口或使用了错误密码

- **第一次根因：** `Access denied for user 'root'@'localhost'`，测试落到默认 `localhost:3306`，而 Docker MySQL 映射到 Windows 的端口是 `3307`。
- **第二次根因：** 修正端口后出现 `root@172.19.0.1` 的密码拒绝，说明已经到达 Docker MySQL，但传入密码仍不正确。
- **修复：** 设置 `DB_URL` 为 Windows 侧 `localhost:3307`，并从项目 `.env` 读取 `DB_PASSWORD`，避免把说明文字或错误值当作密码。
- **判断方法：** `Connection refused` 是端口没有服务；`Access denied` 是已经找到 MySQL但身份验证失败。

### 3. `schema.sql` 已有订单表，但旧 Docker 数据库没有

- **现象：** 测试清理阶段执行 `DELETE FROM sys_order ...` 时出现 `BadSqlGrammarException`。
- **真实根因：** 最底层异常为 `Table 'inventory.sys_order' doesn't exist`，并不是 `DELETE` 语法写错。
- **证据：** 实际 `SHOW TABLES` 只有 `product`、`stock_operation` 和 `sys_user`。
- **原因：** MySQL 数据卷第一次创建后一直复用；后来添加到 `schema.sql` 的 `sys_order` 不会通过 `/docker-entrypoint-initdb.d/` 自动补到旧数据库中。
- **修复：** 对现有数据库手动执行 `CREATE TABLE IF NOT EXISTS sys_order ...`，同时保留完整 `schema.sql` 供新环境初始化。
- **长期方案：** 使用增量 SQL，并在后续学习 Flyway 或 Liquibase；不要为了补一张表随意使用 `docker compose down -v` 删除全部数据库数据。

### 4. 测试在 `finally` 清理失败会掩盖原测试结果

- **现象：** 事务断言可能已经执行完成，但 `finally` 中删除不存在的 `sys_order` 表再次抛异常，Maven 最终仍报告测试错误。
- **规律：** 集成测试的准备、断言和清理任意一步抛异常，整项测试都会失败；看到清理代码行号时，应检查数据库结构和清理 SQL，而不是只检查业务断言。

### 5. 新开 PowerShell 后集成测试再次连接默认数据库

- **现象：** 两个 IT 单独运行成功后，合并运行又同时在第一次数据库写入时报 `Access denied for user 'root'@'localhost'`。
- **原因：** `$env:...` 只在当前 PowerShell 会话有效；新开终端后 `DB_URL` 消失，Spring 回退到配置中的默认 `localhost:3306`。
- **修复：** 新增 `scripts/run-integration-tests.ps1`，每次自动从 `.env` 读取密码并设置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 后再运行测试。
- **安全要求：** 脚本只能读取 `.env`，不能把真实密码写进脚本；`.env` 已在 `.gitignore` 中。

### 6. 条件聚合查询连续遗漏字段分隔逗号

- **现象：** `SELECT` 中的 `created_orders`、`cancelled_orders`、`sold_quantity` 等表达式逻辑正确，但相邻字段之间漏写英文逗号。
- **规律：** `SELECT` 返回多个字段或计算表达式时，除最后一个字段外，每一项末尾都需要英文逗号；新增统计列后应从上到下检查分隔符。
- **本质判断：** 本次属于 SQL 标点错误，不是 `CASE`、`COUNT` 或 `SUM` 的思路错误。

## 2026-08-08 补充：Mockito 测试设计与 PowerShell 集成测试脚本

### 1. 用 `assertEquals()` 代替 Mockito 的调用验证

- **错误写法：** 为了验证取消 SQL 和退库存方法被调用，直接再次执行 Mock 方法并对返回值使用 `assertEquals()`。
- **问题：** 这会产生一次新的调用，并不能证明 `OrderService` 执行期间调用过依赖。
- **正确做法：** 使用 `verify(sysOrderMapper).cancelOrder(...)` 和 `verify(productService).addStock(...)` 检查真实 Service 调用链中的行为。
- **规律：** `assert` 检查返回结果或异常；`verify` 检查 Mock 方法是否被调用，两者不能互相替代。

### 2. 在 `assertThrows()` 中调用 Mapper，而不是被测试的 Service

- **错误写法：** 测试订单状态变化时，在 `assertThrows()` 中调用 `sysOrderMapper.cancelOrder()`。
- **问题：** Mock Mapper 默认只返回 `0`，不会执行 Service 中“影响行数为 0 就抛出业务异常”的判断。
- **正确做法：** 先用 `when(...).thenReturn(0)` 配置 Mapper，再在 `assertThrows()` 中调用 `orderService.cancelOrder()`。
- **规律：** 单元测试必须调用真正要验证的业务方法；Mock 只是为该方法准备依赖行为。

### 3. PowerShell 中文字符串乱码破坏脚本语法

- **现象：** `throw` 后的中文提示变成乱码并丢失结束双引号，随后 `-Dtest` 被报告为意外标记，同时出现缺少 `}` 和字符串终止符等连锁错误。
- **真实原因：** Windows PowerShell 读取脚本时的字符编码与文件编码不一致；后面的 Maven 行不是根因。
- **修复：** 将脚本运行提示改为 ASCII 英文，并使用 PowerShell Parser 在运行前检查语法。
- **规律：** 同一批多个解析错误应先修复最早出现的“字符串未结束”；后续括号、参数错误往往只是连锁反应。

### 4. Docker Desktop 未运行导致脚本无法取得 MySQL 镜像

- **现象：** `failed to connect to the docker API`，并提示找不到 `dockerDesktopLinuxEngine` 管道。
- **原因：** Docker 命令已安装，但 Docker Desktop 的 Linux Engine 尚未运行；安装了 CLI 不等于 Docker 后台服务正在运行。
- **修复：** 启动 Docker Desktop，等待 Engine 就绪，再运行 `./scripts/run-integration-tests.ps1`。
- **判断：** 这不是 Maven、MySQL 密码或 Java 测试失败；请求尚未到达容器和测试阶段。

## 2026-08-10 补充：Flyway、缓存与 Controller 测试

### 1. Flyway 迁移文件名只写一个下划线

- **练习写法：** `V2_add_product_description.sql`。
- **问题：** Flyway 版本迁移的标准命名需要版本和描述之间使用两个下划线。
- **正确写法：** `V2__add_product_description.sql`。
- **规律：** 文件名结构为 `V版本__描述.sql`；迁移未被发现时先检查目录和双下划线，不要直接怀疑 SQL。

### 2. 认为修改 `.env` 会自动修改已有 MySQL 密码

- **现象：** 修改 `.env` 中 `DB_PASSWORD` 后，Navicat 连接 Docker MySQL仍然出现 `Access denied`。
- **原因：** `.env` 只为容器提供配置；已有 MySQL 数据卷已经初始化过，修改环境变量不会自动重设数据库内部账号密码。
- **规律：** 区分“容器启动配置”和“数据库中已保存的真实账号状态”；修改密码后必须在 MySQL 内同步修改，或在明确允许丢弃数据时重新初始化数据卷。

### 3. 把 JSON 数字当成字符串断言

- **原写法：** `jsonPath("$.data.onSaleStockValue").value("990.00")`。
- **问题：** `BigDecimal` 默认序列化为 JSON 数字 `990.00`，不是带双引号的字符串。
- **修正：** 使用 `.value(990.00)`；修改后 `ProductControllerTest` 3 个测试全部通过。
- **规律：** 写 JSON 断言前先确认字段类型；数字、布尔值和字符串的断言值不能混用。

### 4. 看到 `BUILD FAILURE` 直接修改最靠近的测试代码

- **问题：** `BUILD FAILURE` 只是 Maven 的最终汇总，不能说明是断言、编译、数据库还是环境失败。
- **正确排查：** 先看 `Failures` 和 `Errors`，再向上找具体测试与异常类型，最后沿 `Caused by` 找到底层根因。
- **规律：** `Failures` 优先检查断言；`Errors` 优先检查运行异常、连接、Spring 上下文和 SQL；不要只根据最后一行猜原因。

### 5. Mockito 测试先写 `when()`，没有先看业务执行路线

- **问题：** 容易为根本不会执行的依赖准备数据，或遗漏方法真正会先调用的依赖，导致测试提前在错误分支结束。
- **正确方法：** 先用中文写出“输入 → 参数校验 → 查询用户 → 查询商品 → 扣库存 → 保存订单”，再决定 `when`、`assert` 和 `verify`。
- **规律：** 经过且需要返回值的依赖写 `when()`；最终结果写 `assert`；不应执行的依赖写 `verify(..., never())`。
## 2026-08-12：环境与数据库概念修正

### 1. 把 Docker 等同于生产环境

- **误区：** 只要项目使用 Docker 运行，就认为属于生产环境。
- **修正：** Docker 是运行和交付工具；在个人电脑上用于开发、调试的容器仍然属于开发环境。生产环境的核心判断是系统是否正式为真实用户提供服务。
- **准确表达：** 当前完成的是本地容器化部署，不是生产部署。

### 2. 把端口映射理解成容器之间通信的必要条件

- **误区：** MySQL 必须配置 `3307:3306`，Spring Boot 容器才能访问它。
- **修正：** 同一 Compose 网络中的容器通过 `mysql:3306` 直接通信，不需要宿主机端口映射；Windows 上的 IDEA 或 Navicat 访问容器时才使用 `localhost:3307`。
- **规律：** `宿主机端口:容器端口`，左边可按未占用端口调整，右边是服务在容器内监听的端口。

### 3. 混淆 Terminal 与 PowerShell

- **误区：** 把 Terminal 和 PowerShell 当成两个同类程序。
- **修正：** Terminal 是输入和显示命令的窗口；PowerShell 是运行在 Terminal 中解析命令的 Shell。IDEA Terminal 可以配置 PowerShell、CMD、Git Bash 等不同 Shell。
- **当前约定：** 项目命令统一使用 IDEA Terminal + PowerShell，避免混用 `$env:`、`export`、`set` 等语法。

### 4. 对生产日志原因表达不准确

- **原回答：** 生产环境更复杂，因此不打印详细 SQL。
- **更准确原因：** 生产请求量大，详细 SQL 会造成大量日志、增加性能和排错负担，并可能暴露敏感参数；开发环境则需要详细信息定位问题。
## 2026-08-13：请求校验与项目表达修正

### 1. 把 DTO 校验误说成 VO 校验

- **错误表达：** JSON 转成 DTO 后，在 VO 中执行校验注解。
- **正确流程：** `@RequestBody` 把 JSON 转成 Request DTO，`@Valid` 触发 DTO 字段上的校验；VO 只负责组织返回给前端的数据。
- **固定区分：** DTO 接收请求，Entity 对应数据库，VO 面向响应，`ApiResponse` 包装统一格式。

### 2. 不理解校验失败时为何验证 Service 无调用

- `@Valid` 校验发生在 Controller 方法体执行前。
- 校验失败后直接返回 400，不会调用 Service 和 Mapper。
- `verifyNoInteractions(productService)` 用于证明执行路线确实在校验阶段停止，而不是只检查最终状态码。

### 3. 项目介绍中出现能力夸大和术语口误

- 把“角色权限”说成“团队权限”，把“SQL 影响行数”说成“异常函数”，把“本地容器化运行”说成“集群运行”。
- 面试表达必须使用真实术语：条件原子 `UPDATE`、影响行数判断、事务、Redis 缓存穿透标记、Flyway 迁移、本地 Docker Compose。
- 当前项目没有生产集群，不使用“稳定集群运行”等表述。
## 2026-08-14：异常响应与日志信息边界

### 不清楚为什么不能把未知异常原文直接返回前端

- **薄弱点：** 首次接触兜底异常处理时，不清楚为什么不能直接返回 `exception.getMessage()`。
- **风险：** 未知异常信息可能包含 SQL、数据库表和字段、服务器路径、账号或第三方服务地址，把它返回前端会暴露内部实现。
- **正确处理：** 服务端使用 `log.error(..., exception)` 保存真实异常和堆栈；客户端只接收统一的 `500` 与“服务器内部错误”。
- **规律：** 日志用于开发者排查，响应面向接口调用者；两者需要的信息不同，不能把完整内部异常当作接口响应。

## 2026-08-15：工程排错、项目表达与事务传播修正

### 1. Flyway 校验失败只停留在“版本不同”

- `Migration checksum mismatch for migration version 1` 的准确含义是：数据库保存的 V1 校验值与当前本地 V1 文件校验值不一致，通常是已执行的 V1 后来被修改。
- 第一处理动作是从 Git 恢复原始 V1；新的表结构变化写入后续迁移，不能随意改历史记录或删除数据卷掩盖问题。

### 2. 把新迁移缺失笼统说成“Flyway 没打包”

- Docker 数据库缺字段且 Flyway 只到 V2 时，应区分“Flyway 依赖不存在”和“当前 JAR/镜像中缺少 V3 迁移文件”。
- 排查顺序：文件目录和命名 → JAR 内容 → 镜像重建与容器重启 → Flyway 日志与历史表；不能先在 Navicat 手工改表。

### 3. 不理解修改 `.env` 为什么不会更新已有 MySQL 密码

- MySQL 显示 `healthy` 只说明服务可响应，不代表 Spring Boot 使用的凭据正确。
- 已有数据卷保存数据库内部真实账号状态；环境变量主要在空数据目录首次初始化时生效，重建镜像或容器不会覆盖已有密码。
- 需要保留数据时应恢复有效配置，或先进入 MySQL 修改密码再同步 `.env`，不能直接删除数据卷。

### 4. 项目表达混淆商品状态与订单状态

- 商品扣库存条件是 `stock >= quantity`、`status = 1` 且 `is_deleted = 0`。
- 取消订单的原子 SQL 修改的是订单状态 `1（已创建）→ 2（已取消）`；影响行数为 1 后，Service 才调用 `addStock()` 退库存。
- 第二次取消因订单状态已为 2，影响行数为 0，不会重复退库存。

### 5. 误以为外层 catch 可以清除事务回滚状态

- 当内层 `@Transactional(REQUIRED)` 方法抛出运行时异常并离开事务代理时，共享事务可能已被标记为 `rollback-only`。
- 外层捕获异常只能阻止 Java 异常继续传播，不能清除回滚标记；外层结束尝试提交时仍会回滚，并可能出现 `UnexpectedRollbackException`。
- 事务核心流程不要随意吞掉异常；业务失败时通常继续抛出，让事务边界明确处理回滚。

## 2026-08-16：事务原因、Flyway 根因与项目表达修正

### 1. 把不同线程误说成“没有 Spring 代理”

- 同类内部调用事务注解可能失效，是因为调用没有经过 Spring 代理。
- 新线程中调用另一个 Spring Bean 时仍可能经过代理；真正的问题是 Spring 本地事务绑定线程，新线程不能继承原线程事务。
- 新线程里的默认 `REQUIRED` 会发现当前线程没有事务并创建独立事务，而不是加入原事务。

### 2. 误以为 REQUIRES_NEW 会与外层一起回滚

- `REQUIRES_NEW` 会暂停外层事务并开启独立事务；内层正常结束后已经单独提交。
- 如果外层订单随后失败，外层回滚不会撤销已经提交的库存事务，可能造成库存减少但订单不存在。
- 当前订单创建应使用默认 `REQUIRED`，让扣库存、流水和订单保存共享事务。

### 3. 日志只停留在 FlywayMigrateException

- `FlywayMigrateException` 是外层类型；本次最底层根因为 MySQL `1060 Duplicate column name 'description'`。
- 数据库已有字段而历史表没有 V3 成功记录时，Flyway仍会尝试执行 V3，反映数据库真实结构和迁移历史不一致。
- 排查应继续向最底层 `Caused by`、数据库错误码、`Message`、迁移文件和行号读取。

### 4. 一分钟项目表达出现真实含义错误

- 技术名应为 `Java 17 + MyBatis-Plus`，不能说成 `MyBatis-Java`。
- 扣库存条件应准确说成 `stock >= quantity`、`status = 1`、`is_deleted = 0`，不能说成同时处于删除和未删除状态。
- 回滚只在异常时发生，不属于订单正常创建步骤。
- Dockerfile构建应用镜像，Docker Compose编排应用、MySQL和Redis；不能表述为“把数据库打包”。

## 2026-08-17：分页测试数据与索引选择

### 1. 把分页查询返回值理解成单个订单

- `sysOrderMapper.selectPage(...)` 的返回类型是 `IPage<SysOrder>`，不能让 Mock 直接返回 `SysOrder`。
- 分页对象负责保存 `total/current/size/records`，具体订单放在 `records` 中；Service 再把 `List<SysOrder>` 转换为 `List<OrderVO>`。
- 设计测试前先沿真实方法记录每次调用的返回类型，再决定需要创建哪些假对象，不能只根据业务名称猜返回值。

### 2. 因为 id 是主键就优先选择 id 索引

- SQL 使用哪个索引取决于实际过滤、连接和排序条件，不取决于某个字段是否“看起来重要”。
- `WHERE total_price > 1000 ORDER BY total_price DESC` 没有使用 `id`，主键索引不能直接帮助按价格筛选和排序。
- 如果查询增加 `status = 1` 且长期高频、数据量足够大，候选联合索引应从等值条件和后续范围/排序字段出发考虑 `(status, total_price)`，并在添加前用真实数据和执行计划验证。

## 2026-08-18：测试数据自洽与事务日志

### 1. 空分页中仍放入一条空订单

- 测试目标是“用户存在但没有订单”，但首次创建 `SysOrder` 并执行 `orderPage.setRecords(List.of(order))`，导致 `total=0` 与 `records.size()=1` 自相矛盾。
- 空结果应使用有效空集合 `List.of()`，而不是创建字段全为空的订单对象。
- 设计测试数据后先用一句业务事实检查自洽性：总数、当前页记录数和准备的对象数量必须表达同一个场景。

### 2. 排错动作停留在“检查并重启”

- 面对缺少 `JWT_SECRET` 的真实日志，能够找到最底层根因，但第一处理动作只表述为“检查 JWT 密钥”，验证方式只说“重新启动”。
- 可执行动作必须说明配置加在哪里：IDEA 应提供环境变量，`@SpringBootTest` 应提供专用测试属性。
- 验证应重新运行原失败测试并检查上下文成功加载、`Errors: 0` 和 `BUILD SUCCESS`；不改变配置的重启不会解决问题。
