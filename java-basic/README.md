# Java 基础案例

本目录集中存放仅依赖 JDK 的学习案例，使用普通 Java 源码目录结构，不使用 Maven。

## 源码组织

- 源码根目录为 `src`，基础包为 `com.advance.javabasic`。
- 每个案例在基础包下使用独立主题包，包名全部小写，并与目录一致。
- 案例复杂时按实际职责增加子包，不跨案例引用实现或共享可变状态。
- 每个代码包提供详细的中文 `package-info.java`；新增案例时说明主要类、入口、预期行为和边界条件。
- 首个案例为 [简化 Tomcat 类加载器](src/com/advance/javabasic/tomcatloader/README.md)，包含独立夹具和可执行行为验证。
- 第二个案例为 [SPI SDK 与插件平台](src/com/advance/javabasic/spiplatform/README.md)，包含独立 SDK、宿主、两个插件及可调试 main 入口。

## 编译与运行

使用 JDK 21，源码编码为 UTF-8。后续添加案例后，在本目录使用以下命令格式，替换尖括号中的内容为实际路径和类名：

```text
javac -encoding UTF-8 --release 21 -sourcepath src -d out src/com/advance/javabasic/<案例包>/<入口类>.java
java -cp out com.advance.javabasic.<案例包>.<入口类>
```

以上为通用手动命令格式。具体案例请遵循案例说明，尤其不能将类加载器案例的同名夹具合并编译。编译输出目录 `out` 不纳入版本管理。

基础案例如需测试，优先采用 JDK 能支持的验证方式；确需引入外部测试依赖时，按工程规范评估建立独立 Maven 案例，不直接向本目录引入 Maven。
