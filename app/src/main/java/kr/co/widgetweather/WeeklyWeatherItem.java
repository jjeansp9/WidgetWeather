package kr.co.widgetweather;

public class WeeklyWeatherItem {
    String tvWeek; // 요일
    String tvTmxWeek; // 주간 최고온도
    String tvTmnWeek; // 주간최저온도
//    String imgWeatherWeek; // 하늘상태 이미지

    public WeeklyWeatherItem(String tvWeek, String tvTmxWeek, String tvTmnWeek) {
        this.tvWeek = tvWeek;
        this.tvTmxWeek = tvTmxWeek;
        this.tvTmnWeek = tvTmnWeek;
    }
}
