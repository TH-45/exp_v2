package jh.exp.auth.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 组织详情响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgUnitDetailRes {

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 上级组织/部门ID
     */
    private Long parentOrgId;

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 组织类型
     */
    private String orgType;

    /**
     * 组织路径
     */
    private String orgPath;

    /**
     * 层级
     */
    private Integer orgLevel;

    /**
     * 负责人人员ID
     */
    private Long managerPersonId;

    /**
     * 负责人姓名
     */
    private String managerName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 地址
     */
    private String address;

    /**
     * 状态
     */
    private String status;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 创建人
     */
    private String createdName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 备注
     */
    private String remark;
}