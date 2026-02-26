package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCompanyContactReq {
    @NotNull(message = "contactId不能为空")
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
