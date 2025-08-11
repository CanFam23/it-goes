package com.it_goes.api.util.enums;

import lombok.Getter;

@Getter
public enum State {
    MT("Montana"),
    WA("Washington"),
    ID("Idaho"),
    UT("Utah"),
    AK("Alaska"),
    CA("California"),
    WY("Wyoming"),
    OR("Oregon");

    private final String name;

    State(String name){
        this.name = name;
    }

}
