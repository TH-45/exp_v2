package jh.exp.auth.service.entity.imex;

import lombok.Data;

import java.util.List;

@Data
public class PersonExportTaskReq {
    /**
     * SELECTED / FILTER / ALL
     */
    private String mode;
    private List<Long> personIds;
    private String personCode;
    private String personName;
    private String mobile;
}
