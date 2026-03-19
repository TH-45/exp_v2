package jh.exp.bid.contract.service.controller.bus;

import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.BidDetailRes;
import jh.exp.bid.contract.core.entity.res.BidListRes;
import jh.exp.bid.contract.service.service.bus.BidService;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投标管理控制器，对应菜单 bidding:bid
 */
@RestController
@RequestMapping("/bidding")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "bidding:bid", level = 1)
public class BidController {

    private final BidService bidService;

    /**
     * 分页查询投标列表
     */
    @PostMapping("/list")
    public ApiResponse<SimplePageRes<BidListRes>> list(@RequestBody SimplePageReq<QueryBidReq> req) {
        req.pageDefault();
        SimplePageRes<BidListRes> result = bidService.queryBidList(req);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询投标详情
     */
    @GetMapping("/detail")
    public ApiResponse<BidDetailRes> detail(@RequestParam Long bidId) {
        BidDetailRes result = bidService.getBidById(bidId);
        return ApiResponse.success(result);
    }

    /**
     * 创建投标
     */
    @PostMapping("/create")
    @RequiresMenuLevel(code = "bidding:bid", level = 2)
    public ApiResponse<BidDetailRes> create(@RequestBody @Valid CreateBidReq req) {
        BidDetailRes result = bidService.createBid(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新投标
     */
    @PostMapping("/update")
    @RequiresMenuLevel(code = "bidding:bid", level = 2)
    public ApiResponse<BidDetailRes> update(@RequestBody @Valid UpdateBidReq req) {
        BidDetailRes result = bidService.updateBid(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除投标
     */
    @PostMapping("/delete")
    @RequiresMenuLevel(code = "bidding:bid", level = 3)
    public ApiResponse<Void> delete(@RequestBody DeleteBidReq req) {
        bidService.deleteBid(req.getBidId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除投标
     */
    @PostMapping("/batchDelete")
    @RequiresMenuLevel(code = "bidding:bid", level = 3)
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteBidReq req) {
        bidService.batchDeleteBids(req);
        return ApiResponse.success(null);
    }

    /**
     * 更改投标状态
     */
    @PostMapping("/status")
    @RequiresMenuLevel(code = "bidding:bid", level = 2)
    public ApiResponse<BidDetailRes> updateStatus(@RequestBody @Valid BidStatusReq req) {
        BidDetailRes result = bidService.updateBidStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更改投标状态
     */
    @PostMapping("/batchStatus")
    @RequiresMenuLevel(code = "bidding:bid", level = 2)
    public ApiResponse<Void> batchUpdateStatus(@RequestBody @Valid BatchBidStatusReq req) {
        bidService.batchUpdateBidStatus(req);
        return ApiResponse.success(null);
    }

    /**
     * 检查投标编号是否存在
     */
    @GetMapping("/checkBidCode")
    public ApiResponse<Boolean> checkBidCode(@RequestParam String bidCode,
                                             @RequestParam(required = false) Long excludeBidId) {
        boolean exists = bidService.checkBidCodeExists(bidCode, excludeBidId);
        return ApiResponse.success(exists);
    }

    /**
     * 根据招标ID获取投标列表
     */
    @GetMapping("/tenderBids")
    public ApiResponse<List<BidListRes>> getTenderBids(@RequestParam Long tenderId) {
        List<BidListRes> result = bidService.getBidsByTenderId(tenderId);
        return ApiResponse.success(result);
    }

    /**
     * 检查投标单位是否已对招标项目投标
     */
    @GetMapping("/checkSupplierBid")
    public ApiResponse<Boolean> checkSupplierBid(@RequestParam Long tenderId,
                                                 @RequestParam Long supplierId,
                                                 @RequestParam(required = false) Long excludeBidId) {
        boolean exists = bidService.checkSupplierBidExists(tenderId, supplierId, excludeBidId);
        return ApiResponse.success(exists);
    }

    /**
     * 绑定业务员
     */
    @PostMapping("/bindSalesman")
    @RequiresMenuLevel(code = "bidding:bid", level = 2)
    public ApiResponse<Void> bindSalesman(@RequestBody @Valid BindBidSalesmanReq req) {
        bidService.bindSalesman(req);
        return ApiResponse.success(null);
    }

}