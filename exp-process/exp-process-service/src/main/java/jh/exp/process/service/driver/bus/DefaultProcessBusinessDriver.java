package jh.exp.process.service.driver.bus;

import jh.exp.process.service.driver.ProcessBusinessDriver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 默认兜底驱动：不做业务回写，保证流程引擎可独立运行。
 */
@Component
@Order(Integer.MAX_VALUE)
public class DefaultProcessBusinessDriver implements ProcessBusinessDriver {
    @Override
    public String getBusType() {
        return null;
    }
}
