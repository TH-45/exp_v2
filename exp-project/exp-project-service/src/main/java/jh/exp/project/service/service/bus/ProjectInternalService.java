package jh.exp.project.service.service.bus;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.Project;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;

import java.util.List;
import java.util.Map;

public interface ProjectInternalService {
    SimplePageRes<Project> list(SimplePageReq<Object> req);

    Project detail(Long projectId);

    Project create(Project req);

    Project update(Project req);

    void delete(DeleteByIdReq req);

    void batchDelete(BatchDeleteByIdsReq req);

    ApiResponse<Map<Long,Project>> batchGetProjectByIds(List<Long> projectIds);
}
