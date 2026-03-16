package jh.exp.auth.service.service.bus.Impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jh.exp.auth.core.constant.AuthConstant;
import jh.exp.auth.core.entity.Account;
import jh.exp.auth.core.entity.OrgUnit;
import jh.exp.auth.core.entity.Person;
import jh.exp.auth.core.entity.Position;
import jh.exp.auth.core.entity.middle.PersonOrgPostRel;
import jh.exp.auth.core.entity.req.QueryPersonReq;
import jh.exp.auth.core.entity.res.PersonInfoRes;
import jh.exp.auth.core.mapper.AccountMapper;
import jh.exp.auth.core.mapper.OrgUnitMapper;
import jh.exp.auth.core.mapper.PersonMapper;
import jh.exp.auth.core.mapper.PositionMapper;
import jh.exp.auth.core.mapper.middle.PersonOrgPostRelMapper;
import jh.exp.auth.service.entity.imex.PersonExportRow;
import jh.exp.auth.service.entity.imex.PersonExportTaskReq;
import jh.exp.auth.service.entity.imex.PersonImportRow;
import jh.exp.auth.service.service.bus.PersonImexService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.imex.ImexTaskManager;
import jh.exp.common.core.imex.ImexTaskResult;
import jh.exp.common.core.imex.ImexTaskSubmitRes;
import jh.exp.common.core.imex.ImexTaskType;
import jh.exp.common.core.util.RandomInitialPasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonImexServiceImpl implements PersonImexService {

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String BIZ_CODE = "AUTH_PERSON";

    private final PersonMapper personMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final PositionMapper positionMapper;
    private final AccountMapper accountMapper;
    private final PersonOrgPostRelMapper personOrgPostRelMapper;
    private final ImexTaskManager imexTaskManager;

    @Override
    public byte[] downloadImportTemplate() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        EasyExcel.write(os, PersonImportRow.class).sheet("人员导入模板").doWrite(Collections.emptyList());
        return os.toByteArray();
    }

    @Override
    public ImexTaskSubmitRes submitImportTask(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择导入文件");
        }
        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName) || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new RuntimeException("仅支持 .xlsx 文件");
        }
        final byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取导入文件失败");
        }

        ImexTaskSubmitRes submitRes = imexTaskManager.createTask(BIZ_CODE, ImexTaskType.IMPORT);
        imexTaskManager.runAsync(submitRes.getTaskId(), ctx -> {
            List<PersonImportRow> rows;
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                rows = EasyExcel.read(is).head(PersonImportRow.class).sheet().doReadSync();
            } catch (IOException e) {
                ctx.markFailed("读取导入文件失败");
                return;
            }
            if (rows == null) {
                rows = Collections.emptyList();
            }
            rows = rows.stream().filter(r -> !isEmptyRow(r)).toList();
            ctx.setTotalRows(rows.size());
            if (rows.isEmpty()) {
                ctx.setMessage("导入文件没有有效数据");
                return;
            }

            Set<String> personCodes = rows.stream()
                    .map(PersonImportRow::getPersonCode)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toSet());
            Set<String> orgCodes = rows.stream()
                    .map(PersonImportRow::getOrgCode)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toSet());
            Set<String> postCodes = rows.stream()
                    .map(PersonImportRow::getPostCode)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            Set<String> existedCodeSet = personCodes.isEmpty()
                    ? Collections.emptySet()
                    : personMapper.selectByPersonCodes(new ArrayList<>(personCodes)).stream()
                    .map(Person::getPersonCode)
                    .collect(Collectors.toSet());

            Map<String, OrgUnit> orgCodeMap = orgCodes.isEmpty()
                    ? Collections.emptyMap()
                    : orgUnitMapper.selectList(Wrappers.lambdaQuery(OrgUnit.class).in(OrgUnit::getOrgCode, orgCodes)).stream()
                    .collect(Collectors.toMap(OrgUnit::getOrgCode, Function.identity(), (a, b) -> a));

            Map<String, Position> postCodeMap = postCodes.isEmpty()
                    ? Collections.emptyMap()
                    : positionMapper.selectList(Wrappers.lambdaQuery(Position.class).in(Position::getPostCode, postCodes)).stream()
                    .collect(Collectors.toMap(Position::getPostCode, Function.identity(), (a, b) -> a));

            Map<String, Integer> seenCodeMap = new HashMap<>();
            CurrentUser currentUser = CurrentUserHolder.get();
            Long createdBy = currentUser == null ? null : currentUser.getUserId();
            if (createdBy == null) {
                ctx.markFailed("当前登录用户无效");
                return;
            }

            for (int idx = 0; idx < rows.size(); idx++) {
                int rowNo = idx + 2;
                PersonImportRow row = normalizeRow(rows.get(idx));

                if (!StringUtils.hasText(row.getPersonCode())) {
                    ctx.addFailure(rowNo, "REQUIRED_MISSING", "人员编码为空");
                    continue;
                }
                if (!StringUtils.hasText(row.getPersonName())) {
                    ctx.addFailure(rowNo, "REQUIRED_MISSING", "姓名为空");
                    continue;
                }
                if (!StringUtils.hasText(row.getOrgCode()) || !StringUtils.hasText(row.getPostCode())) {
                    ctx.addFailure(rowNo, "REQUIRED_MISSING", "组织编码或岗位编码为空");
                    continue;
                }

                Integer seen = seenCodeMap.putIfAbsent(row.getPersonCode(), rowNo);
                if (seen != null) {
                    ctx.addFailure(rowNo, "DUPLICATE_CODE", "人员编码重复");
                    continue;
                }
                if (existedCodeSet.contains(row.getPersonCode())) {
                    ctx.addFailure(rowNo, "DUPLICATE_CODE", "人员编码已存在");
                    continue;
                }

                OrgUnit orgUnit = orgCodeMap.get(row.getOrgCode());
                if (orgUnit == null) {
                    ctx.addFailure(rowNo, "REF_NOT_FOUND", "组织编码不存在");
                    continue;
                }
                Position position = postCodeMap.get(row.getPostCode());
                if (position == null) {
                    ctx.addFailure(rowNo, "REF_NOT_FOUND", "岗位编码不存在");
                    continue;
                }

                Person person = new Person();
                person.setPersonCode(row.getPersonCode());
                person.setPersonName(row.getPersonName());
                person.setMobile(row.getMobile());
                person.setEmail(row.getEmail());
                person.setGender(resolveGender(row.getGender()));
                person.setOrgId(orgUnit.getOrgId());
                person.setPostId(position.getPostId());
                person.setStatus(resolveStatus(row.getStatus()));
                person.setRemark(row.getRemark());
                person.setCreatedBy(createdBy);
                person.setCreatedTime(LocalDateTime.now());
                person.setUpdatedTime(LocalDateTime.now());
                try {
                    personMapper.insert(person);

                    Account account = Account.builder()
                            .accountName(RandomInitialPasswordUtil.getExpRandomId())
                            .accountDisplay(row.getPersonName())
                            .passwordHash("")
                            .mobile(row.getMobile())
                            .email(row.getEmail())
                            .personId(person.getPersonId())
                            .orgId(person.getOrgId())
                            .postId(person.getPostId())
                            .status(AuthConstant.INIT)
                            .needChangePwd(true)
                            .createdBy(createdBy)
                            .build();
                    accountMapper.insert(account);
                    person.setAccountId(account.getAccountId());
                    personMapper.updateById(person);

                    PersonOrgPostRel rel = PersonOrgPostRel.builder()
                            .personId(person.getPersonId())
                            .orgId(person.getOrgId())
                            .postId(person.getPostId())
                            .roleId(AuthConstant.DEFAULT_ROLE)
                            .isPrimary(1)
                            .startDate(LocalDate.now())
                            .endDate(LocalDate.now().plusYears(1))
                            .status(AuthConstant.STATUS_TBD)
                            .build();
                    personOrgPostRelMapper.insert(rel);
                    ctx.addSuccess(1);
                } catch (Exception ex) {
                    // 导入失败时尽量回滚本行已写入数据，避免残留半条数据
                    Account rollbackAccount = accountMapper.selectOne(Wrappers.lambdaQuery(Account.class)
                            .eq(Account::getPersonId, person.getPersonId()));
                    if (rollbackAccount != null && rollbackAccount.getAccountId() != null) {
                        accountMapper.deleteById(rollbackAccount.getAccountId());
                    }
                    if (person.getPersonId() != null) {
                        personMapper.deleteById(person.getPersonId());
                    }
                    ctx.addFailure(rowNo, "SAVE_FAILED", "保存失败");
                }
            }
            ctx.setMessage("导入完成");
        });
        return submitRes;
    }

    @Override
    public ImexTaskSubmitRes submitExportTask(PersonExportTaskReq req) {
        PersonExportTaskReq actualReq = req == null ? new PersonExportTaskReq() : req;
        ImexTaskSubmitRes submitRes = imexTaskManager.createTask(BIZ_CODE, ImexTaskType.EXPORT);
        imexTaskManager.runAsync(submitRes.getTaskId(), ctx -> {
            String mode = StringUtils.hasText(actualReq.getMode()) ? actualReq.getMode().trim().toUpperCase(Locale.ROOT) : "FILTER";
            if ("SELECTED".equals(mode) && CollectionUtils.isEmpty(actualReq.getPersonIds())) {
                ctx.markFailed("未选择导出数据");
                return;
            }
            QueryPersonReq query = new QueryPersonReq();
            if ("FILTER".equals(mode)) {
                query.setPersonCode(trim(actualReq.getPersonCode()));
                query.setPersonName(trim(actualReq.getPersonName()));
                query.setMobile(trim(actualReq.getMobile()));
            }

            List<Long> personIds = "SELECTED".equals(mode) ? actualReq.getPersonIds() : Collections.emptyList();
            List<PersonInfoRes> list = personMapper.selectPersonForExport(personIds, query);
            ctx.setTotalRows(list.size());
            ctx.addSuccess(list.size());

            List<PersonExportRow> exportRows = list.stream().map(this::toExportRow).toList();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            EasyExcel.write(os, PersonExportRow.class).sheet("人员导出").doWrite(exportRows);
            String fileName = "人员导出_" + System.currentTimeMillis() + ".xlsx";
            ctx.setExportFile(fileName, EXCEL_CONTENT_TYPE, os.toByteArray());
            ctx.setMessage("导出完成");
        });
        return submitRes;
    }

    @Override
    public ImexTaskResult queryTask(String taskId) {
        ImexTaskResult result = imexTaskManager.queryTask(taskId);
        if (result == null) {
            throw new RuntimeException("任务不存在或已过期");
        }
        return result;
    }

    @Override
    public byte[] downloadExportFile(String taskId) {
        byte[] bytes = imexTaskManager.queryExportFileBytes(taskId);
        if (bytes == null || bytes.length == 0) {
            throw new RuntimeException("导出文件未就绪");
        }
        return bytes;
    }

    @Override
    public String queryExportContentType(String taskId) {
        String contentType = imexTaskManager.queryExportContentType(taskId);
        return StringUtils.hasText(contentType) ? contentType : EXCEL_CONTENT_TYPE;
    }

    @Override
    public String queryExportFileName(String taskId) {
        ImexTaskResult result = queryTask(taskId);
        if (StringUtils.hasText(result.getExportFileName())) {
            return result.getExportFileName();
        }
        return "人员导出.xlsx";
    }

    private PersonExportRow toExportRow(PersonInfoRes source) {
        PersonExportRow row = new PersonExportRow();
        row.setPersonCode(source.getPersonCode());
        row.setPersonName(source.getPersonName());
        row.setMobile(source.getMobile());
        row.setEmail(source.getEmail());
        row.setGender(source.getGender());
        row.setOrgName(source.getOrgName());
        row.setPostName(source.getPostName());
        row.setStatus(source.getStatus());
        return row;
    }

    private boolean isEmptyRow(PersonImportRow row) {
        if (row == null) {
            return true;
        }
        return !StringUtils.hasText(row.getPersonCode())
                && !StringUtils.hasText(row.getPersonName())
                && !StringUtils.hasText(row.getMobile())
                && !StringUtils.hasText(row.getOrgCode())
                && !StringUtils.hasText(row.getPostCode());
    }

    private PersonImportRow normalizeRow(PersonImportRow row) {
        PersonImportRow result = row == null ? new PersonImportRow() : row;
        result.setPersonCode(trim(result.getPersonCode()));
        result.setPersonName(trim(result.getPersonName()));
        result.setMobile(trim(result.getMobile()));
        result.setEmail(trim(result.getEmail()));
        result.setGender(trim(result.getGender()));
        result.setOrgCode(trim(result.getOrgCode()));
        result.setPostCode(trim(result.getPostCode()));
        result.setStatus(trim(result.getStatus()));
        result.setRemark(trim(result.getRemark()));
        return result;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String resolveStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return AuthConstant.ONJOB;
        }
        String upper = status.trim().toUpperCase(Locale.ROOT);
        if (Objects.equals(upper, AuthConstant.ONJOB)
                || Objects.equals(upper, AuthConstant.LEAVE)
                || Objects.equals(upper, AuthConstant.DISABLED)) {
            return upper;
        }
        return AuthConstant.ONJOB;
    }

    private String resolveGender(String gender) {
        if (!StringUtils.hasText(gender)) {
            return null;
        }
        String upper = gender.trim().toUpperCase(Locale.ROOT);
        if (Objects.equals(upper, "M") || Objects.equals(upper, "F") || Objects.equals(upper, "OTHER")) {
            return upper;
        }
        if (Objects.equals(gender.trim(), "男")) {
            return "M";
        }
        if (Objects.equals(gender.trim(), "女")) {
            return "F";
        }
        return null;
    }
}
