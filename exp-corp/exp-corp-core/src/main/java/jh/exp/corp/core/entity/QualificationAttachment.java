package jh.exp.corp.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 企业资质附件表，对应 exp_qualification_attachment
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_qualification_attachment")
@TableName("exp_qualification_attachment")
public class QualificationAttachment {

    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "attachment_id")
    private Long attachmentId;

    // 资质ID
    @Column(name = "qualification_id")
    private Long qualificationId;

    // 原始文件名
    @Column(name = "file_name", length = 200)
    private String fileName;

    // 文件存储路径/URL
    @Column(name = "file_path", length = 500)
    private String filePath;

    // 文件大小（字节）
    @Column(name = "file_size")
    private Long fileSize;

    // 上传人用户ID
    @Column(name = "upload_user_id")
    private Long uploadUserId;

    // 上传时间
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}
