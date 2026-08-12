# Java 与算法刷题复盘

更新时间：2026-07-30

用途：单独记录 Java/力扣刷题中真实遇到的问题，包括题意理解、解题思路、Java 语法、数据结构选择、边界条件和复杂度分析。Spring Boot、MySQL、Redis、Docker、Git 等项目问题继续记录在 `LEARNING_ERRORS.md`。

## 记录原则

- 只记录真实做过并遇到困难的题目，不提前堆答案。
- 先保存自己的原始思路和错误代码，再记录修正方法。
- 优先理解暴力解法，再学习优化解法。
- 不背整段答案，重点记录“为什么使用这种数据结构”。
- 同一道题再次出错时补充原条目，不重复创建。
- 每题至少能够说明输入输出、核心思路和时间复杂度。

## 建议记录格式

### 题目：名称或编号

日期：

题目要求：

自己的最初思路：

遇到的问题：

错误代码或错误表现：

正确思路：

关键 Java 知识：

时间复杂度与空间复杂度：

下次重做结果：

## 已记录问题

### 题目：1. 两数之和（暴力解法重做）

日期：2026-07-24

题目要求：在整数数组中找到两个元素，使它们的和等于 `target`，返回这两个元素的下标。

自己的最初思路：使用两层 for 循环，外层选择第一个下标 `i`，内层从 `i + 1` 开始选择第二个下标 `j`，避免同一个元素被使用两次。

遇到的问题：

- 把数组变量 `nums` 当成数组长度使用，写成 `i < nums` 和 `j < nums`。
- 在 `new int[]{i, j}` 中误输入中文逗号 `，`。
- 对 `return new int[]{i, j}` 和 `return new int[0]` 的含义不清楚，尚未稳定区分“返回数组下标”和“返回数组中的数字”。

错误表现：数组不能直接与整数比较；Java 源码不能使用中文逗号分隔数组元素。

正确思路：

```text
i 从 0 遍历到 nums.length - 1
→ j 从 i + 1 遍历到 nums.length - 1
→ 如果 nums[i] + nums[j] == target，返回 i 和 j
→ 全部遍历后仍未找到，返回空数组
```

关键 Java 知识：

- 数组长度使用 `nums.length`，数组本身是 `int[]`，不能与整数比较。
- Java 代码中的逗号、分号、括号必须使用英文标点。
- `new int[]{i, j}` 用于直接创建并返回包含两个下标的数组。
- `return` 会把结果交给调用者并立即结束整个方法；`new int[0]` 表示长度为 0 的空数组，是编译所需的兜底返回。

时间复杂度与空间复杂度：

- 时间复杂度：`O(n²)`，最坏情况下需要比较接近 `n × n / 2` 对元素。
- 额外空间复杂度：`O(1)`，没有创建随输入规模增长的集合。

下次重做结果：修正 `nums.length` 和英文逗号后运行成功。

### 题目：283. 移动零

日期：2026-07-24

题目要求：原地把所有 `0` 移动到数组末尾，同时保持非零元素的相对顺序，不能复制一个完整的新数组。

自己的最初思路：使用 `writeIndex` 记录下一个非零元素应该写入的位置；第一次遍历把非零元素写到前面，第二次遍历把剩余位置填成 `0`。

遇到的问题：

- 不理解返回类型为 `void` 时力扣如何取得结果。
- 第二个循环让 `i` 遍历整个数组，却访问和递增 `nums[writeIndex]`，存在 `writeIndex == nums.length` 后数组越界的风险。
- 修正后在第二个循环中保留了不必要的 `if (nums[i] != 0)`。
- 不清楚两个前后循环的时间复杂度为什么仍然是 `O(n)`。

正确思路：

```text
writeIndex 从 0 开始
→ 第一次遍历：遇到非零元素就写入 nums[writeIndex]，然后 writeIndex 加 1
→ 第二次从 writeIndex 遍历到数组末尾，直接把每个位置写成 0
```

关键 Java 知识：

- `void` 方法不返回新数组，但可以直接修改调用者传入的原数组；力扣检查修改后的 `nums`。
- 第二次循环应使用 `for (int i = writeIndex; i < nums.length; i++)`，访问 `nums[i]`。
- 剩余位置必须全部变成 `0`，不需要先判断当前位置是不是非零。
- 在 IDEA 中需要观察结果时，可在调用方法后使用 `Arrays.toString(nums)` 打印原数组。

