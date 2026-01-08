package jh.exp.auth.entity.exp;



import jh.exp.auth.entity.Person;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class PersonExp extends Person {

    private String orgName;
    private String postName;
    private String accountName;




}
