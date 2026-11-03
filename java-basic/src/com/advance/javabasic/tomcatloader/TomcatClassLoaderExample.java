package com.advance.javabasic.tomcatloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 简化 Tomcat 类加载机制的运行入口及行为验证。
 *
 * <p>使用三个分别编译的目录构建 Common、应用 A 和应用 B 的加载层级。
 * 三个目录包含同名不同实现的 Message 类，以验证应用优先、父优先和类身份隔离。
 * Common 独有的 Shared 类用于验证回退与共享，应用 A 独有的 PrivateFeature 用于验证隔离。</p>
 *
 * <p>无参数时从工程根目录或 java-basic 目录下寻找约定的夹具输出目录，便于 IDE 直接调试。
 * 也可以依次指定 Common、应用 A、应用 B 的编译输出目录。所有检查均显式执行，
 * 无需开启 -ea；检查失败抛出异常并以非零状态结束。入口不会生成源码或编译夹具。</p>
 *
 * <p>创建时间：2026-11-03 23:38:59</p>
 *
 * @author advance
 */
public final class TomcatClassLoaderExample {

    /** 夹具类型使用独立命名空间，避免被入口的系统类路径提前加载。 */
    private static final String FIXTURE_PACKAGE = "com.advance.javabasic.tomcatloader.fixture.";

    /** 工具入口不允许实例化。 */
    private TomcatClassLoaderExample() {
    }

    /**
     * 构建类加载器树并验证关键行为。
     *
     * @param args 留空使用默认目录，或指定三个独立夹具输出目录
     * @throws Exception 参数无效、加载失败、反射失败或资源关闭失败
     */
    public static void main(String[] args) throws Exception {
        String[] directories = resolveDirectories(args);
        URL commonUrl = directoryUrl(directories[0]);
        URL appAUrl = directoryUrl(directories[1]);
        URL appBUrl = directoryUrl(directories[2]);
        try (URLClassLoader common = new URLClassLoader("Common", new URL[] {commonUrl},
                ClassLoader.getSystemClassLoader());
                SimpleWebAppClassLoader appA = new SimpleWebAppClassLoader(
                        "WebApp-A", new URL[] {appAUrl}, common, false);
                SimpleWebAppClassLoader appB = new SimpleWebAppClassLoader(
                        "WebApp-B", new URL[] {appBUrl}, common, false);
                SimpleWebAppClassLoader parentFirst = new SimpleWebAppClassLoader(
                        "WebApp-parent-first", new URL[] {appAUrl}, common, true)) {
            Class<?> messageA = appA.loadClass(FIXTURE_PACKAGE + "Message");
            Class<?> messageB = appB.loadClass(FIXTURE_PACKAGE + "Message");
            verify("应用 A 本地优先", "应用 A".equals(messageA.getMethod("origin").invoke(null))
                    && messageA.getClassLoader() == appA);
            verify("应用 B 本地优先", "应用 B".equals(messageB.getMethod("origin").invoke(null))
                    && messageB.getClassLoader() == appB);
            verify("同名类具有不同身份", messageA != messageB && !messageA.isAssignableFrom(messageB));
            verify("重复加载返回同一个 Class", messageA == appA.loadClass(FIXTURE_PACKAGE + "Message"));
            Class<?> shared = appA.loadClass(FIXTURE_PACKAGE + "Shared");
            verify("Common 回退与共享", shared == appB.loadClass(FIXTURE_PACKAGE + "Shared")
                    && shared.getClassLoader() == common);
            Class<?> parentMessage = parentFirst.loadClass(FIXTURE_PACKAGE + "Message");
            verify("delegate=true 使用父优先", parentMessage.getClassLoader() == common
                    && "Common".equals(parentMessage.getMethod("origin").invoke(null)));
            verify("JDK 类型优先", appA.loadClass("java.lang.String") == String.class
                    && appB.loadClass("java.sql.Driver") == java.sql.Driver.class);
            verify("应用 A 可以加载私有类",
                    appA.loadClass(FIXTURE_PACKAGE + "PrivateFeature").getClassLoader() == appA);
            expectMissing(appB, FIXTURE_PACKAGE + "PrivateFeature");
            expectMissing(common, FIXTURE_PACKAGE + "PrivateFeature");
            expectMissing(appA, FIXTURE_PACKAGE + "Missing");
            System.out.println("全部 11 项检查通过。");
        }
    }

    /**
     * 解析显式参数或根据工作目录选择默认夹具路径。
     *
     * @param args 命令行参数
     * @return 按 Common、应用 A、应用 B 排列的目录
     * @throws IllegalArgumentException 参数数量不符合约定
     */
    private static String[] resolveDirectories(String[] args) {
        if (args.length == 3) {
            return args.clone();
        }
        if (args.length != 0) {
            throw new IllegalArgumentException("请留空参数，或依次指定 Common、应用 A、应用 B 的三个目录");
        }
        Path base = Files.isDirectory(Path.of("java-basic", "src"))
                ? Path.of("java-basic", "out", "tomcatloader") : Path.of("out", "tomcatloader");
        return new String[] {base.resolve("common").toString(),
                base.resolve("app-a").toString(), base.resolve("app-b").toString()};
    }

    /**
     * 检查夹具目录存在，避免将错误路径误判为类加载策略问题。
     *
     * @param directory 编译输出目录
     * @return 目录 URL
     * @throws Exception 目录不存在或 URL 转换失败
     */
    private static URL directoryUrl(String directory) throws Exception {
        Path path = Path.of(directory).toAbsolutePath().normalize();
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("编译输出目录不存在：" + path
                    + "。请先按案例 README 编译三个夹具，并将工作目录设置为工程根目录或 java-basic。");
        }
        return path.toUri().toURL();
    }

    /**
     * 验证预期成立，失败时立即终止演示。
     *
     * @param description 检查项说明
     * @param condition 是否符合预期
     */
    private static void verify(String description, boolean condition) {
        if (!condition) {
            throw new IllegalStateException("检查失败：" + description);
        }
        System.out.println("通过：" + description);
    }

    /**
     * 验证指定加载器不可见某个类，不将其他异常当作预期结果。
     *
     * @param loader 待验证的加载器
     * @param name 应当不可见的类名
     */
    private static void expectMissing(ClassLoader loader, String name) {
        try {
            loader.loadClass(name);
        } catch (ClassNotFoundException expected) {
            System.out.println("通过：" + loader.getName() + " 不可见 " + name);
            return;
        }
        throw new IllegalStateException("隔离检查失败，意外加载了：" + name);
    }
}
