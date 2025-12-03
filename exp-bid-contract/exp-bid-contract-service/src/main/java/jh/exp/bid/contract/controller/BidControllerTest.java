package jh.exp.bid.contract.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bid")
public class BidControllerTest {
    @RequestMapping("/Test")
    public String Test() {
        return "bid contract service 访问成功";
    }
}
