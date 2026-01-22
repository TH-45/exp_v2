package jh.exp.bid.contract.service.controller.bus;

import jh.exp.bid.contract.core.entity.ExpBidEvaluationScore;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationScoreReq;
import jh.exp.bid.contract.service.service.bus.EvaluationScoreService;
import jh.exp.common.annotation.RequiresPermissions;
import jh.exp.common.api.ApiResponse;
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
public class EvaluationScoreController {

    private final EvaluationScoreService scoreService;

    /**
     * 提交评标打分
     */
    @PostMapping("/submit")
    @RequiresPermissions("EVALUATION:SCORE")
    public ApiResponse<ExpBidEvaluationScore> submit(@RequestBody CreateEvaluationScoreReq req) {
        ExpBidEvaluationScore result = scoreService.submitScore(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量提交评标打分
     */
    @PostMapping("/batchSubmit")
    @RequiresPermissions("EVALUATION:SCORE")
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
    @RequiresPermissions("EVALUATION:SCORE")
    public ApiResponse<ExpBidEvaluationScore> update(@RequestParam Long scoreId,
                                                    @RequestBody CreateEvaluationScoreReq req) {
        ExpBidEvaluationScore result = scoreService.updateScore(scoreId, req);
        return ApiResponse.success(result);
    }

    /**
     * 删除评标打分
     */
    @PostMapping("/delete")
    @RequiresPermissions("EVALUATION:DELETE")
    public ApiResponse<Void> delete(@RequestParam Long scoreId) {
        scoreService.deleteScore(scoreId);
        return ApiResponse.success(null);
    }

    /**
     * 根据委员会和投标查询评分记录
     */
    @GetMapping("/list")
    @RequiresPermissions("EVALUATION:VIEW")
    public ApiResponse<List<ExpBidEvaluationScore>> list(@RequestParam Long committeeId,
                                                        @RequestParam Long bidId) {
        List<ExpBidEvaluationScore> result = scoreService.getScoresByCommitteeAndBid(committeeId, bidId);
        return ApiResponse.success(result);
    }

    /**
     * 计算投标的平均评分
     */
    @GetMapping("/average")
    @RequiresPermissions("EVALUATION:VIEW")
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
    @RequiresPermissions("EVALUATION:SCORE")
    public ApiResponse<Void> submitAll(@RequestParam Long committeeId,
                                      @RequestParam Long bidId,
                                      @RequestParam Long expertUserId) {
        scoreService.submitAllExpertScores(committeeId, bidId, expertUserId);
        return ApiResponse.success(null);
    }
}