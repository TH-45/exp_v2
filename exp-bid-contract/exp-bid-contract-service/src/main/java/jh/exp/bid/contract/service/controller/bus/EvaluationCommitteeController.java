package jh.exp.bid.contract.service.controller.bus;

import jh.exp.bid.contract.core.entity.req.CreateEvaluationCommitteeReq;
import jh.exp.bid.contract.core.entity.req.QueryEvaluationCommitteeReq;
import jh.exp.bid.contract.core.entity.res.EvaluationCommitteeListRes;
import jh.exp.bid.contract.service.service.bus.EvaluationCommitteeService;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评标委员会管理控制器，对应菜单 bidding:evaluation
 */
@RestController
@RequestMapping("/evaluation-committee")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "bidding:evaluation", level = 1)
public class EvaluationCommitteeController {

    private final EvaluationCommitteeService committeeService;

    /**
     * 分页查询评标委员会列表
     */
    @PostMapping("/list")
    public ApiResponse<SimplePageRes<EvaluationCommitteeListRes>> list(@RequestBody SimplePageReq<QueryEvaluationCommitteeReq> req) {
        req.pageDefault();
        SimplePageRes<EvaluationCommitteeListRes> result = committeeService.queryCommitteeList(req);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询评标委员会详情
     */
    @GetMapping("/detail")
    public ApiResponse<EvaluationCommitteeListRes> detail(@RequestParam Long committeeId) {
        EvaluationCommitteeListRes result = committeeService.getCommitteeById(committeeId);
        return ApiResponse.success(result);
    }

    /**
     * 创建评标委员会
     */
    @PostMapping("/create")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<EvaluationCommitteeListRes> create(@RequestBody @Valid CreateEvaluationCommitteeReq req) {
        EvaluationCommitteeListRes result = committeeService.createCommittee(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新评标委员会
     */
    @PostMapping("/update")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<EvaluationCommitteeListRes> update(@RequestParam Long committeeId,
                                                         @RequestBody @Valid CreateEvaluationCommitteeReq req) {
        EvaluationCommitteeListRes result = committeeService.updateCommittee(req, committeeId);
        return ApiResponse.success(result);
    }

    /**
     * 删除评标委员会
     */
    @PostMapping("/delete")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 3)
    public ApiResponse<Void> delete(@RequestParam Long committeeId) {
        committeeService.deleteCommittee(committeeId);
        return ApiResponse.success(null);
    }

    /**
     * 批量删除评标委员会
     */
    @PostMapping("/batchDelete")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 3)
    public ApiResponse<Void> batchDelete(@RequestBody List<Long> committeeIds) {
        committeeService.batchDeleteCommittees(committeeIds);
        return ApiResponse.success(null);
    }

    /**
     * 更新委员会状态
     */
    @PostMapping("/status")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<EvaluationCommitteeListRes> updateStatus(@RequestParam Long committeeId,
                                                               @RequestParam String status) {
        EvaluationCommitteeListRes result = committeeService.updateCommitteeStatus(committeeId, status);
        return ApiResponse.success(result);
    }

    /**
     * 批量更新委员会状态
     */
    @PostMapping("/batchStatus")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<Void> batchUpdateStatus(@RequestBody List<Long> committeeIds,
                                              @RequestParam String status) {
        committeeService.batchUpdateCommitteeStatus(committeeIds, status);
        return ApiResponse.success(null);
    }

    /**
     * 检查委员会编号是否存在
     */
    @GetMapping("/checkCode")
    public ApiResponse<Boolean> checkCode(@RequestParam String committeeCode,
                                         @RequestParam(required = false) Long excludeCommitteeId) {
        boolean exists = committeeService.checkCommitteeCodeExists(committeeCode, excludeCommitteeId);
        return ApiResponse.success(exists);
    }

    /**
     * 根据招标ID获取评标委员会列表
     */
    @GetMapping("/byTender")
    public ApiResponse<List<EvaluationCommitteeListRes>> getByTender(@RequestParam Long tenderId) {
        List<EvaluationCommitteeListRes> result = committeeService.getCommitteesByTenderId(tenderId);
        return ApiResponse.success(result);
    }

    /**
     * 检查招标项目是否已有评标委员会
     */
    @GetMapping("/checkTenderCommittee")
    public ApiResponse<Boolean> checkTenderCommittee(@RequestParam Long tenderId,
                                                    @RequestParam(required = false) Long excludeCommitteeId) {
        boolean hasCommittee = committeeService.checkTenderHasCommittee(tenderId, excludeCommitteeId);
        return ApiResponse.success(hasCommittee);
    }
}