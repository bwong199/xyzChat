package com.benwong.geochat;

/**
 * Created by benwong on 2016-03-04.
 */
public class Country {
    private String iso;
    private String code;
    private String name;

    Country(String iso, String code, String name) {
        this.iso = iso;
        this.code = code;
        this.name = name;
    }

    public String toString() {
        return iso + " - " + code + " - " + name.toUpperCase();
    }
}

