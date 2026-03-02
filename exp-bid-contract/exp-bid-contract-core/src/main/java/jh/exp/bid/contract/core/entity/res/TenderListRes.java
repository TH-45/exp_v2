package jh.exp.bid.contract.core.entity.res;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 招标列表最终响应对象
 * 字段名已完全对齐 TenderLisDTO，确保 BeanUtils 自动映射成功
 */
@Data
public class TenderListRes {
    // 招标项目id
    private Long tenderId;

    // 招标项目编号
    private String tenderCode;

    // 招标项目名称
    private String tenderName;

    // 招标方id
    private Long purchaserId;

    // 招标方名称
    private String purchaserName;

    // 招标负责人id
    private Long personId;

    // 招标负责人名称
    private String personIdName;

    // 组织id
    private Long orgId;

    //组织名称
    private String orgName;

    // 招标项目状态
    private String status;


    // 招标方式
    private String tenderMode;

    // 招标类型
    private String tenderType;

    // 招标预算金额 (原 tenderBudgetAmount，现对齐 DTO)
    private BigDecimal budgetAmount;

    // 币种
    private String currency;

    //关联项目 id
    private Long projectId;

    //关联项目名称
    private String projectName;

    // 招标开始时间
    private LocalDateTime bidStartTime;

    // 招标截止时间
    private LocalDateTime bidEndTime;

    // 招标项目创建人id
    private Long createdBy;

    // 招标项目创建人名称
    private String createdByName;

    // 招标项目创建时间
    private LocalDateTime createdTime;

    // 业务员id
    private Long salesmanId;
    //业务员名称
    private String salesmanName;
}