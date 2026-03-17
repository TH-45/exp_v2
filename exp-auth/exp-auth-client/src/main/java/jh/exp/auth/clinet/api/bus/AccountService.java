package jh.exp.auth.clinet.api.bus;

import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.AccountDetailRes;
import jh.exp.auth.core.entity.res.AccountListRes;
import jh.exp.auth.core.entity.res.AccountRoleRes;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange("/account")
public interface AccountService {
    /**
     * 分页查询账号列表
     */
    @PostExchange("/list")
    SimplePageRes<AccountListRes> queryAccountList(@RequestBody SimplePageReq<QueryAccountParam> req);

    /**
     * 根据 ID 查询账号详情
     */
    @GetExchange("/detail")
    ApiResponse<AccountDetailRes> getAccountById(@RequestParam Long accountId);

    /**
     * 创建账号
     */
    @PostExchange("/create")
    AccountDetailRes createAccount(@RequestBody CreateAccountReq req);

    /**
     * 更新账号
     */
    @PostExchange("/update")
    AccountDetailRes updateAccount(@RequestBody UpdateAccountReq req);

    /**
     * 删除账号
     */
    @PostExchange("/delete")
    void deleteAccount(@RequestBody DeleteAccountReq req);

    /**
     * 批量删除账号
     */
    @PostExchange("/batchDelete")
    void batchDeleteAccounts(@RequestBody BatchDeleteAccountReq req);

    /**
     * 更改账号状态
     */
    @PostExchange("/status")
    AccountDetailRes updateAccountStatus(@RequestBody AccountStatusReq req);

    /**
     * 批量更改账号状态
     */
    @PostExchange("/batchStatus")
    void batchUpdateAccountStatus(@RequestBody BatchAccountStatusReq req);

    /**
     * 重置密码（支持批量）
     */
    @PostExchange("/resetPassword")
    void resetPassword(@RequestBody ResetPasswordReq req);

    /**
     * 检查账号名称是否存在
     */
    @GetExchange("/checkAccountName")
    boolean checkAccountNameExists(@RequestParam String accountName, @RequestParam(required = false) Long excludeAccountId);

    /**
     * 获取账号角色信息
     */
    @PostExchange("/roles")
    ApiResponse<List<AccountRoleRes>> getAccountRoles(@RequestBody Map<String, List<Long>> req);
}
