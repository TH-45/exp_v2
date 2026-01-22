package jh.exp.bid.contract.service.service.bus;

import jh.exp.bid.contract.core.entity.req.CreateEvaluationCommitteeReq;
import jh.exp.bid.contract.core.entity.req.QueryEvaluationCommitteeReq;
import jh.exp.bid.contract.core.entity.res.EvaluationCommitteeListRes;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;

import java.util.List;

/**
 * 评标委员会服务接口
 */
public interface EvaluationCommitteeService {

    /**
     * 分页查询评标委员会列表
     */
    SimplePageRes<EvaluationCommitteeListRes> queryCommitteeList(SimplePageReq<QueryEvaluationCommitteeReq> req);

    /**
     * 根据ID查询评标委员会详情
     */
    EvaluationCommitteeListRes getCommitteeById(Long committeeId);

    /**
     * 创建评标委员会
     */
    EvaluationCommitteeListRes createCommittee(CreateEvaluationCommitteeReq req);

    /**
     * 更新评标委员会
     */
    EvaluationCommitteeListRes updateCommittee(CreateEvaluationCommitteeReq req, Long committeeId);

    /**
     * 删除评标委员会
     */
    void deleteCommittee(Long committeeId);

    /**
     * 批量删除评标委员会
     */
    void batchDeleteCommittees(List<Long> committeeIds);

    /**
     * 更新委员会状态
     */
    EvaluationCommitteeListRes updateCommitteeStatus(Long committeeId, String status);

    /**
     * 批量更新委员会状态
     */
    void batchUpdateCommitteeStatus(List<Long> committeeIds, String status);

    /**
     * 检查委员会编号是否存在
     */
    boolean checkCommitteeCodeExists(String committeeCode, Long excludeCommitteeId);

    /**
     * 根据招标ID获取评标委员会列表
     */
    List<EvaluationCommitteeListRes> getCommitteesByTenderId(Long tenderId);

    /**
     * 检查招标项目是否已有评标委员会
     */
    boolean checkTenderHasCommittee(Long tenderId, Long excludeCommitteeId);
}