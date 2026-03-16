package jh.exp.project.core.entity.res;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProjectMaterialRes {
    private Long id;
    private Long projectId;
    private String materialCode;
    private String materialName;
    private String spec;
    private String unit;
    private BigDecimal requiredQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal usedQuantity;
    private BigDecimal stockQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String supplierName;
    private String status;
    private LocalDateTime lastUpdateTime;
}
