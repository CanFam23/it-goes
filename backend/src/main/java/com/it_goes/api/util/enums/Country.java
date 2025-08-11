package com.it_goes.api.util.enums;

import lombok.Getter;

@Getter
public enum Country {
    US("United States"),
    CA("Canada"),
    JPN("Japan");

    private final String name;

    Country(String name){
        this.name = name;
    }
}
