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
}
