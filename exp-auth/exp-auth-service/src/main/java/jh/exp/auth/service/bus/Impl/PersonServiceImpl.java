package jh.exp.auth.service.bus.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.entity.Account;
import jh.exp.auth.entity.middle.PersonOrgPostRel;
import jh.exp.auth.mapper.middle.PersonOrgPostRelMapper;
import jh.exp.auth.service.bus.AccountService;
import jh.exp.auth.service.bus.PersonService;
import jh.exp.auth.constant.AuthConstant;

import jh.exp.auth.entity.Person;
import jh.exp.auth.entity.req.*;
import jh.exp.auth.entity.res.AccountRoleRes;
import jh.exp.auth.entity.res.PersonDetailRes;
import jh.exp.auth.entity.res.PersonInfoRes;
import jh.exp.auth.mapper.AccountMapper;
import jh.exp.auth.mapper.OrgUnitMapper;
import jh.exp.auth.mapper.PersonMapper;
import jh.exp.auth.mapper.PositionMapper;

import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import jh.exp.common.util.RandomInitialPasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonMapper personMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final PositionMapper positionMapper;
    private final AccountMapper accountMapper;
    private final AccountService accountService;
    private final PersonOrgPostRelMapper personOrgPostRelMapper;


    /**
     * 分页查询人员列表
     */
    @Override
    @Transactional(readOnly = true)
    public SimplePageRes<PersonInfoRes> queryPersonInfo(SimplePageReq<QueryPersonReq> personReq) {
        SimplePageRes<PersonInfoRes> res = new SimplePageRes<>();
        Page<PersonInfoRes> page = new Page<>(personReq.getPageNum(), personReq.getPageSize());

        IPage<PersonInfoRes> personInfoIPage = personMapper.selectPositionPage(page, personReq.getQueryParam());
        List<PersonInfoRes> personInfoList = personInfoIPage.getRecords();
        if(CollUtil.isNotEmpty(personInfoList)){
            List<Long> accounts = personInfoList.stream().map(PersonInfoRes::getAccountId).toList();
            List<AccountRoleRes> accountRoleRes = accountMapper.selectRolesByAccountIds(accounts);

            Map<Long, String> roleIdsMap = accountRoleRes.stream()
                    .collect(Collectors.groupingBy(AccountRoleRes::getAccountId, Collectors.mapping(
                            r -> String.valueOf(r.getRoleId()),
                            Collectors.joining(","))));

            Map<Long, String> roleNamesMap = accountRoleRes.stream()
                    .collect(Collectors.groupingBy(AccountRoleRes::getAccountId,
                            Collectors.mapping(
                                    AccountRoleRes::getRoleName,
                                    Collectors.joining(","))));


            personInfoList.forEach(person -> {
                Long accountId = person.getAccountId();
                person.setRoleIds(roleIdsMap.getOrDefault(accountId, ""));
                person.setRoleNames(roleNamesMap.getOrDefault(accountId, ""));
            });
        }

        res.setTotal(personInfoIPage.getTotal());
        res.setPage(personInfoIPage.getCurrent());
        res.setSize(personInfoIPage.getSize());
        res.setList(personInfoList);
        return res;
    }

    /**
     * 更新人员状态
     * @param personId
     */
    @Override
    public void updatePersonStatus(Long personId, String status) {
        Person Person = personMapper.selectById(personId);
        if(status.equals(AuthConstant.ENABLED)&&Person.getStatus().equals(AuthConstant.LEAVE)){
            throw new IllegalArgumentException("该人员已经离职不能启用，请检查！");
        }
        Person.setStatus(AuthConstant.isInside(status));
        personMapper.updateById(Person);
    }


    /**
     * 根据ID查询人员详情
     */
    @Override
    public PersonDetailRes getPersonById(Long personId) {
        PersonDetailRes personDetail = personMapper.selectPersonDetailById(personId);
        if (personDetail == null) {
            throw new RuntimeException("人员不存在");
        }
        // 使用XML多表联查已填充扩展字段：组织信息、岗位信息、账号信息、创建人信息
        return personDetail;
    }

    /**
     * 创建人员
     */
    @Override
    @Transactional
    public PersonDetailRes createPerson(CreatePersonReq req) {
        // 检查人员工号是否已存在
        if (checkPersonCodeExists(req.getPersonCode(), null)) {
            throw new RuntimeException("人员工号已存在");
        }

        Person person = new Person();
        person.setPersonCode(req.getPersonCode());
        person.setPersonName(req.getPersonName());
        person.setGender(req.getGender());
        person.setMobile(req.getMobile());
        person.setEmail(req.getEmail());
        person.setIdCardNo(req.getIdCardNo());
        person.setJobTitle(req.getJobTitle());
        person.setOrgId(req.getOrgId());
        person.setPostId(req.getPostId());
        person.setAccountId(req.getAccountId());
        person.setEntryDate(req.getEntryDate());
        person.setIsExternal(req.getIsExternal());
        person.setStatus(AuthConstant.ONJOB); // 新建人员默认为在职状态
        person.setRemark(req.getRemark());
        person.setCreatedTime(LocalDateTime.now());
        person.setUpdatedTime(LocalDateTime.now());

        CurrentUser currentUser = CurrentUserHolder.get();
        person.setCreatedBy(Long.valueOf(currentUser.getUserId()));

        try{
            // 保存人员信息
            personMapper.insert(person);
            Account account = Account.builder()
                    .accountName(RandomInitialPasswordUtil.getExpRandomId())          // 登录名 = 工号
                    .accountDisplay(req.getPersonName())
                    .passwordHash("")
                    .mobile(req.getMobile())
                    .email(req.getEmail())
                    .personId(person.getPersonId())
                    .orgId(req.getOrgId())
                    .postId(req.getPostId())
                    .status(AuthConstant.INIT)
                    .needChangePwd(true)
                    .createdBy(Long.valueOf(currentUser.getUserId()))
                    .createdTime(null)
                    .updatedTime(null)
                    .build();

            //设置组织关联信息 主组织和岗位
            PersonOrgPostRel personOrgPostRel =PersonOrgPostRel.builder()
                    .personId(person.getPersonId())
                    .orgId(req.getOrgId())
                    .postId(req.getPostId())
                    //默认角色
                    .roleId(AuthConstant.DEFAULT_ROLE)
                    .isPrimary(1)
                    //默认是一年
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .status(AuthConstant.STATUS_TBD)
                    .build();

            personOrgPostRelMapper.insert(personOrgPostRel);

            //设置默认账号
            accountMapper.insert(account);
        }catch (Exception e){
            throw new RuntimeException("创建人员失败"+e.getMessage());
        }

        // 返回创建后的详情信息
        return getPersonById(person.getPersonId());
    }

    /**
     * 更新人员
     */
    @Override
    @Transactional
    public PersonDetailRes updatePerson(UpdatePersonReq req) {
        // 检查人员是否存在
        Person existingPerson = personMapper.selectById(req.getPersonId());
        if (existingPerson == null) {
            throw new RuntimeException("人员不存在");
        }

        // 检查人员工号是否已存在（排除当前人员）
        if (checkPersonCodeExists(req.getPersonCode(), req.getPersonId())) {
            throw new RuntimeException("人员工号已存在");
        }

        Person person = new Person();
        person.setPersonId(req.getPersonId());
        person.setPersonCode(req.getPersonCode());
        person.setPersonName(req.getPersonName());
        person.setGender(req.getGender());
        person.setMobile(req.getMobile());
        person.setEmail(req.getEmail());
        person.setIdCardNo(req.getIdCardNo());
        person.setJobTitle(req.getJobTitle());
        person.setOrgId(req.getOrgId());
        person.setPostId(req.getPostId());
        person.setAccountId(req.getAccountId());
        person.setEntryDate(req.getEntryDate());
        person.setLeaveDate(req.getLeaveDate());
        person.setIsExternal(req.getIsExternal());
        person.setRemark(req.getRemark());
        person.setUpdatedTime(LocalDateTime.now());

        personMapper.updateById(person);

        // 返回更新后的详情信息
        return getPersonById(req.getPersonId());
    }

    /**
     * 删除人员
     */
    @Override
    @Transactional
    public void deletePerson(Long personId) {
        // 检查人员是否存在
        Person person = personMapper.selectById(personId);
        if (person == null) {
            throw new RuntimeException("人员不存在");
        }

        // TODO: 检查人员是否有相关联的业务数据，如果有则不允许删除

        personMapper.deleteById(personId);
    }

    /**
     * 批量删除人员
     */
    @Override
    @Transactional
    public void batchDeletePersons(BatchDeletePersonReq req) {
        if (CollectionUtils.isEmpty(req.getPersonIds())) {
            return;
        }

        // 检查所有人员是否存在
        for (Long personId : req.getPersonIds()) {
            Person person = personMapper.selectById(personId);
            if (person == null) {
                throw new RuntimeException("人员ID " + personId + " 不存在");
            }
            // TODO: 检查人员是否有相关联的业务数据
        }

        // 批量删除
        personMapper.deleteBatchIds(req.getPersonIds());
    }

    /**
     * 更改人员状态
     */
    @Override
    @Transactional
    public PersonDetailRes updatePersonStatus(PersonStatusReq req) {
        // 检查人员是否存在
        Person person = personMapper.selectById(req.getPersonId());
        if (person == null) {
            throw new RuntimeException("人员不存在");
        }

        // 更新状态
        String status = req.getStatus();
        UpdateWrapper<Person> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("person_id", req.getPersonId())
                .set("status", status)
                .set("updated_time", LocalDateTime.now());

        personMapper.update(null, updateWrapper);


        if(AuthConstant.LEAVE.equals(status)||AuthConstant.DISABLED.equals(status)){
            status=AuthConstant.DISABLED;
        }else if(AuthConstant.ONJOB.equals(status)){
            status=AuthConstant.ENABLED;
        }else{
            status=AuthConstant.ENABLED;
        }

        accountService.updateAccountStatus(new AccountStatusReq(req.getPersonId(),status));

        // 返回更新后的详情信息
        return getPersonById(req.getPersonId());
    }

    /**
     * 批量更改人员状态
     */
    @Override
    @Transactional
    public void batchUpdatePersonStatus(BatchPersonStatusReq req) {
        if (CollectionUtils.isEmpty(req.getPersonIds())) {
            return;
        }

        // 检查所有人员是否存在
        for (Long personId : req.getPersonIds()) {
            Person person = personMapper.selectById(personId);
            if (person == null) {
                throw new RuntimeException("人员ID " + personId + " 不存在");
            }
        }

        // 批量更新状态
        UpdateWrapper<Person> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("person_id", req.getPersonIds())
                .set("status", req.getStatus())
                .set("updated_time", LocalDateTime.now());

        personMapper.update(null, updateWrapper);
    }

    /**
     * 检查人员工号是否存在
     */
    @Override
    public boolean checkPersonCodeExists(String personCode, Long excludePersonId) {
        return personMapper.countByPersonCode(personCode, excludePersonId) > 0;
    }


}
