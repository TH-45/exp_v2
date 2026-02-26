package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCompanyContactReq {
    @NotNull(message = "companyId不能为空")
    private Long companyId;
    @NotBlank(message = "contactName不能为空")
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
