package com.advance.javabasic.tomcatloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

/**
 * 模拟 Tomcat Web 应用类加载器的核心委派策略。
 *
 * <p>每个实例代表一个应用，URL 指向该应用独立的类目录或 JAR。
 * 默认先检查已加载类和平台类，再查找应用本地类，最后回退到 Common 父加载器。
 * 开启 delegate 后改为父优先。平台类始终优先，防止应用覆盖 JDK 类型。</p>
 *
 * <p>本类仅用于学习，不是 Tomcat 的完整实现：没有实现 Servlet API 过滤、资源查找策略、
 * 热部署、线程上下文加载器管理及容器生命周期。使用方必须关闭实例释放 URL 资源。</p>
 *
 * <p>创建时间：2026-11-03 23:38:59</p>
 *
 * @author advance
 */
public final class SimpleWebAppClassLoader extends URLClassLoader {

    /** 是否使用父优先委派策略。 */
    private final boolean delegate;

    /**
     * 创建一个与其他应用互相隔离的加载器。
     *
     * @param name 用于诊断的应用加载器名称
     * @param urls 当前应用的类目录或 JAR 地址，目录 URL 必须以斜杠结束
     * @param parent Common 父加载器
     * @param delegate 是否优先委派给父加载器
     */
    public SimpleWebAppClassLoader(String name, URL[] urls, ClassLoader parent, boolean delegate) {
        super(Objects.requireNonNull(name, "加载器名称不能为空"),
                Objects.requireNonNull(urls, "应用地址不能为空").clone(),
                Objects.requireNonNull(parent, "父加载器不能为空"));
        this.delegate = delegate;
    }

    /**
     * 按选定策略加载类，并保证同一加载器不会重复定义同名类。
     *
     * @param name 类的二进制名称
     * @param resolve 是否链接加载的类
     * @return 已加载或本次加载的类
     * @throws ClassNotFoundException 所有允许的查找路径都找不到该类
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
                loadedClass = loadByPolicy(name);
            }
            if (resolve) {
                resolveClass(loadedClass);
            }
            return loadedClass;
        }
    }

    /**
     * 先保护平台可见类型，再执行父优先或应用优先查找。
     *
     * @param name 类的二进制名称
     * @return 查找到的类
     * @throws ClassNotFoundException 本地及父加载器均无法加载该类
     */
    private Class<?> loadByPolicy(String name) throws ClassNotFoundException {
        try {
            return ClassLoader.getPlatformClassLoader().loadClass(name);
        } catch (ClassNotFoundException ignored) {
            // 平台不可见的类型继续执行应用策略；不吞掉链接错误或字节码校验错误。
        }
        if (delegate || name.startsWith("java.")) {
            return super.loadClass(name, false);
        }
        try {
            return findClass(name);
        } catch (ClassNotFoundException ignored) {
            // 仅在本地确实找不到类时才回退，应用 A 不会查找应用 B 的目录。
            return getParent().loadClass(name);
        }
    }
}
