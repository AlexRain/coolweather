package com.healson.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.healson.coolweather.db.City;
import com.healson.coolweather.db.County;
import com.healson.coolweather.db.Province;
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

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressBar progressBar;
    private TextView titleView;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    /**
     *  省列表
     */
    private List<Province> listProvince = new ArrayList<>();

    /**
     * 市列表
     */
    private List<City> listCity = new ArrayList<>();

    /**
     * 县列表
     */
    private List<County> listCounty = new ArrayList<>();

    /**
     * 选中的省份
     */
    private Province currentProvince;

    /**
     * 选中的市
     */
    private City currentCity;

    /**
     * 选中的县
     */
    private County currentCounty;

    /**
     * 选中的级别
     */
    private int currentLevel;

    /**
     *  view创建回调
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleView = view.findViewById(R.id.title_view);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        progressBar = view.findViewById(R.id.progress_bar);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    currentProvince = listProvince.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    currentCity = listCity.get(position);
                    queryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }else if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国省份，优先从数据库查询
     */
    private void queryProvinces(){
        titleView.setText("中国");
        backButton.setVisibility(View.GONE);
        listProvince = LitePal.findAll(Province.class);
        if (listProvince.size() > 0){
            dataList.clear();
            for(Province province:listProvince){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询所选省份下所有的城市，优先从数据库查
     */
    private void queryCities(){
        titleView.setText(currentProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        listCity = LitePal.where("provinceId = ?",
                String.valueOf(currentProvince.getId())).find(City.class);
        if (listCity.size() > 0){
            dataList.clear();
            for(City city:listCity){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = currentProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询所选城市下所有的县
     */
    private void queryCounties(){
        titleView.setText(currentCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        listCounty = LitePal.where("cityId = ?",
                String.valueOf(currentCity.getId())).find(County.class);
        if (listCounty.size() > 0){
            dataList.clear();
            for(County county:listCounty){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = currentProvince.getProvinceCode();
            int cityCode = currentCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型，从服务器中查询相应的省市县数据
     */
    private void queryFromServer(String address,final String type){
        showProgressBar();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressBar();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,currentProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,currentCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressBar();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });

    }

    /**
     * 显示加载控件
     */
    private void showProgressBar(){
        if (null != progressBar){
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏加载控件
     */
    private void hideProgressBar(){
        if (null != progressBar){
            progressBar.setVisibility(View.GONE);
        }
    }
}
