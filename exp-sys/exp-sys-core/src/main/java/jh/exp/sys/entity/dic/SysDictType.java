package jh.exp.sys.entity.dic;


import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型实体
 *
 * 用于定义一类字典的基本信息，例如：
 * - 用户状态
 * - 订单状态
 * - 数据权限范围
 */
@Data
@Entity
@Table(name = "sys_dict_type")
@TableName("sys_dict_type")
public class SysDictType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.ASSIGN_ID)
    @Column(name = "id")
    private Long id;

    /**
     * 字典类型编码（全局唯一，如 USER_STATUS）
     */
    @Column(name = "dict_code", nullable = false, length = 100)
    @TableField("dict_code")
    private String dictCode;

    /**
     * 字典类型名称
     */
    @Column(name = "dict_name", nullable = false, length = 100)
    @TableField("dict_name")
    private String dictName;

    /**
     * 字典类型描述
     */
    @Column(name = "description", length = 255)
    @TableField("description")
    private String description;

    /**
     * 状态
     * ENABLED：启用
     * DISABLED：停用
     */
    @Column(name = "status", length = 20)
    @TableField("status")
    private String status;

    /**
     * 创建人用户ID
     */
    @Column(name = "created_by", updatable = false)
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_time", updatable = false)
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人用户ID
     */
    @Column(name = "updated_by")
    @TableField(value = "updated_by", fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    @TableField(value = "updated_time", fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;
}
