package kr.co.widgetweather.network;

import java.util.ArrayList;
import java.util.List;

import kr.co.widgetweather.model.Body;
import kr.co.widgetweather.model.Item;
import kr.co.widgetweather.model.Items;
import kr.co.widgetweather.model.Result;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("getVilageFcst?serviceKey=CUMIKCkTvdkEuHPM3gdWXxBJ4DyeIHFWvrt8iMu6ZIcrRUhNv2dDE6G985PAAStITAlrPPrSMSjL2eBgPgk%2Bww%3D%3D")
        Call<String> getJson(
        @Query("pageNo") String pageNo,
        @Query("numOfRows") String numOfRows,
        @Query("dataType") String dataType,
        @Query("base_date") String baseDate,
        @Query("base_time") String baseTime,
        @Query("nx") String nx,
        @Query("ny") String ny
    );
}



// https://apis.data.go.kr/1360000/MidFcstInfoService/getMidTa?serviceKey={API_KEY}

/* Request 전송 시 Query를 URI에 자동으로 연결해줌
 * Request 전송 시 URI : https://jsonplaceholder.typicode.com/posts?userId=입력값
 */

//@GET("posts")
//Call<List<PostResult>> getPosts(@Query("userId") String userid);