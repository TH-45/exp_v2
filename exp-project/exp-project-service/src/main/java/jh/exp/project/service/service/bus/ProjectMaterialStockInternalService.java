package jh.exp.project.service.service.bus;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialStock;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;

public interface ProjectMaterialStockInternalService {
    SimplePageRes<ProjectMaterialStock> list(SimplePageReq<Object> req);

    ProjectMaterialStock detail(Long stockId);

    ProjectMaterialStock create(ProjectMaterialStock req);

    ProjectMaterialStock update(ProjectMaterialStock req);

    void delete(DeleteByIdReq req);

    void batchDelete(BatchDeleteByIdsReq req);
}
