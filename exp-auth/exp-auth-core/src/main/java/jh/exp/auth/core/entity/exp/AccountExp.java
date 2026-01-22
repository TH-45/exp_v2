package jh.exp.auth.core.entity.exp;



import jh.exp.auth.core.entity.Account;
import jh.exp.common.core.ext.ExtEntity;

import java.io.Serializable;

public class AccountExp extends Account implements ExtEntity, Serializable {
    private static final long serialVersionUID = 1L;
    // 员工编号
    private String personCode;
    // 姓名
    private String personName;

    //组织代码
    private String orgCode;

    // 组织名称（部门/公司/项目部名称）
    private String orgName;

    private String status;

    //岗位代码
    private String postCode;
    private String postName;

    private String createdName;
    @Override
    public String[] sensitiveFieldsList() {
        return new String[] {"passwordHash", "passwordSalt","idCardNo","loginFailCount","lastLoginTime"
                ,"lastLoginIp","pwdLastChangeTime","needChangePwd","createdTime","updatedTime"};
    }
}
