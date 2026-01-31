package jh.exp.auth.service.service.bus.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jh.exp.auth.core.entity.OrgPostRel;
import jh.exp.auth.core.entity.OrgUnit;
import jh.exp.auth.core.entity.Position;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.auth.core.entity.res.PositionDetailRes;
import jh.exp.auth.core.entity.res.PositionListRes;
import jh.exp.auth.core.mapper.OrgUnitMapper;
import jh.exp.auth.core.mapper.RoleMapper;
import jh.exp.auth.service.service.bus.PersonService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private OrgPostRelMapper orgPostRelMapper;

    @Autowired
    private  OrgUnitMapper orgUnitMapper;

    @Autowired
    private PersonService  personService;

    @Autowired
    private RoleMapper roleMapper;

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

        Long createdBy = position.getCreatedBy();
        if (createdBy==null||createdBy==0){
            res.setCreatedByName("system");
        }else{
            String accountName = personService.getPersonById(createdBy).getAccountName();
            res.setCreatedByName(accountName);
        }

        Long defaultRoleId = position.getDefaultRoleId();
        if (defaultRoleId==null||defaultRoleId==0){
            res.setDefaultRoleName("无");
        }else{
            String roleName = roleMapper.selectRoleDetailById(defaultRoleId).getRoleName();
            res.setDefaultRoleName(roleName);
        }

        return res;
    }

    @Override
    @Transactional
    public PositionDetailRes createPosition(CreatePositionReq req) {
        String postCode = req.getPostCode();
        Integer isOutsourcing = (req.getIsOutsourcing() != null && req.getIsOutsourcing() == 1) ? 1 : 0;
        Integer primaryFlag = isOutsourcing == 1 ? 0 : 1;
        CurrentUser currentUser = CurrentUserHolder.get();
        //主岗位
        if(CommonConstant.NUM_1.equals(primaryFlag)){
            // 检查岗位编码是否重复
            if (checkPostCodeExists(postCode, null)) {
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


            position.setCreatedBy(currentUser.getUserId());
            position.setCreatedTime(LocalDateTime.now());
            position.setUpdatedTime(LocalDateTime.now());

            positionMapper.insert(position);

        }

        Position pos= positionMapper.selectOne(new LambdaQueryWrapper<Position>()
                .eq(Position::getPostCode, postCode)
                .eq(Position::getStatus, "ENABLED")
                .last("LIMIT 1") // 更加保险，防止查出多条报错
        );
        if (ObjectUtil.isEmpty(pos)){
            throw new RuntimeException("系统异常，岗位不存在");
        }

        String orgCode = req.getOrgCode();
        OrgUnit orgUnit = orgUnitMapper.selectOne(new LambdaQueryWrapper<OrgUnit>()
                .eq(OrgUnit::getOrgCode, orgCode)
                .eq(OrgUnit::getStatus, "ENABLED")
                .last("LIMIT 1")
        );
        if (ObjectUtil.isEmpty(orgUnit)){
            throw new RuntimeException("组织不存在");
        }


        //插入关系表 exp_org_post_rel
        OrgPostRel orgPostRel = OrgPostRel.builder()
                .orgId(orgUnit.getOrgId())
                .postId(pos.getPostId())
                .isPrimary(primaryFlag)
                .status(CommonConstant.ENABLED_STATUS_STR)
                .sortNo(CommonConstant.NUM_1)
                .createdBy(currentUser.getUserId())
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .remark((req.getRemark() == null || req.getRemark().isBlank()) ? "" : req.getRemark())
                .build();

        int insert = orgPostRelMapper.insert(orgPostRel);
        if (insert <= 0) {
            throw new RuntimeException("添加岗位关系失败");
        }


        return getPositionById(pos.getPostId());
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

        String orgCode = req.getOrgCode();
        Integer isOutsourcing = req.getIsOutsourcing();
        if (StringUtils.hasText(orgCode) && isOutsourcing != null) {
            OrgUnit orgUnit = orgUnitMapper.selectOne(new LambdaQueryWrapper<OrgUnit>()
                    .eq(OrgUnit::getOrgCode, orgCode)
                    .eq(OrgUnit::getStatus, "ENABLED")
                    .last("LIMIT 1")
            );
            if (ObjectUtil.isEmpty(orgUnit)) {
                throw new RuntimeException("组织不存在");
            }
            // 是否外派：1外派/0非外派（主岗位）
            Integer primaryFlag = isOutsourcing == 1 ? 0 : 1;
            UpdateWrapper<OrgPostRel> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("org_id", orgUnit.getOrgId())
                    .eq("post_id", req.getPostId());
            OrgPostRel relUpdate = new OrgPostRel();
            relUpdate.setIsPrimary(primaryFlag);
            relUpdate.setUpdatedTime(LocalDateTime.now());
            int updated = orgPostRelMapper.update(relUpdate, updateWrapper);
            if (updated <= 0) {
                throw new RuntimeException("岗位关系不存在");
            }
        }

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
    public SimplePageRes<PositionListRes> queryPositions(SimplePageReq<QueryPositionByOrgReq> req) {
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
                List<PositionListRes> positionListRes = positionMapper.selectPositionListByOrg(queryParam.getOrgId(), queryParam.getStatus());
                return new SimplePageRes<>(positionListRes);
            }
        }


        return new SimplePageRes<>(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), iPage.getRecords());
    }

    @Override
    @Transactional
    public String outsourcePosition(OutsourcePositionReq req) {
        Position position = positionMapper.selectById(req.getPostId());
        if (position == null) {
            throw new RuntimeException("岗位不存在");
        }

        OrgUnit currentOrg = orgUnitMapper.selectById(req.getCurrentOrgId());
        if (currentOrg == null) {
            throw new RuntimeException("当前组织不存在");
        }

        OrgUnit targetOrg = orgUnitMapper.selectById(req.getTargetOrgId());
        if (targetOrg == null) {
            throw new RuntimeException("外派组织不存在");
        }

        Integer isOutsourcing = (req.getIsOutsourcing() != null && req.getIsOutsourcing() == 1) ? 1 : 0;
        Integer primaryFlag = isOutsourcing == 1 ? 0 : 1;

        OrgPostRel existing = orgPostRelMapper.selectOne(new LambdaQueryWrapper<OrgPostRel>()
                .eq(OrgPostRel::getOrgId, req.getTargetOrgId())
                .eq(OrgPostRel::getPostId, req.getPostId())
                .last("LIMIT 1")
        );

        CurrentUser currentUser = CurrentUserHolder.get();
        LocalDateTime now = LocalDateTime.now();
        String remark = (req.getRemark() == null || req.getRemark().isBlank()) ? "" : req.getRemark();

        if (existing != null) {
            OrgPostRel relUpdate = new OrgPostRel();
            relUpdate.setIsPrimary(primaryFlag);
            relUpdate.setStatus(req.getStatus());
            relUpdate.setRemark(remark);
            relUpdate.setUpdatedTime(now);
            UpdateWrapper<OrgPostRel> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("org_id", req.getTargetOrgId())
                    .eq("post_id", req.getPostId());
            orgPostRelMapper.update(relUpdate, updateWrapper);
        } else {
            OrgPostRel rel = OrgPostRel.builder()
                    .orgId(req.getTargetOrgId())
                    .postId(req.getPostId())
                    .isPrimary(primaryFlag)
                    .status(req.getStatus())
                    .sortNo(CommonConstant.NUM_1)
                    .createdBy(currentUser.getUserId())
                    .createdTime(now)
                    .updatedTime(now)
                    .remark(remark)
                    .build();
            orgPostRelMapper.insert(rel);
        }

        String orgFullName = buildOrgFullName(targetOrg);
        return "成功外派到‘" + orgFullName + "’/‘" + position.getPostName() + "’";
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

    private String buildOrgFullName(OrgUnit targetOrg) {
        String orgPath = targetOrg.getOrgPath();
        if (!StringUtils.hasText(orgPath)) {
            return targetOrg.getOrgName();
        }
        String trimmed = orgPath;
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (!StringUtils.hasText(trimmed)) {
            return targetOrg.getOrgName();
        }
        String[] parts = trimmed.split("/");
        List<Long> orgIds = new ArrayList<>();
        for (String part : parts) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            try {
                orgIds.add(Long.valueOf(part));
            } catch (NumberFormatException ignored) {
            }
        }
        if (orgIds.isEmpty()) {
            return targetOrg.getOrgName();
        }
        List<OrgUnit> orgUnits = orgUnitMapper.selectBatchIds(orgIds);
        Map<Long, String> nameMap = new HashMap<>();
        for (OrgUnit unit : orgUnits) {
            nameMap.put(unit.getOrgId(), unit.getOrgName());
        }
        List<String> names = new ArrayList<>();
        for (Long orgId : orgIds) {
            String name = nameMap.get(orgId);
            if (StringUtils.hasText(name)) {
                names.add(name);
            }
        }
        return names.isEmpty() ? targetOrg.getOrgName() : String.join("/", names);
    }

}
