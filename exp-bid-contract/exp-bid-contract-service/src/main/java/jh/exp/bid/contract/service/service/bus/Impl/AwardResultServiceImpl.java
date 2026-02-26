package jh.exp.bid.contract.service.service.bus.Impl;

import jh.exp.auth.clinet.api.bus.PersonService;

import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.entity.BidAwardResult;
import jh.exp.bid.contract.core.entity.req.CreateAwardResultReq;
import jh.exp.bid.contract.core.mapper.AwardResultMapper;
import jh.exp.bid.contract.service.service.bus.AwardResultService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 定标结果服务实现类
 */
@Service
@RequiredArgsConstructor
public class AwardResultServiceImpl implements AwardResultService {

    private final AwardResultMapper awardMapper;
    private final PersonService personService;

    @Override
    @Transactional
    public BidAwardResult createAwardResult(CreateAwardResultReq req) {
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

        // 检查是否可以删除
        if ("已签订合同".equals(award.getAwardStatus())) {
            throw new RuntimeException("已签订合同的定标结果不能删除");
        }

        awardMapper.deleteById(awardId);
    }

    @Override
    public BidAwardResult getAwardResultByTenderId(Long tenderId) {
        return awardMapper.selectAwardResultByTenderId(tenderId);
    }

    @Override
    public BidAwardResult getAwardResultByBidId(Long bidId) {
        return awardMapper.selectAwardResultByBidId(bidId);
    }

    @Override
    @Transactional
    public BidAwardResult updateAwardStatus(Long awardId, String awardStatus) {
        awardMapper.updateAwardStatus(awardId, awardStatus);
        return awardMapper.selectById(awardId);
    }

    @Override
    public boolean checkTenderHasAwardResult(Long tenderId, Long excludeAwardId) {
        return awardMapper.countByTenderId(tenderId, excludeAwardId) > 0;
    }
}