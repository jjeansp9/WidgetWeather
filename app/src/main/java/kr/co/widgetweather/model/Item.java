package kr.co.widgetweather.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {

    @SerializedName("baseDate")
    @Expose
    private String baseDate;
    @SerializedName("baseTime")
    @Expose
    private String baseTime;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("fcstDate")
    @Expose
    private String fcstDate;
    @SerializedName("fcstTime")
    @Expose
    private String fcstTime;
    @SerializedName("fcstValue")
    @Expose
    private String fcstValue;
    @SerializedName("nx")
    @Expose
    private Integer nx;
    @SerializedName("ny")
    @Expose
    private Integer ny;

    public String getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(String baseDate) {
        this.baseDate = baseDate;
    }

    public String getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(String baseTime) {
        this.baseTime = baseTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFcstDate() {
        return fcstDate;
    }

    public void setFcstDate(String fcstDate) {
        this.fcstDate = fcstDate;
    }

    public String getFcstTime() {
        return fcstTime;
    }

    public void setFcstTime(String fcstTime) {
        this.fcstTime = fcstTime;
    }

    public String getFcstValue() {
        return fcstValue;
    }

    public void setFcstValue(String fcstValue) {
        this.fcstValue = fcstValue;
    }

    public Integer getNx() {
        return nx;
    }

    public void setNx(Integer nx) {
        this.nx = nx;
    }

    public Integer getNy() {
        return ny;
    }

    public void setNy(Integer ny) {
        this.ny = ny;
    }

}