时间复杂度与空间复杂度：

- 两个循环前后执行，最多为 `n + n = 2n`，忽略常数后是 `O(n)`，不是 `O(n²)`。
- 只有循环变量和 `writeIndex`，额外空间复杂度为 `O(1)`。

下次重做结果：已完成正确的两次遍历写法；后续重做时应独立写出第二个填零循环，并能解释 `void` 和 `O(n)`。

### Java 基础：统计数组中的正数

日期：2026-07-25

练习要求：编写 `countPositive(int[] nums)`，统计数组中大于 0 的元素数量，并通过 `return` 返回结果。

自己的最初思路：使用 `count` 从 0 开始遍历数组，遇到正数就执行 `count++`，最后 `return count`。

遇到的问题：

- 核心统计方法能够独立写对，但类名最初写成小驼峰 `countPositive`，不符合 Java 类名规范。
- 提前写了类的结束大括号，导致 `main()` 方法位于类外。
- IDEA 项目出现“源发行版 21 与 `--enable-preview` 一起使用时无效”，属于 JDK、语言级别和预览参数冲突，不是业务代码错误。

正确结构：

```text
CountPositive 类 {
    countPositive 方法
    main 方法
}
```

关键 Java 知识：

- 类名使用大驼峰 `CountPositive`，方法名使用小驼峰 `countPositive`，公开类名应与文件名一致。
- Java 17 中方法必须位于类的大括号内部。
- `return count` 把方法计算出的整数交给调用者；调用者可以用 `int result` 接收。
- `System.out.println(result)` 只负责把结果显示在控制台。
- 普通基础代码不需要 `--enable-preview`；项目 SDK、Language level 和字节码版本应保持一致。

时间复杂度与空间复杂度：

- 遍历数组一次，时间复杂度为 `O(n)`。
- 只使用 `count` 和循环变量，额外空间复杂度为 `O(1)`。

下次重做结果：修正类结构和 Java 版本配置后运行成功，控制台输出 `2`，进程退出码为 `0`。

### Java 基础：ArrayList、HashMap 与集合遍历

日期：2026-07-25

练习内容：

- 使用 `ArrayList<Integer>` 筛选并保存数组中的正数，运行得到 `[3, 5]`。
- 使用 `HashMap<Integer, Integer>` 统计数组 `{1, 2, 1, 3, 2, 1}` 中每个数字出现的次数。
- 遍历 `Map.Entry` 找出出现次数最多的数字。

最初不理解的内容：

- `ArrayList` 的含义以及它和固定长度数组的区别。
- `put()`、`get()`、`containsKey()` 分别做什么。
- 增强 for 循环 `for (int num : nums)` 如何取得数组元素。
- `for (Map.Entry<Integer, Integer> entry : counts.entrySet())` 如何逐项取得 Map 的键和值。

已经理解：

- `ArrayList` 是底层使用数组、长度可以动态增长的列表，`add()` 用于添加元素。
- 当前统计 Map 保存的是“数字 → 出现次数”；`put(key, value)` 会新增或更新数据，`get(key)` 取得对应的值。
- 增强 for 循环每次取得一个数组元素；遍历 `entrySet()` 时每次取得一组键和值。
- `entry.getKey()` 得到数字，`entry.getValue()` 得到出现次数。
- 使用 `maxCount` 保存当前最大次数，同时用 `mostFrequentNumber` 保存对应数字。

运行结果：

```text
{1=3, 2=2, 3=1}
1出现了3次
2出现了2次
3出现了1次
出现最多次的数字是：1
```

复杂度：

- 统计数组和遍历 Map 总时间复杂度为 `O(n)`。
- Map 最多保存所有不同数字，额外空间复杂度为 `O(k)`，`k` 是不同数字的数量。

### 题目：1. 两数之和（HashMap 解法重新理解）

日期：2026-07-25

本次进展：在理解 `HashMap` 的 `put()`、`get()` 和 `containsKey()` 后，已经能够独立重新写出 HashMap 解法，不再只是复制模板。

核心思路：

```text
Map 保存“已经遍历过的数字 → 该数字的下标”
→ 计算 complement = target - nums[i]
→ 如果 Map 中已有 complement，返回补数旧下标和当前下标
→ 如果没有，保存当前数字和当前下标
```

关键理解：

