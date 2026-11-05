package com.advance.javabasic.spiplatform.sdk;

/**
 * 文本插件 SDK：定义宿主与第三方插件共同遵循的最小契约。
 *
 * <p>插件实现此接口并注册 SPI 服务文件；宿主只依赖接口，不引用实现类。
 * SDK 由宿主父加载器统一加载，插件 JAR 不应重复打包 SDK。
 * 实现应提供公开无参构造器，标识必须非空且在平台内唯一。</p>
 *
 * <p>创建时间：2026-11-04 11:07:50</p>
 *
 * @author advance
 */
public interface TextPlugin {

    /**
     * 返回用于选择插件的稳定标识。
     *
     * @return 非空插件标识
     */
    String id();

    /**
     * 处理文本，不修改外部状态。
     *
     * @param input 非空文本，允许空字符串
     * @return 非空处理结果
     * @throws NullPointerException 输入为 null
     */
    String execute(String input);
}
