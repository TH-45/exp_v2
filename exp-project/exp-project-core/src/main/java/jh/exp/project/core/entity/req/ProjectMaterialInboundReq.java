package jh.exp.project.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectMaterialInboundReq {
    @NotNull(message = "id不能为空")
    private Long id;

    @NotNull(message = "quantity不能为空")
    private BigDecimal quantity;

    private String remarks;
}
