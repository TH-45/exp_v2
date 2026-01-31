package jh.exp.bid.contract.service.service.bus.Impl;

import jh.exp.bid.contract.core.entity.BidEvaluationScore;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationScoreReq;
import jh.exp.bid.contract.core.mapper.EvaluationScoreMapper;
import jh.exp.bid.contract.service.service.bus.EvaluationScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评标打分服务实现类
 */
@Service
@RequiredArgsConstructor
public class EvaluationScoreServiceImpl implements EvaluationScoreService {

    private final EvaluationScoreMapper scoreMapper;

    @Override
    @Transactional
    public BidEvaluationScore submitScore(CreateEvaluationScoreReq req) {
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
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
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

        existingScore.setScoreValue(req.getScoreValue());
        existingScore.setWeightPercentage(req.getWeightPercentage());

        // 重新计算加权得分
        if (req.getWeightPercentage() != null && req.getScoreValue() != null) {
            BigDecimal weightedScore = req.getScoreValue()
                .multiply(req.getWeightPercentage())
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
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
        scoreMapper.deleteById(scoreId);
    }

    @Override
    public List<BidEvaluationScore> getScoresByCommitteeAndBid(Long committeeId, Long bidId) {
        return scoreMapper.selectScoresByCommitteeAndBid(committeeId, bidId);
    }

    @Override
    public BigDecimal calculateAverageScore(Long committeeId, Long bidId, String scoreType) {
        return scoreMapper.calculateAverageScore(committeeId, bidId, scoreType);
    }

    @Override
    @Transactional
    public void submitAllExpertScores(Long committeeId, Long bidId, Long expertUserId) {
        scoreMapper.batchUpdateScoreStatus(committeeId, bidId, expertUserId, "已提交");
    }
}