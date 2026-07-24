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
