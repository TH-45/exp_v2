package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.auth.clinet.api.bus.PersonService;

import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.constant.BidContractConstant;
import jh.exp.bid.contract.core.entity.BidAwardResult;
import jh.exp.bid.contract.core.entity.Bid;
import jh.exp.bid.contract.core.entity.req.AwardProcessDecisionReq;
import jh.exp.bid.contract.core.entity.req.CreateAwardResultReq;
import jh.exp.bid.contract.core.mapper.AwardResultMapper;
import jh.exp.bid.contract.core.mapper.BidMapper;
import jh.exp.bid.contract.service.service.bus.AwardResultService;
import jh.exp.bid.contract.service.service.bus.support.EvaluationFlowEligibilityService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 定标结果服务实现类
 */
@Service
@RequiredArgsConstructor
public class AwardResultServiceImpl implements AwardResultService {

    private final AwardResultMapper awardMapper;
    private final BidMapper bidMapper;
    private final PersonService personService;
    private final EvaluationFlowEligibilityService eligibilityService;

    @Override
    @Transactional
    public BidAwardResult createAwardResult(CreateAwardResultReq req) {
        eligibilityService.ensureTenderEligible(req.getTenderId());
        Bid winningBid = getBidOrThrow(req.getWinningBidId());
        if (!req.getTenderId().equals(winningBid.getTenderId())) {
            throw new RuntimeException("中标投标不属于当前招标项目");
        }
        eligibilityService.ensureBidEligible(winningBid.getBidId());

        // 检查招标项目是否已有定标结果
        if (checkTenderHasAwardResult(req.getTenderId(), null)) {
            throw new RuntimeException("该招标项目已存在定标结果");
        }

        CurrentUser currentUser = CurrentUserHolder.get();
        Long personId = Long.valueOf(currentUser.getUserId());

        PersonDetailRes personDetail = personService.getPersonById(personId);
        if (personDetail == null) {
            throw new RuntimeException("无法获取当前用户信息");
        }

        BidAwardResult award = new BidAwardResult();
        award.setTenderId(req.getTenderId());
        award.setWinningBidId(req.getWinningBidId());
        award.setWinningAmount(req.getWinningAmount());
        award.setCurrency(req.getCurrency());
        award.setAwardNoticeNo(req.getAwardNoticeNo());
        award.setAwardNoticeSendTime(req.getAwardNoticeSendTime());
        award.setContractSignDeadline(req.getContractSignDeadline());
        award.setActualContractSignTime(req.getActualContractSignTime());
        award.setAwardStatus(req.getAwardStatus() != null ? req.getAwardStatus() : "待定标");
        award.setDecisionMakerId(req.getDecisionMakerId());
        award.setDecisionTime(req.getDecisionTime());
        award.setAwardOpinion(req.getAwardOpinion());
        award.setNeedRetender(req.getNeedRetender());
        award.setRetenderReason(req.getRetenderReason());
        award.setRemark(req.getRemark());
        award.setCreatedTime(LocalDateTime.now());
        award.setUpdatedTime(LocalDateTime.now());
        award.setCreatedBy(personId);
        award.setCreatedDeptId(personDetail.getOrgId());
        award.setCreatedPostId(personDetail.getPostId());

        // TODO: 从投标信息中获取中标单位信息
        // 这里需要关联查询获取中标单位信息

        awardMapper.insert(award);
        return award;
    }

    @Override
    @Transactional
    public BidAwardResult updateAwardResult(Long awardId, CreateAwardResultReq req) {
        BidAwardResult existingAward = awardMapper.selectById(awardId);
        if (existingAward == null) {
            throw new RuntimeException("定标结果不存在");
        }
        eligibilityService.ensureTenderEligible(existingAward.getTenderId());

        existingAward.setWinningAmount(req.getWinningAmount());
        existingAward.setCurrency(req.getCurrency());
        existingAward.setAwardNoticeNo(req.getAwardNoticeNo());
        existingAward.setAwardNoticeSendTime(req.getAwardNoticeSendTime());
        existingAward.setContractSignDeadline(req.getContractSignDeadline());
        existingAward.setActualContractSignTime(req.getActualContractSignTime());
        existingAward.setAwardStatus(req.getAwardStatus());
        existingAward.setDecisionMakerId(req.getDecisionMakerId());
        existingAward.setDecisionTime(req.getDecisionTime());
        existingAward.setAwardOpinion(req.getAwardOpinion());
        existingAward.setNeedRetender(req.getNeedRetender());
        existingAward.setRetenderReason(req.getRetenderReason());
        existingAward.setRemark(req.getRemark());
        existingAward.setUpdatedTime(LocalDateTime.now());

        awardMapper.updateById(existingAward);
        return existingAward;
    }

