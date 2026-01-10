package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建组织请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrgUnitReq {

    /**
     * 上级组织/部门ID（根节点为0或NULL）
     */
    private Long parentOrgId;

    /**
     * 组织编码（唯一）
     */
    @NotBlank(message = "组织编码不能为空")
    private String orgCode;

    /**
     * 组织名称
     */
    @NotBlank(message = "组织名称不能为空")
    private String orgName;

    /**
     * 组织类型（COMPANY-公司/法人主体；DEPT-部门；PROJECT-项目部/项目组织；OTHER-其他）
     */
    @NotBlank(message = "组织类型不能为空")
    private String orgType;

    /**
     * 负责人人员ID（可选）
     */
    private Long managerPersonId;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 地址
     */
    private String address;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 备注
     */
    private String remark;
}