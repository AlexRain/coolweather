package com.healson.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 当前天气信息实体
 */
public class Now {
    public String tmp;

    public More more;

    public class More{

        @SerializedName("txt")
        public String info;
    }
}
