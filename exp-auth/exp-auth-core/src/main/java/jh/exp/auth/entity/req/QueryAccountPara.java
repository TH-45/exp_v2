package jh.exp.auth.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueryAccountPara {
    //账号编码
    private String accountName;
    //账号名称
    private String accountDisplay;
    //手机号
    private String mobile;
}
