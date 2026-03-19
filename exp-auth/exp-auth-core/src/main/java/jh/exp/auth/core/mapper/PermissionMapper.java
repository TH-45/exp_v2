package jh.exp.auth.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.auth.core.entity.Permission;
import jh.exp.auth.core.entity.exp.PermissionExp;
import jh.exp.auth.core.entity.middle.RolePermissionRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    /** 根据角色id查询权限 */
    List<PermissionExp> selectPermissionsByRoleId(Long roleId);

    /** 根据角色ID列表批量查询权限（含 grant_type） */
    List<PermissionExp> selectPermissionsByRoleIds(@Param("roleIds") List<Long> roleIds);

    //根据角色id删除权限
    void deletePermissionsByRoleId(
            @Param("roleId") Long roleId,
            @Param("permCodes") List<String> permCodes
    );

    void insertPermissionsByRoleId(@Param("list")List<RolePermissionRel> rolePermissionRel);

    /** 检查 menuCode 对应的权限是否被任意角色使用 */
    long countRolePermissionByMenuCode(@Param("menuCode") String menuCode);

    /** 根据 permCode 查询使用该权限的角色ID列表（用于权限变更时找出受影响角色） */
    List<Long> selectRoleIdsByPermCode(@Param("permCode") String permCode);
}
