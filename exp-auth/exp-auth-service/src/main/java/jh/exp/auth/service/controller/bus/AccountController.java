package jh.exp.auth.service.controller.bus;





import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.AccountDetailRes;
import jh.exp.auth.core.entity.res.AccountListRes;
import jh.exp.auth.core.entity.res.AccountRoleRes;
import jh.exp.auth.service.service.bus.AccountService;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "system:account", level = 1)
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
    @RequiresMenuLevel(code = "system:account", level = 2)
    public ApiResponse<AccountDetailRes> create(@RequestBody @Valid CreateAccountReq req) {
        AccountDetailRes result = accountService.createAccount(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新账号
     */
    @PostMapping("/update")
    @RequiresMenuLevel(code = "system:account", level = 2)
    public ApiResponse<AccountDetailRes> update(@RequestBody @Valid UpdateAccountReq req) {
        AccountDetailRes result = accountService.updateAccount(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除账号
     */
    @PostMapping("/delete")
    @RequiresMenuLevel(code = "system:account", level = 3)
    public ApiResponse<Void> delete(@RequestBody DeleteAccountReq req) {
        accountService.deleteAccount(req.getAccountId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除账号
     */
    @PostMapping("/batchDelete")
    @RequiresMenuLevel(code = "system:account", level = 3)
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteAccountReq req) {
        accountService.batchDeleteAccounts(req);
        return ApiResponse.success(null);
    }

    /**
     * 更改账号状态
     */
    @PostMapping("/status")
    @RequiresMenuLevel(code = "system:account", level = 2)
    public ApiResponse<AccountDetailRes> updateStatus(@RequestBody @Valid AccountStatusReq req) {
        AccountDetailRes result = accountService.updateAccountStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更改账号状态
     */
    @PostMapping("/batchStatus")
    @RequiresMenuLevel(code = "system:account", level = 2)
    public ApiResponse<Void> batchUpdateStatus(@RequestBody @Valid BatchAccountStatusReq req) {
        accountService.batchUpdateAccountStatus(req);
        return ApiResponse.success(null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/resetPassword")
    @RequiresMenuLevel(code = "system:account", level = 2)
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

    /**
     * 关联账号与人员
     */
    @PostMapping("/linkPerson")
    @RequiresMenuLevel(code = "system:account", level = 2)
    public ApiResponse<AccountDetailRes> linkPerson(@RequestBody @Valid LinkAccountPersonReq req) {
        AccountDetailRes result = accountService.linkAccountToPerson(req);
        return ApiResponse.success(result);
    }


}
