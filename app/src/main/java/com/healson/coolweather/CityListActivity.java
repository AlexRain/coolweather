package com.healson.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.healson.coolweather.db.WeatherItem;
import com.healson.coolweather.gson.Weather;
import com.healson.coolweather.util.HttpUtil;
import com.healson.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CityListActivity extends AppCompatActivity {

    private LinearLayout cityLayout;

    private FrameLayout chooseAreaLayout;

    private ImageView bingPicImage;

    private ArrayList<String> weatherInfoArray = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_list);

        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        cityLayout = findViewById(R.id.layout_city);
        chooseAreaLayout = findViewById(R.id.choose_area_layout);
        bingPicImage = findViewById(R.id.bing_pic_big);

        // 加载缓存图片地址
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = preferences.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImage);
        }else{
            loadBingPic();
        }

        // 加载背景图
        ImageView imageView = findViewById(R.id.image_logo);
        Glide.with(this).load("https://www.heweather.com/favicon.ico").into(imageView);

        // 加载缓存信息
        loadWeatherInfo();

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseAreaLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 加载缓存内保存的城市信息
     */
    private void loadWeatherInfo(){
        List<WeatherItem> list;
        list = LitePal.findAll(WeatherItem.class);
        if (list.size() > 0){
            for (WeatherItem item:list){
                Weather weather = Utility.handleWeatherResponse(item.getWeatherContent());
                if (null != weather){
                    addCity(weather);
                }
            }
        }
    }

    /**
     * 加载背景图片
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CityListActivity.this).edit();
                editor.putString("bing_pic",bingPic).apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(CityListActivity.this).load(bingPic).into(bingPicImage);
                    }
                });
            }
        });
    }

    /**
     * 添加城市或地区
     */
    public void addCity(final String weatherId){
        String address = "http://guolin.tech/api/weather?cityid="
                + weatherId + "&key=22b706503d044c99b9c558e36f99c445";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CityListActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != weather && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(CityListActivity.this).
                                    edit();
                            editor.putString("weather",responseText).apply();
                            List<WeatherItem> list = LitePal.where("weatherId = ?",weather.basic.weatherId).find(WeatherItem.class);
                            if (list.size() > 0){
                                list.get(0).setWeatherContent(responseText);
                                list.get(0).save();
                            }else{
                                WeatherItem item = new WeatherItem();
                                item.setWeatherContent(responseText);
                                item.setCountyName(weather.basic.cityName);
                                item.setWeatherId(weather.basic.weatherId);
                                item.save();
                            }
                            addCity(weather);
                            weatherInfoArray.add(responseText);
                        }else {
                            Toast.makeText(CityListActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * 添加城市或地区
     */
    private void addCity(@NotNull final Weather weather){
        final View view = LayoutInflater.from(this).inflate(R.layout.city_list_item,cityLayout,false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra("weatherInfo", weatherInfoArray);
                intent.putExtra("currentWeatherId",(String)view.getTag());
                setResult(0,intent);
                finish();
            }
        });
        TextView city = view.findViewById(R.id.city_name_text);
        TextView temperature = view.findViewById(R.id.temperature_text);
        TextView weatherInfo = view.findViewById(R.id.weather_info_text);
        city.setText(weather.basic.cityName);
        String degree = weather.now.temperature + "˚C";
        temperature.setText(degree);
        String info = weather.now.more.info;
        weatherInfo.setText(info);
        view.setTag(weather.basic.weatherId);
        cityLayout.addView(view);
    }

    /**
     * 隐藏选择城市界面
     */
    public void hideChooseArea(){
        chooseAreaLayout.setVisibility(View.INVISIBLE);
    }
}
