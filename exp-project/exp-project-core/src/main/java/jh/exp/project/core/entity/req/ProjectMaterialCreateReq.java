package jh.exp.project.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectMaterialCreateReq {
    @NotNull(message = "projectId不能为空")
    private Long projectId;

    @NotBlank(message = "materialCode不能为空")
    private String materialCode;

    @NotBlank(message = "materialName不能为空")
    private String materialName;

    @NotBlank(message = "spec不能为空")
    private String spec;

    @NotBlank(message = "unit不能为空")
    private String unit;

    @NotNull(message = "requiredQuantity不能为空")
    private BigDecimal requiredQuantity;

    @NotNull(message = "unitPrice不能为空")
    private BigDecimal unitPrice;

    private String supplierName;

    private BigDecimal safeStockQty;
}
