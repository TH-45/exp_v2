package jh.exp.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.entity.ExpPerson;
import jh.exp.auth.entity.exp.PersonExp;
import jh.exp.auth.entity.req.QueryPersonReq;
import jh.exp.auth.entity.res.PersonInfoRes;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PersonMapper extends BaseMapper<ExpPerson> {

    IPage<PersonInfoRes> selectPositionPage(Page<PersonInfoRes> page, QueryPersonReq queryParam);

    void updateByExp(PersonExp personExpReq);
}
