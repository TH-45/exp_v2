package jh.exp.bid.contract.service.bus;

import jh.exp.bid.contract.entity.ExpBidAwardResult;
import jh.exp.bid.contract.entity.req.CreateAwardResultReq;

/**
 * 定标结果服务接口
 */
public interface AwardResultService {

    /**
     * 创建定标结果
     */
    ExpBidAwardResult createAwardResult(CreateAwardResultReq req);

    /**
     * 更新定标结果
     */
    ExpBidAwardResult updateAwardResult(Long awardId, CreateAwardResultReq req);

    /**
     * 删除定标结果
     */
    void deleteAwardResult(Long awardId);

    /**
     * 根据招标ID查询定标结果
     */
    ExpBidAwardResult getAwardResultByTenderId(Long tenderId);

    /**
     * 根据投标ID查询定标结果
     */
    ExpBidAwardResult getAwardResultByBidId(Long bidId);

    /**
     * 更新定标状态
     */
    ExpBidAwardResult updateAwardStatus(Long awardId, String awardStatus);

    /**
     * 检查招标项目是否已有定标结果
     */
    boolean checkTenderHasAwardResult(Long tenderId, Long excludeAwardId);
}