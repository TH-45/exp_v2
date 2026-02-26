package jh.exp.project.service.service.internal;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectStaffAssign;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;

public interface ProjectStaffAssignInternalService {
    SimplePageRes<ProjectStaffAssign> list(SimplePageReq<Object> req);

    ProjectStaffAssign detail(Long id);

    ProjectStaffAssign create(ProjectStaffAssign req);

    ProjectStaffAssign update(ProjectStaffAssign req);

    void delete(DeleteByIdReq req);

    void batchDelete(BatchDeleteByIdsReq req);
}