- `complement` 表示当前数字距离目标值还差多少，即“补数”。
- `map.get(complement)` 取得补数以前出现时的下标。
- 必须先查询再执行 `map.put(nums[i], i)`，避免当前元素与自己配对。
- `return new int[]{previousIndex, i}` 返回的是两个数组下标。

验证数据：

```text
nums = [3, 2, 4]
target = 6
输出下标：1、2
```

复杂度：

- 平均时间复杂度：`O(n)`。
- 额外空间复杂度：`O(n)`。

下次复习要求：不要求背变量名，但要能独立说明“计算补数 → 查询以前是否出现 → 返回下标 → 未找到则保存当前数字”的顺序。

### Java 基础：对象、字符串比较、null 与异常

日期：2026-07-25 至 2026-07-26

练习内容：

- 使用 `ProductItem` 复习构造方法、成员变量、Getter 和成员方法。
- 从 `List<ProductItem>` 中按照商品名称查找对象。
- 编写扣库存方法，并对非法数量和库存不足主动抛出异常。
- 使用 `try-catch` 捕获库存不足异常。

实际遇到的问题：

- 查找商品时写成 `product.equals(targetName)`，把完整的 `ProductItem` 对象与 `String` 比较。
- 查询返回 `null` 后如果直接调用 `result.getStock()`，会发生 `NullPointerException`。
- 对 `catch` 中应该填写哪一种异常类型不确定。

正确理解：

- 比较商品名称应该使用 `product.getName().equals(targetName)`；比较的是同一含义、同一类型的数据。
- 字符串内容使用 `equals()`，两个可能为 `null` 的对象使用 `Objects.equals()`。
- 方法可能返回 `null` 时，调用 Getter 前必须判空，或者由业务层改为抛出明确异常。
- `throw` 会立即终止当前方法；未处理异常会让程序以非零退出码结束。
- `catch` 应捕获实际抛出的异常类型或它的父类；多个 `catch` 中子类写在父类前面。
- `IllegalArgumentException` 表示参数不合法，`IllegalStateException` 表示对象当前状态不允许操作，两者都继承 `RuntimeException`。

运行验证：

```text
正常扣减：库存从15变成12，退出码0
超量扣减：抛出IllegalStateException: 库存不足，未捕获时退出码1
```

当前掌握程度：构造方法和 Getter 属于已有知识，不再按零基础反复训练；后续重点放在对象字段选择、null 安全、集合组合使用和独立组织完整业务。

### Java 基础：Map 汇总、Entry 遍历与对象列表排序

日期：2026-07-28

练习内容：

- 根据两个同长度数组，把多条购买记录汇总为“商品 ID → 累计购买数量”。
- 遍历 `Map.Entry<Integer, Integer>` 找出购买总数量最多的商品 ID。
- 从 `Map` 中筛选累计数量达到阈值的商品 ID。
- 从 `List<ProductItem>` 中筛选低库存商品名称。
- 使用 Lambda 与 `Integer.compare()` 对商品对象按库存升序、降序排序。

实际遇到的问题：

- 第一次汇总时只把 `getOrDefault(productId, 0)` 放回 Map，没有加上当前 `quantities[i]`，导致每个商品的累计数量都会保持为 0。
- 需求是“大于等于阈值”，最初写成 `quantity > threshold`，遗漏刚好等于阈值的数据。
- 不理解 `entry` 的含义；现已明确 `Map.Entry` 表示 Map 中的一组 key 和 value，`entrySet()` 表示全部键值对组成的集合。
- 对交换 `Integer.compare()` 两个参数为什么能改变升序和降序不清楚。

正确理解：

```text
累计值 = Map中以前的值 + 当前数量
entry.getKey() = 商品ID
entry.getValue() = 累计购买数量
Integer.compare(product1库存, product2库存) = 升序
Integer.compare(product2库存, product1库存) = 降序
```

运行结果：

```text
商品数量汇总：{4=3, 38=7, 39=2}
购买数量最多的商品ID：38
达到阈值的商品ID：[4, 38]
低库存商品：[机械键盘, 显示器]
升序：显示器2、机械键盘3、苹果手机10
降序：苹果手机10、机械键盘3、显示器2
```

当前掌握程度：

