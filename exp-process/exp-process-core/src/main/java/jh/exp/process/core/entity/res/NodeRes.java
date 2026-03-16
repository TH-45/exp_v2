package jh.exp.process.core.entity.res;

import lombok.Data;

@Data
public class NodeRes {
    private Long nodeId;
    private Long procDefId;
    private String nodeName;
    private Integer sortNo;
    private String approveType;
    private String assigneeType;
    private String assigneeId;
    /** 审批人显示名称（人员选择器选中的姓名，用于列表展示） */
    private String assigneeDisplayName;
}
