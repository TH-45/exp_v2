package jh.exp.auth.clinet.api.bus;


import jh.exp.auth.core.entity.Position;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.PositionDetailRes;
import jh.exp.auth.core.entity.res.PositionListRes;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.stereotype.Service;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@Service
@HttpExchange("/position")
public interface PositionService {

    //分页查询职位信息
    SimplePageRes<Position> queryPosition(SimplePageReq<QueryPositionParam> positionReq);

    //根据ID查询岗位详情
    PositionDetailRes getPositionById(Long postId);

    //根据ID批量查询岗位详情
    @PostExchange("/batch/detail")
    Map<Long, PositionDetailRes> batchGetPositionByIds(@RequestBody List<Long> postIds);

    //创建岗位
    PositionDetailRes createPosition(CreatePositionReq req);

    //更新岗位
    PositionDetailRes updatePosition(UpdatePositionReq req);

    //删除岗位
    void deletePosition(Long postId);

    //批量删除岗位
    void batchDeletePositions(BatchDeletePositionReq req);

    //更新岗位状态
    PositionDetailRes updatePositionStatus(PositionStatusReq req);

    //批量更新岗位状态
    void batchUpdatePositionStatus(BatchPositionStatusReq req);

    //根据组织ID查询岗位
    List<PositionListRes> queryPositionsByOrgId(QueryPositionByOrgReq req);

    //检查岗位编码是否存在
    boolean checkPostCodeExists(String postCode, Long excludePostId);

    //获取所有启用的岗位
    List<PositionListRes> getAllEnabledPositions();
}
