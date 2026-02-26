package jh.exp.project.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目物料库存表，对应 exp_project_material_stock
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_project_material_stock")
@TableName("exp_project_material_stock")
public class ProjectMaterialStock {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "stock_id")
    private Long stockId;

    // 项目ID
    @Column(name = "project_id")
    private Long projectId;

    // 物料编码
    @Column(name = "material_code")
    private String materialCode;

    // 物料名称
    @Column(name = "material_name")
    private String materialName;

    // 规格型号
    @Column(name = "spec")
    private String spec;

    // 计量单位
    @Column(name = "unit")
    private String unit;

    // 当前库存数量
    @Column(name = "stock_qty", precision = 20, scale = 4)
    private BigDecimal stockQty;

    // 安全库存数量
    @Column(name = "safe_stock_qty", precision = 20, scale = 4)
    private BigDecimal safeStockQty;

    // 存放地点/仓库
    @Column(name = "location")
    private String location;

    // 状态
    @Column(name = "status")
    private String status;

    // 最近一次库存变动时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // 备注
    @Column(name = "remark")
    private String remark;
}
