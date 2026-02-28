package jh.exp.project.service.service.bus;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectSchedule;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;

public interface ProjectScheduleInternalService {
    SimplePageRes<ProjectSchedule> list(SimplePageReq<Object> req);

    ProjectSchedule detail(Long scheduleId);

    ProjectSchedule create(ProjectSchedule req);

    ProjectSchedule update(ProjectSchedule req);

    void delete(DeleteByIdReq req);

    void batchDelete(BatchDeleteByIdsReq req);
}
