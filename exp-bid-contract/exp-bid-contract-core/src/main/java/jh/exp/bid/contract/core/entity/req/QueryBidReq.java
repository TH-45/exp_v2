package jh.exp.bid.contract.core.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 查询投标请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryBidReq {

    /**
     * 招标单位名称
     */
    private String purchaserName;

    /**
     * 招标项目名称
     */
    private String tenderName;

    /**
     * 关联项目名称
     */
    private String projectName;


    /**
     * 投标编号
     */
    private String bidCode;

    /**
     * 投标名称
     */
    private String bidName;

    /**
     * 投标状态
     */
    private String bidStatus;

    /**
     * 是否中标（0/1）
     */
    private Integer winFlag;


    /**
     * 投标提交时间开始
     */
    private LocalDateTime bidSubmitTimeStart;

    /**
     * 投标提交时间结束
     */
    private LocalDateTime bidSubmitTimeEnd;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间开始
     */
    private LocalDateTime createdTimeStart;

    /**
     * 创建时间结束
     */
    private LocalDateTime createdTimeEnd;
}