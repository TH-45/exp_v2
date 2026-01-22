package jh.exp.common.core.auth;

import jh.exp.common.core.auth.dto.CurrentUser;

/**
 * 当前用户上下文的 ThreadLocal 持有者。
 * <p>
 * 使用说明：
 * <ul>
 *     <li>在请求进入（例如 Filter / 拦截器）时调用 {@link #set(CurrentUser)} 写入当前用户；</li>
 *     <li>在业务代码中通过 {@link #get()} 读取当前用户信息；</li>
 *     <li>在请求结束时务必调用 {@link #clear()}，避免 ThreadLocal 泄漏。</li>
 * </ul>
 */
public final class CurrentUserHolder {

    private static final ThreadLocal<CurrentUser> CONTEXT = new ThreadLocal<>();

    private CurrentUserHolder() {
    }

    /**
     * 设置当前线程绑定的用户上下文。
     */
    public static void set(CurrentUser currentUser) {
        if (currentUser == null) {
            CONTEXT.remove();
        } else {
            CONTEXT.set(currentUser);
        }
    }

    /**
     * 获取当前线程绑定的用户上下文。
     */
    public static CurrentUser get() {
        return CONTEXT.get();
    }

    /**
     * 当前线程是否已经存在登录用户。
     */
    public static boolean hasLogin() {
        return CONTEXT.get() != null;
    }

    /**
     * 清理当前线程绑定的用户上下文。
     */
    public static void clear() {
        CONTEXT.remove();
    }
}


