package jh.exp.auth.entity.dic;



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
    @Column(name = "dict_code", nullable = false, length = 100)
    @TableField("dict_code")
    private String dictCode;

    /**
     * 字典项编码（程序内部使用）
     */
    @Column(name = "item_code", length = 100)
    @TableField("item_code")
    private String itemCode;

    /**
     * 字典项值（业务表真实存储的值）
     * ⚠ 上线后不允许修改
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
     * 父级字典项ID
     * 0 表示根节点（用于树形字典）
     */
    @Column(name = "parent_id")
    @TableField("parent_id")
    private Long parentId;

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
     * 是否默认项
     * 1：是
     * 0：否
     */
    @Column(name = "is_default")
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 租户ID
     * 0 表示平台级字典
     */
    @Column(name = "tenant_id")
    @TableField("tenant_id")
    private Long tenantId;

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
