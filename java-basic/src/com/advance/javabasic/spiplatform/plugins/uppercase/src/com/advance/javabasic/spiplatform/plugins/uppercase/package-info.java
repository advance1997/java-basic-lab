/**
 * uppercase 插件实现包，包含 UppercasePlugin，提供独立的文本处理能力。
 *
 * <p>仅依赖 SDK 的 TextPlugin 接口，由宿主 ServiceLoader 发现并调用，无独立 main。
 * 接入入口为 META-INF/services 中的服务声明；编译和打包方式见案例 README。
 * 不得把 SDK 重复打入插件 JAR，也不依赖其他插件。</p>
 *
 * <p>创建时间：2026-11-04 11:07:50</p>
 *
 * @author advance
 */
package com.advance.javabasic.spiplatform.plugins.uppercase;
