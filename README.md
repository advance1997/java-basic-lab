# java-basic-lab

Java 基础学习与独立案例工程。开发前请阅读 [全局规范](AGENTS.md)。

## 工程结构

```text
java-basic-lab/
├── AGENTS.md
├── README.md
├── LICENSE
└── java-basic/
    ├── README.md
    └── src/com/advance/javabasic/
        └── package-info.java
```

基础案例统一放在 `java-basic` 中，每个案例使用独立主题包，案例之间不得互相依赖。每个代码包必须包含中文 `package-info.java`，详细说明包的内容和使用方式。

确实需要外部依赖管理等 Maven 能力时，再在 `maven-cases/<案例名称>/` 下创建独立 Maven 工程，管理依赖、插件版本和 Java 编译环境。当前没有此类案例，因此不创建 Maven 配置。

## 开发环境

基础源码使用 JDK 21 和 UTF-8 编码，通过 `javac`、`java` 编译和运行。请确保 `JAVA_HOME` 指向完整 JDK，并将其 `bin` 目录加入 `PATH`；`java -version` 和 `javac -version` 应均显示 Java 21。

基础代码的组织和编译说明见 [java-basic/README.md](java-basic/README.md)。

## 案例列表

- [案例一：模拟 Tomcat 类加载器](java-basic/src/com/advance/javabasic/tomcatloader/README.md)：应用本地优先、父优先对照、共享与隔离，附 11 项可运行检查。
- [案例二：SPI SDK 与插件平台](java-basic/src/com/advance/javabasic/spiplatform/README.md)：独立 SDK、插件 JAR 接入、ServiceLoader 发现与线程上下文类加载器。
