package kr.co.widgetweather.model;

public class WeeklyWeatherItem {
    public String tvWeek; // 요일
    public int imgPop; // 강수확률 이미지
    public String tvPop; // 강수확률 텍스트
    public int imgSky; // 하늘상태 이미지
    public String tvTmpWeek; // 1시간 기온

    public WeeklyWeatherItem(String tvWeek, int imgPop, String tvPop, int imgSky, String tvTmpWeek) {
        this.tvWeek = tvWeek;
        this.imgPop = imgPop;
        this.tvPop = tvPop;
        this.imgSky = imgSky;
        this.tvTmpWeek = tvTmpWeek;
    }

    public WeeklyWeatherItem() {
    }
}
