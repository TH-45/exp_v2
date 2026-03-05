package jh.exp.bid.contract.service.service.bus;

import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.BidDetailRes;
import jh.exp.bid.contract.core.entity.res.BidListRes;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;

import java.util.List;

/**
 * 投标服务接口
 */
public interface BidService {

    /**
     * 分页查询投标列表
     */
    SimplePageRes<BidListRes> queryBidList(SimplePageReq<QueryBidReq> req);

    /**
     * 根据ID查询投标详情
     */
    BidDetailRes getBidById(Long bidId);

    /**
     * 创建投标
     */
    BidDetailRes createBid(CreateBidReq req);

    /**
     * 更新投标
     */
    BidDetailRes updateBid(UpdateBidReq req);

    /**
     * 删除投标
     */
    void deleteBid(Long bidId);

    /**
     * 批量删除投标
     */
    void batchDeleteBids(BatchDeleteBidReq req);

    /**
     * 更改投标状态
     */
    BidDetailRes updateBidStatus(BidStatusReq req);

    /**
     * 批量更改投标状态
     */
    void batchUpdateBidStatus(BatchBidStatusReq req);

    /**
     * 检查投标编号是否存在
     */
    boolean checkBidCodeExists(String bidCode, Long excludeBidId);

    /**
     * 根据招标ID获取投标列表
     */
    List<BidListRes> getBidsByTenderId(Long tenderId);

    /**
     * 检查投标单位是否已对该招标项目投标
     */
    boolean checkSupplierBidExists(Long tenderId, Long supplierId, Long excludeBidId);

    /**
     * 检查用户是否有删除投标的权限
     */
    boolean checkDeletePermission(Long bidId, Long userId);

    /**
     * 绑定投标业务员
     */
    void bindSalesman(BindBidSalesmanReq req);
}