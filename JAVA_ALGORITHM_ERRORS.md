# Java 与算法复盘

更新时间：2026-08-18

> 本文件维护当前掌握程度、通用解题模板和仍会重复的错误，不再按日期重复记录同一道题。

## 当前掌握矩阵

| 类型 | 代表题目 | 当前等级 | 下一次验收 |
| --- | --- | --- | --- |
| 数组遍历 | 统计正数、最大值、移动零 | 能独立写 | 随项目练习自然复用 |
| 双指针 | 删除元素、移动零、回文字符串 | 能在少量提示下写 | 隔周复写一道 |
| HashMap | 两数之和、计数、最近重复下标 | 能独立写常见模板 | 加强边界与键值含义 |
| HashSet | 去重、交集、连续序列 | 能在提示下写 | 解释 `add()` 返回值 |
| 滑动窗口 | 固定窗口、最短子数组 | 能在提示下写 | 独立判断适用前提 |
| 前缀和 | 区间和、中心下标 | 能在提示下写 | 独立写构建和区间公式 |
| 栈 | 有效括号、删除相邻重复项 | 能独立写基础题 | 保持复习 |
| 队列 | 二叉树层序遍历 | 能在关键提示下写 | 无提示复写，避免误用 `root` |
| 链表 | 遍历、查找、反转、环、中点 | 反转已能独立写 | 巩固引用变化和返回头节点 |
| 二叉树递归 | 节点数、最大深度、查找、翻转 | 最大深度独立；翻转在骨架下完成 | 无提示复写翻转 |
| 排序与比较 | 两级排序、BigDecimal 比较 | 能看懂和套用 | 结合真实业务再练 |
| Java IO | 读取文件、解析库存、异常跳过 | 能在提示下写 | 暂不扩展 |
| 多线程基础 | Thread、join、线程池、同步计数 | 能解释 | 结合项目集成测试理解 |

## 解题固定流程

1. 用一句话说清输入、输出和目标。
2. 用一个具体示例手动演算，不急着写代码。
3. 给每个变量写清含义，区分自定义变量和 Java API。
4. 写核心循环或递归终止条件。
5. 验证普通、边界、无结果三类输入。
6. 判断时间复杂度和额外空间复杂度。

不要因为见过类似题就直接套模板；先确认题目条件是否满足模板前提。

## 高频错误与规则

### 1. 数组长度与数组本身

错误：

```java
i < nums
```

正确：

```java
i < nums.length
```

`nums` 是数组对象，`nums.length` 才是元素数量。

### 2. 原地修改的 `void` 方法

`moveZeroes(int[] nums)` 不需要返回数组。方法直接修改调用者传入的同一个数组对象，力扣会在调用后检查数组内容。

### 3. 读写指针不是 Java API

`readIndex`、`writeIndex` 是自行命名的变量：

```text
readIndex  → 读取当前位置
writeIndex → 下一个有效元素应该写入的位置
```

压缩非零元素后，第二个循环从 `writeIndex` 开始把剩余位置填 0。

### 4. Map 计数

通用写法：

```java
counts.put(number, counts.getOrDefault(number, 0) + 1);
```

`getOrDefault(key, 0)` 表示有键时取旧值，没有时用 0。不要只在 `containsKey()` 为真时写入，否则第一次出现的数据永远不会保存。

### 5. Map.Entry

```java
for (Map.Entry<String, Integer> entry : counts.entrySet()) {
    String key = entry.getKey();
    Integer value = entry.getValue();
}
```

`entry` 是 Map 中一组键值对，不是下标。找最大项时必须同时更新“最大值”和“对应对象”。

### 6. 字符串与数组

- `text.charAt(i)`：取得下标 `i` 的字符。
- `text.toCharArray()`：转为 `char[]` 后增强 for 遍历。
- `text.split("\\s+")`：按一个或多个空白拆成 `String[]`。
- `StringBuilder`：循环拼接字符串，最后 `toString()` 返回。

核心方法负责计算和返回，不在方法内部混入无关打印。

### 7. 滑动窗口

固定窗口：

```text
先计算前 k 个元素
→ 每次加入右侧新元素
→ 减去离开的左侧元素
```

可变窗口：右边扩张，满足条件时左边收缩。当前“和至少为 target”的写法依赖正整数数组；存在负数时窗口和不再单调，不能直接套用。

