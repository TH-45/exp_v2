package jh.exp.bid.contract.core.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 查询附件请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryAttachmentReq {

    /**
     * 业务类型（TENDER-招标文件，BID-投标文件）
     */
    private String businessType;

    /**
     * 业务ID（招标ID或投标ID）
     */
    private Long businessId;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件分类
     */
    private String fileCategory;

    /**
     * 文件名（模糊查询）
     */
    private String fileName;

    /**
     * 文件格式
     */
    private String fileFormat;

    /**
     * 上传人ID
     */
    private Long uploadUserId;

    /**
     * 保密级别
     */
    private String securityLevel;

    /**
     * 文件状态
     */
    private String fileStatus;

    /**
     * 上传开始时间
     */
    private LocalDateTime uploadTimeStart;

    /**
     * 上传结束时间
     */
    private LocalDateTime uploadTimeEnd;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建时间开始
     */
    private LocalDateTime createdTimeStart;

    /**
     * 创建时间结束
     */
    private LocalDateTime createdTimeEnd;
}