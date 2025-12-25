package entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 投标报价明细表，对应 exp_bid_detail
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_detail")
@TableName("exp_bid_detail")
public class ExpBidDetail {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "detail_id")
    private Long detailId;

    // 投标ID，关联 exp_bid
    @Column(name = "bid_id")
    private Long bidId;

    // 明细类型（工程、服务、物资等）
    @Column(name = "item_type", length = 50)
    private String itemType;

    // 项目/物料名称
    @Column(name = "item_name", length = 200)
    private String itemName;

    // 规格型号
    @Column(name = "spec", length = 200)
    private String spec;

    // 计量单位
    @Column(name = "unit", length = 20)
    private String unit;

    // 数量
    @Column(name = "qty", precision = 15, scale = 4)
    private BigDecimal qty;

    // 单价
    @Column(name = "unit_price", precision = 20, scale = 2)
    private BigDecimal unitPrice;

    // 小计金额
    @Column(name = "subtotal_amount", precision = 20, scale = 2)
    private BigDecimal subtotalAmount;

    // 税率
    @Column(name = "tax_rate", precision = 6, scale = 4)
    private BigDecimal taxRate;

    // 税额
    @Column(name = "tax_amount", precision = 20, scale = 2)
    private BigDecimal taxAmount;

    // 含税金额
    @Column(name = "total_with_tax", precision = 20, scale = 2)
    private BigDecimal totalWithTax;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;

    // 行号/排序号
    @Column(name = "sort_no")
    private Integer sortNo;
}
