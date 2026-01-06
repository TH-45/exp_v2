package jh.exp.auth.controller.bus;

import jh.exp.auth.entity.Account;
import jh.exp.auth.entity.req.QueryAccountPara;
import jh.exp.auth.service.bus.AccountService;
import jh.exp.common.api.ApiResponse;
import jh.exp.common.req.SimplePageReq;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {


    private AccountService accountService;

    /**
     * 查询账号信息
     * @return
     */
    public ApiResponse<QueryAccountPara> getAccount(SimplePageReq<QueryAccountPara> req) {
        return null;
    }
}
