package kr.co.widgetweather.activities;

import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.co.widgetweather.R;
import kr.co.widgetweather.adapters.WeeklyWeatherRecyclerAdapter;
import kr.co.widgetweather.model.WeeklyWeatherItem;
import kr.co.widgetweather.network.RetrofitHelper;
import kr.co.widgetweather.network.RetrofitService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    ProgressDialog dialog;
    int loadData= 0;

    RecyclerView recycler;
    WeeklyWeatherRecyclerAdapter adapter;
    ArrayList<WeeklyWeatherItem> weekItems = new ArrayList<>();

    String nx = "57"; // 위도
    String ny = "127"; // 경도
    String regId1 = "11B00000"; // 예보구역 코드
    String regId2 = "11B10101"; // 예보구역 코드

    TextView loc;
    TextView tmp;
    TextView tvSky;
    ImageView imgSky;
    String tmpCurrent;
    String skyCurrent;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 위치 권한
        permissionLocation();

        recycler = findViewById(R.id.recyler_weather_weekly);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeeklyWeatherRecyclerAdapter(this, weekItems);
        recycler.setAdapter(adapter);

        // 새로고침
        swipeRefreshLayout = findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        getLocation(); // 위치 가져오기

    } // onCreate()

    @Override
    protected void onResume() {
        super.onResume();
        getLocation(); // 위치 가져오기
        //MainThread thread = new MainThread(); // MainThread() 생성
        //thread.start(); // xml 파싱시작

        retrofitParsing(); // json 파싱한 데이터 불러오기
        loadData(); // 디바이스에 저장된 데이터들 불러오기

        // 디바이스에 저장된 위도,경도 데이터값을 불러와서 changeToAddress()에 데이터 넘기기
//        SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
//        nx= pref.getString("nx", nx);
//        ny= pref.getString("ny", ny);
        //changeToAddress(this, nx, ny);


    }

    // 새로고침을 하기 위한 메소드
    @Override
    public void onRefresh() {
        onResume();

        swipeRefreshLayout.setRefreshing(false);
    }

    // 디바이스에 데이터 저장
    void loadData(){

        // 저장된 현재기온 데이터를 불러와서 TextView에 setText
        SharedPreferences pref= getSharedPreferences("weather", MODE_PRIVATE);
        tmpCurrent= pref.getString("tmp0", tmpCurrent);
        tmp= findViewById(R.id.tmp);
        tmp.setText(tmpCurrent);

        // 저장된 현재 하늘상태 데이터를 불러와서 TextView에 setText
        skyCurrent= pref.getString("sky", skyCurrent);
        tvSky= findViewById(R.id.tv_sky);
        tvSky.setText(skyCurrent);

        // 하늘상태에 따라 하늘상태 이미지 변경
        imgSky= findViewById(R.id.img_sky);
        if(skyCurrent == null){
            imgSky.setImageResource(R.drawable.weather_sunny);
        }else if (skyCurrent.equals("맑음")){
            imgSky.setImageResource(R.drawable.weather_sunny);
        }else if (skyCurrent.equals("구름많음")){
            imgSky.setImageResource(R.drawable.weather_cloudy);
        }else if(skyCurrent.equals("흐림")){
            imgSky.setImageResource(R.drawable.weather_blur);
        }
    }

    // 위치권한
    void permissionLocation() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    // Android Development 가이드 문서(사용자의 마지막 위치 가져오기)를 참조하여 작성하였습니다
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
    void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        Log.d("location", location + "");

                        if (location != null) {

                            SharedPreferences pref = getSharedPreferences("location", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            Log.d("LOCATIONSS", location.getLatitude() + "," + location.getLongitude());
                            // 디바이스에 위도,경도 데이터 (Double -> String 변환) 저장
                            editor.putString("nx", Double.toString(location.getLatitude()));
                            editor.putString("ny", Double.toString(location.getLongitude()));
                            editor.commit();
                            Log.d("location", location.getLatitude() + "," + location.getLongitude());
                        } else {
                            Log.d("locationError", "failed");
                        }
                    }
                });
        fusedLocationClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Errors", e.getMessage());
            }
        });
    }

    // 위도, 경도를 주소로 변환하는 메소드
    public String changeToAddress(Context context, String lat, String lng){
        Geocoder geocoder = new Geocoder(context, Locale.KOREA);
        String nowAddress= null;

        // 디바이스에 저장된 위도, 경도 데이터 가져오기
        SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
        lat= pref.getString("nx", lat);
        lng= pref.getString("ny", lng);
        Log.d("lat,lng", lat+lng);

        if(geocoder!=null){
            try {
                List<Address> address= geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 10);
                if (address != null && address.size()>0){
                    String currentAddress= address.get(0).getAdminArea()+" "+address.get(0).getLocality(); // 주소 [ 시, 구 ] 불러오기
                    nowAddress = currentAddress;

                    String city= address.get(0).getLocality(); // 도시

                    // 현재주소를 디바이스에 저장
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("address", nowAddress);
                    editor.commit();

                    Log.d("address", nowAddress);

                    // 현재단말기 위치주소에 따라 regId 변수에 예보구역코드 변환 [ 변환된 예보구역코드 주소로 api문서 요청 ]
                    if(city.equals("서울")||city.equals("서울특별시")){
                        regId2= "11B10101";
                        regId1= "11B00000";
                    }if(city.equals("용인")||city.equals("용인시")){
                        regId2= "11B20612";
                        regId1= "11B00000";
                    }if(city.equals("수원")||city.equals("수원시")){
                        regId2= "11B20601";
                        regId1= "11B00000";
                    }if(city.equals("안양")||city.equals("안양시")){
                        regId2= "11B20602";
                        regId1= "11B00000";
                    }if(city.equals("평택")||city.equals("평택시")){
                        regId2= "11B20606";
                        regId1= "11B00000";
                    }if(city.equals("성남")||city.equals("성남시")){
                        regId2= "11B20605";
                        regId1= "11B00000";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "주소를 가져올 수 없습니다"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        loc= findViewById(R.id.location);
        loc.setText(nowAddress);

        return nowAddress;
    }

    // Json 파싱
    void retrofitParsing(){

        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(80);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("데이터를 불러오는 중입니다.");
        dialog.show();

        long now= System.currentTimeMillis();
        Date today = new Date(now); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*1))

        SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH");

        String getDate= sdf.format(today);
        String hour= sdfHour.format(today)+"00";

        String getTime= "0200";
        String getTime2= "0600";

        // [ 단기예보 ] : 발표시각과 현재시각이 같은경우 현재시간으로 요청항목에 발표시각 변경 (1일 8회)
        if (hour.equals("0200")){
            getTime= hour;
        }
//        else if(hour.equals("0500")){
//            getTime= hour;
//        }else if(hour.equals("0800")){
//            getTime= hour;
//        }else if(hour.equals("1100")){
//            getTime= hour;
//        }else if(hour.equals("1400")){
//            getTime= hour;
//        }else if(hour.equals("1700")){
//            getTime= hour;
//        }else if(hour.equals("2000")){
//            getTime= hour;
//        }else if(hour.equals("2300")){
//            getTime= hour;
//        }

        // [ 중기기온예보 ] : 발표시각과 현재시각이 같은경우 현재시간으로 요청항목에 발표시각 변경 (1일 2회)
        if (hour.equals("0600")){
            getTime2= hour;
        }else if (hour.equals("1800")){
            getTime2= hour;
        }

//        SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
//        nx= pref.getString("nx", nx);
//        ny= pref.getString( "ny", ny);
//
//        int lat= Math.round(Float.parseFloat(nx));
//        int lng= Math.round(Float.parseFloat(ny));

        // 단기 기온조회 baseUrl
        String baseUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/";
        String pageNo= "10";
        String numOfRows= "1500";
        String numOfRows2= "1";
        String dataType= "json";
        String baseDate= getDate; // 현재 날짜를 가져온 데이터
        String baseTime= getTime; // 1일동안 8회 변경
        String x= "57"; // 위도로 x좌표를 구한 값
        String y= "127"; // 경도로 y좌표를 구한 값
        String tmFc= getDate+getTime2;
        Log.d("DATETEST", tmFc);

        WeeklyWeatherItem shortItems[]= {null,null,null,null,null,null,null};
        for (int i= 0; i< shortItems.length; i++){
            shortItems[i]= new WeeklyWeatherItem();
        }

        Retrofit retrofit= RetrofitHelper.getInstance(baseUrl);
        RetrofitService retrofitService= retrofit.create(RetrofitService.class);

        // URL 요청항목 값들을 getJson() 메소드에 대입
        retrofitService.getJson(pageNo, numOfRows, dataType, baseDate, baseTime, x, y).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("URL", call.request().url().toString());
                try{
                    // 오늘부터 4일후 까지의 날짜데이터를 배열형태로 반복문을 통해 가져오기
                    String[] days= {"","","",""};
                    for (int i=0; i<days.length; i++){
                        Date date= new Date(now+(1000*60*60*24*i));
                        days[i]= sdf.format(date);
                        Log.d("days", days[i]+"["+i+"]");
                    }

                    Date date = new Date(now+(1000*60*60*2)); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*1))
                    Date dateYesterday = new Date(now+(1000*60*60*24*(-1)));

                    String yesterday= sdf.format(dateYesterday); // 어제날짜
                    String hour= sdfHour.format(date)+"00"; // 2시간 후

                    int[] changeDays= {0,0,0,0,0};

//                    SNO : 1시간 신적설
//                    REH : 습도
//                    PCP : 1시간 강수량
//                    WAV : 파고
//                    POP : 강수확률
//                    PTY : 강수형태
//                    SKY : 하늘상태
//                    WSD : 풍속
//                    VEC : 풍향
//                    VVV : 풍속(남북성분)
//                    UUU : 풍속(동서성분)
//                    TMP : 1시간 기온

                    // json 문자열을 json 객체로 변환
                    JSONObject jsonObject= new JSONObject(response.body());

                    JSONObject res= jsonObject.getJSONObject("response");
                    JSONObject body= res.getJSONObject("body");
                    JSONObject items= body.getJSONObject("items");
                    JSONArray item= items.getJSONArray("item");

                    // json 문서의 item 객체를 모두 가져올 때 까지 반복 [ 파싱할 데이터 : 1.기온(TMP), 2.강수확률(POP), 3.하늘상태(SKY), 4.습도(REH) ]
                    for (int i=0; i< item.length(); i++){
                        JSONObject obj= item.getJSONObject(i);
                        String category= obj.getString("category");

                        if (category.equals("TMP")) {
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueTMP", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            if (fcstDate.equals(days[0]) && hour.equals(fcstTime) || fcstDate.equals(yesterday)){
                                tmp.setText(fcstValue+"°");
                            }
                        }

                        // 최고기온
                        if (category.equals("TMX")){
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueTMX", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            float value= Float.parseFloat(fcstValue);

                            // 오늘날짜와 예보일자, 현재시간과 예보시각이 같은 경우에 해당하는 데이터 가져오기
                            if (fcstDate.equals(days[0])) { // 오늘 날짜
                                Log.d("trueTMX", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , 오늘");

                                shortItems[0].tvTmpWeek= Math.round(value)+"°";
                                Log.d("testDataTMX", shortItems[0].tvTmpWeek+","+ fcstValue);
                                shortItems[0].tvWeek= "오늘";
                                changeDays[0]+= 1; // 날짜변경
                            }

                            for (int a=1; a<=2; a++){
                                if (fcstDate.equals(days[a])){
                                    Log.d("trueTMX", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , "+dayWeek(changeDays[0]));
                                    shortItems[a].tvTmpWeek= Math.round(value)+"°";
                                    shortItems[a].tvWeek= dayWeek(changeDays[0]);
                                    changeDays[0]+= 1;
                                }
                            }
                        } // if TMX 최고기온

                        // 최저기온
                        if(category.equals("TMN")){
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueTMN", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);
                            float value= Float.parseFloat(fcstValue);

                            if (fcstDate.equals(days[0])){
                                Log.d("trueTMN", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , 오늘");
                                shortItems[0].tvTmnWeek= Math.round(value)+"°";
                                Log.d("tsetDataTMN", shortItems[0].tvTmnWeek+","+ fcstValue);
                                changeDays[1]+= 1;
                            }

                            for (int a=1; a<=2; a++){
                                if (fcstDate.equals(days[a])){
                                    Log.d("trueTMN", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , 오늘");
                                    shortItems[a].tvTmnWeek= Math.round(value)+"°";
                                    Log.d("tsetDataTMN", shortItems[a].tvTmnWeek+","+ fcstValue);
                                    changeDays[1]+= 1;
                                }
                            }
                        }

                        // 강수확률
                        if (category.equals("POP")){
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valuePOP", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            // 오늘날짜와 예보일자, 현재시간과 예보시각이 같은 경우에 해당하는 데이터 가져오기
                            if (fcstDate.equals(days[0]) && hour.equals(fcstTime) || fcstDate.equals(yesterday)) { // 오늘 날짜
                                Log.d("truePOP", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , 오늘");
                                shortItems[0].tvPop= fcstValue+"%";
                                changeDays[2]+= 1; // 날짜변경
                            }
                            for (int k=1; k<=2; k++){
                                if (fcstDate.equals(days[k]) && hour.equals(fcstTime)){
                                    Log.d("truePOP", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , "+dayWeek(changeDays[1]));
                                    changeDays[2]+= 1;
                                    shortItems[k].tvPop= fcstValue+"%";
                                }
                            }
                        } // if POP 강수확률

                        // 하늘상태 [ 맑음 1, 구름많음 3, 흐림 4 ]
                        if (category.equals("SKY")){
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueSKY", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            // 오늘날짜와 예보일자, 현재시간과 예보시각이 같은 경우에 해당하는 데이터 가져오기

                            if (fcstDate.equals(days[0]) && hour.equals(fcstTime) || fcstDate.equals(yesterday)) { // 오늘 날짜
                                Log.d("trueSKY", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , 오늘");
                                changeDays[3]+= 1; // 날짜변경
                                if (fcstValue.equals("1")){
                                    Log.d("WEATHERQ", "맑음");
                                    shortItems[0].imgSky= "맑음";

                                }else if (fcstValue.equals("3")){
                                    Log.d("WEATHERQ", "구름많음");
                                    shortItems[0].imgSky= "구름많음";

                                }else if (fcstValue.equals("4")){
                                    Log.d("WEATHERQ", "흐림");
                                    shortItems[0].imgSky="흐림";
                                }
                            }
                            for (int j= 1; j<= 2; j++){

                                if (fcstDate.equals(days[j]) && hour.equals(fcstTime)) {
                                    Log.d("trueSKY", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , "+dayWeek(changeDays[2]));
                                    changeDays[3]+= 1; // 날짜변경

                                    // 얻어온 하늘상태 데이터 값에 따라 문자열 넣기
                                    if (fcstValue.equals("1")){
                                        Log.d("WEATHERQ", "맑음");
                                        shortItems[j].imgSky= "맑음";
                                    }else if (fcstValue.equals("3")){
                                        Log.d("WEATHERQ", "구름많음");
                                        shortItems[j].imgSky= "구름많음";
                                    }else if (fcstValue.equals("4")){
                                        Log.d("WEATHERQ", "흐림");
                                        shortItems[j].imgSky="흐림";
                                    }
                                }
                            }
                        } // if SKY 하늘상태

                        // 습도
                        if (category.equals("REH")){
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueREH", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            // 오늘날짜와 예보일자, 현재시간과 예보시각이 같은 경우에 해당하는 데이터 가져오기
                            if (fcstDate.equals(days[0]) && hour.equals(fcstTime) || fcstDate.equals(yesterday)) { // 오늘 날짜
                                Log.d("trueREH", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , 오늘");
                                changeDays[4]+= 1; // 날짜변경
                                TextView tvReh;
                                tvReh= findViewById(R.id.tv_reh);
                                tvReh.setText(fcstValue+"%");
                            }
                        } // if REH 습도

                    } // for
                    weekItems.clear();
                    for (int i=0; i<= 2; i++){


                        weekItems.add(i,shortItems[i]);
//                        Log.d("weekItems", weekItems.size()+"");
//                        Log.d("weekitems", shortItems[i].tvTmpWeek);
//                        Log.d("weekitems", shortItems[i].tvWeek);
//                        Log.d("weekitems", shortItems[i].imgSky);
//                        Log.d("weekitems", shortItems[i].tvPop);
//                        Log.d("SIZE", weekItems.size()+"");
                    }


                    // 중기 육상예보조회 base Url
                    String baseUrl2 = "http://apis.data.go.kr/1360000/MidFcstInfoService/";

                    // 중기 육상예보 json 파싱작업
                    Retrofit retrofit2= RetrofitHelper.getInstance(baseUrl2);
                    RetrofitService retrofitService2= retrofit2.create(RetrofitService.class);

                    // URL 요청항목 값들을 getJson() 메소드에 대입
                    retrofitService2.getJson2(pageNo, numOfRows2, dataType, regId1, tmFc).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            String[] rnStAm= {"","","",""};
                            String[] rnStPm= {"","","",""};
                            String[] wfAm= {"","","",""};
                            String[] wfPm= {"","","",""};
                            Log.d("URL", call.request().url().toString());

                            try {
                                JSONObject jsonObject= new JSONObject(response.body());
                                JSONObject res= jsonObject.getJSONObject("response");
                                JSONObject body= res.getJSONObject("body");
                                JSONObject items= body.getJSONObject("items");
                                JSONArray item= items.getJSONArray("item");

                                for (int i=0; i<item.length();i++){
                                    JSONObject obj= item.getJSONObject(i);

                                    int index= 0;

                                    for (int j=3; j<=6; j++){

                                        // 3,4,5,6일 후 오전,오후 강수확률
                                        rnStAm[index]= obj.getString("rnSt"+j+"Am");
                                        rnStPm[index]= obj.getString("rnSt"+j+"Pm");

                                        // 3,4,5,6일 후 오전,오후 하늘상태
                                        wfAm[index]= obj.getString("wf"+j+"Am");
                                        wfPm[index]= obj.getString("wf"+j+"Pm");
                                        Log.d("rnData", rnStAm[index]+","+rnStPm[index]+ ", index : "+ index + " j :" + j);
                                        Log.d("wfData", wfAm[index]+","+wfPm[index]+ ", index : "+ index + " j :" + j);
                                        shortItems[j].tvWeek= dayWeek(index+3);
                                        shortItems[j].tvPop= rnStPm[index]+"%";
                                        shortItems[j].imgSky= wfPm[index];
                                        index+=1;
                                    } // for

                                } // for

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }// catch

                            // 중기 기온조회 base Url
                            String baseUrl3 = "http://apis.data.go.kr/1360000/MidFcstInfoService/";

                            // 중기기온 json 파싱작업
                            Retrofit retrofit3= RetrofitHelper.getInstance(baseUrl3);
                            RetrofitService retrofitService3= retrofit3.create(RetrofitService.class);

                            // URL 요청항목 값들을 getJson() 메소드에 대입
                            retrofitService3.getJson3(pageNo, numOfRows2, dataType, regId2, tmFc).enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    Log.d("URL", call.request().url().toString());
                                    String[] taMax= {"","","",""};

                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body());
                                        JSONObject res= jsonObject.getJSONObject("response");
                                        JSONObject body= res.getJSONObject("body");
                                        JSONObject items= body.getJSONObject("items");
                                        JSONArray item= items.getJSONArray("item");

                                        for (int i=0; i<item.length();i++){
                                            JSONObject obj= item.getJSONObject(i);


                                            int index[]= {0,0};

                                            // 3,4,5,6일 후 최고기온
                                            for (int j=3; j<=6; j++){
                                                taMax[index[0]]= obj.getString("taMax"+j);
                                                shortItems[j].tvTmpWeek= taMax[index[0]]+"°";

                                                index[0]+=1;
                                            } // for

                                            // 3,4,5,6일 후 최저기온
                                            for (int j=3; j<=6; j++){
                                                taMax[index[1]]= obj.getString("taMin"+j);
                                                shortItems[j].tvTmnWeek= taMax[index[1]]+"°";

                                                index[1]+=1;
                                            } // for

                                            for (int j=3; j<=6; j++){
                                                weekItems.add(shortItems[j]);
                                                Log.d("SIZE", weekItems.size()+"");
                                            }
                                            adapter.notifyDataSetChanged();
                                            loadData=1;
                                            if (loadData == 1){
                                                dialog.dismiss();
                                                loadData=0;
                                            }

                                        } // for
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                }
                            });
                        } // onResponse

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    }); // call2 [ 중기기온 ]

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }// 단기예보 onResponse

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.v("TAG", t.getMessage());
            }
        }); // 단기기온 callback

    } // retrofitParsing()


    // XML 파싱
