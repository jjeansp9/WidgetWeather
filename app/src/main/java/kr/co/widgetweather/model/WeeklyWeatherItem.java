package kr.co.widgetweather.model;

public class WeeklyWeatherItem {
    public String tvWeek;
    public String tvTmpWeek;

    public WeeklyWeatherItem(String tvWeek, String tvTmpWeek, String tvTmnWeek) {
        this.tvWeek = tvWeek;
        this.tvTmpWeek = tvTmpWeek;
    }

    public WeeklyWeatherItem() {
    }
}
