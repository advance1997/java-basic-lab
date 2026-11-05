/**
 * 宿主平台包，PluginPlatform 负责发现及调用，SpiPluginExample 提供可调试 main 入口。
 *
 * <p>案例由 SDK、宿主及两个独立打包的插件组成。SDK 由父加载器定义，
 * 插件 JAR 由子 URLClassLoader 加载；显式指定加载器或设置线程上下文加载器完成发现。
 * 插件通过 META-INF/services 声明实现，注册表拒绝重复标识。</p>
 *
 * <p>仅使用 JDK 21，不依赖 Maven 或第一个案例。编译、运行及断点说明见案例 README。
 * 本案例不实现热部署、依赖隔离、版本协商或不可信插件沙箱。</p>
 *
 * <p>创建时间：2026-11-04 11:07:50</p>
 *
 * @author advance
 */
package com.advance.javabasic.spiplatform.host;
