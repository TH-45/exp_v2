package jh.exp.bid.contract.service.controller.bus;

import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.TenderDetailRes;
import jh.exp.bid.contract.core.entity.res.TenderListRes;
import jh.exp.bid.contract.service.service.bus.TenderService;
import jh.exp.common.annotation.RequiresPermissions;
import jh.exp.common.api.ApiResponse;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 招标管理控制器
 */
@RestController
@RequestMapping("/tender")
@RequiredArgsConstructor
public class TenderController {

    private final TenderService tenderService;

    /**
     * 分页查询招标列表
     */
    @PostMapping("/list")
    @RequiresPermissions("TENDER:VIEW")
    public ApiResponse<SimplePageRes<TenderListRes>> list(@RequestBody SimplePageReq<QueryTenderReq> req) {
        req.pageDefault();
        SimplePageRes<TenderListRes> result = tenderService.queryTenderList(req);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询招标详情
     */
    @GetMapping("/detail")
    @RequiresPermissions("TENDER:VIEW")
    public ApiResponse<TenderDetailRes> detail(@RequestParam Long tenderId) {
        TenderDetailRes result = tenderService.getTenderById(tenderId);
        return ApiResponse.success(result);
    }

    /**
     * 创建招标
     */
    @PostMapping("/create")
    @RequiresPermissions("TENDER:ADD")
    public ApiResponse<TenderDetailRes> create(@RequestBody @Valid CreateTenderReq req) {
        TenderDetailRes result = tenderService.createTender(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新招标
     */
    @PostMapping("/update")
    @RequiresPermissions("TENDER:EDIT")
    public ApiResponse<TenderDetailRes> update(@RequestBody @Valid UpdateTenderReq req) {
        TenderDetailRes result = tenderService.updateTender(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除招标
     */
    @PostMapping("/delete")
    @RequiresPermissions("TENDER:DELETE")
    public ApiResponse<Void> delete(@RequestBody DeleteTenderReq req) {
        tenderService.deleteTender(req.getTenderId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除招标
     */
    @PostMapping("/batchDelete")
    @RequiresPermissions("TENDER:DELETE")
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteTenderReq req) {
        tenderService.batchDeleteTenders(req);
        return ApiResponse.success(null);
    }

    /**
     * 更改招标状态
     */
    @PostMapping("/status")
    @RequiresPermissions("TENDER:EDIT")
    public ApiResponse<TenderDetailRes> updateStatus(@RequestBody @Valid TenderStatusReq req) {
        TenderDetailRes result = tenderService.updateTenderStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更改招标状态
     */
    @PostMapping("/batchStatus")
    @RequiresPermissions("TENDER:EDIT")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody @Valid BatchTenderStatusReq req) {
        tenderService.batchUpdateTenderStatus(req);
        return ApiResponse.success(null);
    }

    /**
     * 检查招标编号是否存在
     */
    @GetMapping("/checkTenderCode")
    public ApiResponse<Boolean> checkTenderCode(@RequestParam String tenderCode,
                                                 @RequestParam(required = false) Long excludeTenderId) {
        boolean exists = tenderService.checkTenderCodeExists(tenderCode, excludeTenderId);
        return ApiResponse.success(exists);
    }

    /**
     * 根据项目ID获取项目负责人信息
     */
    @GetMapping("/projectManager")
    public ApiResponse<TenderDetailRes> getProjectManager(@RequestParam Long projectId) {
        TenderDetailRes result = tenderService.getProjectManagerByProjectId(projectId);
        return ApiResponse.success(result);
    }
}