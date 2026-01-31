package jh.exp.bid.contract.service.service.bus;

import jh.exp.bid.contract.core.entity.BidEvaluationMember;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationMemberReq;

import java.util.List;

/**
 * 评标成员服务接口
 */
public interface EvaluationMemberService {

    /**
     * 根据委员会ID查询评标成员列表
     */
    List<BidEvaluationMember> getMembersByCommitteeId(Long committeeId);

    /**
     * 添加评标成员
     */
    BidEvaluationMember addMember(CreateEvaluationMemberReq req);

    /**
     * 批量添加评标成员
     */
    void batchAddMembers(Long committeeId, List<CreateEvaluationMemberReq> members);

    /**
     * 更新评标成员
     */
    BidEvaluationMember updateMember(Long memberId, CreateEvaluationMemberReq req);

    /**
     * 删除评标成员
     */
    void removeMember(Long memberId);

    /**
     * 批量删除评标成员
     */
    void batchRemoveMembers(Long committeeId, List<Long> memberIds);

    /**
     * 更新成员到场状态
     */
    BidEvaluationMember updateMemberPresence(Long memberId, Integer isPresent);

    /**
     * 检查专家是否已在委员会中
     */
    boolean checkExpertInCommittee(Long committeeId, Long expertUserId, Long excludeMemberId);
}