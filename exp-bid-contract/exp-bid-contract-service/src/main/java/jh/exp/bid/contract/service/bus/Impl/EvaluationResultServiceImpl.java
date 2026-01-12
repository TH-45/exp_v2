package jh.exp.bid.contract.service.bus.Impl;

import jh.exp.bid.contract.entity.ExpBidEvaluationResult;
import jh.exp.bid.contract.entity.req.CreateEvaluationResultReq;
import jh.exp.bid.contract.mapper.EvaluationResultMapper;
import jh.exp.bid.contract.service.bus.EvaluationResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评标结果服务实现类
 */
@Service
@RequiredArgsConstructor
public class EvaluationResultServiceImpl implements EvaluationResultService {

    private final EvaluationResultMapper resultMapper;

    @Override
    @Transactional
    public ExpBidEvaluationResult generateEvaluationResult(CreateEvaluationResultReq req) {
        // 检查是否已存在该投标的评标结果
        ExpBidEvaluationResult existingResult = resultMapper.selectResultByBidId(req.getBidId());
        if (existingResult != null) {
            throw new RuntimeException("该投标已存在评标结果");
        }

        ExpBidEvaluationResult result = new ExpBidEvaluationResult();
        result.setCommitteeId(req.getCommitteeId());
        result.setBidId(req.getBidId());
        result.setTechnicalScore(req.getTechnicalScore());
        result.setBusinessScore(req.getBusinessScore());
        result.setComprehensiveScore(req.getComprehensiveScore());
        result.setFinalScore(req.getFinalScore());
        result.setRanking(req.getRanking());
        result.setIsRecommended(req.getIsRecommended());
        result.setEvaluationConclusion(req.getEvaluationConclusion());
        result.setEvaluationOpinion(req.getEvaluationOpinion());
        result.setResultStatus(req.getResultStatus());
        result.setEvaluationCompletedTime(LocalDateTime.now());
        result.setRemark(req.getRemark());
        result.setCreatedTime(LocalDateTime.now());
        result.setUpdatedTime(LocalDateTime.now());

        // TODO: 从投标信息中获取供应商信息
        // 这里需要关联查询获取投标单位信息

        resultMapper.insert(result);
        return result;
    }

    @Override
    @Transactional
    public ExpBidEvaluationResult updateEvaluationResult(Long resultId, CreateEvaluationResultReq req) {
        ExpBidEvaluationResult existingResult = resultMapper.selectById(resultId);
        if (existingResult == null) {
            throw new RuntimeException("评标结果不存在");
        }

        existingResult.setTechnicalScore(req.getTechnicalScore());
        existingResult.setBusinessScore(req.getBusinessScore());
        existingResult.setComprehensiveScore(req.getComprehensiveScore());
        existingResult.setFinalScore(req.getFinalScore());
        existingResult.setRanking(req.getRanking());
        existingResult.setIsRecommended(req.getIsRecommended());
        existingResult.setEvaluationConclusion(req.getEvaluationConclusion());
        existingResult.setEvaluationOpinion(req.getEvaluationOpinion());
        existingResult.setResultStatus(req.getResultStatus());
        existingResult.setRemark(req.getRemark());
        existingResult.setUpdatedTime(LocalDateTime.now());

        resultMapper.updateById(existingResult);
        return existingResult;
    }

    @Override
    @Transactional
    public void deleteEvaluationResult(Long resultId) {
        resultMapper.deleteById(resultId);
    }

    @Override
    public List<ExpBidEvaluationResult> getResultsByCommitteeId(Long committeeId) {
        return resultMapper.selectResultsByCommitteeId(committeeId);
    }

    @Override
    public ExpBidEvaluationResult getResultByBidId(Long bidId) {
        return resultMapper.selectResultByBidId(bidId);
    }

    @Override
    @Transactional
    public void calculateRanking(Long committeeId) {
        resultMapper.updateRankingByCommitteeId(committeeId);
    }

    @Override
    public ExpBidEvaluationResult getRecommendedWinner(Long committeeId) {
        return resultMapper.selectHighestScoreByCommitteeId(committeeId);
    }
}