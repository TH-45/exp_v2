package jh.exp.bid.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 招投标附件统一表，对应 exp_attachment
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_attachment")
@TableName("exp_attachment")
public class ExpAttachment {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "attachment_id")
    private Long attachmentId;

    // 业务类型（TENDER-招标文件，BID-投标文件）
    @Column(name = "business_type", length = 20)
    private String businessType;

    // 业务ID（招标ID或投标ID）
    @Column(name = "business_id")
    private Long businessId;

    // 文件类型（招标公告、招标文件、答疑/澄清、补遗文件、技术标、商务标、资格文件、其他）
    @Column(name = "file_type", length = 50)
    private String fileType;

    // 文件分类（技术文件、商务文件、资格文件、管理文件等）
    @Column(name = "file_category", length = 50)
    private String fileCategory;

    // 原始文件名
    @Column(name = "file_name", length = 200)
    private String fileName;

    // 文件存储路径/URL
    @Column(name = "file_path", length = 500)
    private String filePath;

    // 文件大小（字节）
    @Column(name = "file_size")
    private Long fileSize;

    // 文件格式/扩展名
    @Column(name = "file_format", length = 20)
    private String fileFormat;

    // 文件MD5校验码
    @Column(name = "file_md5", length = 64)
    private String fileMd5;

    // 文件版本号
    @Column(name = "version_no", length = 32)
    private String versionNo;

    // 是否为最新版本（0/1）
    @Column(name = "is_latest")
    private Integer isLatest;

    // 上传人用户ID，关联账号信息
    @Column(name = "upload_user_id")
    private Long uploadUserId;

    // 上传时间
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    // 下载次数
    @Column(name = "download_count")
    private Integer downloadCount;

    // 最后下载时间
    @Column(name = "last_download_time")
    private LocalDateTime lastDownloadTime;

    // 文件状态（正常、已删除、已过期）
    @Column(name = "file_status", length = 20)
    private String fileStatus;

    // 保密级别（公开、内部、机密）
    @Column(name = "security_level", length = 20)
    private String securityLevel;

    // 创建人用户ID，关联账号信息
    @Column(name = "created_by")
    private Long createdBy;

    // 创建人部门ID，关联部门管理
    @Column(name = "created_dept_id")
    private Long createdDeptId;

    // 创建人岗位ID，关联岗位管理
    @Column(name = "created_post_id")
    private Long createdPostId;

    // 创建时间
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    // 更新时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}