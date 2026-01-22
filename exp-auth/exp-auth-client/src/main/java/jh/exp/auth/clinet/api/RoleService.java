package jh.exp.auth.clinet.api;

import jh.exp.auth.entity.req.*;
import jh.exp.auth.entity.res.RoleDetailRes;
import jh.exp.auth.entity.res.RoleListRes;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;
@HttpExchange("/roles")
public interface RoleService {

    /**
     * 分页查询角色列表
     */
    SimplePageRes<RoleListRes> queryRoleList(SimplePageReq<QueryRoleReq> req);

    /**
     * 根据ID查询角色详情
     */
    @PostMapping("/detail")
    RoleDetailRes getRoleById(Long roleId);

    /**
     * 创建角色
     */
    RoleDetailRes createRole(CreateRoleReq req);

    /**
     * 更新角色
     */
    RoleDetailRes updateRole(UpdateRoleReq req);

    /**
     * 删除角色
     */
    void deleteRole(Long roleId);

    /**
     * 批量删除角色
     */
    void batchDeleteRoles(BatchDeleteRoleReq req);

    /**
     * 更改角色状态
     */
    RoleDetailRes updateRoleStatus(RoleStatusReq req);

    /**
     * 批量更改角色状态
     */
    void batchUpdateRoleStatus(BatchRoleStatusReq req);

    /**
     * 检查角色编码是否存在
     */
    boolean checkRoleCodeExists(String roleCode, Long excludeRoleId);

    /**
     * 获取所有启用的角色
     */
    List<RoleListRes> getAllEnabledRoles();

}