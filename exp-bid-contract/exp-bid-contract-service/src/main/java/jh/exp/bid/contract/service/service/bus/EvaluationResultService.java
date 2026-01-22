package jh.exp.bid.contract.service.service.bus;

import jh.exp.bid.contract.core.entity.ExpBidEvaluationResult;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationResultReq;

import java.util.List;

/**
 * 评标结果服务接口
 */
public interface EvaluationResultService {

    /**
     * 生成评标结果
     */
    ExpBidEvaluationResult generateEvaluationResult(CreateEvaluationResultReq req);

    /**
     * 更新评标结果
     */
    ExpBidEvaluationResult updateEvaluationResult(Long resultId, CreateEvaluationResultReq req);

    /**
     * 删除评标结果
     */
    void deleteEvaluationResult(Long resultId);

    /**
     * 根据委员会ID查询评标结果列表
     */
    List<ExpBidEvaluationResult> getResultsByCommitteeId(Long committeeId);

    /**
     * 根据投标ID查询评标结果
     */
    ExpBidEvaluationResult getResultByBidId(Long bidId);

    /**
     * 自动计算评标结果排序
     */
    void calculateRanking(Long committeeId);

    /**
     * 获取推荐中标结果
     */
    ExpBidEvaluationResult getRecommendedWinner(Long committeeId);
}