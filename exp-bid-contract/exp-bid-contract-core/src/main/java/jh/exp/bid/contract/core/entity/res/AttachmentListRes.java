package jh.exp.bid.contract.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 附件列表响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentListRes {

    /**
     * 附件ID
     */
    private Long attachmentId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 业务名称（招标项目名称或投标名称）
     */
    private String businessName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件分类
     */
    private String fileCategory;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件格式/扩展名
     */
    private String fileFormat;

    /**
     * 文件版本号
     */
    private String versionNo;

    /**
     * 是否为最新版本
     */
    private Integer isLatest;

    /**
     * 上传人姓名
     */
    private String uploadUserName;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 文件状态
     */
    private String fileStatus;

    /**
     * 保密级别
     */
    private String securityLevel;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}