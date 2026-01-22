package jh.exp.common.core.util;


import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 参数校验工具类
 *
 * 设计原则：
 * 1. 校验失败立即抛异常（Fail Fast）
 * 2. 错误信息完全由调用方控制
 * 3. 不依赖 Spring，可在任何层使用
 */
public final class ParamVerifyUtil {

    private ParamVerifyUtil() {
        // 工具类禁止实例化
    }

    /**
     * 校验参数是否为空
     *
     * @param param   待校验参数
     * @param message 校验失败时抛出的错误提示（最终错误信息）
     */
    public static void notNull(Object param, String message) {
        if (isEmpty(param)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 判断对象是否为空（仅判断，不抛异常）
     *
     * @param param 待判断对象
     * @return true-为空；false-不为空
     */
    public static boolean isEmpty(Object param) {
        if (param == null) {
            return true;
        }

        // String
        if (param instanceof String) {
            return ((String) param).trim().isEmpty();
        }

        // Collection
        if (param instanceof Collection) {
            return ((Collection<?>) param).isEmpty();
        }

        // Map
        if (param instanceof Map) {
            return ((Map<?, ?>) param).isEmpty();
        }

        // Array
        if (param.getClass().isArray()) {
            return Array.getLength(param) == 0;
        }

        // Optional
        if (param instanceof Optional) {
            return !((Optional<?>) param).isPresent();
        }

        // 其他对象：只要不是 null，就认为合法
        return false;
    }

    /**
     * 批量参数校验入口
     */
    public static BatchVerifier batch() {
        return new BatchVerifier();
    }

    /**
     * 批量校验内部类
     */
    public static class BatchVerifier {

        /**
         * 添加一个待校验参数
         *
         * @param param   参数
         * @param message 失败提示
         * @return 当前 BatchVerifier
         */
        public BatchVerifier add(Object param, String message) {
            ParamVerifyUtil.notNull(param, message);
            return this;
        }

        /**
         * 执行校验
         * 说明：
         * - 由于 add 时已经校验
         * - 这里只是语义完整性保留
         */
        public void verify() {
            // no-op
        }
    }
}
