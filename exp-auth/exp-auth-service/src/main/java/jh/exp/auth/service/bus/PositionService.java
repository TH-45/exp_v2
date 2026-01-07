package jh.exp.auth.service.bus;


import jh.exp.auth.entity.Position;
import jh.exp.auth.entity.req.QueryPositionParam;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import org.springframework.stereotype.Service;

@Service
public interface PositionService {


    //分页查询职位信息
    public SimplePageRes<Position> queryPosition(SimplePageReq<QueryPositionParam> positionReq) ;
}
