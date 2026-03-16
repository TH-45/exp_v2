package jh.exp.auth.service.service.bus;

import jh.exp.auth.service.entity.imex.PersonExportTaskReq;
import jh.exp.common.core.imex.ImexTaskResult;
import jh.exp.common.core.imex.ImexTaskSubmitRes;
import org.springframework.web.multipart.MultipartFile;

public interface PersonImexService {
    byte[] downloadImportTemplate();

    ImexTaskSubmitRes submitImportTask(MultipartFile file);

    ImexTaskSubmitRes submitExportTask(PersonExportTaskReq req);

    ImexTaskResult queryTask(String taskId);

    byte[] downloadExportFile(String taskId);

    String queryExportContentType(String taskId);

    String queryExportFileName(String taskId);
}
