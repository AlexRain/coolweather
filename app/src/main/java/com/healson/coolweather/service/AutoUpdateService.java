package com.healson.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.healson.coolweather.gson.Weather;
import com.healson.coolweather.util.HttpUtil;
import com.healson.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePalApplication;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour = 60 * 1000; //每隔8个小时更新一次天气
        Intent i = new Intent(this,AutoUpdateService.class);
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);//先取消之前的定时服务
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi); //再设置新的
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气
     */
    private void updateWeather(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather",null );
        if ( weatherString != null){
            //有天气数据，直接解析
            Weather weather = Utility.handleWeatherResponse(weatherString);
            if (null != weather){
                String weatherId = weather.basic.weatherId;
                String address = "http://guolin.tech/api/weather?cityid=" +
                        weatherId + "&key=22b706503d044c99b9c558e36f99c445";
                HttpUtil.sendOkHttpRequest(address, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseText = response.body().string();
                        Weather weatherResponse = Utility.handleWeatherResponse(responseText);
                        if (weatherResponse != null && "ok".equals(weatherResponse.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(AutoUpdateService.this).edit();
                            editor.putString("weather",responseText).apply();
                            //TODO 刷新活动页面天气信息
                            Intent intent = new Intent("com.example.broadcasttest.LOCAL_BROADCAST");
                            LocalBroadcastManager.getInstance(LitePalApplication.getContext()).sendBroadcast(intent);
                        }
                    }
                });
            }
        }
    }

    /**
     * 更新背景图
     */
    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic).apply();
            }
        });
    }
}
