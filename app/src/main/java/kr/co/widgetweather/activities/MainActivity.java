package kr.co.widgetweather.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    RecyclerView recycler;
    WeeklyWeatherRecyclerAdapter adapter;
    ArrayList<WeeklyWeatherItem> weekItems = new ArrayList<>();

    String nx= "57"; // 위도
    String ny= "127"; // 경도
    String regId= "11B10101"; // 예보구역 코드

    TextView loc;
    TextView tmp;
    TextView tvSky;
    ImageView imgSky;
    String tmpCurrent;
    String skyCurrent;

    private FusedLocationProviderClient fusedLocationClient;
    SwipeRefreshLayout swipeRefreshLayout;

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
    } // onCreate()

    @Override
    protected void onResume() {
        super.onResume();
        getLocation(); // 위치 가져오기
        //MainThread thread = new MainThread(); // MainThread() 생성
        //thread.start(); // xml 파싱시작

        retrofitParsing();

//        try {
//            jsonParsing(); // json 파싱
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        loadData(); // 디바이스에 저장된 데이터들 불러오기

        // 디바이스에 저장된 위도,경도 데이터값을 불러와서 changeToAddress()에 데이터 넘기기
        SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
        nx= pref.getString("nx", nx);
        ny= pref.getString("ny", ny);
        //changeToAddress(this, nx, ny);

        swipeRefreshLayout = findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
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
    @SuppressLint("MissingPermission")
    void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();

                            // 디바이스에 위도,경도 데이터 (Double -> String 변환) 저장
                            editor.putString("nx", Double.toString(location.getLatitude()));
                            editor.putString("ny", Double.toString(location.getLongitude()));
                            editor.commit();
                            Log.d("location", location.getLatitude()+","+ location.getLongitude());
                        }else{
                            Log.d("locationError", "failed");
                        }
                    }

                });
    }

    // 위도, 경도를 주소로 변환하는 메소드
//    public String changeToAddress(Context context, String lat, String lng){
//        Geocoder geocoder = new Geocoder(context, Locale.KOREA);
//        String nowAddress= null;
//
//        // 디바이스에 저장된 위도, 경도 데이터 가져오기
//        SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
//        lat= pref.getString("nx", lat);
//        lng= pref.getString("ny", lng);
//        Log.d("lat,lng", lat+lng);
//
//        if(geocoder!=null){
//            try {
//                List<Address> address= geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 10);
//                if (address != null && address.size()>0){
//                    String currentAddress= address.get(0).getAdminArea()+" "+address.get(0).getLocality(); // 주소 [ 시, 구 ] 불러오기
//                    nowAddress = currentAddress;
//
//                    String city= address.get(0).getLocality(); // 도시
//
//                    // 현재주소를 디바이스에 저장
//                    SharedPreferences.Editor editor = pref.edit();
//                    editor.putString("address", nowAddress);
//                    editor.commit();
//
//                    Log.d("address", nowAddress);
//
//                    // 현재단말기 위치주소에 따라 regId 변수에 예보구역코드 변환 [ 변환된 예보구역코드 주소로 api문서 요청 ]
//                    if(city.equals("서울")||city.equals("서울특별시")){
//                        regId= "11B10101";
//                    }if(city.equals("용인")||city.equals("용인시")){
//                        regId= "11B20612";
//                    }if(city.equals("수원")||city.equals("수원시")){
//                        regId= "11B20601";
//                    }if(city.equals("안양")||city.equals("안양시")){
//                        regId= "11B20602";
//                    }if(city.equals("평택")||city.equals("평택시")){
//                        regId= "11B20606";
//                    }if(city.equals("성남")||city.equals("성남시")){
//                        regId= "11B20605";
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(this, "주소를 가져올 수 없습니다"+ e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        }
//        loc= findViewById(R.id.location);
//        loc.setText(nowAddress);
//
//        return nowAddress;
//    }

    // Json 파싱
    void retrofitParsing(){

        long now= System.currentTimeMillis();
        Date date = new Date(now); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*2))

        SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH");

        String pageNo= "1";
        String numOfRows= "100";
        String dataType= "json";
        String baseDate= sdf.format(date);
        String baseTime= "0500";
        String nx= "57";
        String ny= "127";

        String result= "";

        Log.d("time", baseDate);

        Retrofit retrofit= RetrofitHelper.getInstance();
        RetrofitService retrofitService= retrofit.create(RetrofitService.class);

        Call<String> call= retrofitService.getJson(pageNo, numOfRows, dataType, baseDate, baseTime, nx, ny);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try{

                    int elementSNO= 1; // 1시간 신적설
                    int elementREH= 2; // 습도
                    int elementPCP= 3; // 1시간 강수량
                    int elementWAV= 4; // 파고
                    int elementPOP= 5; // 강수확률
                    int elementPTY= 6; // 강수형태
                    int elementSKY= 7; // 하늘상태
                    int elementWSD= 8; // 풍속
                    int elementVEC= 9; // 풍향
                    int elementVVV= 10; // 풍속(남북성분)
                    int elementUUU= 11; // 풍속(동서성분)
                    int elementTMP= 12; // 1시간 기온

                    // json 문자열을 json 객체로 변환
                    JSONObject jsonObject= new JSONObject(response.body());

                    JSONObject res= jsonObject.getJSONObject("response");
                    JSONObject body= res.getJSONObject("body");
                    JSONObject items= body.getJSONObject("items");
                    JSONArray item= items.getJSONArray("item");

                    for (int i=0; i< item.length(); i++){
                        JSONObject obj= item.getJSONObject(i);
                        String fcstValue= obj.getString("fcstValue");


                        if (elementREH%12 == 0 || elementREH == 0){

                            Log.d("fcstValue", fcstValue+ obj);

                        }
                        elementREH+=1;

                    }







                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.v("TAG", t.getMessage());
            }
        });
    }


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



