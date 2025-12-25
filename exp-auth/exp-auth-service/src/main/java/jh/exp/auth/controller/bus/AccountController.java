package jh.exp.auth.controller.bus;

import jh.exp.auth.entity.Account;
import jh.exp.common.api.ApiResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {

    public ApiResponse<Account> getAccount() {
        return null;
    }
}
