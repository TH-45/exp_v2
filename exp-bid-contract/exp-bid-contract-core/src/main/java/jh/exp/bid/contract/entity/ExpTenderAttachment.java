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
 * 招标文件附件表，对应 exp_tender_attachment
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_tender_attachment")
@TableName("exp_tender_attachment")
public class ExpTenderAttachment {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "attachment_id")
    private Long attachmentId;

    // 招标ID，关联 exp_tender
    @Column(name = "tender_id")
    private Long tenderId;

    // 文件类型（招标公告、招标文件、答疑/澄清、补遗文件等）
    @Column(name = "file_type", length = 50)
    private String fileType;

    // 原始文件名
    @Column(name = "file_name", length = 200)
    private String fileName;

    // 文件存储路径/URL
    @Column(name = "file_path", length = 500)
    private String filePath;

    // 文件大小
    @Column(name = "file_size")
    private Long fileSize;

    // 文件版本号
    @Column(name = "version_no", length = 32)
    private String versionNo;

    // 上传人用户ID，关联账号信息
    @Column(name = "upload_user_id")
    private Long uploadUserId;

    // 上传时间
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}