- 能独立写基础 Map 汇总、`entrySet()` 遍历、List 筛选和 Lambda 排序。
- 仍需在提交前主动核对 `>`、`>=`、`<`、`<=` 等边界条件。
- 下一次练习优先要求从方法签名独立组织完整方法，不重复逐行填空。

### Java 基础：对象列表生成 Map 并应用补货计划

日期：2026-07-29

练习内容：

- 从 `List<ProductItem>` 生成“商品名称 → 需要补充数量”的 `Map<String, Integer>`。
- 遍历商品对象，使用补货 Map 修改原对象库存。
- 使用 IDEA Rename 重构统一修正变量名。

独立完成的核心流程：

```text
遍历商品
→ 库存低于目标值
→ 需要补充数量 = 目标库存 - 当前库存
→ Map保存商品名称和补充数量

再次遍历商品
→ containsKey判断是否需要补货
→ get取得补货数量
→ 调用addStock修改商品对象
```

实际问题与理解：

- 变量 `product` 最初拼写为 `prodcut`，不影响编译，但降低可读性；已学习使用 `Shift + F6` 安全重命名。
- `Map.get(key)` 根据商品名称取得补货数量，调用前先用 `containsKey(key)` 判断，可以避免不存在的 key 返回 `null` 后拆箱出错。
- `applyRestockPlan()` 使用 `void`，因为它直接修改列表中已有 `ProductItem` 对象，不需要返回新列表。
- `HashMap` 不保证遍历和打印顺序，`{显示器=3, 机械键盘=2}` 与相反顺序表达的是同一组键值数据。

运行结果：

```text
{显示器=3, 机械键盘=2}
机械键盘：5
显示器：5
苹果手机：10
```

当前掌握程度：已经能从方法签名独立组织 `List + 对象 + Map + 方法调用` 的完整小功能，不再只是逐行填空。

### Java 基础：返回最低库存对象与 null 边界

日期：2026-07-30

练习要求：从 `List<ProductItem>` 中返回库存最低的完整商品对象，同时安全处理 `null` 列表、空列表、列表中的 `null` 元素以及全部元素为 `null`。

最初问题：

- 方法返回类型为 `ProductItem`，却使用 `int lowestStock` 保存结果并尝试返回，混淆了“最低库存数字”和“具有最低库存的商品对象”。
- 使用 `999999999` 作为初始最小值比较基准，属于不安全的魔法数字。
- 第一次修改后仍未跳过 `null` 元素，执行 `product.getStock()` 会出现 `NullPointerException`。

正确思路：

```text
列表为null或空 → 返回null
lowest初始为null
遍历商品 → 当前元素为null则continue
lowest为null或当前库存更少 → lowest指向当前商品
循环结束 → 返回lowest
```

关键 Java 知识：

- 返回类型是 `ProductItem`，就需要保存并返回对象引用；调用者随后可以取得名称、库存等多个字段。
- `continue` 结束当前这一轮循环，直接处理下一个元素。
- `lowest == null || ...` 使用短路判断，第一次找到非空商品时不会调用 `lowest.getStock()`。
- `List.of()` 不允许 `null`；需要构造包含 `null` 的测试列表时可使用 `Arrays.asList()`。

运行结果：

```text
显示器：2
null
```

当前掌握程度：能够在提示后修正返回类型和空值边界；下次遇到“返回对象还是返回字段”时，需要先核对方法签名再选择临时变量类型。

### Java 基础：HashSet 中自定义对象的相等规则

日期：2026-07-30

本次理解：

- 两个字段内容相同但分别通过 `new` 创建的 `ProductItem`，在没有重写 `equals()` 和 `hashCode()` 时仍是两个不同对象，加入 `HashSet` 后大小为 2。
- `HashSet` 先利用 `hashCode()` 进行哈希定位，再使用 `equals()` 判断对象是否相等。
- `String`、`Integer` 等标准类型已经提供内容相等规则，自定义类需要根据业务身份决定是否重写。
- 相等对象必须返回相同的 hashCode。
- 不应轻易把会变化的 `stock` 作为哈希身份字段；对象加入 `HashSet` 后修改参与 hashCode 的字段，可能导致集合无法正常查找该对象。

下一步练习：在独立 `ProductItem` 练习类中，根据明确业务规则生成 `equals()` 和 `hashCode()`，验证第二次添加相同身份商品时 `HashSet.add()` 返回 `false`。

### Java 基础：两级排序、包装类型与金额比较

日期：2026-07-31

