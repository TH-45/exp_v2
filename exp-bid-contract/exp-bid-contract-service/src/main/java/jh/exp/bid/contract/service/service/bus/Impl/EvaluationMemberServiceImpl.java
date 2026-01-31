package jh.exp.bid.contract.service.service.bus.Impl;

import jh.exp.bid.contract.core.entity.BidEvaluationMember;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationMemberReq;
import jh.exp.bid.contract.core.mapper.EvaluationMemberMapper;
import jh.exp.bid.contract.service.service.bus.EvaluationMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评标成员服务实现类
 */
@Service
@RequiredArgsConstructor
public class EvaluationMemberServiceImpl implements EvaluationMemberService {

    private final EvaluationMemberMapper memberMapper;

    @Override
    public List<BidEvaluationMember> getMembersByCommitteeId(Long committeeId) {
        return memberMapper.selectMembersByCommitteeId(committeeId);
    }

    @Override
    @Transactional
    public BidEvaluationMember addMember(CreateEvaluationMemberReq req) {
        // 检查专家是否已在委员会中
        if (checkExpertInCommittee(req.getCommitteeId(), req.getExpertUserId(), null)) {
            throw new RuntimeException("该专家已在评标委员会中");
        }

        BidEvaluationMember member = new BidEvaluationMember();
        member.setCommitteeId(req.getCommitteeId());
        member.setExpertUserId(req.getExpertUserId());
        member.setExpertType(req.getExpertType());
        member.setCommitteeRole(req.getCommitteeRole());
        member.setIsChairman(req.getIsChairman());
        member.setExpertTitle(req.getExpertTitle());
        member.setExpertField(req.getExpertField());
        member.setContactPhone(req.getContactPhone());
        member.setContactEmail(req.getContactEmail());
        member.setIsPresent(0); // 默认未到场
        member.setMemberStatus("待确认");
        member.setJoinTime(LocalDateTime.now());
        member.setRemark(req.getRemark());
        member.setCreatedTime(LocalDateTime.now());
        member.setUpdatedTime(LocalDateTime.now());

        memberMapper.insert(member);
        return member;
    }

    @Override
    @Transactional
    public void batchAddMembers(Long committeeId, List<CreateEvaluationMemberReq> members) {
        for (CreateEvaluationMemberReq req : members) {
            req.setCommitteeId(committeeId);
            addMember(req);
        }
    }

    @Override
    @Transactional
    public BidEvaluationMember updateMember(Long memberId, CreateEvaluationMemberReq req) {
        BidEvaluationMember existingMember = memberMapper.selectById(memberId);
        if (existingMember == null) {
            throw new RuntimeException("评标成员不存在");
        }

        // 检查专家是否已在委员会中（排除当前成员）
        if (checkExpertInCommittee(req.getCommitteeId(), req.getExpertUserId(), memberId)) {
            throw new RuntimeException("该专家已在评标委员会中");
        }

        BidEvaluationMember member = new BidEvaluationMember();
        member.setMemberId(memberId);
        member.setExpertType(req.getExpertType());
        member.setCommitteeRole(req.getCommitteeRole());
        member.setIsChairman(req.getIsChairman());
        member.setExpertTitle(req.getExpertTitle());
        member.setExpertField(req.getExpertField());
        member.setContactPhone(req.getContactPhone());
        member.setContactEmail(req.getContactEmail());
        member.setRemark(req.getRemark());
        member.setUpdatedTime(LocalDateTime.now());

        memberMapper.updateById(member);
        return memberMapper.selectById(memberId);
    }

    @Override
    @Transactional
    public void removeMember(Long memberId) {
        BidEvaluationMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new RuntimeException("评标成员不存在");
        }

        memberMapper.deleteById(memberId);
    }

    @Override
    @Transactional
    public void batchRemoveMembers(Long committeeId, List<Long> memberIds) {
        memberMapper.batchDeleteByCommitteeId(committeeId, memberIds);
    }

    @Override
    @Transactional
    public BidEvaluationMember updateMemberPresence(Long memberId, Integer isPresent) {
        memberMapper.updatePresentStatus(memberId, isPresent);
        return memberMapper.selectById(memberId);
    }

    @Override
    public boolean checkExpertInCommittee(Long committeeId, Long expertUserId, Long excludeMemberId) {
        return memberMapper.countByCommitteeAndExpert(committeeId, expertUserId, excludeMemberId) > 0;
    }
}