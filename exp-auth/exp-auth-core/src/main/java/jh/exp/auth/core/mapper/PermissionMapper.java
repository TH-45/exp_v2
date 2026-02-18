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
    //根据角色id查询权限
    List<PermissionExp> selectPermissionsByRoleId(Long roleId);

    //根据角色id删除权限
    void deletePermissionsByRoleId(
            @Param("roleId") Long roleId,
            @Param("permCodes") List<String> permCodes
    );

    //根据角色id新增权限，菜单编码临时储存在备注字段
    void insertPermissionsByRoleId(@Param("list")List<RolePermissionRel> rolePermissionRel);
}
