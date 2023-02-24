package kr.co.widgetweather.network;

import java.util.ArrayList;

import kr.co.widgetweather.model.WeeklyWeatherItem;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("getMidTa?serviceKey=CUMIKCkTvdkEuHPM3gdWXxBJ4DyeIHFWvrt8iMu6ZIcrRUhNv2dDE6G985PAAStITAlrPPrSMSjL2eBgPgk%2Bww%3D%3D")
    Call<WeeklyWeatherItem> getJson(
        @Query("pageNo") String pageNo,
        @Query("numOfRows") String numOfRows,
        @Query("dataType") String dataType,
        @Query("regId") String regId,
        @Query("tmFc") String tmFc,
        @Query("taMax3") String taMax3
    );
}



// https://apis.data.go.kr/1360000/MidFcstInfoService/getMidTa?serviceKey={API_KEY}

/* Request 전송 시 Query를 URI에 자동으로 연결해줌
 * Request 전송 시 URI : https://jsonplaceholder.typicode.com/posts?userId=입력값
 */

//@GET("posts")
//Call<List<PostResult>> getPosts(@Query("userId") String userid);