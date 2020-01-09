package com.healson.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * basic实体类
 */
public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
