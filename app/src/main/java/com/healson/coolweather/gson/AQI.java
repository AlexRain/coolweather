package com.healson.coolweather.gson;

/**
 * 空气质量实体
 */
public class AQI {

    public AQICity city;

    public class AQICity{

        public String aqi;

        public String pm25;
    }
}
