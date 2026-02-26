package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteQualificationAttachmentReq {
    @NotEmpty(message = "attachmentIds不能为空")
    private List<Long> attachmentIds;
}
