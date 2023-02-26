package kr.co.widgetweather.model;

public class WeeklyWeatherItem {
    public String tvWeek;
    public String tvTmxWeek;
    public String tvTmnWeek;

    public WeeklyWeatherItem(String tvWeek, String tvTmxWeek, String tvTmnWeek) {
        this.tvWeek = tvWeek;
        this.tvTmxWeek = tvTmxWeek;
        this.tvTmnWeek = tvTmnWeek;
    }

    public WeeklyWeatherItem() {
    }
}
