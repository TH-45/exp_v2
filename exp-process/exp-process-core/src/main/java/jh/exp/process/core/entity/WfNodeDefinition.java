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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "node_id")
    private Long nodeId;

    @Column(name = "proc_def_id")
    private Long procDefId;

    @Column(name = "node_name")
    private String nodeName;

    @Column(name = "sort_no")
    private Integer sortNo;

    @Column(name = "approve_type")
    private String approveType;

    @Column(name = "assignee_type")
    private String assigneeType;

    @Column(name = "assignee_id")
    private String assigneeId;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
