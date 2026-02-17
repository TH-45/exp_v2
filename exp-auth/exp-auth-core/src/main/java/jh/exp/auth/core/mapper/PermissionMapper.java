package jh.exp.auth.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.auth.core.entity.Permission;
import jh.exp.auth.core.entity.exp.PermissionExp;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    //根据角色id查询权限
    List<PermissionExp> selectPermissionsByRoleId(Long roleId);
}
