package jh.exp.process.service.driver.bus;

import jh.exp.bid.contract.client.api.ContractClient;
import jh.exp.bid.contract.core.constant.BidContractConstant;
import jh.exp.bid.contract.core.entity.req.UpdateContractStatusReq;
import jh.exp.bid.contract.core.entity.res.ContractDetailRes;
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
 * 资金流入类合同签核流程驱动。
 */
@Component
@Order(101)
@Slf4j
@RequiredArgsConstructor
public class CapitalInflowContractDriver implements ProcessBusinessDriver<BusParamBase> {
    private final ContractClient contractClient;

    /** 流程编码：资金流入类合同签核流程 */
    @Override
    public String getProcCode() {
        return ProcessConstant.PROCESS_CONTRACT_FUND_IN;
    }

    @Override
    public String getBusType() {
        return ProcessConstant.PROCESS_TYPE_CONTRACT;
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
        ApiResponse<ContractDetailRes> res = contractClient.detail(busParamBase.getBusId());
        return res == null ? null : res.getData();
    }

    @Override
    public void afterHandle(ProcessDriveContext ctx) {
        ContractDetailRes contract = loadContractDetail(ctx.getBusId());
        String targetStatus = resolveTargetStatus(ctx, contract.getStatus());
        if (targetStatus == null || targetStatus.equals(contract.getStatus())) {
            return;
        }
        ApiResponse<Void> updateRes = contractClient.updateStatus(new UpdateContractStatusReq(ctx.getBusId(), targetStatus));
        if (updateRes == null || !updateRes.isSuccess()) {
            throw new RuntimeException("回写合同状态失败，contractId=" + ctx.getBusId());
        }
        log.info("资金流入流程回写合同状态成功 contractId={}, action={}, instanceStatus={}, {} -> {}",
                ctx.getBusId(), ctx.getAction(), ctx.getInstanceStatus(), contract.getStatus(), targetStatus);
    }

    private ContractDetailRes loadContractDetail(Long contractId) {
        ApiResponse<ContractDetailRes> res = contractClient.detail(contractId);
        if (res == null || !res.isSuccess() || res.getData() == null) {
            throw new RuntimeException("查询合同详情失败，contractId=" + contractId);
        }
        return res.getData();
    }

    /**
     * 按“流程动作 + 当前合同状态”确定目标状态，兼容多节点/单节点审批。
     */
    private String resolveTargetStatus(ProcessDriveContext ctx, String currentStatus) {
        String action = ctx.getAction();
        // 不同意、强制关闭：返回合同登记/起草。
        if (ProcessConstant.ACTION_REJECT.equals(action)
                || ProcessConstant.ACTION_CLOSE.equals(action)) {
            return BidContractConstant.CONTRACT_STATUS_DRAFT;
        }
        // 驳回：仅当实例已关闭（驳回到初始节点）时回到起草；普通驳回到上一级保持审核中。
        if (ProcessConstant.ACTION_RETURN.equals(action)
                && ProcessConstant.INSTANCE_CLOSED.equals(ctx.getInstanceStatus())) {
            return BidContractConstant.CONTRACT_STATUS_DRAFT;
        }
        // 审核通过：仅在实例已完成时推进到拟签。
        if (ProcessConstant.ACTION_AGREE.equals(action)
                && ProcessConstant.INSTANCE_COMPLETED.equals(ctx.getInstanceStatus())
                && BidContractConstant.CONTRACT_STATUS_UNDER_REVIEW.equals(currentStatus)) {
            return BidContractConstant.CONTRACT_STATUS_PENDING_SIGN;
        }
        return null;
    }
}
