package jh.exp.auth.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.auth.core.entity.OrgUnit;
import jh.exp.auth.core.entity.res.OrgUnitDetailRes;
import jh.exp.auth.core.entity.res.OrgUnitListRes;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrgUnitMapper extends BaseMapper<OrgUnit> {

    /**
     * 分页查询组织列表
     */
    IPage<OrgUnitListRes> selectOrgUnitList(IPage<OrgUnitListRes> page,
                                            @Param("orgCode") String orgCode,
                                            @Param("orgName") String orgName);

    /**
     * 根据组织ID查询组织详情信息（多表联查）
     */
    OrgUnitDetailRes selectOrgUnitDetailById(@Param("orgId") Long orgId);

    /**
     * 检查组织编码是否存在
     */
    int countByOrgCode(@Param("orgCode") String orgCode, @Param("excludeOrgId") Long excludeOrgId);

    /**
     * 根据组织ID列表批量查询组织详情
     */
    List<OrgUnitDetailRes> selectOrgUnitDetailByIds(@Param("orgIds") List<Long> orgIds);

    /**
     * 根据上级组织ID查询直接子组织列表
     */
    List<OrgUnit> selectChildrenByParentId(@Param("parentOrgId") Long parentOrgId);

    /**
     * 根据组织路径查询所有子组织（包含间接子组织）
     */
    List<OrgUnit> selectAllChildrenByPath(@Param("orgPath") String orgPath);
}
