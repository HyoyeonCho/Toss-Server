package com.payday.toss_server.dto;

import lombok.Data;

@Data
public class UserDTO {

    private long userId;
    private String customerKey;
    private String name;
    private String email;
    private String phone;

}
