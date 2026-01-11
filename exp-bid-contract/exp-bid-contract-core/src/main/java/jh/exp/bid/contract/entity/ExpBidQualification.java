package jh.exp.bid.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 投标资质关联表，对应 exp_bid_qualification
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_qualification")
@TableName("exp_bid_qualification")
public class ExpBidQualification {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "id")
    private Long id;

    // 投标ID，关联 exp_bid
    @Column(name = "bid_id")
    private Long bidId;

    // 资质ID，关联企业外部信息-资质信息表
    @Column(name = "qualification_id")
    private Long qualificationId;

    // 是否招标文件要求的必备资质（0/1）
    @Column(name = "is_required")
    private Integer isRequired;

    // 本次投标中资质状态（已关联、缺失、已过期、即将过期等）
    @Column(name = "status", length = 32)
    private String status;

    // 资质有效期起（快照）
    @Column(name = "valid_from")
    private LocalDate validFrom;

    // 资质有效期止（快照）
    @Column(name = "valid_to")
    private LocalDate validTo;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}