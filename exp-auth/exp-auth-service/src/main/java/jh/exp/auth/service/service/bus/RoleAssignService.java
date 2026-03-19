package jh.exp.auth.service.service.bus;

import jh.exp.auth.core.entity.req.RoleAssignSaveReq;
import jh.exp.auth.core.entity.res.RoleAssignRes;

/**
 * 角色授权服务：角色授权给账号/人员/岗位/组织的查询、保存。
 */
public interface RoleAssignService {

    /**
     * 根据角色ID查询授权列表（按主体类型分组，含主体名称）
     */
    RoleAssignRes listByRoleId(Long roleId);

    /**
     * 保存角色授权：替换该角色下所有授权为请求中的列表，并触发权限重建。
     */
    void save(RoleAssignSaveReq req);
}
