package jh.exp.sys.core.entity.dic;


import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典项实体
 *
 * 字典中真正被业务表引用的值定义
 */
@Data
@Entity
@Table(name = "sys_dict_item")
@TableName("sys_dict_item")
public class SysDictItem implements Serializable {

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
     * 所属字典类型编码
     */
    @Column(name = "dict_type_code", nullable = false, length = 100)
    @TableField("dict_type_code")
    private String dictTypeCode;

    /**
     * 字典项编码
     */
    @Column(name = "item_code", length = 100)
    @TableField("item_code")
    private String itemCode;

    /**
     * 字典项值（业务表真实存储的值）
     */
    @Column(name = "item_value", nullable = false, length = 100)
    @TableField("item_value")
    private String itemValue;

    /**
     * 字典项显示名称
     */
    @Column(name = "item_label", nullable = false, length = 100)
    @TableField("item_label")
    private String itemLabel;

    /**
     * 排序号
     */
    @Column(name = "sort_no")
    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 状态
     * ENABLED：启用
     * DISABLED：停用
     */
    @Column(name = "status", length = 20)
    @TableField("status")
    private String status;


    /**
     * 备注说明
     */
    @Column(name = "remark", length = 255)
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "created_time", updatable = false)
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    @TableField(value = "updated_time", fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;
}
