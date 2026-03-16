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
@Table(name = "wf_node_definition")
@TableName("wf_node_definition")
public class WfNodeDefinition {

    /**
     * 节点 ID，主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "node_id")
    private Long nodeId;

    /**
     * 流程定义 ID
     */
    @Column(name = "proc_def_id")
    private Long procDefId;

    /**
     * 节点名称
     */
    @Column(name = "node_name")
    private String nodeName;

    /**
     * 排序号
     */
    @Column(name = "sort_no")
    private Integer sortNo;

    /**
     * 审批类型
     */
    @Column(name = "approve_type")
    private String approveType;

    /**
     * 处理人类型
     */
    @Column(name = "assignee_type")
    private String assigneeType;

    /**
     * 处理人 ID
     */
    @Column(name = "assignee_id")
    private String assigneeId;

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
