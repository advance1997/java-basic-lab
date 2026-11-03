package com.advance.javabasic.tomcatloader.fixture;

/**
 * Common 的 Shared 夹具，用于验证独立目录中的类加载来源与可见性。
 *
 * <p>此类型仅存在于当前夹具目录，用于验证共享或应用私有性。
 * 本类由演示加载器加载，不应加入入口程序的系统类路径。</p>
 *
 * <p>创建时间：2026-11-03 23:38:59</p>
 *
 * @author advance
 */
public final class Shared {

    /** 夹具仅提供静态行为，不允许实例化。 */
    private Shared() {
    }

    /**
     * 返回当前夹具所属位置。
     *
     * @return 加载来源说明
     */
    public static String origin() {
        return "Common";
    }
}
