# 案例一：模拟 Tomcat 类加载器

## 学习目标

理解双亲委派是一种查找策略，并非所有类加载器都必须完全照搬默认实现。Tomcat 为 Web 应用提供独立加载空间，默认允许应用自己的普通类优先于父加载器中的同名类，同时保留平台类型和容器 API 的保护规则。

## 本案例的层级

```text
Bootstrap / Platform
        ↑
      System（案例入口）
        ↑
      Common（共享夹具）
        ↑
        ├── WebApp-A（应用 A 夹具，本地优先）
        ├── WebApp-B（应用 B 夹具，本地优先）
        └── WebApp-parent-first（应用 A 夹具，父优先对照）
```

箭头表示向父加载器委派。A 与 B 互为兄弟，不能通过父链访问对方。

- `SimpleWebAppClassLoader`：继承 `URLClassLoader`，复用目录及 JAR 字节码查找能力，重写 `loadClass` 控制委派顺序。
- `TomcatClassLoaderExample`：构建上述层级，执行 11 项检查，失败时以异常终止，不依赖 `-ea`。
- `fixtures/common`：包含返回 `Common` 的 `Message` 和共享类 `Shared`。
- `fixtures/app-a`：包含返回 `应用 A` 的 `Message` 和私有类 `PrivateFeature`。
- `fixtures/app-b`：包含返回 `应用 B` 的 `Message`。

三个夹具的同名类是隔离实验的必要条件，不能合并编译。

## 加载顺序

默认 `delegate=false`：

1. 在加载锁内检查 `findLoadedClass`，避免重复定义。
2. 尝试平台类加载器，优先使用 JDK 类型。
3. 调用 `findClass`，从当前应用 URL 查找类。
4. 只有本地抛出 `ClassNotFoundException` 时才委派给 Common。
5. 根据调用参数决定是否链接类。

`delegate=true` 时，平台检查之后使用父优先策略：父加载器找不到时才查找本地。`java.*` 不走普通应用优先分支。

类加载并不等于类初始化。入口通过反射调用静态方法时会触发相应类的初始化。类身份由二进制名称和定义它的加载器共同决定，因此 A、B 的同名 `Message` 是不同类型，不能互相赋值。

## 手动编译与运行

要求完整 JDK 21，`java` 与 `javac` 均指向该版本。从仓库的 `java-basic` 目录执行以下命令，无需 Maven 或脚本。

```bash
javac -encoding UTF-8 --release 21 -d out/tomcatloader/main src/com/advance/javabasic/tomcatloader/*.java
javac -encoding UTF-8 --release 21 -d out/tomcatloader/common src/com/advance/javabasic/tomcatloader/fixtures/common/com/advance/javabasic/tomcatloader/fixture/*.java
javac -encoding UTF-8 --release 21 -d out/tomcatloader/app-a src/com/advance/javabasic/tomcatloader/fixtures/app-a/com/advance/javabasic/tomcatloader/fixture/*.java
javac -encoding UTF-8 --release 21 -d out/tomcatloader/app-b src/com/advance/javabasic/tomcatloader/fixtures/app-b/com/advance/javabasic/tomcatloader/fixture/*.java
java -cp out/tomcatloader/main com.advance.javabasic.tomcatloader.TomcatClassLoaderExample out/tomcatloader/common out/tomcatloader/app-a out/tomcatloader/app-b
```

系统类路径中只能放 `main` 输出目录。若把夹具目录也放进去，就改变了加载层级，无法正确观察预期的隔离和共享。

预期最后输出 `全部 11 项检查通过。`，覆盖：A/B 本地优先、同名类身份隔离、重复加载一致性、Common 共享与回退、父优先模式、JDK 类型优先、A 私有类可见、B 和 Common 不可见 A 私有类，以及缺失类正确抛出异常。

## 在 IDE 中通过 main 打断点

1. 使用 JDK 21，将 `java-basic/src` 设置为源码根目录，并将案例下的 `fixtures` 目录排除出 IDE 的源码编译范围。夹具包含同名类，必须按上面的三条夹具编译命令分别编译；修改夹具后也需重新编译。
2. 打开 `TomcatClassLoaderExample.java`，在 `main` 方法中首次调用 `appA.loadClass` 的位置打断点。
3. 创建该类的 Application 调试配置，工作目录设为工程根目录或 `java-basic`，程序参数留空。运行类路径包含入口类与加载器类，不添加三个夹具输出目录。
4. 点击 Debug。程序将使用默认的 `out/tomcatloader` 夹具目录，也仍支持显式传入三个目录参数。
5. 在 `SimpleWebAppClassLoader.loadClass` 和 `loadByPolicy` 中打断点，依次观察 `findLoadedClass`、平台查找、`findClass` 和 `getParent().loadClass`。可设置条件 `name.equals("com.advance.javabasic.tomcatloader.fixture.Message")`，减少 JDK 类型加载的干扰。

观察变量 `name`、`delegate`、`loadedClass` 和 `this.getName()`；回到入口比较 `messageA`、`messageB`、`shared` 的 `getClassLoader()`。调试 `parentFirst.loadClass` 时可对比父优先路径。

若使用命令行编译产物进行调试，在上面的四条 `javac` 命令中添加 `-g`，保留完整局部变量调试信息。IDE 编译时也应开启调试信息。

## 与真实 Tomcat 的差异

真实 Tomcat 的 Web 应用加载器还包含容器 API 过滤等规则，例如 Servlet API 通常应委派给容器，而不能简单由应用覆盖。本案例使用平台加载器保护 JDK 可见类型，没有引入 Servlet 依赖，也没有完整复刻 Tomcat 的过滤清单。

本案例用 URL 目录代替真实的 `WEB-INF/classes` 和 `WEB-INF/lib` 扫描，不实现资源委派、热部署、类卸载、线程上下文类加载器管理、并行加载优化或 HTTP 服务。继承的资源查找行为与这里重写的类加载顺序不等价。

只在“类不存在”时回退；字节码格式错误、依赖链接错误等应直接暴露，不能将所有异常吞掉后继续查找。加载器通过 try-with-resources 关闭；关闭释放资源，并不保证类立即卸载。本案例不是运行不可信代码的安全沙箱。
