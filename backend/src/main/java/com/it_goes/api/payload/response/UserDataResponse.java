package com.it_goes.api.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDataResponse {
    private String username;
    private String firstName;

    public UserDataResponse(String username, String firstName) {
        this.username = username;
        this.firstName = firstName;
    }
}
