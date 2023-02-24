package kr.co.widgetweather.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import retrofit2.http.Query;

public class WeeklyWeatherItem {
    public String tvWeek; // 요일
    public String tvTmxWeek; // 주간 최고온도
    public String tvTmnWeek; // 주간최저온도
//    public String imgWeatherWeek; // 하늘상태 이미지

    public String numOfRows;
    public String pageNo;
    public String dataType;
    public String regId;
    public String tmFc;
    public String taMax3;

    public WeeklyWeatherItem(String taMax3, String tvWeek, String tvTmxWeek, String tvTmnWeek, String numOfRows, String pageNo, String dataType, String regId, String tmFc) {
        this.taMax3 = taMax3;
        this.tvWeek = tvWeek;
        this.tvTmxWeek = tvTmxWeek;
        this.tvTmnWeek = tvTmnWeek;
        this.numOfRows = numOfRows;
        this.pageNo = pageNo;
        this.dataType = dataType;
        this.regId = regId;
        this.tmFc = tmFc;
    }

    public WeeklyWeatherItem() {
    }
}


