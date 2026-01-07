package jh.exp.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Logback 的“服务休眠监控”Appender。
 *
 * 功能：
 * - 监听所有日志事件，记录最近一次非监控日志输出的时间；
 * - 当超过指定阈值（默认 120 秒）没有任何业务日志输出时，
 *   通过专门的 logger 打印一条显眼的休眠提示日志：
 *   ====== [SERVICE_IDLE] [serviceName] 系统已进入休眠，等待激活 ======
 *
 * 设计要点：
 * - 通过 loggerName（默认为 SERVICE_IDLE_MONITOR）区分自身输出的监控日志，
 *   避免“监控日志本身”被当作业务活动刷新 lastLogTime；
 * - 每个服务各自拥有一个 IdleMonitorAppender 实例和调度线程，互不影响；
 * - 可通过 logback-spring.xml 配置 serviceName、idleThresholdSeconds 等属性。
 */
public class IdleMonitorAppender extends AppenderBase<ILoggingEvent> {

    /**
     * 用于输出休眠提示的 logger 名称，避免与业务 logger 混淆。
     */
    public static final String MONITOR_LOGGER_NAME = "SERVICE_IDLE_MONITOR";

    /**
     * 服务名称（例如 auth、bid-contract），用于日志中区分不同服务。
     * 由 logback-spring.xml 通过 <serviceName>${LOG_FILE}</serviceName> 进行配置。
     */
    private String serviceName = "unknown";

    /**
     * 判定“休眠”的时间阈值（毫秒），默认 120 秒。
     */
    private long idleThresholdMillis = TimeUnit.SECONDS.toMillis(120);

    /**
     * 定时检查的间隔（毫秒），默认 30 秒。
     */
    private long checkIntervalMillis = TimeUnit.SECONDS.toMillis(30);

    /**
     * 最近一次“非监控日志”输出的时间戳。
     */
    private volatile long lastLogTime = System.currentTimeMillis();

    /**
     * 最近一次输出休眠提示日志的时间戳，用于防止高频重复输出。
     */
    private volatile long lastIdleLogTime = 0L;

    /**
     * 定时任务线程池。
     */
    private ScheduledExecutorService scheduler;

    private final Logger monitorLogger = LoggerFactory.getLogger(MONITOR_LOGGER_NAME);

    // ======== logback 配置属性的 setter（由 XML 进行注入） ========

    public void setServiceName(String serviceName) {
        if (serviceName != null && !serviceName.isBlank()) {
            this.serviceName = serviceName;
        }
    }

    /**
     * 供 XML 以秒为单位配置休眠阈值。
     */
    public void setIdleThresholdSeconds(long idleThresholdSeconds) {
        if (idleThresholdSeconds > 0) {
            this.idleThresholdMillis = TimeUnit.SECONDS.toMillis(idleThresholdSeconds);
        }
    }

    /**
     * 供 XML 以秒为单位配置检查间隔。
     */
    public void setCheckIntervalSeconds(long checkIntervalSeconds) {
        if (checkIntervalSeconds > 0) {
            this.checkIntervalMillis = TimeUnit.SECONDS.toMillis(checkIntervalSeconds);
        }
    }

    // ======== 生命周期管理 ========

    @Override
    public void start() {
        if (isStarted()) {
            return;
        }
        super.start();
        // 初始化 lastLogTime，避免刚启动立即打印休眠日志
        this.lastLogTime = System.currentTimeMillis();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "idle-monitor-" + serviceName);
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleAtFixedRate(this::checkIdle, checkIntervalMillis, checkIntervalMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        super.stop();
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    // ======== 日志事件监听 ========

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted() || eventObject == null) {
            return;
        }
        // 忽略由自身监控 logger 打出的日志，避免影响 lastLogTime
        if (MONITOR_LOGGER_NAME.equals(eventObject.getLoggerName())) {
            return;
        }
        // 任何一条业务日志输出都认为服务处于“活跃”
        this.lastLogTime = System.currentTimeMillis();
    }

    // ======== 休眠检测逻辑 ========

    private void checkIdle() {
        if (!isStarted()) {
            return;
        }
        long now = System.currentTimeMillis();
        long sinceLastLog = now - lastLogTime;

        // 未达到休眠阈值，不输出
        if (sinceLastLog < idleThresholdMillis) {
            return;
        }

        // 控制休眠提示的输出频率：至少间隔一个 idleThresholdMillis 再输出下一条
        long sinceLastIdleLog = now - lastIdleLogTime;
        if (lastIdleLogTime != 0L && sinceLastIdleLog < idleThresholdMillis) {
            return;
        }

        lastIdleLogTime = now;

        // 输出显眼的休眠提示日志
        String message = "====== [SERVICE_IDLE] [" + serviceName + "] 系统已进入休眠，等待激活 ======";
        monitorLogger.info(message);
    }
}






















