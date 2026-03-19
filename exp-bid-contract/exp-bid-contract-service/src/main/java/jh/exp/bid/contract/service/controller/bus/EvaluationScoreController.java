package jh.exp.bid.contract.service.controller.bus;

import jh.exp.bid.contract.core.entity.BidEvaluationScore;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationScoreReq;
import jh.exp.bid.contract.service.service.bus.EvaluationScoreService;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评标打分管理控制器
 */
@RestController
@RequestMapping("/evaluation-score")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "bidding:evaluation", level = 1)
public class EvaluationScoreController {

    private final EvaluationScoreService scoreService;

    /**
     * 提交评标打分
     */
    @PostMapping("/submit")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<BidEvaluationScore> submit(@RequestBody CreateEvaluationScoreReq req) {
        BidEvaluationScore result = scoreService.submitScore(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量提交评标打分
     */
    @PostMapping("/batchSubmit")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<Void> batchSubmit(@RequestParam Long committeeId,
                                        @RequestParam Long bidId,
                                        @RequestBody List<CreateEvaluationScoreReq> scores) {
        scoreService.batchSubmitScores(committeeId, bidId, scores);
        return ApiResponse.success(null);
    }

    /**
     * 更新评标打分
     */
    @PostMapping("/update")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<BidEvaluationScore> update(@RequestParam Long scoreId,
                                                    @RequestBody CreateEvaluationScoreReq req) {
        BidEvaluationScore result = scoreService.updateScore(scoreId, req);
        return ApiResponse.success(result);
    }

    /**
     * 删除评标打分
     */
    @PostMapping("/delete")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 3)
    public ApiResponse<Void> delete(@RequestParam Long scoreId) {
        scoreService.deleteScore(scoreId);
        return ApiResponse.success(null);
    }

    /**
     * 根据委员会和投标查询评分记录
     */
    @GetMapping("/list")
    public ApiResponse<List<BidEvaluationScore>> list(@RequestParam Long committeeId,
                                                        @RequestParam Long bidId) {
        List<BidEvaluationScore> result = scoreService.getScoresByCommitteeAndBid(committeeId, bidId);
        return ApiResponse.success(result);
    }

    /**
     * 计算投标的平均评分
     */
    @GetMapping("/average")
    public ApiResponse<BigDecimal> average(@RequestParam Long committeeId,
                                          @RequestParam Long bidId,
                                          @RequestParam String scoreType) {
        BigDecimal result = scoreService.calculateAverageScore(committeeId, bidId, scoreType);
        return ApiResponse.success(result);
    }

    /**
     * 提交所有专家评分
     */
    @PostMapping("/submitAll")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<Void> submitAll(@RequestParam Long committeeId,
                                      @RequestParam Long bidId,
                                      @RequestParam Long expertUserId) {
        scoreService.submitAllExpertScores(committeeId, bidId, expertUserId);
        return ApiResponse.success(null);
    }
}