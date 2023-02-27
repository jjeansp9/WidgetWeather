package kr.co.widgetweather.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import kr.co.widgetweather.R;
import kr.co.widgetweather.databinding.ActivityMainBinding;
import kr.co.widgetweather.adapters.WeeklyWeatherRecyclerAdapter;
import kr.co.widgetweather.model.WeeklyWeatherItem;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    RecyclerView recycler;
    WeeklyWeatherRecyclerAdapter adapter;
    ArrayList<WeeklyWeatherItem> weekItems = new ArrayList<>();

    String apiKey = "CUMIKCkTvdkEuHPM3gdWXxBJ4DyeIHFWvrt8iMu6ZIcrRUhNv2dDE6G985PAAStITAlrPPrSMSjL2eBgPgk%2Bww%3D%3D";
    String numOfrows = "10";
    String pageNo = "1";
    String dataType = "XML";
    String baseDate = "20230227";
    String baseTime = "0500";
    String nx = "55";
    String ny = "127";

    TextView locations;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.recyler_weather_weekly);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeeklyWeatherRecyclerAdapter(this, weekItems);
        recycler.setAdapter(adapter);

        MainThread thread = new MainThread();
        thread.start(); // xml 파싱시작
        permissionLocation(); // 위치 권한
        getLocation(); // 위치 가져오기





    } // onCreate()

    // 위치권한
    void permissionLocation(){
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    // 마지막으로 알려진 위치 가져오기
    @SuppressLint("MissingPermission")
    void getLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            locations= findViewById(R.id.location);
                            locations.setText(location.getLatitude()+","+location.getLongitude());
                        }
                    }
                });
    }

    // 위치 권한
    ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts
            .RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false);
                if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                }else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Only approximate location access granted.
                }else {
                    // No location access granted.
                }
            });


    // XML 파싱
    class MainThread extends Thread{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    weekItems.clear();
                    adapter.notifyDataSetChanged();
                }
            });

            // 단기예보
            String apiUrl= "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?"
                    + "serviceKey=" + apiKey
                    + "&pageNo=" + pageNo
                    + "&numOfRows=" + numOfrows
                    + "&dataType" + dataType
                    + "&base_date=" + baseDate
                    + "&base_time=" + baseTime
                    + "&nx=" + nx
                    + "&ny=" + ny;

            // 중기예보
            String apiUrl2= "https://apis.data.go.kr/1360000/MidFcstInfoService/getMidTa?"
                    + "serviceKey=" + apiKey
                    + "&pageNo=" + pageNo
                    + "&numOfRows=" + numOfrows
                    + "&regId=" + "11B10101"
                    + "&tmFc=" + baseDate+baseTime;

            try {
                URL url= new URL(apiUrl);

                InputStream inputStream= url.openStream();
                InputStreamReader inputStreamReader= new InputStreamReader(inputStream);

                XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
                XmlPullParser xpp= factory.newPullParser();

                xpp.setInput(inputStreamReader);

                int eventType= xpp.getEventType();

                WeeklyWeatherItem weekItem= null;

                while(eventType != XmlPullParser.END_DOCUMENT){
                    switch (eventType){
                        case XmlPullParser.START_DOCUMENT:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {adapter.notifyDataSetChanged();}
                            });
                            break;
                        case XmlPullParser.START_TAG:
                            String tagName= xpp.getName();

                            if(tagName.equals("item")){
                                weekItem= new WeeklyWeatherItem();
                            }else if(tagName.equals("category")){ // 요일
                                xpp.next();
                                weekItem.tvWeek= dayWeek();

                            }else if(tagName.equals("fcstTime")){
                                xpp.next();
                                weekItem.tvTmxWeek= xpp.getText();
                            }else if(tagName.equals("fcstValue")){
                                xpp.next();
                                weekItem.tvTmnWeek= xpp.getText();
                            }
                            break;
                        case XmlPullParser.TEXT:
                            break;
                        case XmlPullParser.END_TAG:
                            if(xpp.getName().equals("item")){
                                weekItems.add(weekItem);
                            }
                            break;
                    } // switch
                    eventType= xpp.next();
                } // while

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

        } // run()
    } // Thread()

    // 요일관련 메소드
    String dayWeek(){
        long now= System.currentTimeMillis();
        Date date = new Date(now);

        Calendar cal= Calendar.getInstance();
        cal.setTime(date);

        int dayWeeks = cal.get(Calendar.DAY_OF_WEEK);
        String strWeek= "";

        switch(dayWeeks){
            case 1:
                strWeek = "일요일";
                break;
            case 2:
                strWeek = "월요일";
                break;
            case 3:
                strWeek = "화요일";
                break;
            case 4:
                strWeek = "수요일";
                break;
            case 5:
                strWeek = "목요일";
                break;
            case 6:
                strWeek = "금요일";
                break;
            case 7:
                strWeek = "토요일";
                break;
        }
        return strWeek;
    }


}



