package jh.exp.auth.core.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryAccountParam {
    //账号名称
    private String accountName;
    //账号人姓名
    private String personName;;
    //人员编码
    private String personCode;
    //手机号
    private String mobile;
}
