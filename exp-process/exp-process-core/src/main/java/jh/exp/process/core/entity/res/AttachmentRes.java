package jh.exp.process.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentRes {
    private Long id;
    private String name;
    private String url;
    private Long size;
    private LocalDateTime uploadTime;
}
