package jh.exp.auth.service.bus;

import jh.exp.auth.entity.req.*;
import jh.exp.auth.entity.res.AccountDetailRes;
import jh.exp.auth.entity.res.AccountListRes;

import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;


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
}
