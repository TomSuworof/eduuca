package com.dreamteam.eduuca.payload.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SignupRequest {
    private String username;
    private String email;
    @ToString.Exclude private String password;
}
