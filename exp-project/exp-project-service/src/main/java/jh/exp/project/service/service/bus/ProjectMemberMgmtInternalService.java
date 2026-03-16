package jh.exp.project.service.service.bus;

import jh.exp.project.core.entity.req.ProjectMemberCreateReq;
import jh.exp.project.core.entity.req.ProjectMemberDeleteReq;
import jh.exp.project.core.entity.req.ProjectMemberUpdateReq;
import jh.exp.project.core.entity.res.ProjectMemberRes;

import java.util.List;

public interface ProjectMemberMgmtInternalService {
    List<ProjectMemberRes> listByProjectId(Long projectId);

    ProjectMemberRes create(ProjectMemberCreateReq req);

    ProjectMemberRes update(ProjectMemberUpdateReq req);

    void delete(ProjectMemberDeleteReq req);
}
