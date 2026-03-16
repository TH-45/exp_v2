package jh.exp.project.service.service.bus;

import jh.exp.project.core.entity.req.ProjectMaterialCreateReq;
import jh.exp.project.core.entity.req.ProjectMaterialDeleteReq;
import jh.exp.project.core.entity.req.ProjectMaterialInboundReq;
import jh.exp.project.core.entity.req.ProjectMaterialOutboundReq;
import jh.exp.project.core.entity.req.ProjectMaterialUpdateReq;
import jh.exp.project.core.entity.res.ProjectMaterialDetailRes;
import jh.exp.project.core.entity.res.ProjectMaterialRes;

public interface ProjectMaterialMgmtInternalService {
    ProjectMaterialDetailRes detail(Long projectId);

    ProjectMaterialRes create(ProjectMaterialCreateReq req);

    ProjectMaterialRes update(ProjectMaterialUpdateReq req);

    void delete(ProjectMaterialDeleteReq req);

    ProjectMaterialRes inbound(ProjectMaterialInboundReq req);

    ProjectMaterialRes outbound(ProjectMaterialOutboundReq req);
}
