package kr.co.widgetweather.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
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
import java.text.SimpleDateFormat;
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

    TextView loc;
    TextView tmp;
    TextView weather;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.recyler_weather_weekly);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeeklyWeatherRecyclerAdapter(this, weekItems);
        recycler.setAdapter(adapter);

        permissionLocation(); // 위치 권한
        getLocation(); // 위치 가져오기
        MainThread thread = new MainThread(); // MainThread() 생성
        thread.start(); // xml 파싱시작

        long now = System.currentTimeMillis();
        Date date = new Date(now - (1000 * 60 * 60 * 24 * 1)); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*2))
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String getTime = sdf.format(date);


    } // onCreate()

    // 위치권한
    void permissionLocation() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts
            .RequestMultiplePermissions(), result -> {
        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
        if (fineLocationGranted != null && fineLocationGranted) {

        } else if (coarseLocationGranted != null && coarseLocationGranted) {

        } else {

        }
    });

    // 마지막으로 알려진 위치 가져오기
    @SuppressLint("MissingPermission")
    void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            loc = findViewById(R.id.location);
                            loc.setText((int) location.getLatitude() + "," + (int) location.getLongitude());

                            SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putInt("nx", (int)(location.getLatitude()));
                            editor.putInt("ny", (int)(location.getLongitude()));
                            editor.commit();
                        }
                    }
                });
    }


    // XML 파싱
    class MainThread extends Thread {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    weekItems.clear();
                    adapter.notifyDataSetChanged();
                }
            });

            String apiKey = "CUMIKCkTvdkEuHPM3gdWXxBJ4DyeIHFWvrt8iMu6ZIcrRUhNv2dDE6G985PAAStITAlrPPrSMSjL2eBgPgk%2Bww%3D%3D";
            String numOfrows = "2000";
            String pageNo = "1";
            String dataType = "XML";
            String baseDate = "20230227";
            String baseTime = "0500";
            int nx= 57; // 위도
            int ny= 127; // 경도
            String type= "";
            int itemNum= 0;
            int tmpResult= 0;
            int sky= 0; // 하늘상태 ( 0~5 맑음, 6~8 구름많음, 9~10 흐림 )
            int changeDay= 0;
            String fcstDate = null;
            String fcstTime = null;

            SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
            nx= pref.getInt("nx", nx);
            ny= pref.getInt("ny", ny);

            long now= System.currentTimeMillis();
            Date date = new Date(now); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*2))

            SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdfHour = new SimpleDateFormat("HH");

            String getTime = sdf.format(date);
            String getHour = sdfHour.format(date);

            Log.d("sdf",getHour);

            // 단기예보
            String apiUrl= "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?"
                    + "serviceKey=" + apiKey // api 키
                    + "&pageNo=" + pageNo // 페이지 번호
                    + "&numOfRows=" + numOfrows // 한 페이지 결과 수
                    + "&dataType" + dataType // 응답자료형식(XML/JSON)
                    + "&base_date=" + getTime // 발표일자 (ex.20230227)
                    + "&base_time=" + baseTime // 발표시각 (ex.0500)
                    + "&nx=" + nx // 예보지점 x좌표
                    + "&ny=" + ny; // 예보지점 y좌표
            Log.d("values", nx+","+ny);

            // 중기예보
            String apiUrl2= "https://apis.data.go.kr/1360000/MidFcstInfoService/getMidTa?"
                    + "serviceKey=" + apiKey // api 키
                    + "&pageNo=" + pageNo // 페이지 번호
                    + "&numOfRows=" + numOfrows // 한 페이지 결과 수
                    + "&regId=" + "11B10101" // 예보 지역코드
                    + "&tmFc=" + baseDate+baseTime; // 발표날짜+발표시간 (ex.202302270600)

            try {
                URL urlToday= new URL(apiUrl);
                URL urlWeek= new URL(apiUrl2);

                InputStream inputStreamToday= urlToday.openStream();
                InputStream inputStreamWeek= urlWeek.openStream();

                InputStreamReader inputStreamReaderToday= new InputStreamReader(inputStreamToday);
                InputStreamReader inputStreamReaderWeek= new InputStreamReader(inputStreamWeek);

                XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
                XmlPullParser xpp= factory.newPullParser();
                XmlPullParser xppWeek= factory.newPullParser();

                xpp.setInput(inputStreamReaderToday);
                xppWeek.setInput(inputStreamReaderWeek);

                int eventType= xpp.getEventType();
                WeeklyWeatherItem weekItem= null;


                // SKY : 하늘상태 ( 0~5 맑음, 6~8 구름많음, 9~10 흐림 )

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
                            }else if(tagName.equals("category")){
                                xpp.next();

                                if(xpp.getText().equals("TMP") && itemNum == 0){
                                    weekItem.tvWeek= dayWeek().length()+changeDay+""; // 현재 요일
                                    type = xpp.getText();
                                    itemNum+=1;

                                    Log.d("itemNum", itemNum+"");
                                    Log.d("tmpResult", getTime);
                                }

                            }else if (tagName.equals("fcstDate") && type.equals("TMP")){
                                xpp.next();
                                fcstDate= xpp.getText();

                            }else if (tagName.equals("fcstTime") && type.equals("TMP")) {
                                xpp.next();
                                fcstTime= xpp.getText();

                            }else if (tagName.equals("fcstValue") && type.equals("TMP") &&fcstTime.equals(getHour+"00") && fcstDate.equals(getTime)) {
                                xpp.next();
                                weekItem.tvTmpWeek = xpp.getText() + "°";

                                type = "";
                                itemNum += 1;
                                tmpResult += 1;

                                Log.d("itemNum", itemNum+"");
                                Log.d("tmpResults", fcstDate);
                                Log.d("tmpResult", getTime);

                            }
                            break;

                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            if(xpp.getName().equals("item") && itemNum == 2){
                                weekItems.add(weekItem);
                                itemNum=0;
                                changeDay+=1;

                                date = new Date(now+(1000*60*60*24*changeDay)); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*1))
                                getTime= sdf.format(date);

                                Log.d("itemNum", itemNum+"");
                                Log.d("tmpResults", fcstDate);
                                Log.d("tmpResult", getTime);

                            }

                            if (xpp.getName().equals("items")){
                                tmpResult=0;
                            }
                            break;

                    } // switch
                    eventType= xpp.next();
                } // while
//                tmp = findViewById(R.id.tmp);
//                tmp.setText(xpp.getText()+"°");


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
            case 0:
                strWeek = "토요일";
                break;
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



