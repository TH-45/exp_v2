package jh.exp.auth.service.bus;

import jh.exp.auth.entity.req.*;
import jh.exp.auth.entity.res.PersonDetailRes;
import jh.exp.auth.entity.res.PersonInfoRes;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;

public interface PersonService {
    /**
     * 分页查询人员列表
     */
    SimplePageRes<PersonInfoRes> queryPersonInfo(SimplePageReq<QueryPersonReq> personReq);

    void updatePersonStatus(Long personId, String status);

    /**
     * 根据ID查询人员详情
     */
    PersonDetailRes getPersonById(Long personId);

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
