package jh.exp.auth.service.bus.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.constant.AuthConstant;
import jh.exp.auth.entity.ExpOrgUnit;
import jh.exp.auth.entity.ExpPerson;
import jh.exp.auth.entity.Position;
import jh.exp.auth.entity.req.PersonExpReq;
import jh.exp.auth.entity.req.QueryPersonReq;
import jh.exp.auth.entity.res.PersonInfoRes;
import jh.exp.auth.mapper.OrgUnitMapper;
import jh.exp.auth.mapper.PersonMapper;
import jh.exp.auth.mapper.PositionMapper;
import jh.exp.auth.service.bus.PersonService;
import jh.exp.common.api.ApiResponse;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private OrgUnitMapper orgUnitMapper;
    @Autowired
    private PositionMapper positionMapper;
    /**
     * 查询人员信息
     * @param personReq
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public SimplePageRes<PersonInfoRes> queryPersonInfo(SimplePageReq<QueryPersonReq> personReq) {
        SimplePageRes<PersonInfoRes> res = new SimplePageRes<>();
        Page<PersonInfoRes> page = new Page<>(personReq.getPageNum(), personReq.getPageSize());

        IPage<PersonInfoRes> personInfoIPage= personMapper.selectPositionPage(page, personReq.getQueryParam());
        res.setTotal(personInfoIPage.getTotal());
        res.setPage(personInfoIPage.getCurrent());
        res.setSize(personInfoIPage.getSize());
        res.setList(personInfoIPage.getRecords());
        return res;
    }

    /**
     * 更新人员状态
     * @param personId
     */
    @Override
    public void updatePersonStatus(Long personId,String status) {
        ExpPerson expPerson = personMapper.selectById(personId);
        if(status.equals(AuthConstant.ENABLED)&&expPerson.getStatus().equals(AuthConstant.LEAVE)){
            throw new IllegalArgumentException("该人员已经离职不能启用，请检查！");
        }
        expPerson.setStatus(AuthConstant.isInside(status));
        personMapper.updateById(expPerson);
    }

    /**
     * 修改人员信息
     */
    @Override
    public void updatePersonInfo(PersonExpReq personExpReq) {
        Long personId = personExpReq.getPersonId();
        ExpPerson expPerson = personMapper.selectById(personId);
        if(expPerson==null){ throw new IllegalArgumentException("该人员不存在！");}
        String status = personExpReq.getStatus();
        if(AuthConstant.ENABLED.equals(status)&&AuthConstant.LEAVE.equals(expPerson.getStatus())){
            throw new IllegalArgumentException("该人员已经离职不能启用，请检查！");
        }
        CurrentUser currentUser = CurrentUserHolder.get();
        List<String> roles = currentUser.getRoles();
        if(StrUtil.isNotBlank(personExpReq.getIdCardNo())){
            if(!roles.contains(AuthConstant.ADMIN)){
                throw new IllegalArgumentException("无权限修改人员身份证信息！");
            }
        }
        ExpPerson updatePerson = new ExpPerson();
        BeanUtils.copyProperties(personExpReq, updatePerson);
        updatePerson.setUpdatedTime(LocalDateTime.now());
        personMapper.updateById(updatePerson);

    }

}