本次学习内容：

- 使用 Comparator 对商品先按库存升序排序，库存相同时再按商品名称排序。
- 比较器返回负数表示第一个对象排在前面，返回正数表示第二个对象排在前面，返回 0 表示当前比较条件相同。
- `Integer.compare(product1.getStock(), product2.getStock())` 表示按库存升序比较。
- 第一层库存比较结果不为 0 时直接返回；只有库存相同时，才执行 `product1.getName().compareTo(product2.getName())`。
- `Long`、`Integer`、`BigDecimal` 都是对象类型；业务 ID 比较不能依赖 `==`，金额大小不能依赖普通算术运算。
- `BigDecimal.compareTo(BigDecimal.ZERO) < 0` 表示当前金额小于 0。
- `final` 限制引用变量重新赋值，不代表引用对象的内容完全不可修改。

当前掌握程度：

- 能在具体数据示例下理解两级排序过程，但暂时不能要求脱离模板独立写出完整 Comparator。
- 能读懂常见 `compareTo()` 大小判断，并能在提示下写金额范围校验。
- 后续优先在真实业务中继续使用这些写法，不单独反复背诵语法。

### Java 基础：使用 Map 与 BigDecimal 计算订单总价

日期：2026-07-31

练习要求：根据“商品名称到价格”的 `Map<String, BigDecimal>` 和“商品名称到购买数量”的 `Map<String, Integer>` 计算订单总价。

独立完成部分：

- 能创建 `BigDecimal total`，遍历 Map，根据商品名称取得单价和数量，使用 `multiply()` 计算小计，并用 `add()` 累加总价。
- 正常数据运行结果为 `3495.00`。
- 能补充价格缺失和数量不合法的异常判断，并在提示后加入 `quantity == null` 防止自动拆箱空指针。

本次修正：

- 最初遍历 `prices.keySet()`；当价格表存在未购买商品时，数量 Map 返回 `null`。应遍历订单实际购买内容 `quantities`。
- 初始金额推荐使用 `BigDecimal.ZERO`，整数数量使用 `BigDecimal.valueOf(quantity)` 转换。
- `quantity == null || quantity <= 0` 利用短路判断，左侧为真时不会执行右侧拆箱比较。

金额计算规则：

- 不使用 `new BigDecimal(0.1)`，因为 `double` 的二进制近似值会产生很长的小数；使用字符串构造或 `BigDecimal.valueOf()`。
- 除法必须明确小数位数和 `RoundingMode`，例如保留两位并使用 `HALF_UP`。
- `BigDecimal` 是不可变对象，`add()` 和 `multiply()` 返回新对象，因此必须接收返回值，例如 `total = total.add(productTotal)`。

当前掌握程度：能够从方法签名独立完成正常订单金额汇总，在提示后补齐业务遍历对象、空值和金额精度边界；仍需在写代码前先判断“应该遍历哪一份业务数据”。

### Java 基础：枚举状态查找

- 已理解 `values()` 是 Java 为枚举自动提供的静态方法，返回该枚举的全部值。
- 能结合 `OrderStatus.getText(2)` 说明增强 `for` 循环先检查 `CREATED`，再检查 `CANCELLED`，匹配后 `return` 立即结束方法。
- 使用 `Objects.equals()` 比较状态码，可安全处理传入 code 为 `null` 的情况。
- 已参照 `OrderStatus` 自己完成 `ProductStatus`，但首次把下架 code 错写为 `2`；后续定义枚举前必须先核对真实业务编码。

### Java 基础：手动分页下标与 `subList()`

日期：2026-08-01

本次学习内容：

- 页码从 1 开始时，当前页起始下标为 `fromIndex = (pageNum - 1) * pageSize`。
- 结束下标使用 `toIndex = Math.min(fromIndex + pageSize, products.size())`，避免最后一页越过列表长度。
- `products.subList(fromIndex, toIndex)` 左闭右开：包含 `fromIndex`，不包含 `toIndex`。
- `fromIndex >= products.size()` 时应返回空列表；否则直接调用 `subList()` 可能出现下标越界。
- 返回 `new ArrayList<>(products.subList(...))` 可得到独立列表，避免返回值继续作为原列表的视图使用。

掌握边界：能够理解并使用分页公式，但不需要死背整个方法；重点是知道“跳过前面多少条”和“结束位置不能超过总数”。

