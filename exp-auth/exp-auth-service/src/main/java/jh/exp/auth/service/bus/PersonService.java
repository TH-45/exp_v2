package jh.exp.auth.service.bus;

import jh.exp.auth.entity.req.PersonExpReq;
import jh.exp.auth.entity.req.QueryPersonReq;
import jh.exp.auth.entity.res.PersonInfoRes;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;

public interface PersonService {
    SimplePageRes<PersonInfoRes> queryPersonInfo(SimplePageReq<QueryPersonReq> personReq);

    void updatePersonStatus(Long personId,String status);

    void updatePersonInfo(PersonExpReq personExpReq);
}
