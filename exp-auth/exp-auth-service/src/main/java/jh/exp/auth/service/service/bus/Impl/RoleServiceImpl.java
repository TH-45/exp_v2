package jh.exp.auth.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import jh.exp.auth.core.entity.Role;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.RoleDetailRes;
import jh.exp.auth.core.entity.res.RoleListRes;
import jh.exp.auth.core.mapper.RoleMapper;
import jh.exp.auth.service.service.bus.RoleService;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.exception.BizException;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    /**
     * 分页查询角色列表
     */
    @Override
    public SimplePageRes<RoleListRes> queryRoleList(SimplePageReq<QueryRoleReq> req) {
        req.pageDefault();
        Page<RoleListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryRoleReq queryParam = req.getQueryParam();
        if (queryParam == null) {
            queryParam = new QueryRoleReq();
        }

        IPage<RoleListRes> result = roleMapper.selectRoleList(page,
                queryParam.getRoleCode(),
                queryParam.getRoleName(),
                queryParam.getStatus(),
                queryParam.getRoleType());

        SimplePageRes<RoleListRes> pageRes = new SimplePageRes<>();
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        pageRes.setList(result.getRecords());
        return pageRes;
    }

    /**
     * 根据ID查询角色详情
     */
    @Override
    public RoleDetailRes getRoleById(Long roleId) {
        RoleDetailRes result = roleMapper.selectRoleDetailById(roleId);
        if (result == null) {
            throw new BizException("角色不存在");
        }
        return result;
    }

    /**
     * 创建角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleDetailRes createRole(CreateRoleReq req) {
        // 检查角色编码是否已存在
        if (checkRoleCodeExists(req.getRoleCode(), null)) {
            throw new BizException("角色编码已存在");
        }

        // 创建角色实体
        Role role = new Role();
        BeanUtils.copyProperties(req, role);
        role.setStatus("ENABLED");
        if (role.getSortNo() == null) {
            role.setSortNo(0);
        }

        // 设置创建者信息
        CurrentUser currentUser = CurrentUserHolder.get();
        if (currentUser != null && currentUser.getUserId() != null) {
            role.setCreatedBy(Long.valueOf(currentUser.getUserId()));
        }
        role.setCreatedTime(LocalDateTime.now());
        role.setUpdatedTime(LocalDateTime.now());

        // 保存角色
        roleMapper.insert(role);

        return getRoleById(role.getRoleId());
    }

    /**
     * 更新角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleDetailRes updateRole(UpdateRoleReq req) {
        // 检查角色是否存在
        Role existingRole = roleMapper.selectById(req.getRoleId());
        if (existingRole == null) {
            throw new BizException("角色不存在");
        }

        // 检查系统内置角色不能修改
        if (existingRole.getIsSystem() != null && existingRole.getIsSystem() == 1) {
            throw new BizException("系统内置角色不能修改");
        }

        // 检查角色编码是否已存在
        if (checkRoleCodeExists(req.getRoleCode(), req.getRoleId())) {
            throw new BizException("角色编码已存在");
        }

        // 更新角色信息
        Role role = new Role();
        BeanUtils.copyProperties(req, role);
        role.setUpdatedTime(LocalDateTime.now());

        roleMapper.updateById(role);

        return getRoleById(req.getRoleId());
    }

    /**
     * 删除角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException("角色不存在");
        }

        // 检查系统内置角色不能删除
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            throw new BizException("系统内置角色不能删除");
        }

        // 检查是否有用户正在使用该角色（这里可能需要关联用户角色表，暂时跳过）

        roleMapper.deleteById(roleId);
    }

    /**
     * 批量删除角色
     * 注意：采用"全部成功或全部失败"的策略，如果任何一个删除失败，整个批量操作都会回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRoles(BatchDeleteRoleReq req) {
        for (Long roleId : req.getRoleIds()) {
            deleteRoleInternal(roleId);
        }
    }

    /**
     * 更改角色状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleDetailRes updateRoleStatus(RoleStatusReq req) {
        Role role = roleMapper.selectById(req.getRoleId());
        if (role == null) {
            throw new BizException("角色不存在");
        }

        // 验证状态值
        if (!"ENABLED".equals(req.getStatus()) && !"DISABLED".equals(req.getStatus())) {
            throw new BizException("无效的状态值");
        }

        role.setStatus(req.getStatus());
        role.setUpdatedTime(LocalDateTime.now());
        roleMapper.updateById(role);

        return getRoleById(req.getRoleId());
    }

    /**
     * 批量更改角色状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateRoleStatus(BatchRoleStatusReq req) {
        // 验证状态值
        if (!"ENABLED".equals(req.getStatus()) && !"DISABLED".equals(req.getStatus())) {
            throw new BizException("无效的状态值");
        }

        UpdateWrapper<Role> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("role_id", req.getRoleIds())
                    .set("status", req.getStatus())
                    .set("updated_time", LocalDateTime.now());

        roleMapper.update(null, updateWrapper);
    }

    /**
     * 检查角色编码是否存在
     */
    @Override
    public boolean checkRoleCodeExists(String roleCode, Long excludeRoleId) {
        return roleMapper.countByRoleCode(roleCode, excludeRoleId) > 0;
    }

    /**
     * 获取所有启用的角色
     */
    @Override
    public List<RoleListRes> getAllEnabledRoles() {
        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, "ENABLED")
                .orderByAsc(Role::getSortNo, Role::getCreatedTime));

        return roles.stream()
                .map(role -> {
                    RoleListRes result = new RoleListRes();
                    BeanUtils.copyProperties(role, result);
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 内部删除角色方法（不带事务注解，避免事务嵌套问题）
     */
    private void deleteRoleInternal(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException("角色不存在");
        }

        // 检查系统内置角色不能删除
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            throw new BizException("系统内置角色不能删除");
        }

        // 检查是否有用户正在使用该角色（这里可能需要关联用户角色表，暂时跳过）

        roleMapper.deleteById(roleId);
    }
}