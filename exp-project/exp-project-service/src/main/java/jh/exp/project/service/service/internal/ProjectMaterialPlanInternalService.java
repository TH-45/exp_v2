package jh.exp.project.service.service.internal;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialPlan;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;

public interface ProjectMaterialPlanInternalService {
    SimplePageRes<ProjectMaterialPlan> list(SimplePageReq<Object> req);

    ProjectMaterialPlan detail(Long planId);

    ProjectMaterialPlan create(ProjectMaterialPlan req);

    ProjectMaterialPlan update(ProjectMaterialPlan req);

    void delete(DeleteByIdReq req);

    void batchDelete(BatchDeleteByIdsReq req);
}