### Java 基础：使用 `Deque` 检查有效括号

日期：2026-08-01

本次学习内容：

- `Deque` 是 Double Ended Queue（双端队列），本题使用 `ArrayDeque<Character>` 把它当作栈。
- 栈遵循 LIFO（Last In, First Out，后进先出）；`push()` 放入栈顶，`pop()` 取出并删除栈顶，`isEmpty()` 判断是否为空。
- 遇到 `(`、`[`、`{` 时压栈；遇到右括号时，先检查栈是否为空，再弹出最近的左括号进行配对。
- `char left = stack.pop()` 中的 `left` 是自己定义的局部变量，保存从栈顶取出的左括号；`current` 是增强 `for` 循环当前读到的字符。
- 已独立补出三种不匹配判断：`)` 必须对应 `(`、`]` 必须对应 `[`、`}` 必须对应 `{`。
- 循环结束必须返回 `stack.isEmpty()`；例如 `"((("` 没有遇到错误右括号，但仍有未匹配左括号，因此结果应为 `false`。

运行结果：`"()[]{}"`、`"([)]"`、`"({[]})"` 和 `"((("` 等示例已验证成功。

当前掌握程度：能够根据具体输入解释压栈、出栈和括号配对，并在模板中完成核心判断；暂时未达到从空白稳定写出完整方法，后续再独立重写一次即可验收。

### Java 栈复验：有效括号与删除相邻重复项

日期：2026-08-02

复验结果：

- 已从空白独立写出有效括号完整方法，不再只是补充三组 `if` 判断。
- 能正确使用 `text.toCharArray()` 和增强 `for` 逐字符遍历。
- 5 组括号输入运行结果均符合预期，掌握程度升级为“能够独立完成基础版本”。
- 新完成删除相邻重复项：当栈不为空且 `current == stack.peek()` 时 `pop()`，否则 `push(current)`。
- 理解 `peek()` 只查看栈顶而不删除，`pop()` 会取得并删除栈顶；使用 `!stack.isEmpty() && ...` 的短路判断避免空栈取值问题。
- 使用 `StringBuilder` 与 `stack.removeLast()` 从栈底到栈顶恢复原字符串顺序，避免结果反转。

复杂度：

```text
有效括号：时间 O(n)，空间 O(n)
删除相邻重复项：时间 O(n)，空间 O(n)
```

当前掌握程度：已能独立使用栈解决两道简单题，并解释核心 API 和复杂度；下一步不再重复同类填空，转向数组、Map 或 Set 的独立题目。

### Java Map：有效的字母异位词

日期：2026-08-03

- 使用 `Map<Character, Integer>` 统计字符次数：遍历第一个字符串时加 1，遍历第二个字符串时减 1。
- 首次错误使用 `map.get(i)` 检查结果；`i` 是整数下标，而 Map 的 key 是 `Character`，两者不是同一种业务含义。
- 正确检查方式是遍历 `map.values()`，只要存在不为 0 的次数，就说明两个字符串不是字母异位词。
- 方法开头应先比较字符串长度；长度不同可以直接返回 `false`。
- 多组输入结果为 `true、false、false、true`，时间复杂度 `O(n)`，空间复杂度 `O(n)`。

### Java Map：多数元素

日期：2026-08-03

- 使用 `HashMap<Integer, Integer>` 累计每个数字出现的次数，每次更新后检查当前次数。
- 首次错误写成 `map.get(num) > nums.length`；任何元素的出现次数都不可能大于数组总长度。
- 多数元素定义为出现次数严格大于数组长度的一半，因此条件应为：

```java
map.get(num) > nums.length / 2
```

- 三组输入得到 `3、2、5`，HashMap 解法时间复杂度 `O(n)`，空间复杂度 `O(n)`。
- 当前阶段先掌握计数解法；Boyer-Moore 的 `O(1)` 空间解法属于后续优化，不要求现在记忆。
## 2026-08-03：Map 键类型与多数元素阈值

### 1. 把 Map 当成数组按数字下标读取

错误写法出现在字母异位词检查中：

```java
for (int i = 0; i < map.size(); i++) {
    if (map.get(i) != 0) {
        return false;
    }
}
```

`map` 的类型是 `Map<Character, Integer>`，键是字符，不是从 `0` 开始的连续数字。`map.get(i)` 使用的是整数键，因此不能正确读取字符计数。

