package jh.exp.bid.contract.service.service.support;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 流程服务 API 客户端，用于发起合同审批等流程
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessApiClient {

    private final RestTemplate processRestTemplate;

    /**
     * 发起流程
     * @param busType 业务类型，如 contract（当 procDefId 和 procCode 都为空时，按 busType 取第一个启用流程）
     * @param busId 业务主键
     * @param procDefId 流程定义ID（可选）
     * @param procCode 流程编码（可选）
     * @return 流程实例ID
     */
    public Long startProcess(String busType, String busId, Long procDefId, String procCode) {
        Long useProcDefId = procDefId;
        String useProcCode = procCode;
        if (useProcDefId == null && (useProcCode == null || useProcCode.isEmpty())) {
            var def = resolveDefaultProcess(busType);
            if (def != null) {
                useProcDefId = def.procDefId;
                useProcCode = def.procCode;
            }
        }
        if (useProcDefId == null && (useProcCode == null || useProcCode.isEmpty())) {
            throw new RuntimeException("未找到合同审批流程，请在流程管理中配置 busType=contract 的流程");
        }
        return doStartProcess(busId, useProcDefId, useProcCode);
    }

    private ProcDef resolveDefaultProcess(String busType) {
        String url = "http://exp-process/definition/list";
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("pageNum", 1);
        body.put("pageSize", 10);
        body.put("queryParam", Map.of("busType", busType, "isActive", 1));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<JsonNode> resp = processRestTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), JsonNode.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode data = resp.getBody().get("data");
                if (data != null && data.has("list")) {
                    JsonNode list = data.get("list");
                    if (list.isArray() && list.size() > 0) {
                        JsonNode first = list.get(0);
                        long procDefId = first.path("procDefId").asLong();
                        String procCode = first.path("procCode").asText("");
                        return new ProcDef(procDefId, procCode);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查询流程定义失败", e);
        }
        return null;
    }

    private Long doStartProcess(String busId, Long procDefId, String procCode) {
        String url = "http://exp-process/approval/start";
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("busId", busId);
        if (procDefId != null) body.put("procDefId", procDefId);
        if (procCode != null && !procCode.isEmpty()) body.put("procCode", procCode);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<JsonNode> response = processRestTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), JsonNode.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode data = response.getBody().get("data");
                if (data != null && !data.isNull()) return data.asLong();
            }
        } catch (Exception e) {
            log.error("调用流程服务发起失败", e);
            throw new RuntimeException("发起审批失败：" + (e.getMessage() != null ? e.getMessage() : "网络或服务异常"));
        }
        throw new RuntimeException("发起审批失败：未返回流程实例ID");
    }

    private record ProcDef(long procDefId, String procCode) {}
}
