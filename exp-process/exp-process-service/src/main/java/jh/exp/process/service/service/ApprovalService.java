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
    Long start(StartProcessReq req);

    ApprovalStatsRes stats();

    SimplePageRes<ApprovalTaskRes> listTasks(SimplePageReq<ApprovalTaskQueryReq> req);

    ApprovalDetailRes detail(Long taskId);

    List<ApprovalHistoryRes> history(Long taskId);

    void approve(ApprovalActionReq req);

    void reject(ApprovalActionReq req);

    void batchApprove(ApprovalActionReq req);

    void batchReject(ApprovalActionReq req);

    void forceClose(ForceCloseReq req);
}
