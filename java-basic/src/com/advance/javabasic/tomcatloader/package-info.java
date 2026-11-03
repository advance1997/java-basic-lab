/**
 * 简化 Tomcat 类加载器案例，演示父优先委派与 Web 应用本地优先策略的区别。
 *
 * <p>SimpleWebAppClassLoader 实现加载策略；TomcatClassLoaderExample 是运行入口，
 * 使用独立编译的 Common、应用 A、应用 B 夹具验证共享、隔离、重复加载与平台类型保护。
 * 夹具源码位于本案例 fixtures 目录，不得合并编译或放入入口的系统类路径。</p>
 *
 * <p>仅依赖 JDK 21，不使用 Maven。编译与运行命令见本目录 README.md。
 * 本案例聚焦类加载，不模拟 HTTP 服务、Servlet 容器、热部署或完整资源加载机制。</p>
 *
 * <p>创建时间：2026-11-03 23:38:59</p>
 *
 * @author advance
 */
package com.advance.javabasic.tomcatloader;
