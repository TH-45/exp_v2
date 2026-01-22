package jh.exp.auth.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 组织树响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgUnitTreeRes {

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
     * 层级
     */
    private Integer orgLevel;

    /**
     * 负责人姓名
     */
    private String managerName;

    /**
     * 状态
     */
    private String status;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 子组织列表
     */
    private List<OrgUnitTreeRes> children;

    /**
     * 是否有子节点
     */
    private Boolean hasChildren;

}