package com.healson.coolweather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.healson.coolweather.gson.Weather;
import com.healson.coolweather.util.Utility;

import java.util.prefs.PreferenceChangeEvent;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carwashText;

    private TextView sportText;

    private LinearLayout forecastLayout;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);

        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carwashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        forecastLayout = findViewById(R.id.forecast_layout);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather",null);
        if (null != weatherString){
            // 有缓存数据时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
        }else{
            // 无缓存时通过服务器获取天气数据
            String weatherId = getIntent().getStringExtra("weather_id");
        }
    }

    /**
     * 根据ID请求天气数据
     */
    private void requestWeather(final String weatherId){
        
    }
}
