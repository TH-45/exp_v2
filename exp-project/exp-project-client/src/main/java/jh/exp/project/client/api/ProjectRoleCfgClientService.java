package jh.exp.project.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectRoleCfg;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/project/projectRoleCfg")
public interface ProjectRoleCfgClientService {
    @PostExchange("/list")
    ApiResponse<SimplePageRes<ProjectRoleCfg>> list(@RequestBody SimplePageReq<Object> req);

    @GetExchange("/detail")
    ApiResponse<ProjectRoleCfg> detail(@RequestParam("cfgId") Long cfgId);

    @PostExchange("/create")
    ApiResponse<ProjectRoleCfg> create(@RequestBody ProjectRoleCfg req);

    @PostExchange("/update")
    ApiResponse<ProjectRoleCfg> update(@RequestBody ProjectRoleCfg req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteByIdReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteByIdsReq req);
}
