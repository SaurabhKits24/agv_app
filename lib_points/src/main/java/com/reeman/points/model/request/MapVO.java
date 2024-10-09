package com.reeman.points.model.request;

import com.google.gson.annotations.SerializedName;

public class MapVO {

    @SerializedName("name")
    public String name;
    @SerializedName("alias")
    public String alias;

    public boolean selected = false;

    public MapVO(String name, String alias, boolean selected) {
        this.name = name;
        this.alias = alias;
        this.selected = selected;
    }

    public MapVO() {
    }
}
