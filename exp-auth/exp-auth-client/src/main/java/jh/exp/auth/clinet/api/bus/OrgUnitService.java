package jh.exp.auth.clinet.api.bus;


import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.OrgUnitDetailRes;
import jh.exp.auth.core.entity.res.OrgUnitListRes;
import jh.exp.auth.core.entity.res.OrgUnitTreeRes;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange("/orgunit")
public interface OrgUnitService {

    /**
     * 分页查询组织列表
     */
    @PostExchange("/list")
    SimplePageRes<OrgUnitListRes> queryOrgUnitList(@RequestBody SimplePageReq<QueryOrgUnitReq> req);

    /**
     * 查询组织树
     */
    @GetExchange("/tree")
    List<OrgUnitTreeRes> queryOrgUnitTree(QueryOrgUnitReq req);

    /**
     * 根据ID查询组织详情
     */
    @GetExchange("/detail")
    ApiResponse<OrgUnitDetailRes> getOrgUnitById(@RequestParam("orgId") Long orgId);

    /**
     * 根据ID批量查询组织详情
     */
    @PostExchange("/batch/detail")
    ApiResponse<Map<Long, OrgUnitDetailRes>> batchGetOrgUnitByIds(@RequestBody List<Long> orgIds);

    /**
     * 创建组织
     */
    @PostExchange("/create")
    OrgUnitDetailRes createOrgUnit(@RequestBody CreateOrgUnitReq req);

    /**
     * 更新组织
     */
    @PostExchange("/update")
    OrgUnitDetailRes updateOrgUnit(@RequestBody UpdateOrgUnitReq req);

    /**
     * 删除组织
     */
    @PostExchange("/delete")
    void deleteOrgUnit(@RequestBody DeleteOrgUnitReq req);

    /**
     * 批量删除组织
     */
    @PostExchange("/batchDelete")
    void batchDeleteOrgUnits(@RequestBody BatchDeleteOrgUnitReq req);

    /**
     * 更改组织状态
     */
    @PostExchange("/status")
    OrgUnitDetailRes updateOrgUnitStatus(@RequestBody OrgUnitStatusReq req);

    /**
     * 批量更改组织状态
     */
    @PostExchange("/batchStatus")
    void batchUpdateOrgUnitStatus(@RequestBody BatchOrgUnitStatusReq req);

    /**
     * 移动组织（更改组织树结构）
     */
    @PostExchange("/move")
    OrgUnitDetailRes moveOrgUnit(@RequestBody MoveOrgUnitReq req);

    /**
     * 检查组织编码是否存在
     */
    @GetExchange("/checkOrgCode")
    boolean checkOrgCodeExists(@RequestParam("orgCode") String orgCode,
                               @RequestParam(value = "excludeOrgId", required = false) Long excludeOrgId);
}