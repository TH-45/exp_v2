package jh.exp.bid.contract.controller.bus;

import jh.exp.bid.contract.entity.ExpBidEvaluationMember;
import jh.exp.bid.contract.entity.req.CreateEvaluationMemberReq;
import jh.exp.bid.contract.service.bus.EvaluationMemberService;
import jh.exp.common.annotation.RequiresPermissions;
import jh.exp.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评标成员管理控制器
 */
@RestController
@RequestMapping("/evaluation-member")
@RequiredArgsConstructor
public class EvaluationMemberController {

    private final EvaluationMemberService memberService;

    /**
     * 根据委员会ID查询评标成员列表
     */
    @GetMapping("/list")
    @RequiresPermissions("EVALUATION:VIEW")
    public ApiResponse<List<ExpBidEvaluationMember>> list(@RequestParam Long committeeId) {
        List<ExpBidEvaluationMember> result = memberService.getMembersByCommitteeId(committeeId);
        return ApiResponse.success(result);
    }

    /**
     * 添加评标成员
     */
    @PostMapping("/add")
    @RequiresPermissions("EVALUATION:EDIT")
    public ApiResponse<ExpBidEvaluationMember> add(@RequestBody CreateEvaluationMemberReq req) {
        ExpBidEvaluationMember result = memberService.addMember(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量添加评标成员
     */
    @PostMapping("/batchAdd")
    @RequiresPermissions("EVALUATION:EDIT")
    public ApiResponse<Void> batchAdd(@RequestParam Long committeeId,
                                     @RequestBody List<CreateEvaluationMemberReq> members) {
        memberService.batchAddMembers(committeeId, members);
        return ApiResponse.success(null);
    }

    /**
     * 更新评标成员
     */
    @PostMapping("/update")
    @RequiresPermissions("EVALUATION:EDIT")
    public ApiResponse<ExpBidEvaluationMember> update(@RequestParam Long memberId,
                                                     @RequestBody CreateEvaluationMemberReq req) {
        ExpBidEvaluationMember result = memberService.updateMember(memberId, req);
        return ApiResponse.success(result);
    }

    /**
     * 删除评标成员
     */
    @PostMapping("/remove")
    @RequiresPermissions("EVALUATION:DELETE")
    public ApiResponse<Void> remove(@RequestParam Long memberId) {
        memberService.removeMember(memberId);
        return ApiResponse.success(null);
    }

    /**
     * 批量删除评标成员
     */
    @PostMapping("/batchRemove")
    @RequiresPermissions("EVALUATION:DELETE")
    public ApiResponse<Void> batchRemove(@RequestParam Long committeeId,
                                        @RequestBody List<Long> memberIds) {
        memberService.batchRemoveMembers(committeeId, memberIds);
        return ApiResponse.success(null);
    }

    /**
     * 更新成员到场状态
     */
    @PostMapping("/presence")
    @RequiresPermissions("EVALUATION:EDIT")
    public ApiResponse<ExpBidEvaluationMember> updatePresence(@RequestParam Long memberId,
                                                             @RequestParam Integer isPresent) {
        ExpBidEvaluationMember result = memberService.updateMemberPresence(memberId, isPresent);
        return ApiResponse.success(result);
    }

    /**
     * 检查专家是否已在委员会中
     */
    @GetMapping("/checkExpert")
    public ApiResponse<Boolean> checkExpert(@RequestParam Long committeeId,
                                           @RequestParam Long expertUserId,
                                           @RequestParam(required = false) Long excludeMemberId) {
        boolean exists = memberService.checkExpertInCommittee(committeeId, expertUserId, excludeMemberId);
        return ApiResponse.success(exists);
    }
}