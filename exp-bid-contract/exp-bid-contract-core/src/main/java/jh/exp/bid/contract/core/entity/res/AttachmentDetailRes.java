package jh.exp.bid.contract.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 附件详情响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDetailRes {

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
     * 业务名称
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
     * 文件存储路径/URL
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件格式/扩展名
     */
    private String fileFormat;

    /**
     * 文件MD5校验码
     */
    private String fileMd5;

    /**
     * 文件版本号
     */
    private String versionNo;

    /**
     * 是否为最新版本
     */
    private Integer isLatest;

    /**
     * 上传人ID
     */
    private Long uploadUserId;

    /**
     * 上传人姓名
     */
    private String uploadUserName;

    /**
     * 上传人部门
     */
    private String uploadDeptName;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 最后下载时间
     */
    private LocalDateTime lastDownloadTime;

    /**
     * 文件状态
     */
    private String fileStatus;

    /**
     * 保密级别
     */
    private String securityLevel;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建人部门ID
     */
    private Long createdDeptId;

    /**
     * 创建人部门名称
     */
    private String createdDeptName;

    /**
     * 创建人岗位ID
     */
    private Long createdPostId;

    /**
     * 创建人岗位名称
     */
    private String createdPostName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 备注
     */
    private String remark;
}