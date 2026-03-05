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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "proc_def_id")
    private Long procDefId;

    @Column(name = "proc_code")
    private String procCode;

    @Column(name = "proc_name")
    private String procName;

    @Column(name = "bus_type")
    private String busType;

    @Column(name = "is_active")
    private Integer isActive;

    @Column(name = "version")
    private Integer version;

    @Column(name = "remark")
    private String remark;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
