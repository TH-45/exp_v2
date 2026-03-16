package jh.exp.process.service.service;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.process.core.entity.req.ApprovalActionReq;
import jh.exp.process.core.entity.req.ApprovalTaskQueryReq;
import jh.exp.process.core.entity.req.ForceCloseReq;
import jh.exp.process.core.entity.req.StartProcessReq;
import jh.exp.process.core.entity.res.ApprovalDetailRes;
import jh.exp.process.core.entity.res.ApprovalHistoryRes;
import jh.exp.process.core.entity.res.ApprovalStatsRes;
import jh.exp.process.core.entity.res.ApprovalTaskRes;

import java.util.List;

public interface ApprovalService {
    Long create(StartProcessReq req);

    /** 待办/已办/我发起/已关闭 数量统计 */
    ApprovalStatsRes getStats();

    SimplePageRes<ApprovalTaskRes> listTasks(SimplePageReq<ApprovalTaskQueryReq> req);

    ApprovalDetailRes detail(Long taskId);

    List<ApprovalHistoryRes> history(Long taskId);

    void approve(ApprovalActionReq req);

    /** 拒绝：直接关闭实例，不再流转 */
    void reject(ApprovalActionReq req);

    /** 驳回：回到上一节点，由上一个人重新审批，流程继续流转 */
    void returnToPrev(ApprovalActionReq req);

    void batchApprove(ApprovalActionReq req);

    void batchReject(ApprovalActionReq req);

    void forceClose(ForceCloseReq req);
}
