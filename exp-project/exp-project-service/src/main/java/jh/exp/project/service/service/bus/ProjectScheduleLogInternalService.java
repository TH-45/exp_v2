package jh.exp.project.service.service.bus;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectScheduleLog;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;

public interface ProjectScheduleLogInternalService {
    SimplePageRes<ProjectScheduleLog> list(SimplePageReq<Object> req);

    ProjectScheduleLog detail(Long logId);

    ProjectScheduleLog create(ProjectScheduleLog req);

    ProjectScheduleLog update(ProjectScheduleLog req);

    void delete(DeleteByIdReq req);

    void batchDelete(BatchDeleteByIdsReq req);
}
