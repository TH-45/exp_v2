package jh.exp.bid.contract.core.entity.dto;

import lombok.Data;

@Data
public class ManagerAndSalespersonDTO {
    private Long orgId;
    private String orgName;
    private Long managerId;
    private String managerName;
    //管理员角色
    private String managerRole;
    private Long salespersonId;
    private String salespersonName;
    //业务员角色
    private String salespersonRole;
}
