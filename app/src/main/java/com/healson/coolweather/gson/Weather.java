package com.healson.coolweather.gson;


import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 天气信息实体
 */
public class Weather {
    /**
     * 请求状态
     */
    public String status;

    /**
     * 基本信息
     */
    public Basic basic;

    /**
     * 空气质量
     */
    public AQI aqi;

    /**
     * 当前天气信息
     */
    public Now now;

    /**
     * 建议信息
     */
    public Suggestion suggestion;

    /**
     * 未来天气信息
     */
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
