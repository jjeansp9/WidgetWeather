
package kr.co.widgetweather.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Body {

    @SerializedName("dataType")
    @Expose
    private String dataType;
    @SerializedName("items")
    @Expose
    private Items items;
    @SerializedName("pageNo")
    @Expose
    private Integer pageNo;
    @SerializedName("numOfRows")
    @Expose
    private Integer numOfRows;
    @SerializedName("totalCount")
    @Expose
    private Integer totalCount;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getNumOfRows() {
        return numOfRows;
    }

    public void setNumOfRows(Integer numOfRows) {
        this.numOfRows = numOfRows;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

}