//    class MainThread extends Thread {
//        @Override
//        public void run() {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    weekItems.clear();
//                    adapter.notifyDataSetChanged();
//
//                }
//            });
//
//            String apiKey = "CUMIKCkTvdkEuHPM3gdWXxBJ4DyeIHFWvrt8iMu6ZIcrRUhNv2dDE6G985PAAStITAlrPPrSMSjL2eBgPgk%2Bww%3D%3D";
//            String numOfrows = "1000";
//            String pageNo = "1";
//            String dataType = "XML";
//            String baseTime = "0500";
//            String type= "";
//            String skyType= "";
//            int itemNum= 0;
//            int tmpResult= 0;
//            int changeDay= 0;
//            int skyFin=0;
//            int tmpNum=0;
//            String fcstDate = null;
//            String fcstTime = null;
//
//            SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
//
//            nx= pref.getString("nx", nx);
//            ny= pref.getString( "ny", ny);
//
//            int lat= Math.round(Float.parseFloat(nx));
//            int lng= Math.round(Float.parseFloat(ny));
//
//
//            long now= System.currentTimeMillis();
//            Date date = new Date(now); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*2))
//
//            SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
//            SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
//
//            String getTime = sdf.format(date);
//            String getHour = sdfHour.format(date);
//
//            Log.d("dates", getTime+","+getHour);
//
//            // 단기예보
//            String apiUrl= "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?"
//                    + "serviceKey=" + apiKey // api 키
//                    + "&pageNo=" + pageNo // 페이지 번호
//                    + "&numOfRows=" + numOfrows // 한 페이지 결과 수
//                    + "&dataType" + dataType // 응답자료형식(XML/JSON)
//                    + "&base_date=" + getTime // 발표일자 (ex.20230227)
//                    + "&base_time=" + baseTime // 발표시각 (ex.0500)
//                    + "&nx=" + lat // 예보지점 x좌표
//                    + "&ny=" + lng; // 예보지점 y좌표
//            Log.d("values", regId);
//
//            // 중기예보
//            String apiUrl2= "https://apis.data.go.kr/1360000/MidFcstInfoService/getMidTa?"
//                    + "serviceKey=" + apiKey // api 키
//                    + "&pageNo=" + pageNo // 페이지 번호
//                    + "&numOfRows=" + "10" // 한 페이지 결과 수
//                    + "&regId=" + regId // 예보 지역코드
//                    + "&tmFc=" + getTime + "0600"; // 발표날짜+발표시간 (ex.202302270600)
//
//            try {
//                URL urlToday= new URL(apiUrl);
//                URL urlWeek= new URL(apiUrl2);
//
//                InputStream inputStreamToday= urlToday.openStream();
//                InputStream inputStreamWeek= urlWeek.openStream();
//
//                InputStreamReader inputStreamReaderToday= new InputStreamReader(inputStreamToday);
//                InputStreamReader inputStreamReaderWeek= new InputStreamReader(inputStreamWeek);
//
//                XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
//                XmlPullParser xpp= factory.newPullParser();
//                XmlPullParser xppWeek= factory.newPullParser();
//
//                xpp.setInput(inputStreamReaderToday);
//                xppWeek.setInput(inputStreamReaderWeek);
//
//                int eventTypeDay= xpp.getEventType();
//                int eventTypeWeek= xppWeek.getEventType();
//                WeeklyWeatherItem weekItem[]= {null,null,null,null,null};
//
//                SharedPreferences tmpPref= getSharedPreferences("weather", MODE_PRIVATE);
//                SharedPreferences.Editor tmpEditor = tmpPref.edit();
//
//                while(eventTypeDay != XmlPullParser.END_DOCUMENT || eventTypeWeek != XmlPullParser.END_DOCUMENT){
//
//                    // 단기예보 api
//                    switch (eventTypeDay){
//                        case XmlPullParser.START_DOCUMENT:
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {adapter.notifyDataSetChanged();}
//                            });
//                            break;
//                        case XmlPullParser.START_TAG:
//                            String tagName= xpp.getName();
//
//                            if(tagName.equals("item")){
//                                weekItem[0]= new WeeklyWeatherItem();
//                            }else if(tagName.equals("category")){
//                                xpp.next();
//
//                                if(xpp.getText().equals("TMP")){  // 카테고리 : TMP(기온)
//                                    type = xpp.getText();
//
//                                }else if (xpp.getText().equals("SKY")&& skyFin==0){ // 카테고리 : SKY(하늘상태 = 0~5 맑음, 6~8 구름많음, 9~10 흐림)
//                                    skyType = xpp.getText();
//                                }
//
//                            }else if (tagName.equals("fcstDate") && type.equals("TMP") && itemNum==0){  // 날짜
//                                xpp.next();
//                                fcstDate= xpp.getText();
//
//                                if (fcstDate.equals(getTime)){
//                                    itemNum+=1;
//                                    fcstDate= xpp.getText();
//                                }
//
//                            }else if (tagName.equals("fcstTime") && type.equals("TMP")&& itemNum==1) {  // 시간
//                                xpp.next();
//                                fcstTime= xpp.getText();
//                                if (fcstTime.equals(getHour+"00")){
//                                    itemNum+=1;
//                                    fcstTime= xpp.getText();
//                                }
//
//                            // 기온 값
//                            }else if (tagName.equals("fcstValue") && type.equals("TMP") && fcstTime.equals(getHour+"00") && fcstDate.equals(getTime) && itemNum==2) {
//                                xpp.next();
//                                weekItem[0].tvWeek= dayWeek(changeDay); // 요일
//                                weekItem[0].tvTmpWeek = xpp.getText() + "°"; // 기온
//                                type = "";
//                                itemNum += 1;
//                                Log.d("tmpData", xpp.getText()+","+lat+","+lng);
//
//                                tmpEditor.putString("tmp"+tmpNum, xpp.getText() + "°");
//                                tmpEditor.commit();
//                                tmpNum+=1;
//
//                            // 하늘상태
//                            }else if (tagName.equals("fcstValue") && skyType.equals("SKY") && skyFin==0){
//                                xpp.next();
//
//                                // api 문서에서 가져온 데이터 하늘상태 ( 0~5 맑음, 6~8 구름많음, 9~10 흐림 )
//                                int sky= Integer.parseInt(xpp.getText());
//
//                                // api문서에 명시된 하늘상태 데이터 값에 따라 하늘상태 문자열로 변환
//                                if (sky >= 0 || sky <= 5){
//                                    skyCurrent = "맑음";
//                                }else if (sky >=6 || sky <=8){
//                                    skyCurrent = "구름많음";
//                                }else if (sky >= 9 || sky <= 10){
//                                    skyCurrent = "흐림";
//                                }
//
//                                // 문자열로 변환한 하늘상태 데이터를 디바이스에 저장
//                                SharedPreferences skyPref= getSharedPreferences("weather", MODE_PRIVATE);
//                                SharedPreferences.Editor editor = skyPref.edit();
//                                editor.putString("sky", skyCurrent);
//                                editor.commit();
//                                skyFin+=1;
//                            }
//                            break;
//
//                        case XmlPullParser.TEXT:
//                            break;
//
//                        case XmlPullParser.END_TAG:
//                            if(xpp.getName().equals("item") && itemNum == 3){
//                                weekItems.add(weekItem[0]);
//                                itemNum=0;
//                                changeDay+=1;
//
//                                date = new Date(now+(1000*60*60*24*changeDay)); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*1))
//                                getTime= sdf.format(date);
//                            }
//
//                            if (xpp.getName().equals("items") && tmpResult==0){
//                                tmpResult=1;
//                            }
//                            break;
//                    } // switch
//                    eventTypeDay= xpp.next();
//
//                    // 단기예보 api의 데이터를 모두 불러왔을 때 실행
//                    if (tmpResult== 1){
//                        // 중기예보 api
//                        switch (eventTypeWeek){
//                            case XmlPullParser.START_DOCUMENT:
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        adapter.notifyDataSetChanged();
//                                    }
//                                });
//                                break;
//                            case XmlPullParser.START_TAG:
//                                String tagName= xppWeek.getName();
//                                if (tagName.equals("item")){
//                                    for (int i=1; i<=4 ; i++){
//                                        weekItem[i]= new WeeklyWeatherItem();
//                                    }
//                                }else if(tagName.equals("taMin3")){ // 3일 후 최저기온
//                                    xppWeek.next();
//
//                                    weekItem[1].tvWeek= dayWeek(changeDay); // 현재 요일
//                                    weekItem[1].tvTmpWeek = xppWeek.getText() + "°"; // 기온
//
//                                    tmpEditor.putString("tmp"+tmpNum, xppWeek.getText() + "°");
//                                    tmpEditor.commit();
//                                    tmpNum+=1;
//
//                                    weekItems.add(weekItem[1]);
//                                    changeDay+=1;
//
//                                }else if(tagName.equals("taMin4")){ // 4일 후 최저기온
//                                    xppWeek.next();
//                                    weekItem[2].tvWeek= dayWeek(changeDay); // 현재 요일
//                                    weekItem[2].tvTmpWeek = xppWeek.getText() + "°"; // 기온
//
//                                    tmpEditor.putString("tmp"+tmpNum, xppWeek.getText() + "°");
//                                    tmpEditor.commit();
//                                    tmpNum+=1;
//
//                                    weekItems.add(weekItem[2]);
//                                    changeDay+=1;
//
//                                }else if(tagName.equals("taMin5")){ // 5일 후 최저기온
//                                    xppWeek.next();
//                                    weekItem[3].tvWeek= dayWeek(changeDay); // 현재 요일
//                                    weekItem[3].tvTmpWeek = xppWeek.getText() + "°"; // 기온
//
//                                    tmpEditor.putString("tmp"+tmpNum, xppWeek.getText() + "°");
//                                    tmpEditor.commit();
//                                    tmpNum+=1;
//
//                                    weekItems.add(weekItem[3]);
//                                    changeDay+=1;
//
//                                }else if(tagName.equals("taMin6")){ // 6일 후 최저기온
//                                    xppWeek.next();
//                                    weekItem[4].tvWeek= dayWeek(changeDay); // 현재 요일
//                                    weekItem[4].tvTmpWeek = xppWeek.getText() + "°"; // 기온
//
//                                    tmpEditor.putString("tmp"+tmpNum, xppWeek.getText() + "°");
//                                    tmpEditor.commit();
//                                    tmpNum+=1;
//
//                                    weekItems.add(weekItem[4]);
//                                    changeDay+=1;
//                                }
//                                break;
//
//                            case XmlPullParser.TEXT:
//                                break;
//
//                            case XmlPullParser.END_TAG:
//                                if(xppWeek.getName().equals("item")){
//                                    tmpResult+=1; // END_TAG가 item일 경우 break;
//                                }
//                                break;
//                        } // switch
//                        eventTypeWeek= xppWeek.next();
//                    } // if
//
//                } // while
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (XmlPullParserException e) {
//                e.printStackTrace();
//            }
//        } // run()
//    } // Thread()

    // 요일관련 메소드
    String dayWeek(int i){
        long now= System.currentTimeMillis();
        Date date = new Date(now);

        Calendar cal= Calendar.getInstance();
        cal.setTime(date);

        int dayWeeks = cal.get(Calendar.DAY_OF_WEEK);
        String strWeek= "";

        switch(dayWeeks+i){

            case 1: case 8: case 15:
                strWeek = "일요일";
                break;
            case 2: case 9: case 16:
                strWeek = "월요일";
                break;
            case 3: case 10: case 17:
                strWeek = "화요일";
                break;
            case 4: case 11: case 18:
                strWeek = "수요일";
                break;
            case 5: case 12: case 19:
                strWeek = "목요일";
                break;
            case 6: case 13: case 20:
                strWeek = "금요일";
                break;
            case 7: case 14: case 0:
                strWeek = "토요일";
                break;

        }
        return strWeek;
    }


}



