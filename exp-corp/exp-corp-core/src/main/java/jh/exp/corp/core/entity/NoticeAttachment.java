package jh.exp.corp.core.entity;

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

import java.time.LocalDateTime;

/**
 * 制度与公告附件表，对应 exp_notice_attachment
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_notice_attachment")
@TableName("exp_notice_attachment")
public class NoticeAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "attachment_id")
    private Long attachmentId;

    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "upload_user_id")
    private Long uploadUserId;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Column(name = "remark", length = 500)
    private String remark;
}
