package com.it_goes.api.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDataResponse {
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    public UserDataResponse(String username, String email, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
