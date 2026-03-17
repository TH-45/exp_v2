package jh.exp.auth.clinet.api.bus;


import jh.exp.auth.core.entity.Position;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.PositionDetailRes;
import jh.exp.auth.core.entity.res.PositionListRes;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange("/position")
public interface PositionService {

    //分页查询职位信息
    @PostExchange("/list")
    SimplePageRes<Position> queryPosition(@RequestBody SimplePageReq<QueryPositionParam> positionReq);

    //根据ID查询岗位详情
    @GetExchange("/detail")
    PositionDetailRes getPositionById(@RequestParam("postId") Long postId);

    //根据ID批量查询岗位详情
    @PostExchange("/batch/detail")
    ApiResponse<Map<Long, PositionDetailRes>> batchGetPositionByIds(@RequestBody List<Long> postIds);

    //创建岗位
    @PostExchange("/create")
    PositionDetailRes createPosition(@RequestBody CreatePositionReq req);

    //更新岗位
    @PostExchange("/update")
    PositionDetailRes updatePosition(@RequestBody UpdatePositionReq req);

    //删除岗位
    @PostExchange("/delete")
    void deletePosition(@RequestBody DeletePositionReq req);

    //批量删除岗位
    @PostExchange("/batchDelete")
    void batchDeletePositions(@RequestBody BatchDeletePositionReq req);

    //更新岗位状态
    @PostExchange("/status")
    PositionDetailRes updatePositionStatus(@RequestBody PositionStatusReq req);

    //批量更新岗位状态
    @PostExchange("/batchStatus")
    void batchUpdatePositionStatus(@RequestBody BatchPositionStatusReq req);

    //根据组织ID查询岗位
    @PostExchange("/queryByOrg")
    SimplePageRes<PositionListRes> queryPositionsByOrgId(@RequestBody SimplePageReq<QueryPositionByOrgReq> req);

    //检查岗位编码是否存在
    @GetExchange("/checkPostCode")
    boolean checkPostCodeExists(@RequestParam("postCode") String postCode,
                                @RequestParam(value = "excludePostId", required = false) Long excludePostId);

    //获取所有启用的岗位
    @GetExchange("/enabledList")
    List<PositionListRes> getAllEnabledPositions();
}
