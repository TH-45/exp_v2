package jh.exp.process.service.driver.bus;

import jh.exp.bid.contract.client.api.AwardResultClient;
import jh.exp.bid.contract.core.entity.BidAwardResult;
import jh.exp.bid.contract.core.entity.req.AwardProcessDecisionReq;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.process.core.constant.ProcessConstant;
import jh.exp.process.core.entity.dto.BusParamBase;
import jh.exp.process.core.entity.dto.ProcessDriveContext;
import jh.exp.process.service.driver.ProcessBusinessDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 招投标业务流程驱动（procCode=BID_BUSINESS）。
 * busId 约定为定标结果 award_id。
 */
@Component
@Order(102)
@Slf4j
@RequiredArgsConstructor
public class BidBusinessProcessDriver implements ProcessBusinessDriver<BusParamBase> {
    private final AwardResultClient awardResultClient;

    @Override
    public String getProcCode() {
        return ProcessConstant.PROCESS_BID_BUSINESS;
    }

    @Override
    public String getBusType() {
        return ProcessConstant.PROCESS_TYPE_BID;
    }

    @Override
    public boolean supportsAction(String action) {
        return ProcessConstant.ACTION_AGREE.equals(action)
                || ProcessConstant.ACTION_REJECT.equals(action)
                || ProcessConstant.ACTION_RETURN.equals(action)
                || ProcessConstant.ACTION_CLOSE.equals(action);
    }

    @Override
    public Object getBusinessData(BusParamBase busParamBase) {
        ApiResponse<BidAwardResult> res = awardResultClient.detail(busParamBase.getBusId());
        return res == null ? null : res.getData();
    }

    @Override
    public void afterHandle(ProcessDriveContext ctx) {
        String processAction = ctx.getAction();
        // 多节点审批：仅在实例完成时才执行通过回写，避免中间节点提前落业务结果。
        if (ProcessConstant.ACTION_AGREE.equals(processAction)
                && !ProcessConstant.INSTANCE_COMPLETED.equals(ctx.getInstanceStatus())) {
            return;
        }

        String decisionAction = mapDecisionAction(ctx);
        if (decisionAction == null) {
            return;
        }
        AwardProcessDecisionReq req = new AwardProcessDecisionReq();
        req.setAwardId(ctx.getBusId());
        req.setAction(decisionAction);
        req.setOpinion(ctx.getReq() == null ? null : ctx.getReq().getComments());

        ApiResponse<BidAwardResult> res = awardResultClient.processDecision(req);
        if (res == null || !res.isSuccess()) {
            throw new RuntimeException("回写定标流程决策失败，awardId=" + ctx.getBusId());
        }
        log.info("招投标流程回写成功 awardId={}, processAction={}, instanceStatus={}, decision={}",
                ctx.getBusId(), processAction, ctx.getInstanceStatus(), decisionAction);
    }

    /**
     * 流程动作映射到定标回调动作：
     * AGREE -> APPROVE；REJECT/RETURN/CLOSE -> REJECT（RETURN 仅实例关闭时触发）。
     */
    private String mapDecisionAction(ProcessDriveContext ctx) {
        String action = ctx.getAction();
        if (ProcessConstant.ACTION_AGREE.equals(action)) {
            return "APPROVE";
        }
        if (ProcessConstant.ACTION_REJECT.equals(action)) {
            return "REJECT";
        }
        if (ProcessConstant.ACTION_CLOSE.equals(action)) {
            return "REJECT";
        }
        if (ProcessConstant.ACTION_RETURN.equals(action)
                && ProcessConstant.INSTANCE_CLOSED.equals(ctx.getInstanceStatus())) {
            return "REJECT";
        }
        return null;
    }
}
