package jh.exp.auth.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.mapper.PositionMapper;
import jh.exp.auth.entity.Position;
import jh.exp.auth.entity.req.QueryPositionReq;
import jh.exp.auth.service.bus.PositionService;
import jh.exp.common.constant.CommonConstant;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PositionServiceImpl implements PositionService{
    @Autowired
    private PositionMapper positionMapper;
    @Override
    public SimplePageRes<Position> queryPosition(SimplePageReq<QueryPositionReq> positionReq) {
        int pageNum = positionReq.getPageNum();
        int pageSize = positionReq.getPageSize();
        String sort = positionReq.getSort();
        Page<Position> page = new Page<>(pageNum, pageSize);
        QueryPositionReq queryParam = positionReq.getQueryParam();
        QueryWrapper<Position> positionQueryWrapper = new QueryWrapper<>();
        positionQueryWrapper.eq(StringUtils.hasText(queryParam.getPostCode()), "post_code", queryParam.getPostCode())
                .eq(StringUtils.hasText(queryParam.getPostName()), "post_name", queryParam.getPostName())
                .eq(StringUtils.hasText(queryParam.getPostType()), "post_type", queryParam.getPostType())
                .eq(StringUtils.hasText(queryParam.getPostCategory()), "department", queryParam.getPostCategory())
                .eq(StringUtils.hasText(queryParam.getPostLevel()), "post_category", queryParam.getPostLevel())
                .eq("Status", CommonConstant.ENABLED_STATUS)
                .orderBy(true,sort.equals("ASC"),"create_time");
//        List<Position> positionList = positionMapper.selectList(positionQueryWrapper);
        IPage<Position> positionPage = positionMapper.selectPage(page, positionQueryWrapper);
        SimplePageRes<Position> res = new SimplePageRes<>();
        res.setList(positionPage.getRecords());
        res.setTotal(positionPage.getTotal());
        res.setPage(positionPage.getCurrent());
        res.setSize(positionPage.getSize());
        return res;
    }
}
