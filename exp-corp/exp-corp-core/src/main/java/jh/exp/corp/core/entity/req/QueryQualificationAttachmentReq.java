package jh.exp.corp.core.entity.req;

import lombok.Data;

@Data
public class QueryQualificationAttachmentReq {
    private Long qualificationId;
    private String fileName;
    private Long uploadUserId;
}
