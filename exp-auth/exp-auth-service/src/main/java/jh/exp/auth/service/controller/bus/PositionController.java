package jh.exp.auth.service.controller.bus;

import jakarta.validation.Valid;


import jh.exp.auth.core.entity.Position;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.PositionDetailRes;
import jh.exp.auth.core.entity.res.PositionListRes;
import jh.exp.auth.service.service.bus.PositionService;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.constant.CommonConstant;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /**
     * 分页查询岗位列表
     */
    @PostMapping("/list")
    public ApiResponse<SimplePageRes<Position>> list(@RequestBody SimplePageReq<QueryPositionParam> req) {
        req.pageDefault();
        SimplePageRes<Position> result = positionService.queryPosition(req);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询岗位详情
     */
    @GetMapping("/detail")
    public ApiResponse<PositionDetailRes> detail(@RequestParam Long postId) {
        PositionDetailRes result = positionService.getPositionById(postId);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID批量查询岗位详情
     */
    @PostMapping("/batch/detail")
    public ApiResponse<Map<Long, PositionDetailRes>> batchDetail(@RequestBody List<Long> postIds) {
        Map<Long, PositionDetailRes> result = positionService.batchGetPositionByIds(postIds);
        return ApiResponse.success(result);
    }

    /**
     * 创建岗位
     */
    @PostMapping("/create")
    public ApiResponse<PositionDetailRes> create(@RequestBody @Valid CreatePositionReq req) {
        PositionDetailRes result = null;
        try {
            result = positionService.createPosition(req);
        } catch (Exception e) {
            return ApiResponse.fail(CommonConstant.ERROR_CODE_STR,e.getMessage());
        }
        return ApiResponse.success(result);
    }

    /**
     * 更新岗位
     */
    @PostMapping("/update")
    public ApiResponse<PositionDetailRes> update(@RequestBody @Valid UpdatePositionReq req) {
        PositionDetailRes result = positionService.updatePosition(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除岗位
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid DeletePositionReq req) {
        positionService.deletePosition(req.getPostId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除岗位
     */
    @PostMapping("/batchDelete")
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeletePositionReq req) {
        positionService.batchDeletePositions(req);
        return ApiResponse.success(null);
    }

    /**
     * 更新岗位状态
     */
    @PostMapping("/status")
    public ApiResponse<PositionDetailRes> updateStatus(@RequestBody @Valid PositionStatusReq req) {
        PositionDetailRes result = positionService.updatePositionStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更新岗位状态
     */
    @PostMapping("/batchStatus")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody @Valid BatchPositionStatusReq req) {
        positionService.batchUpdatePositionStatus(req);
        return ApiResponse.success(null);
    }

    /**
     * 根据组织ID查询岗位（兼容条件查询）
     */
    @PostMapping("/queryByOrg")
    public ApiResponse<SimplePageRes<PositionListRes>> queryByOrg(@RequestBody @Valid SimplePageReq<QueryPositionByOrgReq> req) {
        req.pageDefault();
        SimplePageRes<PositionListRes> result = positionService.queryPositions(req);
        return ApiResponse.success(result);
    }



    /**
     * 检查岗位编码是否存在
     */
    @GetMapping("/checkPostCode")
    public ApiResponse<Boolean> checkPostCode(@RequestParam String postCode,
                                             @RequestParam(required = false) Long excludePostId) {
        boolean exists = positionService.checkPostCodeExists(postCode, excludePostId);
        return ApiResponse.success(exists);
    }

    /**
     * 获取所有启用的岗位
     */
    @GetMapping("/enabledList")
    public ApiResponse<List<PositionListRes>> getEnabledList() {
        List<PositionListRes> result = positionService.getAllEnabledPositions();
        return ApiResponse.success(result);
    }

    /**
     * 外派岗位
     */
    @PostMapping("/outsource")
    public ApiResponse<String> outsource(@RequestBody @Valid OutsourcePositionReq req) {
        try {
            String message = positionService.outsourcePosition(req);
            return ApiResponse.success(message);
        } catch (Exception e) {
            return ApiResponse.fail(CommonConstant.ERROR_CODE_STR, e.getMessage());
        }
    }

}