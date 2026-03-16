package jh.exp.bid.contract.service.service.bus;

import jh.exp.bid.contract.core.entity.BidAwardResult;
import jh.exp.bid.contract.core.entity.req.AwardProcessDecisionReq;
import jh.exp.bid.contract.core.entity.req.CreateAwardResultReq;

/**
 * 定标结果服务接口
 */
public interface AwardResultService {

    /**
     * 创建定标结果
     */
    BidAwardResult createAwardResult(CreateAwardResultReq req);

    /**
     * 更新定标结果
     */
    BidAwardResult updateAwardResult(Long awardId, CreateAwardResultReq req);

    /**
     * 删除定标结果
     */
    void deleteAwardResult(Long awardId);

    /**
     * 根据招标ID查询定标结果
     */
    BidAwardResult getAwardResultByTenderId(Long tenderId);

    /**
     * 根据投标ID查询定标结果
     */
    BidAwardResult getAwardResultByBidId(Long bidId);

    /**
     * 根据定标结果ID查询详情
     */
    BidAwardResult getAwardResultById(Long awardId);

    /**
     * 更新定标状态
     */
    BidAwardResult updateAwardStatus(Long awardId, String awardStatus);

    /**
     * 检查招标项目是否已有定标结果
     */
    boolean checkTenderHasAwardResult(Long tenderId, Long excludeAwardId);

    /**
     * 处理定标审批流程回调决策
     */
    BidAwardResult processDecision(AwardProcessDecisionReq req);
}