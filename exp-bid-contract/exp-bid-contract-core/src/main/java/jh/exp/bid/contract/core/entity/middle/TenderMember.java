package jh.exp.bid.contract.core.entity.middle;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 招标成员表
 * 对应表：exp_tender_member
 */
@Entity
@Table(name = "exp_tender_member")
@TableName("exp_tender_member")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TenderMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "id")
    private Long id;

    /**
     * 招标ID
     */
    @Column(name = "tender_id", nullable = false)
    private Long tenderId;

    /**
     * 人员ID 默认为部门负责人
     */
    @Column(name = "person_id", nullable = false)
    private Long personId;

    /**
     * 成员角色 由前端传入
     */
    @Column(name = "member_role", length = 50)
    private String memberRole;

    /**
     * 关联部门ID
     */
    @Column(name = "org_id")
    private Long orgId;

    /**
     * 任职岗位ID 通过人员查询
     */
    @Column(name = "post_id")
    private Long postId;

    /**
     * 任职开始日期 项目开始日期 通过项目查询
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 任职结束日期 项目结束日期 通过项目查询
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 状态 启动、禁用
     */
    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "remark", length = 500)
    private String remark;

}