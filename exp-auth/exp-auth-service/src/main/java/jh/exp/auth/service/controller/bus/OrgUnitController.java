package jh.exp.auth.service.controller.bus;





import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.OrgUnitDetailRes;
import jh.exp.auth.core.entity.res.OrgUnitListRes;
import jh.exp.auth.core.entity.res.OrgUnitTreeRes;
import jh.exp.auth.service.service.bus.OrgUnitService;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 组织管理接口。
 * 与岗位管理同属「岗位管理」页面（左侧组织树），权限编码统一为 system:organdpostandpost。
 */
@RestController
@RequestMapping("/orgunit")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "system:organdpost", level = 1)
public class OrgUnitController {

    private final OrgUnitService orgUnitService;

    /**
     * 分页查询组织列表
     */
    @PostMapping("/list")
    public ApiResponse<SimplePageRes<OrgUnitListRes>> list(@RequestBody SimplePageReq<QueryOrgUnitReq> req) {
        req.pageDefault();
        SimplePageRes<OrgUnitListRes> result = orgUnitService.queryOrgUnitList(req);
        return ApiResponse.success(result);
    }

    /**
     * 查询组织树
     */
    @GetMapping("/tree")
    public ApiResponse<List<OrgUnitTreeRes>> tree(QueryOrgUnitReq req) {
        if (req == null) {
            req = new QueryOrgUnitReq();
        }
        List<OrgUnitTreeRes> result = orgUnitService.queryOrgUnitTree(req);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询组织详情
     */
    @GetMapping("/detail")
    public ApiResponse<OrgUnitDetailRes> detail(@RequestParam Long orgId) {
        OrgUnitDetailRes result = orgUnitService.getOrgUnitById(orgId);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID批量查询组织详情
     */
    @PostMapping("/batch/detail")
    public ApiResponse<Map<Long, OrgUnitDetailRes>> batchDetail(@RequestBody List<Long> orgIds) {
        Map<Long, OrgUnitDetailRes> result = orgUnitService.batchGetOrgUnitByIds(orgIds);
        return ApiResponse.success(result);
    }

    /**
     * 创建组织
     */
    @PostMapping("/create")
    @RequiresMenuLevel(code = "system:organdpost", level = 2)
    public ApiResponse<OrgUnitDetailRes> create(@RequestBody @Valid CreateOrgUnitReq req) {
        OrgUnitDetailRes result = orgUnitService.createOrgUnit(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新组织
     */
    @PostMapping("/update")
    @RequiresMenuLevel(code = "system:organdpost", level = 2)
    public ApiResponse<OrgUnitDetailRes> update(@RequestBody @Valid UpdateOrgUnitReq req) {
        OrgUnitDetailRes result = orgUnitService.updateOrgUnit(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除组织
     */
    @PostMapping("/delete")
    @RequiresMenuLevel(code = "system:organdpost", level = 3)
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteOrgUnitReq req) {
        orgUnitService.deleteOrgUnit(req.getOrgId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除组织
     */
    @PostMapping("/batchDelete")
    @RequiresMenuLevel(code = "system:organdpost", level = 3)
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteOrgUnitReq req) {
        orgUnitService.batchDeleteOrgUnits(req);
        return ApiResponse.success(null);
    }

    /**
     * 更改组织状态
     */
    @PostMapping("/status")
    @RequiresMenuLevel(code = "system:organdpost", level = 2)
    public ApiResponse<OrgUnitDetailRes> updateStatus(@RequestBody @Valid OrgUnitStatusReq req) {
        OrgUnitDetailRes result = orgUnitService.updateOrgUnitStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更改组织状态
     */
    @PostMapping("/batchStatus")
    @RequiresMenuLevel(code = "system:organdpost", level = 2)
    public ApiResponse<Void> batchUpdateStatus(@RequestBody @Valid BatchOrgUnitStatusReq req) {
        orgUnitService.batchUpdateOrgUnitStatus(req);
        return ApiResponse.success(null);
    }

    /**
     * 移动组织（更改组织树结构）
     */
    @PostMapping("/move")
    @RequiresMenuLevel(code = "system:organdpost", level = 2)
    public ApiResponse<OrgUnitDetailRes> move(@RequestBody @Valid MoveOrgUnitReq req) {
        OrgUnitDetailRes result = orgUnitService.moveOrgUnit(req);
        return ApiResponse.success(result);
    }

    /**
     * 检查组织编码是否存在
     */
    @GetMapping("/checkOrgCode")
    public ApiResponse<Boolean> checkOrgCode(@RequestParam String orgCode,
                                            @RequestParam(required = false) Long excludeOrgId) {
        boolean exists = orgUnitService.checkOrgCodeExists(orgCode, excludeOrgId);
        return ApiResponse.success(exists);
    }
}