最短长度不能初始化为 0 后直接取 `Math.min()`；可使用 `nums.length + 1` 表示尚未找到。

### 8. 前缀和

```java
prefix[i + 1] = prefix[i] + nums[i];
```

区间 `[left, right]`：

```java
prefix[right + 1] - prefix[left]
```

减去 `prefix[left]` 是移除 `left` 之前的所有元素，不会移除 `left` 本身。

### 9. 二分查找

前提：数组有序。

```java
while (left <= right)
```

找到时返回下标 `mid`，不是返回 `nums[mid]`；缩小区间使用 `mid - 1` 或 `mid + 1`，因为 `mid` 已经检查过。

### 10. 链表遍历

`next` 是 `ListNode` 中自行定义的引用字段，不是 Java 固定方法：

```java
current = current.next;
```

只改变局部变量 `current` 指向谁；而：

```java
current.next = previous;
```

会真正修改链表连接。循环中遗漏 `current = current.next` 可能造成死循环。

### 11. 反转链表

每轮四步：

```java
ListNode nextNode = current.next;
current.next = previous;
previous = current;
current = nextNode;
```

必须先保存 `nextNode`，否则修改 `current.next` 后会丢失后半段入口。循环结束后新头节点是 `previous`。

时间 `O(n)`，额外空间 `O(1)`。

### 12. 快慢指针

安全循环条件：

```java
while (fast != null && fast.next != null)
```

`&&` 短路保证访问 `fast.next` 前 `fast` 不为空。

- 找中点：slow 一步、fast 两步，fast 到末尾时 slow 在中间。
- 判断环：fast 到 `null` 说明无环；移动后 `slow == fast` 才能证明有环。

### 13. 二叉树递归返回值

节点统计与最大深度必须接收子调用返回值：

```java
int leftResult = method(root.left);
int rightResult = method(root.right);
```

只调用递归却不接返回值，无法把深层结果逐层传回。

递归时间通常为 `O(n)`；调用栈空间取决于树高 `h`，为 `O(h)`，最坏退化树为 `O(n)`。

### 14. 共享结果列表

递归前序遍历中，每层的 `result` 参数变量都指向外部创建的同一个 `ArrayList`，因此所有 `add()` 会累计到同一列表。

### 15. Queue 层序遍历

- `offer()`：从队尾入队。
- `poll()`：从队头取出并移除等待记录。
- FIFO：First In, First Out，先进先出。

循环中必须处理当前取出的节点：

```java
TreeNode current = queue.poll();
```

随后使用 `current.left`、`current.right`，不能一直使用固定的 `root.left`、`root.right`，否则会反复加入根节点的孩子。

`poll()` 不会删除树节点，只是从队列中取出其引用。

### 16. 翻转二叉树

原地翻转时，每个节点交换自己的 `left` 和 `right`，然后递归处理两棵子树。交换后两棵原始子树只是换了引用位置，并没有丢失。

如果方法创建全新的树，就必须接住递归返回值并连接：

```java
newRoot.left = invertToNewTree(root.right);
newRoot.right = invertToNewTree(root.left);
```

仅仅“接住返回值”不等于完成算法；还要把新对象连接到正确的位置。

## 复杂度判断

### 时间复杂度

看输入扩大后执行次数如何变化：

- 遍历一次数组、链表或树：通常 `O(n)`。
- 两层完整嵌套循环：通常 `O(n²)`。
- 每次把搜索区间缩小一半：`O(log n)`。

### 额外空间复杂度

只计算算法额外创建的数据，不把输入本身算进去：

- 固定几个变量：`O(1)`。
- HashMap、HashSet、结果数组随输入增长：`O(n)`。
- 递归还要计算调用栈，树递归通常为 `O(h)`。

## 下一次算法验收

1. 无提示复写二叉树层序遍历，验证普通树、单节点和空树。
2. 无提示复写原地翻转二叉树，并解释为什么递归不会漏掉原始子树。
3. 给出时间和空间复杂度，说明 `root`、`current` 和返回值分别指向什么。

## 历史记录

优化前的逐题长记录已保存在 Git 中：

```powershell
git show 8d58397:JAVA_ALGORITHM_ERRORS.md
```
