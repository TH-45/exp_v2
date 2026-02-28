package jh.exp.project.service.service.bus;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialUsage;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;

public interface ProjectMaterialUsageInternalService {
    SimplePageRes<ProjectMaterialUsage> list(SimplePageReq<Object> req);

    ProjectMaterialUsage detail(Long usageId);

    ProjectMaterialUsage create(ProjectMaterialUsage req);

    ProjectMaterialUsage update(ProjectMaterialUsage req);

    void delete(DeleteByIdReq req);

    void batchDelete(BatchDeleteByIdsReq req);
}
