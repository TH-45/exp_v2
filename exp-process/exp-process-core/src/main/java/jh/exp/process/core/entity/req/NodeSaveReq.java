package jh.exp.process.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NodeSaveReq {
    private Long nodeId;

    @NotNull(message = "流程定义ID不能为空")
    private Long procDefId;

    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    @NotNull(message = "排序号不能为空")
    private Integer sortNo;

    @NotBlank(message = "审批类型不能为空")
    private String approveType;

    @NotBlank(message = "办理人类型不能为空")
    private String assigneeType;

    @NotBlank(message = "办理对象不能为空")
    private String assigneeId;
}
