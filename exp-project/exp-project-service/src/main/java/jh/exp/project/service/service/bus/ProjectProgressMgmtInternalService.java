package jh.exp.project.service.service.bus;

import jh.exp.project.core.entity.req.ProjectMilestoneCreateReq;
import jh.exp.project.core.entity.req.ProjectMilestoneDeleteReq;
import jh.exp.project.core.entity.req.ProjectMilestoneProgressUpdateReq;
import jh.exp.project.core.entity.req.ProjectMilestoneUpdateReq;
import jh.exp.project.core.entity.res.ProjectMilestoneRes;
import jh.exp.project.core.entity.res.ProjectProgressRes;

public interface ProjectProgressMgmtInternalService {
    ProjectProgressRes detail(Long projectId);

    ProjectMilestoneRes createMilestone(ProjectMilestoneCreateReq req);

    ProjectMilestoneRes updateMilestone(ProjectMilestoneUpdateReq req);

    void deleteMilestone(ProjectMilestoneDeleteReq req);

    ProjectMilestoneRes updateProgress(ProjectMilestoneProgressUpdateReq req);
}