检查所有次数应该遍历值：

```java
for (int count : map.values()) {
    if (count != 0) {
        return false;
    }
}
```

### 2. 多数元素判断阈值写成数组长度

- **错误条件：** `map.get(num) > nums.length`
- **问题：** 单个数字的出现次数不可能大于整个数组长度，因此条件永远不会成立。
- **正确条件：** `map.get(num) > nums.length / 2`
- **原因：** 多数元素的定义是出现次数超过数组长度的一半。

### 3. 本轮复杂度

- 字母异位词 Map 解法：时间复杂度 `O(n)`，空间复杂度 `O(n)`。
- 多数元素 Map 解法：时间复杂度 `O(n)`，空间复杂度 `O(n)`。
## 2026-08-04 刷题状态

- 本日主要完成 Service 测试、前后端联调、MySQL 索引和事务锁学习，没有新增已经运行验证的算法题。
- 已布置“删除有序数组中的重复项”，目标是复用移动零题中的双指针思想；尚未编写和运行，因此不能标记为已掌握。
- 下一次从方法签名 `removeDuplicates(int[] nums)` 开始独立编写，验收包括空数组或最小输入、连续重复和全部不同三类数据，以及时间/空间复杂度。

## 2026-08-04：双指针、回文与同构字符串

### 删除有序数组重复项

- 已完成 `removeDuplicates(int[] nums)`，三组输入分别得到有效元素 `1 2 3`、`1 2 3` 和 `2`。
- `readIndex` 用于逐个读取，`writeIndex` 表示下一个有效元素的写入位置；两者都是自行命名的局部变量。
- 时间复杂度 `O(n)`，空间复杂度 `O(1)`。

### 删除指定元素

- 核心判断 `nums[readIndex] != target` 正确，但首次遗漏 `writeIndex++`。
- 如果写入后不增加 `writeIndex`，所有有效元素会反复覆盖同一个位置。
- 修正后输入 `[3, 2, 2, 3]`、目标值 `3`，得到有效长度 `2`，有效元素为 `2、2`。

### 移动零

- 独立写出两阶段逻辑：先把所有非零元素写到数组前部，再把 `[writeIndex, nums.length)` 填成 0。
- 两个循环先后执行而非嵌套，因此时间复杂度仍为 `O(n)`；原地修改使空间复杂度为 `O(1)`。

### 验证回文字符串

- 使用左右双指针检查字符串，借助 `Character.isLetterOrDigit()` 跳过无效字符，使用 `Character.toLowerCase()` 忽略大小写。
- 首次在移动无效字符指针后缺少 `continue`，导致仍可能在当前轮比较标点或连续无效字符。
- 修正为移动指针后立即 `continue`，两组输入运行成功。
- 时间复杂度 `O(n)`，空间复杂度 `O(1)`。

### 同构字符串

- 使用正向 Map 和反向 Map 保证字符之间是一一对应关系。
- 能通过 `egg/add` 说明重复字符保持同一映射，通过 `ab/aa` 说明为什么必须检查反向映射。
- 四组测试全部通过，时间复杂度 `O(n)`，空间复杂度 `O(n)`。

### 本轮规律

- 双指针题写完赋值后，要检查写指针是否需要移动。
- 跳过无效字符时，要明确当前循环是否应该立即结束；需要重新判断新位置时使用 `continue`。
- Map 映射题先用中文确定关系是“单向”还是“一一对应”；一一对应通常需要同时验证两个方向。

## 2026-08-07：二分查找与最长公共前缀

### 二分查找

- 从空白写出 `binarySearch(int[] nums, int target)`，使用闭区间 `[left, right]`，因此循环条件是 `left <= right`。
- 找到目标返回 `mid`；目标更小时执行 `right = mid - 1`，目标更大时执行 `left = mid + 1`。
- 使用 `mid = left + (right - left) / 2`，避免直接相加在极大下标下可能产生整数溢出。
- 三组输入输出为 `3、0、-1`，时间复杂度 `O(log n)`，额外空间复杂度 `O(1)`。
- 使用前提是数组已经有序；无序数组不能直接套用二分查找。

### 最长公共前缀

