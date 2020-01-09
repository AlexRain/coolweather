package com.healson.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 天气建议信息实体
 */
public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort{
        @SerializedName("txt")
        public String tips;
    }

    public class CarWash{
        @SerializedName("txt")
        public String tips;
    }

    public class Sport{
        @SerializedName("txt")
        public String tips;
    }
}
