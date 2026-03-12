package jh.exp.process.service.driver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * 合同审批流程业务驱动：审批通过/驳回时回调合同服务更新状态
 */
@Component
@Order(100)
@Slf4j
@RequiredArgsConstructor
public class ContractProcessBusinessDriver implements ProcessBusinessDriver {

    private final RestTemplate bidContractRestTemplate;

    @Override
    public String getBusType() {
        return "contract";
    }

    @Override
    public void afterHandle(ProcessDriveContext ctx) {
        if (!"contract".equalsIgnoreCase(ctx.getBusType())) {
            return;
        }
        String instanceStatus = ctx.getInstanceStatus();
        String busId = ctx.getBusId();
        if (!StringUtils.hasText(busId) || !StringUtils.hasText(instanceStatus)) {
            return;
        }
        if (!"COMPLETED".equalsIgnoreCase(instanceStatus) && !"REJECTED".equalsIgnoreCase(instanceStatus)) {
            return;
        }
        try {
            Long contractId = Long.parseLong(busId);
            String url = "http://exp-bid-contract/contract/updateStatusByProcessResult?contractId=" + contractId
                    + "&instanceStatus=" + instanceStatus;
            bidContractRestTemplate.postForEntity(url, new HttpEntity<>(new HttpHeaders()), Void.class);
        } catch (Exception e) {
            log.error("合同流程回调更新状态失败 busId={} instanceStatus={}", busId, instanceStatus, e);
            throw new RuntimeException("合同状态更新失败", e);
        }
    }
}