- 把第一个字符串作为候选 `prefix`，从第二个字符串开始逐个检查 `startsWith(prefix)`。
- 不匹配时通过 `prefix.substring(0, prefix.length() - 1)` 删除候选前缀的最后一个字符，直到当前单词匹配。
- 首次只检查 `words.length == 0`；健壮写法应先使用短路判断 `words == null || words.length == 0`。
- 已运行共同前缀、没有共同前缀和完全相同三类输入。
- 核心规律：不是删除原字符串内容，而是在不断缩短“候选答案”，最终留下所有字符串都具有的前缀。

### 合并两个有序数组

- 使用 `index1 = m - 1`、`index2 = n - 1` 和 `writeIndex = m + n - 1` 从数组末尾开始比较和写入。
- 核心循环能够独立完成：把较大的当前元素写入 `nums1[writeIndex]`，随后移动对应读取指针和写指针。
- 主循环结束后只需要继续复制 `nums2` 的剩余元素；`nums1` 的剩余元素原本就在正确位置，不需要重复移动。
- 三组输入运行成功，时间复杂度 `O(m+n)`，额外空间复杂度 `O(1)`。

### 最大子数组和

- 使用 `currentSum = Math.max(nums[i], currentSum + nums[i])` 判断从当前数字重新开始，还是延续前面的连续子数组。
- 使用 `maxSum = Math.max(maxSum, currentSum)` 保存遍历过程中出现过的全局最大和。
- `currentSum` 和 `maxSum` 必须从 `nums[0]` 初始化，不能直接使用 `0`，否则全负数数组会错误返回 `0`。
- 三组输入运行正确；时间复杂度 `O(n)`，额外空间复杂度 `O(1)`。

## 2026-08-08：滑动窗口、二分查找与最大子数组和复写

### 固定长度滑动窗口

- 先计算前 `k` 个元素的 `windowSum`，把它作为初始 `maxSum`。
- 窗口每次右移一格时执行 `windowSum += nums[i] - nums[i - k]`：加入右侧新元素，同时删除左侧离开窗口的元素。
- 不需要为每个窗口重新循环求和，时间复杂度为 `O(n)`，额外空间复杂度为 `O(1)`。

### 二分查找复写错误

- 首次使用 `while (left < right)`，会遗漏只剩一个候选位置的情况；闭区间 `[left, right]` 应使用 `left <= right`。
- 首次找到目标后返回 `nums[mid]`，这返回的是元素值；题目需要下标时应返回 `mid`。
- 规律：写方法前先确认返回的是“值、下标、数量还是布尔结果”，不能只根据变量名字猜测。

### 最大子数组和复写错误

- 首次把 `currentSum` 初始化为 0，并在循环中只更新 `maxSum`；第二次仍没有把本轮结果保存回 `currentSum`。
- 正确顺序是先更新 `currentSum = Math.max(nums[i], currentSum + nums[i])`，再更新 `maxSum = Math.max(maxSum, currentSum)`。
- 两个变量含义必须分开：`currentSum` 是“必须以当前位置结尾的最大连续和”，`maxSum` 是“到目前为止见过的最大连续和”。

## 2026-08-10：前缀和与中心下标

### 前缀和

- 使用长度为 `nums.length + 1` 的 `prefix` 数组，令 `prefix[0] = 0`。
- 构建公式为 `prefix[i + 1] = prefix[i] + nums[i]`，`prefix[k]` 表示原数组前 `k` 个元素之和。
- 区间 `[left, right]` 的和为 `prefix[right + 1] - prefix[left]`；右侧加 1 是因为 `prefix` 的下标表示元素数量，不是原数组当前元素下标。
- 已理解前缀和适合“同一个数组需要多次查询区间和”：预处理 `O(n)`，每次查询 `O(1)`，额外空间 `O(n)`。

### 数组中心下标

- 第一次循环计算数组总和 `totalSum`，第二次循环维护当前位置左侧和 `leftSum`。
- 当前位置右侧和使用 `rightSum = totalSum - leftSum - nums[i]`，需要减去中心元素本身。
- 当 `leftSum == rightSum` 时返回当前下标；检查完成后再执行 `leftSum += nums[i]`，把当前元素加入下一位置的左侧。
- 已独立写出并运行成功，时间复杂度 `O(n)`，额外空间复杂度 `O(1)`。

### 本轮规律

- 前缀和公式不要只死记下标；先明确 `prefix[k]` 表示“前 k 个元素的总和”。
- 使用“总和减左边减当前值”计算右边时，不能漏掉当前中心元素。
