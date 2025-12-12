package jh.exp.auth.inter;

import jh.exp.auth.entity.Position;
import jh.exp.common.api.PageRequest;
import org.springframework.data.domain.Page;

public interface PositionService {

    //分页查询职位信息
    public Page<Position> queryPosition(PageRequest query) ;
}
