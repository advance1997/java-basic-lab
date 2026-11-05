package com.advance.javabasic.spiplatform.host;

import com.advance.javabasic.spiplatform.sdk.TextPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * 基于 Java SPI 的最小插件注册与调用平台。
 *
 * <p>通过调用方指定的加载器发现服务，遍历 ServiceLoader 时实例化插件。
 * 平台按标识注册并拒绝重复标识，调用者无需知道实现类名。
 * SPI 配置错误直接暴露为 ServiceConfigurationError，不静默忽略损坏的插件。</p>
 *
 * <p>平台不持有加载器的关闭责任，不提供并发调度或插件生命周期管理。
 * 本案例只执行可信插件，类加载器不构成安全沙箱。</p>
 *
 * <p>创建时间：2026-11-04 11:07:50</p>
 *
 * @author advance
 */
public final class PluginPlatform {

    /** 当前平台已发现的插件，注册完成后不再变更。 */
    private final Map<String, TextPlugin> plugins = new LinkedHashMap<>();

    /**
     * 从指定加载器可见的 SPI 服务中建立注册表。
     *
     * @param loader 能够同时看到 SDK 和插件的加载器
     * @throws IllegalArgumentException 插件标识为空或重复
     */
    public PluginPlatform(ClassLoader loader) {
        ServiceLoader<TextPlugin> services = ServiceLoader.load(TextPlugin.class,
                Objects.requireNonNull(loader, "插件加载器不能为空"));
        for (TextPlugin plugin : services) {
            String id = plugin.id();
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("插件标识不能为空：" + plugin.getClass().getName());
            }
            if (plugins.putIfAbsent(id, plugin) != null) {
                throw new IllegalArgumentException("插件标识重复：" + id);
            }
        }
    }

    /**
     * 返回注册表的只读快照，便于观察实现类及其定义加载器。
     *
     * @return 插件注册表快照
     */
    public Map<String, TextPlugin> plugins() {
        return Map.copyOf(plugins);
    }

    /**
     * 通过标识调用插件。
     *
     * @param id 插件标识
     * @param input 非空输入文本
     * @return 非空插件执行结果
     * @throws IllegalArgumentException 插件不存在
     * @throws NullPointerException 参数或插件返回结果为 null
     */
    public String execute(String id, String input) {
        Objects.requireNonNull(id, "插件标识不能为空");
        Objects.requireNonNull(input, "输入不能为空");
        TextPlugin plugin = plugins.get(id);
        if (plugin == null) {
            throw new IllegalArgumentException("未找到插件：" + id);
        }
        return Objects.requireNonNull(plugin.execute(input), "插件返回结果不能为空");
    }
}
