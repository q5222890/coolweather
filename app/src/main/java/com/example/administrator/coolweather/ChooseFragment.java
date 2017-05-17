package com.example.administrator.coolweather;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.coolweather.db.City;
import com.example.administrator.coolweather.db.County;
import com.example.administrator.coolweather.db.Province;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseFragment extends Fragment {

    private static final String TAG ="ChooseFragment";

    private static final int LEVEL_PROVINCE =0;
    private static final int LEVEL_CITY =1;
    private static final int LEVEL_COUNTY =2;
    private ProgressDialog progressDialog;
    private TextView titleTxt;
    private Button backBtn;
    private ListView listView;
    private List<String> dataList =new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    public ChooseFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.choose_area,container,false);

        titleTxt = (TextView) view.findViewById(R.id.text_title);
        backBtn = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter =new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel ==LEVEL_PROVINCE){
                    selectedProvince =provinceList.get(position);
                    queryCities();
                }
                if(currentLevel ==LEVEL_CITY){
                    selectedCity =cityList.get(position);
                    queryCounties();
                }else if(currentLevel ==LEVEL_COUNTY){
                    String weatherId =countyList.get(position).getWeatherId();
                    Intent intent =new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel ==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {

        titleTxt.setText("中国");
        backBtn.setVisibility(View.GONE);
        provinceList =DataSupport.findAll(Province.class);
        if(provinceList.size() > 0){
            dataList.clear();
            for (Province province :provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel =LEVEL_PROVINCE;

        }else{
            String address ="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }

    }

    private void queryCities() {
        titleTxt.setText(selectedProvince.getProvinceName());
        backBtn.setVisibility(View.VISIBLE);
        cityList =DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() >0){
            dataList.clear();
            for (City city :cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel =LEVEL_CITY;
        }else{
            int provinceCode =selectedProvince.getProvinceCode();
            String address ="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounties() {
        titleTxt.setText(selectedCity.getCityName());
        backBtn.setVisibility(View.VISIBLE);
        countyList =DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() >0){
            dataList.clear();
            for (County county :countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel =LEVEL_COUNTY;
        }else{
            int provinceCode =selectedProvince.getProvinceCode();
            int cityCode =selectedCity.getCityCode();
            String address ="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {

        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.i(TAG, "onFailure : "+e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseString = response.body().string();
                Log.i(TAG, "onResponse: "+ responseString);
                boolean result =false;
                if("province".equals(type)){
                    result = Utility.handlerProvinceResponse(responseString);
                }else if("city".equals(type)){
                    result =Utility.handlerCityResponse(responseString,selectedProvince.getId());
                }else if("county".equals(type)){
                    result =Utility.handlerCountyResponse(responseString,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void closeProgressDialog() {

        if(progressDialog !=null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {

        if(progressDialog ==null){
            progressDialog =new ProgressDialog(getContext());
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }
}
