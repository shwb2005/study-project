//package com.example.entity.user;
//
//
//import lombok.Data;
//
//@Data
//public class AccountUser {
//    int id;
//    String username;
//    String email;
//}

package com.example.entity.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountUser {
    private Integer id;
    private String username;
    private String email;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountUser() {
    }

    public AccountUser(Integer id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}

