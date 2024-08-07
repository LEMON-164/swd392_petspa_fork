package org.petspa.petcaresystem.authenuser.model.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomAuthenUserForRegister {
    private String userName;
    private String email;
    private String fullName;
    private int age;
    private String gender;
    private String address;
    private String phone;
    private String create_date;
    private String status;
    private String role;
}
