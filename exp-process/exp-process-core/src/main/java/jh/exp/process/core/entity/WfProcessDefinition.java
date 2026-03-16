package jh.exp.process.core.entity;

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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wf_process_definition")
@TableName("wf_process_definition")
public class WfProcessDefinition {

    /**
     * 流程定义ID，主键自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "proc_def_id")
    private Long procDefId;

    /**
     * 流程编码
     */
    @Column(name = "proc_code")
    private String procCode;

    /**
     * 流程名称
     */
    @Column(name = "proc_name")
    private String procName;

    /**
     * 业务类型
     */
    @Column(name = "bus_type")
    private String busType;

    /**
     * 是否激活 (1:是, 0:否)
     */
    @Column(name = "is_active")
    private Integer isActive;

    /**
     * 版本号
     */
    @Column(name = "version")
    private Integer version;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
