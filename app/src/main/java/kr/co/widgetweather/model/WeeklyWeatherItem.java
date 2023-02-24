package kr.co.widgetweather.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeeklyWeatherItem {
    public String tvWeek; // 요일
    public String tvTmxWeek; // 주간 최고온도
    public String tvTmnWeek; // 주간최저온도
//    public String imgWeatherWeek; // 하늘상태 이미지

    public String taMin4;

    public WeeklyWeatherItem(String taMin4, String tvWeek, String tvTmxWeek, String tvTmnWeek) {
        this.taMin4= taMin4;
        this.tvWeek = tvWeek;
        this.tvTmxWeek = tvTmxWeek;
        this.tvTmnWeek = tvTmnWeek;
    }

    class items{
        List<Item> item;
    }
    class Item{
        String taMin4;
    }
}




