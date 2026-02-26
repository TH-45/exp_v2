package jh.exp.project.service.service.internal;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.PersonProjectRel;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;

public interface PersonProjectRelInternalService {
    SimplePageRes<PersonProjectRel> list(SimplePageReq<Object> req);

    PersonProjectRel detail(Long id);

    PersonProjectRel create(PersonProjectRel req);

    PersonProjectRel update(PersonProjectRel req);

    void delete(DeleteByIdReq req);

    void batchDelete(BatchDeleteByIdsReq req);
}
