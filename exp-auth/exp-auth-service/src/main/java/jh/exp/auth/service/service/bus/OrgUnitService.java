package jh.exp.auth.service.service.bus;





import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.OrgUnitDetailRes;
import jh.exp.auth.core.entity.res.OrgUnitListRes;
import jh.exp.auth.core.entity.res.OrgUnitTreeRes;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;

import java.util.List;
import java.util.Map;

public interface OrgUnitService {

    /**
     * 分页查询组织列表
     */
    SimplePageRes<OrgUnitListRes> queryOrgUnitList(SimplePageReq<QueryOrgUnitReq> req);

    /**
     * 查询组织树
     */
    List<OrgUnitTreeRes> queryOrgUnitTree(QueryOrgUnitReq req);

    /**
     * 根据ID查询组织详情
     */
    OrgUnitDetailRes getOrgUnitById(Long orgId);

    /**
     * 根据ID批量查询组织详情
     */
    Map<Long, OrgUnitDetailRes> batchGetOrgUnitByIds(List<Long> orgIds);

    /**
     * 创建组织
     */
    OrgUnitDetailRes createOrgUnit(CreateOrgUnitReq req);

    /**
     * 更新组织
     */
    OrgUnitDetailRes updateOrgUnit(UpdateOrgUnitReq req);

    /**
     * 删除组织
     */
    void deleteOrgUnit(Long orgId);

    /**
     * 批量删除组织
     */
    void batchDeleteOrgUnits(BatchDeleteOrgUnitReq req);

    /**
     * 更改组织状态
     */
    OrgUnitDetailRes updateOrgUnitStatus(OrgUnitStatusReq req);

    /**
     * 批量更改组织状态
     */
    void batchUpdateOrgUnitStatus(BatchOrgUnitStatusReq req);

    /**
     * 移动组织（更改组织树结构）
     */
    OrgUnitDetailRes moveOrgUnit(MoveOrgUnitReq req);

    /**
     * 检查组织编码是否存在
     */
    boolean checkOrgCodeExists(String orgCode, Long excludeOrgId);
}