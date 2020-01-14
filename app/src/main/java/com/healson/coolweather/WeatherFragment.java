package com.healson.coolweather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.healson.coolweather.db.WeatherItem;
import com.healson.coolweather.gson.Forecast;
import com.healson.coolweather.gson.Weather;
import com.healson.coolweather.util.HttpUtil;
import com.healson.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherFragment extends Fragment{

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private LinearLayout forecastLayout;

    public SwipeRefreshLayout swipeRefresh;

    private Weather weatherInfo;

    private String weatherId;

    public WeatherFragment(Weather info) {
        super();
        this.weatherInfo = info;
    }

    public WeatherFragment() {
        super();
    }

    public WeatherFragment(String weatherId) {
        super();
        this.weatherId = weatherId;
    }

    String getWeatherId(){
        return this.weatherId;
    }

    void setWeatherId(String weatherId){
        this.weatherId = weatherId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_info,container,false);
        weatherLayout = view.findViewById(R.id.weather_layout);

        titleCity = view.findViewById(R.id.title_city);
        titleUpdateTime = view.findViewById(R.id.title_update_time);
        degreeText = view.findViewById(R.id.degree_text);
        weatherInfoText = view.findViewById(R.id.weather_info_text);

        aqiText = view.findViewById(R.id.aqi_text);
        pm25Text = view.findViewById(R.id.pm25_text);
        comfortText = view.findViewById(R.id.comfort_text);
        carWashText = view.findViewById(R.id.car_wash_text);
        sportText = view.findViewById(R.id.sport_text);
        forecastLayout = view.findViewById(R.id.forecast_layout);

        // 下拉刷新
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (null != weatherId){
                    requestWeather(weatherId);
                }
            }
        });

        if(weatherId != null){
            requestWeather();
        }
        else if (null != weatherInfo){
            showWeatherInfo(weatherInfo);
        }

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 根据ID请求天气数据
     */
    public void requestWeather(@NotNull final String weatherId){
        String address = "http://guolin.tech/api/weather?cityid="
                + weatherId + "&key=22b706503d044c99b9c558e36f99c445";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != weather && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(getActivity()).
                                    edit();
                            editor.putString("weather",responseText).apply();
                            setWeatherId(weatherId);
                            showWeatherInfo(weather);

                            // 更新缓存下的天气信息
                            List<WeatherItem> list = LitePal.where("weatherId = ?",
                                    weather.basic.weatherId).find(WeatherItem.class);
                            if (list.size() > 0){
                                list.get(0).setWeatherContent(responseText);
                                list.get(0).save();
                            }
                        }else {
                            Toast.makeText(getActivity(), "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 根据缓存的城市地点ID查询天气情况
     */
    public void requestWeather(){
        if (weatherId != null){
            requestWeather(weatherId);
        }
    }

    /**
     * 显示天气信息
     */
    public void showWeatherInfo(Weather weather){
        this.weatherId = weather.basic.weatherId;
        String cityName =weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1]; //取时间
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = view.findViewById(R.id.data_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView minText = view.findViewById(R.id.min_text);
            TextView maxText = view.findViewById(R.id.max_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            minText.setText(forecast.temperature.min);
            maxText.setText(forecast.temperature.max);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.tips;
        String carWash = "洗车指数：" + weather.suggestion.carWash.tips;
        String sport = "运动建议：" + weather.suggestion.sport.tips;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