    @Override
    @Transactional
    public void deleteAwardResult(Long awardId) {
        BidAwardResult award = awardMapper.selectById(awardId);
        if (award == null) {
            throw new RuntimeException("定标结果不存在");
        }
        eligibilityService.ensureTenderEligible(award.getTenderId());

        // 检查是否可以删除
        if ("已签订合同".equals(award.getAwardStatus())) {
            throw new RuntimeException("已签订合同的定标结果不能删除");
        }

        awardMapper.deleteById(awardId);
    }

    @Override
    public BidAwardResult getAwardResultByTenderId(Long tenderId) {
        eligibilityService.ensureTenderEligible(tenderId);
        return awardMapper.selectAwardResultByTenderId(tenderId);
    }

    @Override
    public BidAwardResult getAwardResultByBidId(Long bidId) {
        eligibilityService.ensureBidEligible(bidId);
        return awardMapper.selectAwardResultByBidId(bidId);
    }

    @Override
    @Transactional
    public BidAwardResult updateAwardStatus(Long awardId, String awardStatus) {
        BidAwardResult existingAward = awardMapper.selectById(awardId);
        if (existingAward == null) {
            throw new RuntimeException("定标结果不存在");
        }
        eligibilityService.ensureTenderEligible(existingAward.getTenderId());
        awardMapper.updateAwardStatus(awardId, awardStatus);
        return awardMapper.selectById(awardId);
    }

    @Override
    public boolean checkTenderHasAwardResult(Long tenderId, Long excludeAwardId) {
        eligibilityService.ensureTenderEligible(tenderId);
        return awardMapper.countByTenderId(tenderId, excludeAwardId) > 0;
    }

    @Override
    @Transactional
    public BidAwardResult processDecision(AwardProcessDecisionReq req) {
        BidAwardResult award = awardMapper.selectById(req.getAwardId());
        if (award == null) {
            throw new RuntimeException("定标结果不存在");
        }
        eligibilityService.ensureTenderEligible(award.getTenderId());

        String action = normalize(req.getAction());
        if ("APPROVE".equals(action)) {
            if (award.getNeedRetender() != null && award.getNeedRetender() == 1) {
                // 重新招标：保持定标状态字典与主业务一致，按“已放弃”处理
                awardMapper.updateAwardStatus(award.getAwardId(), "已放弃");
            } else {
                awardMapper.updateAwardStatus(award.getAwardId(), "已定标");
                syncBidStatusAfterAwardApproved(award);
            }
            BidAwardResult latest = awardMapper.selectById(award.getAwardId());
            appendOpinion(latest, req.getOpinion());
            return latest;
        }

        if ("REJECT".equals(action)) {
            // 审批驳回后回退到待定标，避免写入流程态枚举值污染业务状态字典
            awardMapper.updateAwardStatus(award.getAwardId(), "待定标");
            BidAwardResult latest = awardMapper.selectById(award.getAwardId());
            appendOpinion(latest, req.getOpinion());
            return latest;
        }

        throw new RuntimeException("不支持的流程动作: " + req.getAction());
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }

    private Bid getBidOrThrow(Long bidId) {
        Bid bid = bidMapper.selectById(bidId);
        if (bid == null) {
            throw new RuntimeException("投标信息不存在");
        }
        if (bid.getTenderId() == null) {
            throw new RuntimeException("投标信息缺少关联招标，无法定标");
        }
        return bid;
    }

    private void appendOpinion(BidAwardResult award, String opinion) {
        if (award == null) {
            return;
        }
        if (opinion == null || opinion.trim().isEmpty()) {
            return;
        }
        String existing = award.getAwardOpinion();
        if (existing == null || existing.trim().isEmpty()) {
            award.setAwardOpinion(opinion.trim());
        } else {
            award.setAwardOpinion(existing + "\n" + opinion.trim());
        }
        award.setUpdatedTime(LocalDateTime.now());
        awardMapper.updateById(award);
    }

    private void syncBidStatusAfterAwardApproved(BidAwardResult award) {
        if (award == null || award.getTenderId() == null || award.getWinningBidId() == null) {
            return;
        }
        List<Bid> bidList = bidMapper.selectList(
                new LambdaQueryWrapper<Bid>().eq(Bid::getTenderId, award.getTenderId())
        );
        if (bidList == null || bidList.isEmpty()) {
            return;
        }
        for (Bid bid : bidList) {
            if (bid.getBidId() == null) {
                continue;
            }
            bid.setWinFlag(award.getWinningBidId().equals(bid.getBidId()) ? 1 : 0);
            bid.setBidStatus(award.getWinningBidId().equals(bid.getBidId())
                    ? BidContractConstant.BID_CONTRACT_PROJECT_WON
                    : BidContractConstant.BID_CONTRACT_PROJECT_LOST);
            bid.setUpdatedTime(LocalDateTime.now());
            bidMapper.updateById(bid);
        }
    }
}