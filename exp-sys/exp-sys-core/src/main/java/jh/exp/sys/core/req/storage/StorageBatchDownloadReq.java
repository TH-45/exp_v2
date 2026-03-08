package jh.exp.sys.core.req.storage;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量下载请求参数
 */
@Data
public class StorageBatchDownloadReq {
    @NotEmpty(message = "objectKeys 不能为空")
    private List<String> objectKeys;

    /**
     * 压缩包名称（可选）
     */
    private String archiveName;
}
