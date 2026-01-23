package jh.exp.auth.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jh.exp.auth.core.entity.OrgUnit;
import jh.exp.auth.core.entity.Position;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.PositionDetailRes;
import jh.exp.auth.core.entity.res.PositionListRes;
import jh.exp.auth.core.mapper.OrgUnitMapper;
import jh.exp.auth.service.service.bus.PositionService;





import jh.exp.auth.core.mapper.OrgPostRelMapper;
import jh.exp.auth.core.mapper.PositionMapper;

import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.constant.CommonConstant;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private OrgPostRelMapper orgPostRelMapper;

    @Autowired
    private  OrgUnitMapper orgUnitMapper;

    @Override
    public SimplePageRes<Position> queryPosition(SimplePageReq<QueryPositionParam> positionReq) {
        int pageNum = positionReq.getPageNum();
        int pageSize = positionReq.getPageSize();
        String sort = positionReq.getSort();
        Page<Position> page = new Page<>(pageNum, pageSize);
        QueryPositionParam queryParam = positionReq.getQueryParam();
        QueryWrapper<Position> positionQueryWrapper = new QueryWrapper<>();

        // 根据用户要求，支持按岗位编码、岗位名称、岗位状态查询
        positionQueryWrapper
                .eq(StringUtils.hasText(queryParam.getPostCode()), "post_code", queryParam.getPostCode())
                .like(StringUtils.hasText(queryParam.getPostName()), "post_name", queryParam.getPostName())
                .eq(StringUtils.hasText(queryParam.getStatus()), "status", queryParam.getStatus())
                .eq(StringUtils.hasText(queryParam.getPostType()), "post_type", queryParam.getPostType())
                .orderBy(true, sort.equals("ASC"), "sort_no, create_time");

        IPage<Position> positionPage = positionMapper.selectPage(page, positionQueryWrapper);
        SimplePageRes<Position> res = new SimplePageRes<>();
        res.setList(positionPage.getRecords());
        res.setTotal(positionPage.getTotal());
        res.setPage(positionPage.getCurrent());
        res.setSize(positionPage.getSize());
        return res;
    }

    @Override
    public PositionDetailRes getPositionById(Long postId) {
        Position position = positionMapper.selectById(postId);
        if (position == null) {
            return null;
        }

        PositionDetailRes res = new PositionDetailRes();
        BeanUtils.copyProperties(position, res);

        // TODO: 这里可以添加关联查询，比如查询角色名称、创建人姓名等
        // 这里暂时只返回基本信息

        return res;
    }

    @Override
    @Transactional
    public PositionDetailRes createPosition(CreatePositionReq req) {
        // 检查岗位编码是否重复
        if (checkPostCodeExists(req.getPostCode(), null)) {
            throw new RuntimeException("岗位编码已存在");
        }

        Position position = new Position();
        BeanUtils.copyProperties(req, position);

        // 设置默认值
        position.setStatus(CommonConstant.ENABLED_STATUS_STR);
        if (position.getSortNo() == null) {
            position.setSortNo(0);
        }
        if (position.getIsSystem() == null) {
            position.setIsSystem(0);
        }

        CurrentUser currentUser = CurrentUserHolder.get();
        position.setCreatedBy(Long.valueOf(currentUser.getUserId()));
        position.setCreatedTime(LocalDateTime.now());
        position.setUpdatedTime(LocalDateTime.now());

        positionMapper.insert(position);

        return getPositionById(position.getPostId());
    }

    @Override
    @Transactional
    public PositionDetailRes updatePosition(UpdatePositionReq req) {
        Position existing = positionMapper.selectById(req.getPostId());
        if (existing == null) {
            throw new RuntimeException("岗位不存在");
        }

        // 检查岗位编码是否重复
        if (checkPostCodeExists(req.getPostCode(), req.getPostId())) {
            throw new RuntimeException("岗位编码已存在");
        }

        Position position = new Position();
        BeanUtils.copyProperties(req, position);
        position.setUpdatedTime(LocalDateTime.now());

        positionMapper.updateById(position);

        return getPositionById(req.getPostId());
    }

    @Override
    @Transactional
    public void deletePosition(Long postId) {
        Position position = positionMapper.selectById(postId);
        if (position == null) {
            throw new RuntimeException("岗位不存在");
        }

        // 检查是否为系统内置岗位
        if (position.getIsSystem() != null && position.getIsSystem() == 1) {
            throw new RuntimeException("系统内置岗位不允许删除");
        }

        // TODO: 检查是否有人员正在使用此岗位，如果有则不允许删除

        positionMapper.deleteById(postId);
    }

    @Override
    @Transactional
    public void batchDeletePositions(BatchDeletePositionReq req) {
        for (Long postId : req.getPostIds()) {
            deletePosition(postId);
        }
    }

    @Override
    @Transactional
    public PositionDetailRes updatePositionStatus(PositionStatusReq req) {
        Position position = positionMapper.selectById(req.getPostId());
        if (position == null) {
            throw new RuntimeException("岗位不存在");
        }

        UpdateWrapper<Position> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("post_id", req.getPostId())
                    .set("status", req.getStatus())
                    .set("updated_time", LocalDateTime.now());

        positionMapper.update(null, updateWrapper);

        return getPositionById(req.getPostId());
    }

    @Override
    @Transactional
    public void batchUpdatePositionStatus(BatchPositionStatusReq req) {
        UpdateWrapper<Position> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("post_id", req.getPostIds())
                    .set("status", req.getStatus())
                    .set("updated_time", LocalDateTime.now());

        positionMapper.update(null, updateWrapper);
    }

    @Override
    public SimplePageRes<PositionListRes> queryPositionsByOrgId(SimplePageReq<QueryPositionByOrgReq> req) {
        QueryPositionByOrgReq queryParam = req.getQueryParam();
        Page<PositionListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        OrgUnit orgUnit = orgUnitMapper.selectById(queryParam.getOrgId());
        if(orgUnit==null){
            throw new RuntimeException("组织不存在");
        }

        IPage<PositionListRes> iPage=null;
        Boolean includeChildren = queryParam.getIncludeChildren();

        //是根组织
        if (orgUnit.getOrgLevel() == 1) {
            if(includeChildren){
                //传空查所有
                iPage = positionMapper.selectPositionPageByOrg(page, null, queryParam.getStatus());
            }else{
                return new SimplePageRes<>(); // 直接返回空结果
            }

        }else{
            if(includeChildren){
                iPage= positionMapper.selectPositionPageByOrgAndChildren(page, queryParam.getOrgId(), queryParam.getStatus());
            }else{
                iPage= positionMapper.selectPositionPageByOrg(page, queryParam.getOrgId(), queryParam.getStatus());
            }
        }


        return new SimplePageRes<>(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), iPage.getRecords());
    }

    @Override
    public boolean checkPostCodeExists(String postCode, Long excludePostId) {
        QueryWrapper<Position> wrapper = new QueryWrapper<>();
        wrapper.eq("post_code", postCode);
        if (excludePostId != null) {
            wrapper.ne("post_id", excludePostId);
        }

        return positionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<PositionListRes> getAllEnabledPositions() {
        QueryWrapper<Position> wrapper = new QueryWrapper<>();
        wrapper.eq("status", CommonConstant.ENABLED_STATUS)
               .orderBy(true, true, "sort_no");

        List<Position> positions = positionMapper.selectList(wrapper);

        return positions.stream().map(position -> {
            PositionListRes res = new PositionListRes();
            BeanUtils.copyProperties(position, res);
            if (position.getCreatedTime() != null) {
                res.setCreatedTime(position.getCreatedTime().toString());
            }
            return res;
        }).collect(Collectors.toList());
    }
}
