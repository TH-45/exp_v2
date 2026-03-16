package jh.exp.corp.core.entity.req;

import lombok.Data;

@Data
public class QueryNoticeReq {
    private String title;
    private String noticeType;
    private String publishStatus;
    private String publishStartDate;
    private String publishEndDate;
}
