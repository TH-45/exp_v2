package jh.exp.process.service.driver;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class ProcessBusinessDriverRegistry {

    private final List<ProcessBusinessDriver> drivers;

    public ProcessBusinessDriverRegistry(List<ProcessBusinessDriver> drivers) {
        this.drivers = drivers;
    }

    /**
     * 路由优先级：procCode > busType > default。
     */
    public ProcessBusinessDriver route(String action, String busType, String procCode) {
        ProcessBusinessDriver byProcCode = drivers.stream()
                .filter(driver -> driver.supportsAction(action))
                .filter(driver -> StringUtils.hasText(driver.getProcCode()))
                .filter(driver -> driver.getProcCode().equalsIgnoreCase(procCode))
                .findFirst()
                .orElse(null);
        if (byProcCode != null) {
            return byProcCode;
        }

        ProcessBusinessDriver byBusType = drivers.stream()
                .filter(driver -> driver.supportsAction(action))
                .filter(driver -> StringUtils.hasText(driver.getBusType()))
                .filter(driver -> driver.getBusType().equalsIgnoreCase(busType))
                .findFirst()
                .orElse(null);
        if (byBusType != null) {
            return byBusType;
        }

        return drivers.stream()
                .filter(driver -> driver.supportsAction(action))
                .filter(driver -> !StringUtils.hasText(driver.getProcCode()) && !StringUtils.hasText(driver.getBusType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到默认流程驱动"));
    }
}
