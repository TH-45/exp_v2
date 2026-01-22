package jh.exp.auth.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jh.exp.auth.entity.Person;
import jh.exp.auth.entity.req.QueryPersonReq;
import jh.exp.auth.entity.res.PersonDetailRes;
import jh.exp.auth.entity.res.PersonInfoRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PersonMapper extends BaseMapper<Person> {

    IPage<PersonInfoRes> selectPositionPage(Page<PersonInfoRes> page, QueryPersonReq queryParam);

    /**
     * 检查人员工号是否存在
     * @param personCode 人员工号
     * @param excludePersonId 排除的人员ID（用于更新时检查）
     * @return 存在数量
     */
    int countByPersonCode(@Param("personCode") String personCode, @Param("excludePersonId") Long excludePersonId);

    /**
     * 根据人员ID查询人员详情信息（多表联查）
     * @param personId 人员ID
     * @return 人员详情信息
     */
    PersonDetailRes selectPersonDetailById(@Param("personId") Long personId);
}
