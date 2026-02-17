package jh.exp.auth.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.auth.core.entity.RoleAssign;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleAssignMapper extends BaseMapper<RoleAssign> {
    //根据角色ID查询角色授权信息
}
