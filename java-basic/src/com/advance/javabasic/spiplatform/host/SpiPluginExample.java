package com.advance.javabasic.spiplatform.host;

import com.advance.javabasic.spiplatform.sdk.TextPlugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * SPI 插件平台的可断点调试入口。
 *
 * <p>宿主运行时仅依赖 SDK，从外部目录扫描插件 JAR，通过独立子加载器接入。
 * 分别对比显式传入加载器与使用线程上下文加载器的发现行为。
 * 无参数时从工程根目录或 java-basic 目录定位默认插件目录，也可传入一个目录参数。</p>
 *
 * <p>main 只加载现有 JAR，不生成源码或执行编译。关闭 URLClassLoader 只释放资源，
 * 不保证立即卸载类，也不提供运行中热部署能力。</p>
 *
 * <p>创建时间：2026-11-04 11:07:50</p>
 *
 * @author advance
 */
public final class SpiPluginExample {

    /** 仅提供静态入口。 */
    private SpiPluginExample() {
    }

    /**
     * 加载插件并验证 SDK 共享和上下文加载器作用。
     *
     * @param args 可选的插件 JAR 目录
     * @throws Exception 目录无效、JAR 加载或资源关闭失败
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 1) {
            throw new IllegalArgumentException("仅接受一个插件目录参数，也可留空");
        }
        Path base = Files.isDirectory(Path.of("java-basic", "src"))
                ? Path.of("java-basic", "out", "spiplatform") : Path.of("out", "spiplatform");
        Path directory = args.length == 0 ? base.resolve("plugins") : Path.of(args[0]);
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("插件目录不存在，请按 README 编译打包：" + directory);
        }
        URL[] urls;
        try (var files = Files.list(directory)) {
            Path[] jars = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted().toArray(Path[]::new);
            urls = new URL[jars.length];
            for (int index = 0; index < jars.length; index++) {
                urls[index] = jars[index].toUri().toURL();
            }
        }
        ClassLoader sdkLoader = TextPlugin.class.getClassLoader();
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        try (URLClassLoader pluginLoader = new URLClassLoader("SPI-plugins", urls, sdkLoader)) {
            // 使用受控上下文作对照，不依赖 IDE 启动时设置的上下文加载器。
            thread.setContextClassLoader(sdkLoader);
            verify("宿主加载器无法发现外部插件", ServiceLoader.load(TextPlugin.class).findFirst().isEmpty());
            PluginPlatform platform = new PluginPlatform(pluginLoader);
            verify("至少发现一个插件", !platform.plugins().isEmpty());
            for (var entry : platform.plugins().entrySet()) {
                TextPlugin plugin = entry.getValue();
                verify("实现由插件加载器定义：" + entry.getKey(),
                        plugin.getClass().getClassLoader() == pluginLoader);
                System.out.println(entry.getKey() + " => " + platform.execute(entry.getKey(), "Hello SPI"));
            }
            verify("子加载器使用父加载器中的 SDK",
                    pluginLoader.loadClass(TextPlugin.class.getName()) == TextPlugin.class);
            thread.setContextClassLoader(pluginLoader);
            long count = ServiceLoader.load(TextPlugin.class).stream().count();
            verify("设置上下文后默认 SPI 发现全部插件", count == platform.plugins().size());
            if (platform.plugins().containsKey("uppercase")) {
                verify("大写插件结果", "HELLO SPI".equals(platform.execute("uppercase", "Hello SPI")));
            }
            if (platform.plugins().containsKey("greeting")) {
                verify("问候插件结果", "你好，Hello SPI".equals(platform.execute("greeting", "Hello SPI")));
            }
        } finally {
            // 必须恢复上下文，避免线程长期持有插件加载器引用。
            thread.setContextClassLoader(original);
        }
        verify("线程上下文已恢复", thread.getContextClassLoader() == original);
        System.out.println("SPI 案例全部检查通过。");
    }

    /**
     * 显式验证行为，无需启用 JVM 断言选项。
     *
     * @param description 检查说明
     * @param condition 是否符合预期
     */
    private static void verify(String description, boolean condition) {
        if (!condition) {
            throw new IllegalStateException("检查失败：" + description);
        }
        System.out.println("通过：" + description);
    }
}
