package jh.exp.bid.contract.service.service.bus;

import jh.exp.bid.contract.core.entity.BidEvaluationScore;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationScoreReq;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评标打分服务接口
 */
public interface EvaluationScoreService {

    /**
     * 提交评标打分
     */
    BidEvaluationScore submitScore(CreateEvaluationScoreReq req);

    /**
     * 批量提交评标打分
     */
    void batchSubmitScores(Long committeeId, Long bidId, List<CreateEvaluationScoreReq> scores);

    /**
     * 更新评标打分
     */
    BidEvaluationScore updateScore(Long scoreId, CreateEvaluationScoreReq req);

    /**
     * 删除评标打分
     */
    void deleteScore(Long scoreId);

    /**
     * 根据委员会和投标查询评分记录
     */
    List<BidEvaluationScore> getScoresByCommitteeAndBid(Long committeeId, Long bidId);

    /**
     * 计算投标的平均评分
     */
    BigDecimal calculateAverageScore(Long committeeId, Long bidId, String scoreType);

    /**
     * 提交所有专家评分
     */
    void submitAllExpertScores(Long committeeId, Long bidId, Long expertUserId);
}