package jh.exp.process.core.entity.req;

import lombok.Data;

@Data
public class ApprovalTaskQueryReq {
    private String tab;
    private String keyword;
    private String busType;
    private String status;
}
