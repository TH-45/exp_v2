package jh.exp.bid.contract.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 投标参与人员表，对应 exp_bid_member
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_member")
@TableName("exp_bid_member")
public class BidMember {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "id")
    private Long id;

    // 投标ID，关联 exp_bid
    @Column(name = "bid_id")
    private Long bidId;

    // 用户ID，关联账号信息
    @Column(name = "user_id")
    private Long userId;

    // 部门ID，关联部门管理
    @Column(name = "dept_id")
    private Long deptId;

    // 岗位ID，关联岗位管理
    @Column(name = "post_id")
    private Long postId;

    // 在本次投标中的角色（项目经理、技术负责人等）
    @Column(name = "role_in_bid", length = 100)
    private String roleInBid;

    // 职责说明
    @Column(name = "responsibility_desc", length = 500)
    private String responsibilityDesc;

    // 是否本投标项目负责人（0/1）
    @Column(name = "is_leader")
    private Integer isLeader;

    // 加入时间
    @Column(name = "join_time")
    private LocalDateTime joinTime;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}
