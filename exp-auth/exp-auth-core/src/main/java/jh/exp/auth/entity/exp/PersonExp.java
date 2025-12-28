package jh.exp.auth.entity.exp;


import jh.exp.auth.entity.ExpPerson;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class PersonExp extends ExpPerson {

    private String orgName;
    private String postName;
    private String accountName;




}
