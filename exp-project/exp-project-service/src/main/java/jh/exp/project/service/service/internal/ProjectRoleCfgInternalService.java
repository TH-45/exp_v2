package jh.exp.project.service.service.internal;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectRoleCfg;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;

public interface ProjectRoleCfgInternalService {
    SimplePageRes<ProjectRoleCfg> list(SimplePageReq<Object> req);

    ProjectRoleCfg detail(Long cfgId);

    ProjectRoleCfg create(ProjectRoleCfg req);

    ProjectRoleCfg update(ProjectRoleCfg req);

    void delete(DeleteByIdReq req);

    void batchDelete(BatchDeleteByIdsReq req);
}
