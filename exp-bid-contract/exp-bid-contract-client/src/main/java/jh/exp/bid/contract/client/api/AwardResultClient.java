package jh.exp.bid.contract.client.api;

import jh.exp.bid.contract.core.entity.BidAwardResult;
import jh.exp.bid.contract.core.entity.req.AwardProcessDecisionReq;
import jh.exp.common.core.api.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 定标结果服务客户端接口，供流程服务回调使用。
 */
@HttpExchange("/award-result")
public interface AwardResultClient {

    /**
     * 按定标结果ID查询详情。
     */
    @GetExchange("/detail")
    ApiResponse<BidAwardResult> detail(@RequestParam("awardId") Long awardId);

    /**
     * 处理定标审批流程回调决策（APPROVE/REJECT）。
     */
    @PostExchange("/processDecision")
    ApiResponse<BidAwardResult> processDecision(@RequestBody AwardProcessDecisionReq req);
}
