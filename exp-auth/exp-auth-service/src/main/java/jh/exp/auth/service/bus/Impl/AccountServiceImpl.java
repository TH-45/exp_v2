package jh.exp.auth.service.bus.Impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jh.exp.auth.service.bus.AccountService;
import jh.exp.auth.core.constant.AuthConstant;
import jh.exp.auth.entity.Account;
import jh.exp.auth.entity.Person;
import jh.exp.auth.entity.res.AccountDetailRes;
import jh.exp.auth.entity.res.AccountListRes;
import jh.exp.auth.entity.res.AccountRoleRes;
import jh.exp.auth.entity.req.*;
import jh.exp.auth.core.mapper.AccountMapper;
import jh.exp.auth.core.mapper.PersonMapper;

import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;

    private final PersonMapper personMapper;

//    private final PersonService personService;

    @Override
    public SimplePageRes<AccountListRes> queryAccountList(SimplePageReq<QueryAccountParam> req) {
        // 创建分页对象
        Page<AccountListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryAccountParam queryParam = req.getQueryParam();
        // 如果前端没有传递查询参数，创建一个默认的空对象
        if (queryParam == null) {
            queryParam = new QueryAccountParam();
        }

        // 使用MyBatis-Plus自动分页查询
        IPage<AccountListRes> result = accountMapper.selectAccountList(page,
                queryParam.getAccountName(),
                queryParam.getPersonName(),
                queryParam.getMobile());

        // 转换为统一的响应格式
        SimplePageRes<AccountListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(result.getRecords());
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        return pageRes;
    }

    @Override
    public AccountDetailRes getAccountById(Long accountId) {
        AccountDetailRes accountDetail = accountMapper.selectAccountDetailById(accountId);
        if (accountDetail == null) {
            throw new RuntimeException("账号不存在");
        }
        // 使用XML多表联查已填充扩展字段：人员信息、组织信息、岗位信息、创建人信息
        return accountDetail;
    }

    @Override
    @Transactional
    public AccountDetailRes createAccount(CreateAccountReq req) {
        // 检查账号名称是否已存在
        if (checkAccountNameExists(req.getAccountName(), null)) {
            throw new RuntimeException("账号名称已存在");
        }
//        PersonDetailRes person = personService.getPersonById(req.getPersonId());


        Account account = new Account();
        account.setAccountName(req.getAccountName());
        account.setAccountDisplay(req.getAccountDisplay());
        account.setPasswordHash(BCrypt.hashpw(AuthConstant.INITIAL_PASSWORD));
        account.setMobile(req.getMobile());
        account.setEmail(req.getEmail());
        account.setPersonId(req.getPersonId());
        account.setOrgId(req.getOrgId());
        account.setPostId(req.getPostId());
        account.setStatus("ENABLED"); // 新建账号默认为启用状态
        account.setNeedChangePwd(false);
        account.setRemark(req.getRemark());
        account.setCreatedTime(LocalDateTime.now());
        account.setUpdatedTime(LocalDateTime.now());
        CurrentUser currentUser = CurrentUserHolder.get();
        account.setCreatedBy(Long.valueOf(currentUser.getUserId()));
        accountMapper.insert(account);

        // 返回创建后的账号详情信息
        return getAccountById(account.getAccountId());
    }

    @Override
    @Transactional
    public AccountDetailRes updateAccount(UpdateAccountReq req) {
        // 检查账号是否存在
        Account existingAccount = accountMapper.selectById(req.getAccountId());
        if (existingAccount == null) {
            throw new RuntimeException("账号不存在");
        }

        // 检查账号名称是否已存在（排除当前账号）
        if (checkAccountNameExists(existingAccount.getAccountName(), req.getAccountId())) {
            throw new RuntimeException("账号名称已存在");
        }

        Account account = new Account();
        account.setAccountId(req.getAccountId());
        account.setAccountDisplay(req.getAccountDisplay());
        account.setMobile(req.getMobile());
        account.setEmail(req.getEmail());
        account.setPersonId(req.getPersonId());
        account.setOrgId(req.getOrgId());
        account.setPostId(req.getPostId());
        account.setRemark(req.getRemark());
        account.setUpdatedTime(LocalDateTime.now());

        accountMapper.updateById(account);

        // 返回更新后的账号信息
        return getAccountById(req.getAccountId());
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        // 检查账号是否存在
        Account account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new RuntimeException("账号不存在");
        }



        accountMapper.deleteById(accountId);
    }

    @Override
    @Transactional
    public void batchDeleteAccounts(BatchDeleteAccountReq req) {
        if (CollectionUtils.isEmpty(req.getAccountIds())) {
            return;
        }

        // 检查所有账号是否存在
        for (Long accountId : req.getAccountIds()) {
            Account account = accountMapper.selectById(accountId);
            if (account == null) {
                throw new RuntimeException("账号ID " + accountId + " 不存在");
            }
            // TODO: 检查账号是否有相关联的业务数据
        }

        // 批量删除
        accountMapper.deleteBatchIds(req.getAccountIds());
    }

    @Override
    @Transactional
    public AccountDetailRes updateAccountStatus(AccountStatusReq req) {
        // 检查账号是否存在
        Account account = accountMapper.selectById(req.getAccountId());
        if (account == null) {
            throw new RuntimeException("账号不存在");
        }

        // 更新状态
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("account_id", req.getAccountId())
                .set("status", req.getStatus())
                .set("updated_time", LocalDateTime.now());

        accountMapper.update(null, updateWrapper);

        // 返回更新后的账号信息
        return getAccountById(req.getAccountId());
    }

    @Override
    @Transactional
    public void batchUpdateAccountStatus(BatchAccountStatusReq req) {
        if (CollectionUtils.isEmpty(req.getAccountIds())) {
            return;
        }

        // 检查所有账号是否存在
        for (Long accountId : req.getAccountIds()) {
            Account account = accountMapper.selectById(accountId);
            if (account == null) {
                throw new RuntimeException("账号ID " + accountId + " 不存在");
            }
        }

        // 批量更新状态
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("account_id", req.getAccountIds())
                .set("status", req.getStatus())
                .set("updated_time", LocalDateTime.now());

        accountMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordReq req) {
        if (req.getAccountIds() == null || req.getAccountIds().isEmpty()) {
            return;
        }

        // 检查所有账号是否存在
        for (Long accountId : req.getAccountIds()) {
            Account account = accountMapper.selectById(accountId);
            if (account == null) {
                throw new RuntimeException("账号ID " + accountId + " 不存在");
            }
        }

        // 批量更新密码和相关字段
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("account_id", req.getAccountIds())
                .set("password_hash", BCrypt.hashpw(req.getNewPassword()))
                .set("pwd_last_change_time", LocalDateTime.now())
                .set("need_change_pwd", false)
                .set("login_fail_count", 0) // 重置登录失败次数
                .set("updated_time", LocalDateTime.now());

        accountMapper.update(null, updateWrapper);
    }

    @Override
    public boolean checkAccountNameExists(String accountName, Long excludeAccountId) {
        return accountMapper.countByAccountName(accountName, excludeAccountId) > 0;
    }

    @Override
    public List<AccountRoleRes> getAccountRoles(List<Long> accountIds) {
        if (CollectionUtils.isEmpty(accountIds)) {
            return Collections.emptyList();
        }
        return accountMapper.selectRolesByAccountIds(accountIds);
    }

    @Override
    @Transactional
    public AccountDetailRes linkAccountToPerson(LinkAccountPersonReq req) {
        // 检查账号是否存在
        Account account = accountMapper.selectById(req.getAccountId());
        if (account == null) {
            throw new RuntimeException("账号不存在");
        }

        // 检查人员是否存在
        Person person = personMapper.selectById(req.getPersonId());
        if (person == null) {
            throw new RuntimeException("人员不存在");
        }

        // 检查账号是否已经被关联到其他人员
        if (account.getPersonId() != null && !account.getPersonId().equals(req.getPersonId())) {
            throw new RuntimeException("该账号已被关联到其他人员");
        }

        // 检查人员是否已经被关联到其他账号
        if (person.getAccountId() != null && !person.getAccountId().equals(req.getAccountId())) {
            throw new RuntimeException("该人员已被关联到其他账号");
        }

        // 更新账号信息：关联人员，更新个人信息（不包括组织和岗位）
        Account updateAccount = new Account();
        updateAccount.setAccountId(req.getAccountId());
        updateAccount.setAccountName(req.getAccountName()); // 更新账号名称
        updateAccount.setCreatedBy(account.getCreatedBy());// 保持创建人
        updateAccount.setStatus(AuthConstant.ENABLED);
        updateAccount.setUpdatedTime(LocalDateTime.now());
        updateAccount.setCreatedTime(account.getCreatedTime());

        accountMapper.updateById(updateAccount);

        // 返回更新后的账号详情信息
        return getAccountById(req.getAccountId());
    }
}
