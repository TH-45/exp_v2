package jh.exp.auth.clinet.api.bus;


import jh.exp.auth.core.entity.dto.OrgIdAndPersonIdDTO;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.auth.core.entity.res.PersonInfoRes;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange("/person")
public interface PersonService {
    /**
     * 分页查询人员列表
     */
    @PostExchange("/list")
    SimplePageRes<PersonInfoRes> queryPersonInfo(SimplePageReq<QueryPersonReq> personReq);

    @PostExchange("/update-status")
    void updatePersonStatus(@RequestParam("personId") Long personId, @RequestParam("status") String status);


    /**
     * 业务接口-批量查询组织的部门负责人/人员信息
     * 对比传入 personId 与组织的 managerPersonId：一致返回部门负责人信息，不一致返回传入 id 的人员信息
     *
     * @param orgIdAndPersonIds 组织ID与人员ID对列表
     * @return 组织ID -> 人员详情（key 为 orgId，每个 orgId 对应一条人员详情）
     */
    @PostExchange("/list/project-manager")
    Map<Long, PersonDetailRes> queryProjectManager(@RequestBody List<OrgIdAndPersonIdDTO> orgIdAndPersonIds);


    /**
     * 根据ID查询人员详情
     * 注意：服务端返回 ApiResponse 包装，调用方需通过 resp.getData() 获取 PersonDetailRes
     */
    @GetExchange("/detail")
    ApiResponse<PersonDetailRes> getPersonById(@RequestParam("personId") Long personId);

    /**
     * 批量查询人员详情
     */
    @PostExchange("/batch")
    Map<Long , PersonDetailRes> batchGetPersonByIds(@RequestBody  List<Long> personIds);

    /**
     * 批量查询人员详情
     * 入参：标识和人员id list
     * 返回：标识和人员信息 map
     */
    @PostExchange("/batch/flag")
    Map<Long, PersonDetailRes> batchFlagPersonByIds(@RequestBody List<PersonFlagReq> personFlagResList);

    /**
     * 创建人员
     */
    @PostExchange("/create")
    PersonDetailRes createPerson(@RequestBody CreatePersonReq req);

    /**
     * 更新人员
     */
    @PostExchange("/update")
    PersonDetailRes updatePerson(@RequestBody UpdatePersonReq req);

    /**
     * 删除人员
     */
    @DeleteExchange("/delete")
    void deletePerson(@RequestParam("personId") Long personId);

    /**
     * 批量删除人员
     */
    @PostExchange("/batch-delete")
    void batchDeletePersons(@RequestBody BatchDeletePersonReq req);

    /**
     * 更改人员状态
     */
    @PostExchange("/update-status-by-req")
    PersonDetailRes updatePersonStatus(@RequestBody PersonStatusReq req);

    /**
     * 批量更改人员状态
     */
    @PostExchange("/batch-update-status")
    void batchUpdatePersonStatus(@RequestBody BatchPersonStatusReq req);

    /**
     * 检查人员工号是否存在
     */
    @GetExchange("/check-code-exists")
    boolean checkPersonCodeExists(@RequestParam("personCode") String personCode, @RequestParam(value = "excludePersonId", required = false) Long excludePersonId);
}
