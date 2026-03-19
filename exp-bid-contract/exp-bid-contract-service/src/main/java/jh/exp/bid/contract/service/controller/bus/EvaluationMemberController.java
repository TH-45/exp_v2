package jh.exp.bid.contract.service.controller.bus;

import jh.exp.bid.contract.core.entity.BidEvaluationMember;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationMemberReq;
import jh.exp.bid.contract.service.service.bus.EvaluationMemberService;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评标成员管理控制器
 */
@RestController
@RequestMapping("/evaluation-member")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "bidding:evaluation", level = 1)
public class EvaluationMemberController {

    private final EvaluationMemberService memberService;

    /**
     * 根据委员会ID查询评标成员列表
     */
    @GetMapping("/list")
    public ApiResponse<List<BidEvaluationMember>> list(@RequestParam Long committeeId) {
        List<BidEvaluationMember> result = memberService.getMembersByCommitteeId(committeeId);
        return ApiResponse.success(result);
    }

    /**
     * 添加评标成员
     */
    @PostMapping("/add")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<BidEvaluationMember> add(@RequestBody CreateEvaluationMemberReq req) {
        BidEvaluationMember result = memberService.addMember(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量添加评标成员
     */
    @PostMapping("/batchAdd")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<Void> batchAdd(@RequestParam Long committeeId,
                                     @RequestBody List<CreateEvaluationMemberReq> members) {
        memberService.batchAddMembers(committeeId, members);
        return ApiResponse.success(null);
    }

    /**
     * 更新评标成员
     */
    @PostMapping("/update")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<BidEvaluationMember> update(@RequestParam Long memberId,
                                                     @RequestBody CreateEvaluationMemberReq req) {
        BidEvaluationMember result = memberService.updateMember(memberId, req);
        return ApiResponse.success(result);
    }

    /**
     * 删除评标成员
     */
    @PostMapping("/remove")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 3)
    public ApiResponse<Void> remove(@RequestParam Long memberId) {
        memberService.removeMember(memberId);
        return ApiResponse.success(null);
    }

    /**
     * 批量删除评标成员
     */
    @PostMapping("/batchRemove")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 3)
    public ApiResponse<Void> batchRemove(@RequestParam Long committeeId,
                                        @RequestBody List<Long> memberIds) {
        memberService.batchRemoveMembers(committeeId, memberIds);
        return ApiResponse.success(null);
    }

    /**
     * 更新成员到场状态
     */
    @PostMapping("/presence")
    @RequiresMenuLevel(code = "bidding:evaluation", level = 2)
    public ApiResponse<BidEvaluationMember> updatePresence(@RequestParam Long memberId,
                                                             @RequestParam Integer isPresent) {
        BidEvaluationMember result = memberService.updateMemberPresence(memberId, isPresent);
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