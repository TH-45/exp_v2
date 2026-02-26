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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目物料需求计划表，对应 exp_project_material_plan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_project_material_plan")
@TableName("exp_project_material_plan")
public class ProjectMaterialPlan {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "plan_id")
    private Long planId;

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

    // 计划需求数量
    @Column(name = "plan_qty", precision = 20, scale = 4)
    private BigDecimal planQty;

    // 计划开始使用日期
    @Column(name = "plan_use_start_date")
    private LocalDate planUseStartDate;

    // 计划完成使用日期
    @Column(name = "plan_use_end_date")
    private LocalDate planUseEndDate;

    // 对应工程/施工段落
    @Column(name = "related_work_section")
    private String relatedWorkSection;

    // 状态
    @Column(name = "status")
    private String status;

    // 关联采购单ID
    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    // 创建人
    @Column(name = "created_by")
    private Long createdBy;

    // 创建时间
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    // 更新时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // 备注
    @Column(name = "remark")
    private String remark;
}
