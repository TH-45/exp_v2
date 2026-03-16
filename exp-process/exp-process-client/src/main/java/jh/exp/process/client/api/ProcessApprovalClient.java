package jh.exp.process.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.process.core.entity.req.StartProcessReq;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 流程审批服务客户端接口，用于发起流程、审批等
 */
@HttpExchange("/approval")
public interface ProcessApprovalClient {

    /**
     * 发起流程
     *
     * @param req 发起流程请求（busId 业务id 必填，busCategory业务类别、busType业务类型）
     * @return 流程实例ID
     */
    @PostExchange("/create")
    ApiResponse<Long> createProcess(StartProcessReq req);
}
