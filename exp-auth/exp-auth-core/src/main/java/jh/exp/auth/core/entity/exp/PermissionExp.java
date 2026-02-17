package jh.exp.auth.core.entity.exp;

import jh.exp.auth.core.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionExp extends Permission {
    //扩展信息
    private Long extFieldLong ;
}
