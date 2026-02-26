package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateQualificationAttachmentReq {
    @NotNull(message = "qualificationId不能为空")
    private Long qualificationId;
    @NotBlank(message = "fileName不能为空")
    private String fileName;
    @NotBlank(message = "filePath不能为空")
    private String filePath;
    private Long fileSize;
    @NotNull(message = "uploadUserId不能为空")
    private Long uploadUserId;
    private LocalDateTime uploadTime;
    private String remark;
}
