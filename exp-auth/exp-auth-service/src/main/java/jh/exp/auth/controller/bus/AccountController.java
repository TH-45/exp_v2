package jh.exp.auth.controller.bus;

import jh.exp.auth.entity.req.*;
import jh.exp.auth.entity.res.AccountDetailRes;
import jh.exp.auth.entity.res.AccountListRes;
import jh.exp.auth.entity.res.AccountRoleRes;
import jh.exp.auth.service.bus.AccountService;
import jh.exp.common.api.ApiResponse;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 分页查询账号列表
     */
    @PostMapping("/list")
    public ApiResponse<SimplePageRes<AccountListRes>> list(@RequestBody SimplePageReq<QueryAccountParam> req) {
        req.pageDefault();
        SimplePageRes<AccountListRes> result = accountService.queryAccountList(req);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询账号详情
     */
    @GetMapping("/detail")
    public ApiResponse<AccountDetailRes> detail(@RequestParam Long accountId) {
        AccountDetailRes result = accountService.getAccountById(accountId);
        return ApiResponse.success(result);
    }

    /**
     * 创建账号
     */
    @PostMapping("/create")
    public ApiResponse<AccountDetailRes> create(@RequestBody @Valid CreateAccountReq req) {
        AccountDetailRes result = accountService.createAccount(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新账号
     */
    @PostMapping("/update")
    public ApiResponse<AccountDetailRes> update(@RequestBody @Valid UpdateAccountReq req) {
        AccountDetailRes result = accountService.updateAccount(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除账号
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody DeleteAccountReq req) {
        accountService.deleteAccount(req.getAccountId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除账号
     */
    @PostMapping("/batchDelete")
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteAccountReq req) {
        accountService.batchDeleteAccounts(req);
        return ApiResponse.success(null);
    }

    /**
     * 更改账号状态
     */
    @PostMapping("/status")
    public ApiResponse<AccountDetailRes> updateStatus(@RequestBody @Valid AccountStatusReq req) {
        AccountDetailRes result = accountService.updateAccountStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更改账号状态
     */
    @PostMapping("/batchStatus")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody @Valid BatchAccountStatusReq req) {
        accountService.batchUpdateAccountStatus(req);
        return ApiResponse.success(null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/resetPassword")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordReq req) {
        accountService.resetPassword(req);
        return ApiResponse.success(null);
    }

    /**
     * 检查账号名称是否存在
     */
    @GetMapping("/checkAccountName")
    public ApiResponse<Boolean> checkAccountName(@RequestParam String accountName,
                                                 @RequestParam(required = false) Long excludeAccountId) {
        boolean exists = accountService.checkAccountNameExists(accountName, excludeAccountId);
        return ApiResponse.success(exists);
    }

    /**
     * 获取账号角色信息
     */
    @PostMapping("/roles")
    public ApiResponse<List<AccountRoleRes>> getAccountRoles(@RequestBody Map<String, List<Long>> req) {
        List<Long> accountIds = req.get("accountIds");
        if (accountIds == null || accountIds.isEmpty()) {
            return ApiResponse.success(new ArrayList<>());
        }
        List<AccountRoleRes> result = accountService.getAccountRoles(accountIds);
        return ApiResponse.success(result);
    }
}
