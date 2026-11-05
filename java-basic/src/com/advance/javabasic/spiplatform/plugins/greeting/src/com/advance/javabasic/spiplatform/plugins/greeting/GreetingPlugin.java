package com.advance.javabasic.spiplatform.plugins.greeting;

import com.advance.javabasic.spiplatform.sdk.TextPlugin;
import java.util.Objects;

/**
 * 中文问候插件，演示仅依赖 SDK 的第三方扩展接入。
 *
 * <p>公开无参构造器供 ServiceLoader 实例化，服务文件将本类注册为 TextPlugin 提供者。
 * 插件不依赖宿主实现或其他插件，打包时只包含本插件代码和注册资源。</p>
 *
 * <p>创建时间：2026-11-04 11:07:50</p>
 *
 * @author advance
 */
public final class GreetingPlugin implements TextPlugin {

    /** 创建无状态插件实例，供 SPI 反射调用。 */
    public GreetingPlugin() {
    }

    /**
     * 返回插件的稳定标识。
     *
     * @return greeting 标识
     */
    @Override
    public String id() {
        return "greeting";
    }

    /**
     * 执行文本处理，允许空字符串，拒绝 null。
     *
     * @param input 非空输入文本
     * @return 处理结果
     * @throws NullPointerException 输入为 null
     */
    @Override
    public String execute(String input) {
        Objects.requireNonNull(input, "输入不能为空");
        return "你好，" + input;
    }
}
