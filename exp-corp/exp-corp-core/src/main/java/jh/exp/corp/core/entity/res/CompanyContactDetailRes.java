package jh.exp.corp.core.entity.res;

import lombok.Data;

@Data
public class CompanyContactDetailRes {
    private Long contactId;
    private Long companyId;
    private String contactName;
    private String position;
    private String mobile;
    private String phone;
    private String email;
    private String wechat;
    private Integer isPrimary;
    private String status;
    private String remark;
}
