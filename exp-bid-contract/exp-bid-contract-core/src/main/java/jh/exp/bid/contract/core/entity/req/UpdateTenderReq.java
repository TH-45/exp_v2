package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新招标请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTenderReq {

    /**
     * 招标ID
     */
    @NotNull(message = "招标ID不能为空")
    private Long tenderId;

    /**
     * 招标编号
     */
    @NotBlank(message = "招标编号不能为空")
    private String tenderCode;

    /**
     * 招标项目名称
     */
    @NotBlank(message = "招标项目名称不能为空")
    private String tenderName;

    /**
     * 招标类型（工程、服务、货物等）
     */
    @NotBlank(message = "招标类型不能为空")
    private String tenderType;

    /**
     * 招标方式（公开招标、邀请招标、竞争性谈判等）
     */
    @NotBlank(message = "招标方式不能为空")
    private String tenderMode;

    /**
     * 招标人/采购方ID，关联企业信息（内部单位或外部单位）
     */
    @NotNull(message = "招标人ID不能为空")
    private Long companyId;

    /**
     * 招标控制价/预算金额
     */
    @NotNull(message = "招标预算金额不能为空")
    private BigDecimal budgetAmount;

    /**
     * 币种
     */
    @NotBlank(message = "币种不能为空")
    private String currency;

    /**
     * 招标项目概要/公告摘要
     */
    private String tenderBrief;

    /**
     * 招标公告发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 投标开始时间
     */
    @NotNull(message = "投标开始时间不能为空")
    private LocalDateTime bidStartTime;

    /**
     * 投标截止时间
     */
    @NotNull(message = "投标截止时间不能为空")
    private LocalDateTime bidEndTime;

    /**
     * 开标时间
     */
    private LocalDateTime openTime;

    /**
     * 开标地点或开标会议方式（线上/线下）
     */
    private String openAddress;

    /**
     * 备注
     */
    private String remark;
}