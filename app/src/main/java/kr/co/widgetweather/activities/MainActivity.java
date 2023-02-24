package kr.co.widgetweather.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

import kr.co.widgetweather.R;
import kr.co.widgetweather.databinding.ActivityMainBinding;
import kr.co.widgetweather.model.WeeklyWeatherItem;
import kr.co.widgetweather.adapters.WeeklyWeatherRecyclerAdapter;
import kr.co.widgetweather.network.RetrofitHelper;
import kr.co.widgetweather.network.RetrofitService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    RecyclerView recycler;
    WeeklyWeatherRecyclerAdapter adapter;
    ArrayList<WeeklyWeatherItem> weekItems = new ArrayList<>();

    // 공공데이터 사이트에서 발급받은 api 키
    static final String API_KEY = "CUMIKCkTvdkEuHPM3gdWXxBJ4DyeIHFWvrt8iMu6ZIcrRUhNv2dDE6G985PAAStITAlrPPrSMSjL2eBgPgk%2Bww%3D%3D";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler= findViewById(R.id.recyler_weather_weekly);
        adapter= new WeeklyWeatherRecyclerAdapter(this, weekItems);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

//        for(int i= 0; i<7 ; i++){
//            weekItems.add(new WeeklyWeatherItem("월","25","19"));
//        }

        Retrofit retrofit= RetrofitHelper.getInstance();
        RetrofitService retrofitService= retrofit.create(RetrofitService.class);
        Call<WeeklyWeatherItem> call= retrofitService.getJson( "1", "1", "json", "11B00000", "202302240600", "21");
        call.enqueue(new Callback<WeeklyWeatherItem>() {

            // 데이터를 불러왔을 때
            @Override
            public void onResponse(Call<WeeklyWeatherItem> call, Response<WeeklyWeatherItem> response) {
                weekItems.clear();
                adapter.notifyDataSetChanged();

                WeeklyWeatherItem item= response.body();

                AlertDialog.Builder failDialog = new AlertDialog.Builder(MainActivity.this);
                failDialog.setTitle("성공");
                failDialog.setMessage(item.regId);
                failDialog.show();



            }

            // 데이터를 불러오지 못했을 때
            @Override
            public void onFailure(Call<WeeklyWeatherItem> call, Throwable t) {

                AlertDialog.Builder failDialog = new AlertDialog.Builder(MainActivity.this);
                failDialog.setTitle("실패");
                failDialog.setMessage("데이터를 불러오지 못했습니다." + t.toString());
                failDialog.show();
            }
        });
    }
}


//        예보구역 코드

//        11B00000	서울, 인천, 경기도
//        11D10000	강원도영서
//        11C20000	대전, 세종, 충청남도
//        11C10000	충청북도
//        11F20000	광주, 전라남도
//        11F10000	전라북도
