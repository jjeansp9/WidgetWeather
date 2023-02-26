package kr.co.widgetweather.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Helper {

    // 중기기온조회 baseUrl
    static final String BASE_URL = "http://apis.data.go.kr/1360000/MidFcstInfoService/";

    public static Retrofit getInstance(){
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
