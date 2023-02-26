package kr.co.widgetweather.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {

    @SerializedName("regId")
    @Expose
    private String regId;
    @SerializedName("taMin3")
    @Expose
    private Integer taMin3;
    @SerializedName("taMin3Low")
    @Expose
    private Integer taMin3Low;


    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public Integer getTaMin3() {
        return taMin3;
    }

    public void setTaMin3(Integer taMin3) {
        this.taMin3 = taMin3;
    }

    public Integer getTaMin3Low() {
        return taMin3Low;
    }

    public void setTaMin3Low(Integer taMin3Low) {
        this.taMin3Low = taMin3Low;
    }
}