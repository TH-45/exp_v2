package jh.exp.auth.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.auth.entity.Role;
import jh.exp.auth.entity.res.RoleDetailRes;
import jh.exp.auth.entity.res.RoleListRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 分页查询角色列表
     */
    IPage<RoleListRes> selectRoleList(IPage<RoleListRes> page,
                                     @Param("roleCode") String roleCode,
                                     @Param("roleName") String roleName,
                                     @Param("status") String status,
                                     @Param("roleType") String roleType);

    /**
     * 根据角色ID查询角色详情信息（多表联查）
     */
    RoleDetailRes selectRoleDetailById(@Param("roleId") Long roleId);

    /**
     * 检查角色编码是否存在
     */
    int countByRoleCode(@Param("roleCode") String roleCode, @Param("excludeRoleId") Long excludeRoleId);

    /**
     * 根据角色ID列表批量查询角色详情
     */
    List<RoleDetailRes> selectRoleDetailByIds(@Param("roleIds") List<Long> roleIds);

}