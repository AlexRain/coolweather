package com.healson.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

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

public class WeatherPagerActivity extends AppCompatActivity {

    private ViewPager viewPager;

    private List<Fragment> mListFragment = new ArrayList<>();

    private WeatherPagerAdapter adapter;

    private WeatherActivity.LocalReceiver localReceiver;

    private LocalBroadcastManager manager;

    private Button buttonList;

    private ImageView bingPicImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_pager);

        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        buttonList = findViewById(R.id.city_list);
        buttonList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherPagerActivity.this,CityListActivity.class);
                startActivityForResult(intent,0);
            }
        });

        viewPager = findViewById(R.id.view_pager);
        adapter = new WeatherPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // 设置背景图
        bingPicImage = findViewById(R.id.bing_pic_big);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = preferences.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImage);
        }else{
            loadBingPic();
        }

        // 加载缓存信息
        loadWeatherInfo();
    }

    /**
     * 加载缓存内保存的城市信息
     */
    private void loadWeatherInfo(){
        List<WeatherItem> list;
        list = LitePal.findAll(WeatherItem.class);
        if (list.size() > 0){
            for (WeatherItem item:list){
                addCity(item.getWeatherId());
            }
        }
    }

    /**
     * 接收activity返回值
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0 && null != data){
            ArrayList<String> weatherInfo = data.getStringArrayListExtra("weatherInfo");
            String currentId = data.getStringExtra("currentWeatherId");
            for (String info : weatherInfo) {
                Weather weather = Utility.handleWeatherResponse(info);
                addCity(weather);
            }
            if (null != currentId){
                for (int i = 0;i < mListFragment.size();i++){
                    WeatherFragment fragment = (WeatherFragment)mListFragment.get(i);
                    if (fragment.getWeatherId().equals(currentId)){
                        viewPager.setCurrentItem(i);
                        break;
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 加载背景大图
     */
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherPagerActivity.this).edit();
                editor.putString("bing_pic",bingPic).apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherPagerActivity.this).load(bingPic).into(bingPicImage);
                    }
                });
            }
        });
    }

    /**
     * 刷新当前页天气情况
     */
    public void refreshCurrentPageWeather() {
        WeatherFragment fragment = (WeatherFragment) mListFragment.
                get(viewPager.getCurrentItem());
        fragment.requestWeather();
    }

    /**
     * 添加城市天气☁️🐂🍺
     */
    public void addCity(Weather weatherInfo){
        WeatherFragment fragment = new WeatherFragment(weatherInfo);
        mListFragment.add(fragment);
        adapter.notifyDataSetChanged();
    }

    /**
     * 添加城市天气☁️🐂🍺
     */
    public void addCity(String weatherId){
        WeatherFragment fragment = new WeatherFragment(weatherId);
        mListFragment.add(fragment);
        adapter.notifyDataSetChanged();
    }

    private class WeatherPagerAdapter extends FragmentPagerAdapter{

        WeatherPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mListFragment.get(position);
        }

        @Override
        public int getCount() {
            return mListFragment.size();
        }
    }
}
