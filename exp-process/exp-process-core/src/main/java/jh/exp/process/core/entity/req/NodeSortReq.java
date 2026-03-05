package jh.exp.process.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NodeSortReq {
    @NotNull(message = "节点ID不能为空")
    private Long nodeId;
    @NotNull(message = "目标排序不能为空")
    private Integer targetSortNo;
}
