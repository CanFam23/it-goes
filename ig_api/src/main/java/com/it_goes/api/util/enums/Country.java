package com.it_goes.api.util.enums;

import lombok.Getter;

@Getter
public enum Country {
    US("United States"),
    CA("Canada"),
    JPN("Japan"),
    FR("France"),
    IT("Italy"),
    CH("Switzerland"),
    NO("Norway");

    private final String name;

    Country(String name){
        this.name = name;
    }
}
