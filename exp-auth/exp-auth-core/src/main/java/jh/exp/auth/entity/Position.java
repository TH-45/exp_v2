package jh.exp.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 岗位定义表，对应 docs 中的：
 * 岗位定义表 exp_post
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_post")
@TableName("exp_post")
public class Position {
    // 岗位ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "post_id")
    private Long postId;
    // 岗位编码
    @Column(name = "post_code", nullable = false, unique = true, length = 64)
    private String postCode;
    // 岗位名称
    @Column(name = "post_name", nullable = false, length = 100)
    private String postName;
    // 岗位类型
    @Column(name = "post_type", length = 32)
    private String postType;
    // 岗位级别
    @Column(name = "post_level", length = 32)
    private String postLevel;
    // 岗位类别
    @Column(name = "post_category", length = 32)
    private String postCategory;
    // 岗位描述
    @Column(name = "post_desc", columnDefinition = "TEXT")
    private String postDesc;
    // 岗位状态
    @Column(name = "status", length = 32)
    private String status;
    // 默认角色ID
    @Column(name = "default_role_id")
    private Long defaultRoleId;
    // 默认数据范围
    @Column(name = "default_data_scope", length = 64)
    private String defaultDataScope;
    // 是否系统内置
    @Column(name = "is_system")
    private Integer isSystem;
    // 排序
    @Column(name = "sort_no")
    private Integer sortNo;
    // 创建者
    @Column(name = "created_by")
    private Long createdBy;
    // 创建时间
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "remark", length = 500)
    private String remark;


}

