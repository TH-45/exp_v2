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

/**
 * 项目物料使用记录表，对应 exp_project_material_usage
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_project_material_usage")
@TableName("exp_project_material_usage")
public class ProjectMaterialUsage {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "usage_id")
    private Long usageId;

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

    // 本次使用数量
    @Column(name = "qty", precision = 20, scale = 4)
    private BigDecimal qty;

    // 使用日期
    @Column(name = "use_date")
    private LocalDate useDate;

    // 使用对应的施工段/工程部位
    @Column(name = "work_section")
    private String workSection;

    // 关联物料需求计划ID
    @Column(name = "related_plan_id")
    private Long relatedPlanId;

    // 操作人用户ID
    @Column(name = "operator_user_id")
    private Long operatorUserId;

    // 备注
    @Column(name = "remark")
    private String remark;
}
