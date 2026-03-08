package jh.exp.bid.contract.service.service.bus.Impl;

import jh.exp.bid.contract.core.entity.BidEvaluationScore;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationScoreReq;
import jh.exp.bid.contract.core.mapper.EvaluationScoreMapper;
import jh.exp.bid.contract.service.service.bus.EvaluationScoreService;
import jh.exp.bid.contract.service.service.bus.support.EvaluationFlowEligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评标打分服务实现类
 */
@Service
@RequiredArgsConstructor
public class EvaluationScoreServiceImpl implements EvaluationScoreService {

    private final EvaluationScoreMapper scoreMapper;
    private final EvaluationFlowEligibilityService eligibilityService;

    @Override
    @Transactional
    public BidEvaluationScore submitScore(CreateEvaluationScoreReq req) {
        eligibilityService.ensureCommitteeEligible(req.getCommitteeId());
        eligibilityService.ensureBidEligible(req.getBidId());

        BidEvaluationScore score = new BidEvaluationScore();
        score.setCommitteeId(req.getCommitteeId());
        score.setBidId(req.getBidId());
        score.setExpertUserId(req.getExpertUserId());
        score.setScoreType(req.getScoreType());
        score.setScoreItem(req.getScoreItem());
        score.setScoreValue(req.getScoreValue());
        score.setWeightPercentage(req.getWeightPercentage());

        // 计算加权得分
        if (req.getWeightPercentage() != null && req.getScoreValue() != null) {
            BigDecimal weightedScore = req.getScoreValue()
                .multiply(req.getWeightPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            score.setWeightedScore(weightedScore);
        }

        score.setScoreComment(req.getScoreComment());
        score.setScoreTime(LocalDateTime.now());
        score.setScoreStatus("已提交");
        score.setRemark(req.getRemark());
        score.setCreatedTime(LocalDateTime.now());
        score.setUpdatedTime(LocalDateTime.now());

        scoreMapper.insert(score);
        return score;
    }

    @Override
    @Transactional
    public void batchSubmitScores(Long committeeId, Long bidId, List<CreateEvaluationScoreReq> scores) {
        eligibilityService.ensureCommitteeEligible(committeeId);
        eligibilityService.ensureBidEligible(bidId);
        for (CreateEvaluationScoreReq req : scores) {
            req.setCommitteeId(committeeId);
            req.setBidId(bidId);
            submitScore(req);
        }
    }

    @Override
    @Transactional
    public BidEvaluationScore updateScore(Long scoreId, CreateEvaluationScoreReq req) {
        BidEvaluationScore existingScore = scoreMapper.selectById(scoreId);
        if (existingScore == null) {
            throw new RuntimeException("评标打分记录不存在");
        }
        eligibilityService.ensureCommitteeEligible(existingScore.getCommitteeId());
        eligibilityService.ensureBidEligible(existingScore.getBidId());

        existingScore.setScoreValue(req.getScoreValue());
        existingScore.setWeightPercentage(req.getWeightPercentage());

        // 重新计算加权得分
        if (req.getWeightPercentage() != null && req.getScoreValue() != null) {
            BigDecimal weightedScore = req.getScoreValue()
                .multiply(req.getWeightPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            existingScore.setWeightedScore(weightedScore);
        }

        existingScore.setScoreComment(req.getScoreComment());
        existingScore.setScoreTime(LocalDateTime.now());
        existingScore.setUpdatedTime(LocalDateTime.now());

        scoreMapper.updateById(existingScore);
        return existingScore;
    }

    @Override
    @Transactional
    public void deleteScore(Long scoreId) {
        BidEvaluationScore existingScore = scoreMapper.selectById(scoreId);
        if (existingScore == null) {
            throw new RuntimeException("评标打分记录不存在");
        }
        eligibilityService.ensureCommitteeEligible(existingScore.getCommitteeId());
        eligibilityService.ensureBidEligible(existingScore.getBidId());
        scoreMapper.deleteById(scoreId);
    }

    @Override
    public List<BidEvaluationScore> getScoresByCommitteeAndBid(Long committeeId, Long bidId) {
        eligibilityService.ensureCommitteeEligible(committeeId);
        eligibilityService.ensureBidEligible(bidId);
        return scoreMapper.selectScoresByCommitteeAndBid(committeeId, bidId);
    }

    @Override
    public BigDecimal calculateAverageScore(Long committeeId, Long bidId, String scoreType) {
        eligibilityService.ensureCommitteeEligible(committeeId);
        eligibilityService.ensureBidEligible(bidId);
        return scoreMapper.calculateAverageScore(committeeId, bidId, scoreType);
    }

    @Override
    @Transactional
    public void submitAllExpertScores(Long committeeId, Long bidId, Long expertUserId) {
        eligibilityService.ensureCommitteeEligible(committeeId);
        eligibilityService.ensureBidEligible(bidId);
        scoreMapper.batchUpdateScoreStatus(committeeId, bidId, expertUserId, "已提交");
    }
}