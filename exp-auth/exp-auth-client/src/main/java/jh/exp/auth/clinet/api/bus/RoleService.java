package jh.exp.auth.clinet.api.bus;


import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.RoleDetailRes;
import jh.exp.auth.core.entity.res.RoleListRes;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/roles")
public interface RoleService {

    /**
     * 分页查询角色列表
     */
    @PostExchange("/list")
    SimplePageRes<RoleListRes> queryRoleList(@RequestBody SimplePageReq<QueryRoleReq> req);

    /**
     * 根据ID查询角色详情
     */
    @GetExchange("/detail")
    RoleDetailRes getRoleById(@RequestParam("roleId") Long roleId);

    /**
     * 创建角色
     */
    @PostExchange("/create")
    RoleDetailRes createRole(@RequestBody CreateRoleReq req);

    /**
     * 更新角色
     */
    @PostExchange("/update")
    RoleDetailRes updateRole(@RequestBody UpdateRoleReq req);

    /**
     * 删除角色
     */
    @PostExchange("/delete")
    void deleteRole(@RequestBody DeleteRoleReq req);

    /**
     * 批量删除角色
     */
    @PostExchange("/batchDelete")
    void batchDeleteRoles(@RequestBody BatchDeleteRoleReq req);

    /**
     * 更改角色状态
     */
    @PostExchange("/status")
    RoleDetailRes updateRoleStatus(@RequestBody RoleStatusReq req);

    /**
     * 批量更改角色状态
     */
    @PostExchange("/batchStatus")
    void batchUpdateRoleStatus(@RequestBody BatchRoleStatusReq req);

    /**
     * 检查角色编码是否存在
     */
    @GetExchange("/checkRoleCode")
    boolean checkRoleCodeExists(@RequestParam("roleCode") String roleCode,
                                @RequestParam(value = "excludeRoleId", required = false) Long excludeRoleId);

    /**
     * 获取所有启用的角色
     */
    @GetExchange("/enabledList")
    List<RoleListRes> getAllEnabledRoles();

}