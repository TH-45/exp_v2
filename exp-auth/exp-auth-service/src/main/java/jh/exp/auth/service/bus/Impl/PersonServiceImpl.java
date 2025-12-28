package jh.exp.auth.service.bus.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.constant.AuthConstant;
import jh.exp.auth.entity.ExpPerson;
import jh.exp.auth.entity.exp.PersonExp;
import jh.exp.auth.entity.req.QueryPersonReq;
import jh.exp.auth.entity.res.PersonInfoRes;
import jh.exp.auth.mapper.AccountMapper;
import jh.exp.auth.mapper.OrgUnitMapper;
import jh.exp.auth.mapper.PersonMapper;
import jh.exp.auth.mapper.PositionMapper;
import jh.exp.auth.service.bus.PersonService;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private OrgUnitMapper orgUnitMapper;
    @Autowired
    private PositionMapper positionMapper;
    @Autowired
    private AccountMapper accountMapper;
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
     * 不更新的字段值为null/0
     */
    @Override
    public void updatePersonInfo(PersonExp personExp) {
        Long personId = personExp.getPersonId();
        ExpPerson expPerson = personMapper.selectById(personId);
        if(expPerson==null){ throw new IllegalArgumentException("该人员不存在！");}
        String status = personExp.getStatus();
        if(AuthConstant.ENABLED.equals(status)&&AuthConstant.LEAVE.equals(expPerson.getStatus())){
            throw new IllegalArgumentException("该人员已经离职不能启用，请检查！");
        }
        CurrentUser currentUser = CurrentUserHolder.get();
        List<String> roles = currentUser.getRoles();
        if(StrUtil.isNotBlank(personExp.getIdCardNo())){
            if(!roles.contains(AuthConstant.ADMIN)){
                throw new IllegalArgumentException("无权限修改人员身份证信息！");
            }
        }

        personMapper.updateByExp(personExp);



    }

    @Override
    public PersonExp queryPersonDetail(Long personId) {
        ExpPerson expPerson = personMapper.selectById(personId);
        if(expPerson==null){ throw new IllegalArgumentException("该人员不存在！");}
        PersonExp personExp = new PersonExp();
        BeanUtils.copyProperties(expPerson, personExp);

        String orgName = orgUnitMapper.selectById(expPerson.getOrgId()).getOrgName();
        String postName = positionMapper.selectById(expPerson.getPersonId()).getPostName();
        String accountName = accountMapper.selectById(expPerson.getAccountId()).getAccountName();
        personExp.setOrgName(orgName);
        personExp.setPostName(postName);
        personExp.setAccountName(accountName);

        return null;
    }


}
