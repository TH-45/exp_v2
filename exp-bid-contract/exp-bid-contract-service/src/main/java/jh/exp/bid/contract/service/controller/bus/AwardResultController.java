package jh.exp.bid.contract.service.controller.bus;

import jh.exp.bid.contract.core.entity.ExpBidAwardResult;
import jh.exp.bid.contract.core.entity.req.CreateAwardResultReq;
import jh.exp.bid.contract.service.service.bus.AwardResultService;
import jh.exp.common.core.annotation.RequiresPermissions;
import jh.exp.common.core.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 定标结果管理控制器
 */
@RestController
@RequestMapping("/award-result")
@RequiredArgsConstructor
public class AwardResultController {

    private final AwardResultService awardService;

    /**
     * 创建定标结果
     */
    @PostMapping("/create")
    @RequiresPermissions("AWARD:ADD")
    public ApiResponse<ExpBidAwardResult> create(@RequestBody CreateAwardResultReq req) {
        ExpBidAwardResult result = awardService.createAwardResult(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新定标结果
     */
    @PostMapping("/update")
    @RequiresPermissions("AWARD:EDIT")
    public ApiResponse<ExpBidAwardResult> update(@RequestParam Long awardId,
                                                @RequestBody CreateAwardResultReq req) {
        ExpBidAwardResult result = awardService.updateAwardResult(awardId, req);
        return ApiResponse.success(result);
    }

    /**
     * 删除定标结果
     */
    @PostMapping("/delete")
    @RequiresPermissions("AWARD:DELETE")
    public ApiResponse<Void> delete(@RequestParam Long awardId) {
        awardService.deleteAwardResult(awardId);
        return ApiResponse.success(null);
    }

    /**
     * 根据招标ID查询定标结果
     */
    @GetMapping("/byTender")
    @RequiresPermissions("AWARD:VIEW")
    public ApiResponse<ExpBidAwardResult> getByTender(@RequestParam Long tenderId) {
        ExpBidAwardResult result = awardService.getAwardResultByTenderId(tenderId);
        return ApiResponse.success(result);
    }

    /**
     * 根据投标ID查询定标结果
     */
    @GetMapping("/byBid")
    @RequiresPermissions("AWARD:VIEW")
    public ApiResponse<ExpBidAwardResult> getByBid(@RequestParam Long bidId) {
        ExpBidAwardResult result = awardService.getAwardResultByBidId(bidId);
        return ApiResponse.success(result);
    }

    /**
     * 更新定标状态
     */
    @PostMapping("/status")
    @RequiresPermissions("AWARD:EDIT")
    public ApiResponse<ExpBidAwardResult> updateStatus(@RequestParam Long awardId,
                                                      @RequestParam String awardStatus) {
        ExpBidAwardResult result = awardService.updateAwardStatus(awardId, awardStatus);
        return ApiResponse.success(result);
    }

    /**
     * 检查招标项目是否已有定标结果
     */
    @GetMapping("/checkTenderAward")
    public ApiResponse<Boolean> checkTenderAward(@RequestParam Long tenderId,
                                                @RequestParam(required = false) Long excludeAwardId) {
        boolean hasAward = awardService.checkTenderHasAwardResult(tenderId, excludeAwardId);
        return ApiResponse.success(hasAward);
    }
}