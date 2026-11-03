/**
 * Common 独立类加载空间的测试夹具包。
 *
 * <p>包含 Message、Shared，用于验证本地加载来源及父子加载器可见性。
 * 与其他夹具使用相同包名是刻意设计，必须单独编译到独立目录。
 * 本包没有运行入口，由 TomcatClassLoaderExample 通过反射及 Class 身份检查验证。
 * 仅依赖 JDK，具体命令见案例 README.md。</p>
 *
 * <p>创建时间：2026-11-03 23:38:59</p>
 *
 * @author advance
 */
package com.advance.javabasic.tomcatloader.fixture;
