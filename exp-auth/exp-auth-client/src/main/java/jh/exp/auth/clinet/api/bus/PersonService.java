package jh.exp.auth.clinet.api.bus;


import jh.exp.auth.core.entity.dto.OrgIdAndPersonIdDTO;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.auth.core.entity.res.PersonInfoRes;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.web.bind.annotation.RequestBody;
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

    void updatePersonStatus(Long personId, String status);


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
     */
    @PostExchange("/detail/{personId}")
    PersonDetailRes getPersonById(Long personId);

    /**
     * 批量查询人员详情
     */
    @PostExchange("/batch")
    Map<Long , PersonDetailRes> batchGetPersonByIds(@RequestBody  List<Long>  personIds);

    /**
     * 创建人员
     */
    PersonDetailRes createPerson(CreatePersonReq req);

    /**
     * 更新人员
     */
    PersonDetailRes updatePerson(UpdatePersonReq req);

    /**
     * 删除人员
     */
    void deletePerson(Long personId);

    /**
     * 批量删除人员
     */
    void batchDeletePersons(BatchDeletePersonReq req);

    /**
     * 更改人员状态
     */
    PersonDetailRes updatePersonStatus(PersonStatusReq req);

    /**
     * 批量更改人员状态
     */
    void batchUpdatePersonStatus(BatchPersonStatusReq req);

    /**
     * 检查人员工号是否存在
     */
    boolean checkPersonCodeExists(String personCode, Long excludePersonId);
}
