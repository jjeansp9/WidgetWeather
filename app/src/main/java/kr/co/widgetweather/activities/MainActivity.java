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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler= findViewById(R.id.recyler_weather_weekly);
        adapter= new WeeklyWeatherRecyclerAdapter(this, weekItems);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

//        리사이클러뷰가 잘 작동하는지 확인하기 위해 더미데이터로 테스트
//        for(int i= 0; i<7 ; i++){
//            weekItems.add(new WeeklyWeatherItem("월","25","19"));
//        }

        Retrofit retrofit= RetrofitHelper.getInstance();
        RetrofitService retrofitService= retrofit.create(RetrofitService.class);
        Call<WeeklyWeatherItem> call= retrofitService.getJson("1","10", "json", "11B10101", "202302240600");
        call.enqueue(new Callback<WeeklyWeatherItem>() {
            @Override
            public void onResponse(Call<WeeklyWeatherItem> call, Response<WeeklyWeatherItem> response) {
                //                weekItems.clear();
//                adapter.notifyDataSetChanged();


                WeeklyWeatherItem item= response.body();

                AlertDialog.Builder failDialog = new AlertDialog.Builder(MainActivity.this);
                failDialog.setTitle("성공");
                failDialog.setMessage(item.taMin4);
                failDialog.show();
            }

            @Override
            public void onFailure(Call<WeeklyWeatherItem> call, Throwable t) {
                AlertDialog.Builder failDialog = new AlertDialog.Builder(MainActivity.this);
                failDialog.setTitle("실패");
                failDialog.setMessage("데이터를 불러오지 못했습니다." + t.toString());
                failDialog.show();
            }
        });



//        json 문서 통째로 가져오는 코드

//        Retrofit retrofit= RetrofitHelper.getInstance();
//        RetrofitService retrofitService= retrofit.create(RetrofitService.class);
//        Call<String> call= retrofitService.getPlainText("1","10","json","11B10101","202302240600");
//        call.enqueue(new Callback<String>() {
//
//            // 데이터를 불러왔을 때
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
////                weekItems.clear();
////                adapter.notifyDataSetChanged();
//
//
//                String item= response.body();
//
//                AlertDialog.Builder failDialog = new AlertDialog.Builder(MainActivity.this);
//                failDialog.setTitle("성공");
//                failDialog.setMessage(item);
//                failDialog.show();
//            }
//
//            // 데이터를 불러오지 못했을 때
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//
//                AlertDialog.Builder failDialog = new AlertDialog.Builder(MainActivity.this);
//                failDialog.setTitle("실패");
//                failDialog.setMessage("데이터를 불러오지 못했습니다." + t.toString());
//                failDialog.show();
//            }
//        });
    }
}


//        예보구역 코드

//        11B00000	서울, 인천, 경기도
//        11D10000	강원도영서
//        11C20000	대전, 세종, 충청남도
//        11C10000	충청북도
//        11F20000	광주, 전라남도
//        11F10000	전라북도
