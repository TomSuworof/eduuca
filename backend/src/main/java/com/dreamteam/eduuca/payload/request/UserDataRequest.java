package com.dreamteam.eduuca.payload.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Setter
@ToString
public class UserDataRequest {
    private Optional<String> avatar;
    private Optional<String> email;
    private Optional<String> bio;

    @ToString.Exclude
    private Optional<String> password;

    public UserDataRequest() {
        this.avatar = Optional.empty();
        this.email = Optional.empty();
        this.bio = Optional.empty();
        this.password = Optional.empty();
    }
}
