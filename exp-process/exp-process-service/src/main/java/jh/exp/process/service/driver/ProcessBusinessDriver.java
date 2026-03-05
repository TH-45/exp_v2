package jh.exp.process.service.driver;

public interface ProcessBusinessDriver {

    /**
     * 是否支持当前 action。
     */
    default boolean supportsAction(String action) {
        return true;
    }

    /**
     * 处理器绑定的流程编码；返回 null 表示不按 procCode 绑定。
     */
    default String getProcCode() {
        return null;
    }

    /**
     * 处理器绑定的业务类型；返回 null 表示不按 busType 绑定。
     */
    default String getBusType() {
        return null;
    }

    /**
     * 发起前校验；抛异常即阻断发起。
     */
    default void beforeHandle(ProcessDriveContext ctx) {
    }

    /**
     * 引擎动作完成后回调；关键回写要求同事务处理。
     */
    default void afterHandle(ProcessDriveContext ctx) {
    }
}
