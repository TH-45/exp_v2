package jh.exp.auth.service.service.bus;


import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.AccountDetailRes;
import jh.exp.auth.core.entity.res.AccountListRes;
import jh.exp.auth.core.entity.res.AccountRoleRes;



import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;

import java.util.List;


public interface AccountService {
    /**
     * 分页查询账号列表
     */
    SimplePageRes<AccountListRes> queryAccountList(SimplePageReq<QueryAccountParam> req);

    /**
     * 根据ID查询账号详情
     */
    AccountDetailRes getAccountById(Long accountId);

    /**
     * 创建账号
     */
    AccountDetailRes createAccount(CreateAccountReq req);

    /**
     * 更新账号
     */
    AccountDetailRes updateAccount(UpdateAccountReq req);

    /**
     * 删除账号
     */
    void deleteAccount(Long accountId);

    /**
     * 批量删除账号
     */
    void batchDeleteAccounts(BatchDeleteAccountReq req);

    /**
     * 更改账号状态
     */
    AccountDetailRes updateAccountStatus(AccountStatusReq req);

    /**
     * 批量更改账号状态
     */
    void batchUpdateAccountStatus(BatchAccountStatusReq req);

    /**
     * 重置密码（支持批量）
     */
    void resetPassword(ResetPasswordReq req);

    /**
     * 检查账号名称是否存在
     */
    boolean checkAccountNameExists(String accountName, Long excludeAccountId);

    /**
     * 获取账号角色信息
     */
    List<AccountRoleRes> getAccountRoles(List<Long> accountIds);

    /**
     * 关联账号与人员
     */
    AccountDetailRes linkAccountToPerson(LinkAccountPersonReq req);
}
