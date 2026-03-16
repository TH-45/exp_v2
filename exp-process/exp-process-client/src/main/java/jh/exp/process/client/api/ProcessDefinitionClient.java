package jh.exp.process.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.process.core.entity.req.ProcessDefinitionQueryReq;
import jh.exp.process.core.entity.res.ProcessDefinitionListRes;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 流程定义服务客户端接口，用于查询流程定义等
 */
@HttpExchange("/definition")
public interface ProcessDefinitionClient {

    /**
     * 分页查询流程定义列表
     *
     * @param req 分页查询请求
     * @return 流程定义分页结果
     */
    @PostExchange("/list")
    ApiResponse<SimplePageRes<ProcessDefinitionListRes>> list(SimplePageReq<ProcessDefinitionQueryReq> req);
}
