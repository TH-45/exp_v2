package jh.exp.bid.contract.service.controller.bus;

import jh.exp.bid.contract.core.entity.BidEvaluationResult;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationResultReq;
import jh.exp.bid.contract.service.service.bus.EvaluationResultService;
import jh.exp.common.core.annotation.RequiresPermissions;
import jh.exp.common.core.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评标结果管理控制器
 */
@RestController
@RequestMapping("/evaluation-result")
@RequiredArgsConstructor
public class EvaluationResultController {

    private final EvaluationResultService resultService;

    /**
     * 生成评标结果
     */
    @PostMapping("/generate")
    //@RequiresPermissions("EVALUATION:RESULT")
    public ApiResponse<BidEvaluationResult> generate(@RequestBody CreateEvaluationResultReq req) {
        BidEvaluationResult result = resultService.generateEvaluationResult(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新评标结果
     */
    @PostMapping("/update")
    //@RequiresPermissions("EVALUATION:RESULT")
    public ApiResponse<BidEvaluationResult> update(@RequestParam Long resultId,
                                                     @RequestBody CreateEvaluationResultReq req) {
        BidEvaluationResult result = resultService.updateEvaluationResult(resultId, req);
        return ApiResponse.success(result);
    }

    /**
     * 删除评标结果
     */
    @PostMapping("/delete")
    //@RequiresPermissions("EVALUATION:DELETE")
    public ApiResponse<Void> delete(@RequestParam Long resultId) {
        resultService.deleteEvaluationResult(resultId);
        return ApiResponse.success(null);
    }

    /**
     * 根据委员会ID查询评标结果列表
     */
    @GetMapping("/list")
    //@RequiresPermissions("EVALUATION:VIEW")
    public ApiResponse<List<BidEvaluationResult>> list(@RequestParam Long committeeId) {
        List<BidEvaluationResult> result = resultService.getResultsByCommitteeId(committeeId);
        return ApiResponse.success(result);
    }

    /**
     * 根据投标ID查询评标结果
     */
    @GetMapping("/byBid")
    //@RequiresPermissions("EVALUATION:VIEW")
    public ApiResponse<BidEvaluationResult> getByBid(@RequestParam Long bidId) {
        BidEvaluationResult result = resultService.getResultByBidId(bidId);
        return ApiResponse.success(result);
    }

    /**
     * 自动计算评标结果排序
     */
    @PostMapping("/calculateRanking")
    //@RequiresPermissions("EVALUATION:RESULT")
    public ApiResponse<Void> calculateRanking(@RequestParam Long committeeId) {
        resultService.calculateRanking(committeeId);
        return ApiResponse.success(null);
    }

    /**
     * 获取推荐中标结果
     */
    @GetMapping("/recommendedWinner")
    //@RequiresPermissions("EVALUATION:VIEW")
    public ApiResponse<BidEvaluationResult> getRecommendedWinner(@RequestParam Long committeeId) {
        BidEvaluationResult result = resultService.getRecommendedWinner(committeeId);
        return ApiResponse.success(result);
    }
}