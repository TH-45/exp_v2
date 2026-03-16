package jh.exp.common.core.imex;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImexTaskResult {
    private String taskId;
    private String bizCode;
    private ImexTaskType taskType;
    private ImexTaskStatus status;
    private Integer totalRows;
    private Integer successRows;
    private Integer failedRows;
    private Integer errorOverflowCount;
    private String message;
    private String exportFileName;
    private boolean downloadable;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private List<ImexTaskErrorItem> errorPreview = new ArrayList<>();
}
