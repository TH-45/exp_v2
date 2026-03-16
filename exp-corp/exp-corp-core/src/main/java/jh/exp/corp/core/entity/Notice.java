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
 * 制度与公告主表，对应 exp_notice
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_notice")
@TableName("exp_notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "notice_code", length = 64)
    private String noticeCode;

    @Column(name = "notice_type", length = 32)
    private String noticeType;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "attach_flag")
    private Integer attachFlag;

    @Column(name = "publish_status", length = 32)
    private String publishStatus;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "scope_type", length = 32)
    private String scopeType;

    @Column(name = "scope_detail", length = 1000)
    private String scopeDetail;

    @Column(name = "creator_user_id")
    private Long creatorUserId;

    @Column(name = "publisher_user_id")
    private Long publisherUserId;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "remark", length = 500)
    private String remark;
}
