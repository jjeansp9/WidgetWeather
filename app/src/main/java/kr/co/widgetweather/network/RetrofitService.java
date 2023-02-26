package kr.co.widgetweather.network;

import kr.co.widgetweather.model.Result;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("getMidTa?serviceKey=CUMIKCkTvdkEuHPM3gdWXxBJ4DyeIHFWvrt8iMu6ZIcrRUhNv2dDE6G985PAAStITAlrPPrSMSjL2eBgPgk%2Bww%3D%3D")
        Call<Result> getJson(
        @Query("pageNo") String pageNo,
        @Query("numOfRows") String numOfRows,
        @Query("dataType") String dataType,
        @Query("regId") String regId,
        @Query("tmFc") String tmFc
    );

    // 9. GET 방식으로 서버로부터 응답을 받되, 그냥 글씨로 파싱없이
    @GET("getMidTa?serviceKey=CUMIKCkTvdkEuHPM3gdWXxBJ4DyeIHFWvrt8iMu6ZIcrRUhNv2dDE6G985PAAStITAlrPPrSMSjL2eBgPgk%2Bww%3D%3D")
    Call<String> getPlainText(
            @Query("pageNo") String pageNo,
            @Query("numOfRows") String numOfRows,
            @Query("dataType") String dataType,
            @Query("regId") String regId,
            @Query("tmFc") String tmFc
    );
}



// https://apis.data.go.kr/1360000/MidFcstInfoService/getMidTa?serviceKey={API_KEY}

/* Request 전송 시 Query를 URI에 자동으로 연결해줌
 * Request 전송 시 URI : https://jsonplaceholder.typicode.com/posts?userId=입력값
 */

//@GET("posts")
//Call<List<PostResult>> getPosts(@Query("userId") String userid);