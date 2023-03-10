package kr.co.widgetweather.activities;

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
import kr.co.widgetweather.WidgetProject;
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

    // ???????????? ???????????? ???????????? ?????? ??????
    public static Context context_main; // ???????????? ???????????? ????????? ?????? Context ?????? ??????
    public String[] widgetTmx={"","","","","","",""}; // ????????????
    public String widgetTmn[]={"","","","","","",""}; // ????????????
    public int imgWidgetSky[]= {0,0,0,0,0,0,0,0}; // ???????????? ?????????
    public String tvWidgetSky[]= {"","","","","","",""}; // ???????????? ?????????
    public int widgetNum= 0; // ??????????????? ????????? ????????? ?????? ??????
    public int widgetChangeDays=0; // ?????? ????????? ?????? ??????
    public String address1= null; // ??????1 [ ??? ]
    public String address2= null; // ??????2 [ ??? ]
    // ??????????????? ???????????????
    public long widgetNow= System.currentTimeMillis();
    public Date widgetDate= new Date(widgetNow);

    ProgressDialog dialog; // ?????????????????? ???????????????
    int loadData= 0; // ???????????? ?????? ???????????? ??? ????????????????????? ?????????????????? ??????

    RecyclerView recycler;
    WeeklyWeatherRecyclerAdapter adapter;
    ArrayList<WeeklyWeatherItem> weekItems = new ArrayList<>();

    String nx = "57"; // ??????
    String ny = "127"; // ??????
    String regId1 = "11B00000"; // ???????????? ??????
    String regId2 = "11B10101"; // ???????????? ??????

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


        permissionLocation(); // ?????? ??????

        recycler = findViewById(R.id.recyler_weather_weekly);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeeklyWeatherRecyclerAdapter(this, weekItems);
        recycler.setAdapter(adapter);

        // ????????????
        swipeRefreshLayout = findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        context_main= this;


    } // onCreate()

    @Override
    protected void onResume() {
        super.onResume();

        //MainThread thread = new MainThread(); // MainThread() ??????
        //thread.start(); // xml ????????????

        getLocation(); // ?????? ????????????
        retrofitParsing(); // json ????????? ????????? ????????????
        loadData(); // ??????????????? ????????? ???????????? ????????????

        SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
        nx= pref.getString("nx", null);
        ny= pref.getString("ny", null);

        // ??????????????? ????????? ??????,?????? ??????????????? ???????????? changeToAddress()??? ????????? ?????????
        if (nx!= null && ny!= null){ // ?????? ????????? ??????????????? ????????? ???????????? ???????????? ????????????
            changeToAddress(this, nx, ny);
        }
    }

    // ??????????????? ?????? ?????? ?????????
    @Override
    public void onRefresh() {
        onResume();

        swipeRefreshLayout.setRefreshing(false);
    }

    // ??????????????? ????????? ??????
    void loadData(){

        // ????????? ???????????? ???????????? ???????????? TextView??? setText
        SharedPreferences pref= getSharedPreferences("weather", MODE_PRIVATE);
        tmpCurrent= pref.getString("tmp0", tmpCurrent);
        tmp= findViewById(R.id.tmp);
        tmp.setText(tmpCurrent);

        // ????????? ?????? ???????????? ???????????? ???????????? TextView??? setText
        skyCurrent= pref.getString("sky", skyCurrent);
        tvSky= findViewById(R.id.tv_sky);
        tvSky.setText(skyCurrent);

        // ??????????????? ?????? ???????????? ????????? ??????
        imgSky= findViewById(R.id.img_sky);
        if(skyCurrent == null){
            imgSky.setImageResource(R.drawable.weather_sunny);
        }else if (skyCurrent.equals("??????")){
            imgSky.setImageResource(R.drawable.weather_sunny);
        }else if (skyCurrent.equals("????????????")){
            imgSky.setImageResource(R.drawable.weather_cloudy);
        }else if(skyCurrent.equals("??????")){
            imgSky.setImageResource(R.drawable.weather_blur);
        }
    }

    // ????????????
    void permissionLocation() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    // Android Development ????????? ??????(???????????? ????????? ?????? ????????????)??? ???????????? ?????????????????????
    ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts
            .RequestMultiplePermissions(), result -> {
        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

        if (fineLocationGranted != null && fineLocationGranted) {
            getLocation(); // ?????? ????????????
            Toast.makeText(context_main, "???????????? ??????1", Toast.LENGTH_SHORT).show();

        } else if (coarseLocationGranted != null && coarseLocationGranted) {
            getLocation(); // ?????? ????????????
            Toast.makeText(context_main, "???????????? ??????2", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context_main, "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
        }
    });


    // ??????????????? ????????? ?????? ????????????
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
                            // ??????????????? ??????,?????? ????????? (Double -> String ??????) ??????
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

    // ??????, ????????? ????????? ???????????? ?????????
    public String changeToAddress(Context context, String lat, String lng){
        Geocoder geocoder = new Geocoder(context, Locale.KOREA);
        String nowAddress= null;


        // ??????????????? ????????? ??????, ?????? ????????? ????????????
        SharedPreferences pref= getSharedPreferences("location", MODE_PRIVATE);
        lat= pref.getString("nx", lat);
        lng= pref.getString("ny", lng);
        Log.d("lat,lng", lat+lng);

        if(geocoder!=null){
            try {
                List<Address> address= geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 10);
                if (address != null && address.size()>0){
                    String currentAddress= address.get(0).getAdminArea()+" "+address.get(0).getLocality(); // ?????? [ ???, ??? ] ????????????
                    address1= address.get(0).getAdminArea(); // ?????? [ ??? ] ????????? ?????????
                    address2= address.get(0).getLocality(); // ?????? [ ??? ] ????????? ?????????

                    nowAddress = currentAddress;



                    String city= address.get(0).getLocality(); // ??????

                    // ??????????????? ??????????????? ??????
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("address", nowAddress);
                    editor.commit();

                    Log.d("address", nowAddress);

                    // ??????????????? ??????????????? ?????? regId ????????? ?????????????????? ?????? [ ????????? ?????????????????? ????????? api?????? ?????? ]
                    if(city.equals("??????")||city.equals("???????????????")){
                        regId2= "11B10101";
                        regId1= "11B00000";
                    }if(city.equals("??????")||city.equals("?????????")){
                        regId2= "11B20612";
                        regId1= "11B00000";
                    }if(city.equals("??????")||city.equals("?????????")){
                        regId2= "11B20601";
                        regId1= "11B00000";
                    }if(city.equals("??????")||city.equals("?????????")){
                        regId2= "11B20602";
                        regId1= "11B00000";
                    }if(city.equals("??????")||city.equals("?????????")){
                        regId2= "11B20606";
                        regId1= "11B00000";
                    }if(city.equals("??????")||city.equals("?????????")){
                        regId2= "11B20605";
                        regId1= "11B00000";
                    }
                    loc= findViewById(R.id.location);
                    loc.setText(nowAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "????????? ????????? ??? ????????????"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        return nowAddress;
    }

    // Json ??????
    void retrofitParsing(){

        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(80);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("???????????? ???????????? ????????????.");
        dialog.show();

        long now= System.currentTimeMillis();
        Date today = new Date(now); // ?????????????????? ?????? ????????? : new Date(now+(1000*60*60*24*1))

        SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH");

        String getDate= sdf.format(today);
        String hour= sdfHour.format(today)+"00";

        String getTime= "0200";
        String getTime2= "0600";

        // [ ???????????? ] : ??????????????? ??????????????? ???????????? ?????????????????? ??????????????? ???????????? ?????? (1??? 8???)
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

        // [ ?????????????????? ] : ??????????????? ??????????????? ???????????? ?????????????????? ??????????????? ???????????? ?????? (1??? 2???)
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

        // ?????? ???????????? baseUrl
        String baseUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/";
        String pageNo= "10";
        String numOfRows= "1500";
        String numOfRows2= "1";
        String dataType= "json";
        String baseDate= getDate; // ?????? ????????? ????????? ?????????
        String baseTime= getTime; // 1????????? 8??? ??????
        String x= "57"; // ????????? x????????? ?????? ???
        String y= "127"; // ????????? y????????? ?????? ???
        String tmFc= getDate+getTime2;
        Log.d("DATETEST", tmFc);

        WeeklyWeatherItem shortItems[]= {null,null,null,null,null,null,null,null};
        for (int i= 0; i< shortItems.length; i++){
            shortItems[i]= new WeeklyWeatherItem();
        }

        Retrofit retrofit= RetrofitHelper.getInstance(baseUrl);
        RetrofitService retrofitService= retrofit.create(RetrofitService.class);

        // URL ???????????? ????????? getJson() ???????????? ??????
        retrofitService.getJson(pageNo, numOfRows, dataType, baseDate, baseTime, x, y).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("URL", call.request().url().toString());
                try{

                    // ???????????? 4?????? ????????? ?????????????????? ??????????????? ???????????? ?????? ????????????
                    String[] days= {"","","",""};

                    for (int i=0; i<days.length; i++){

                        Date date= new Date(now+(1000*60*60*24*i));
                        days[i]= sdf.format(date);
                        Log.d("days", days[i]+"["+i+"]");
                    }

                    Date date = new Date(now+(1000*60*60*2)); // ?????????????????? ?????? ????????? : new Date(now+(1000*60*60*24*1))
                    Date dateYesterday = new Date(now+(1000*60*60*24*(-1)));

                    String yesterday= sdf.format(dateYesterday); // ????????????
                    String hour= sdfHour.format(date)+"00"; // 2?????? ???

                    int[] changeDays= {0,0,0,0,0};

//                    SNO : 1?????? ?????????
//                    REH : ??????
//                    PCP : 1?????? ?????????
//                    WAV : ??????
//                    POP : ????????????
//                    PTY : ????????????
//                    SKY : ????????????
//                    WSD : ??????
//                    VEC : ??????
//                    VVV : ??????(????????????)
//                    UUU : ??????(????????????)
//                    TMP : 1?????? ??????

                    // json ???????????? json ????????? ??????
                    JSONObject jsonObject= new JSONObject(response.body());

                    JSONObject res= jsonObject.getJSONObject("response");
                    JSONObject body= res.getJSONObject("body");
                    JSONObject items= body.getJSONObject("items");
                    JSONArray item= items.getJSONArray("item");

                    // json ????????? item ????????? ?????? ????????? ??? ?????? ?????? [ ????????? ????????? : 1.??????(TMP), 2.????????????(POP), 3.????????????(SKY), 4.??????(REH) ]
                    for (int i=0; i< item.length(); i++){
                        JSONObject obj= item.getJSONObject(i);
                        String category= obj.getString("category");

                        // 1?????? ??????
                        if (category.equals("TMP")) {
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueTMP", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            if (fcstDate.equals(days[0]) && hour.equals(fcstTime) || fcstDate.equals(yesterday)){
                                tmp.setText(fcstValue+"??");
                            }
                        }

                        // ????????????
                        if (category.equals("TMX")){
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueTMX", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            float value= Float.parseFloat(fcstValue);

                            // ??????????????? ????????????, ??????????????? ??????????????? ?????? ????????? ???????????? ????????? ????????????
                            if (fcstDate.equals(days[0])) { // ?????? ??????
                                Log.d("trueTMX", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , ??????");

                                shortItems[0].tvTmpWeek= Math.round(value)+"??";
                                Log.d("testDataTMX", shortItems[0].tvTmpWeek+","+ fcstValue);
                                shortItems[0].tvWeek= "??????";
                                widgetTmx[0]= Math.round(value)+"??"; // ????????? ????????? ??????

                                changeDays[0]+= 1; // ????????????
                            }

                            for (int a=1; a<=2; a++){
                                if (fcstDate.equals(days[a])){
                                    Log.d("trueTMX", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , "+dayWeek(changeDays[0]));
                                    shortItems[a].tvTmpWeek= Math.round(value)+"??";
                                    shortItems[a].tvWeek= dayWeek(changeDays[0]);
                                    widgetTmx[a]= Math.round(value)+"??"; // ????????? ????????? ??????

                                    changeDays[0]+= 1;
                                }
                            }
                        } // if TMX ????????????

                        // ????????????
                        if(category.equals("TMN")){

                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueTMN", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            float value= Float.parseFloat(fcstValue);

                            if (fcstDate.equals(days[0])){

                                Log.d("trueTMN", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , ??????");
                                shortItems[0].tvTmnWeek= Math.round(value)+"??";
                                Log.d("tsetDataTMN", shortItems[0].tvTmnWeek+","+ fcstValue);
                                widgetTmn[0]= Math.round(value)+"??"; // ????????? ????????? ??????
                                Log.d("testWidgetTmn", widgetTmn[0]+"");
                                changeDays[1]+= 1;
                            }

                            for (int a=1; a<=2; a++){

                                if (fcstDate.equals(days[a])){

                                    Log.d("trueTMN", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , ??????");
                                    shortItems[a].tvTmnWeek= Math.round(value)+"??";
                                    Log.d("tsetDataTMN", shortItems[a].tvTmnWeek+","+ fcstValue);
                                    widgetTmn[a]= Math.round(value)+"??"; // ????????? ????????? ??????
                                    changeDays[1]+= 1;
                                }
                            }
                        }

                        // ????????????
                        if (category.equals("POP")){

                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valuePOP", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            // ??????????????? ????????????, ??????????????? ??????????????? ?????? ????????? ???????????? ????????? ????????????
                            if (fcstDate.equals(days[0]) && hour.equals(fcstTime) || fcstDate.equals(yesterday)) { // ?????? ??????
                                Log.d("truePOP", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , ??????");
                                shortItems[0].tvPop= fcstValue+"%";
                                changeDays[2]+= 1; // ????????????
                            }
                            for (int k=1; k<=2; k++){
                                if (fcstDate.equals(days[k]) && hour.equals(fcstTime)){
                                    Log.d("truePOP", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , "+dayWeek(changeDays[1]));
                                    changeDays[2]+= 1;
                                    shortItems[k].tvPop= fcstValue+"%";
                                }
                            }
                        } // if POP ????????????

                        // ???????????? [ ?????? 1, ???????????? 3, ?????? 4 ]
                        if (category.equals("SKY")){
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueSKY", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            // ??????????????? ????????????, ??????????????? ??????????????? ?????? ????????? ???????????? ????????? ????????????
                            if (fcstDate.equals(days[0]) && fcstTime.equals("0900") || fcstDate.equals(yesterday)) { // ?????? ??????
                                Log.d("trueSKY", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , ??????");

                                if (fcstValue.equals("1")){
                                    Log.d("WEATHERQ", "??????");
                                    shortItems[0].imgSkyMax= R.drawable.weather_sunny;


                                }else if (fcstValue.equals("3")){
                                    Log.d("WEATHERQ", "????????????");
                                    shortItems[0].imgSkyMax= R.drawable.weather_cloudy;

                                }else if (fcstValue.equals("4")){
                                    Log.d("WEATHERQ", "??????");
                                    shortItems[0].imgSkyMax= R.drawable.weather_blur;
                                }
                            }
                            if (fcstDate.equals(days[0]) && fcstTime.equals("1800") || fcstDate.equals(yesterday)){
                                Log.d("trueSKY", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , ??????");
                                changeDays[3]+= 1; // ????????????
                                if (fcstValue.equals("1")){
                                    Log.d("WEATHERQ", "??????");
                                    shortItems[0].imgSkyMin= R.drawable.weather_sunny;
                                    imgWidgetSky[0]= R.drawable.weather_sunny;
                                    tvWidgetSky[0]= "??????";

                                }else if (fcstValue.equals("3")){
                                    Log.d("WEATHERQ", "????????????");
                                    shortItems[0].imgSkyMin= R.drawable.weather_cloudy;
                                    imgWidgetSky[0]= R.drawable.weather_cloudy;
                                    tvWidgetSky[0]= "??????";

                                }else if (fcstValue.equals("4")){
                                    Log.d("WEATHERQ", "??????");
                                    shortItems[0].imgSkyMin= R.drawable.weather_blur;
                                    imgWidgetSky[0]= R.drawable.weather_blur; // ?????? ???????????????
                                    tvWidgetSky[0]= "??????"; // ?????? ???????????????
                                }
                            }
                            for (int j= 1; j<= 2; j++){

                                // ?????? ????????????
                                if (fcstDate.equals(days[j]) && fcstTime.equals("0900")) {
                                    Log.d("trueSKYS", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , "+dayWeek(changeDays[2]));

                                    // ????????? ???????????? ????????? ?????? ?????? ????????? ??????
                                    if (fcstValue.equals("1")){
                                        Log.d("WEATHERQ", "??????");
                                        shortItems[j].imgSkyMax= R.drawable.weather_sunny;
                                        imgWidgetSky[j]= R.drawable.weather_sunny;
                                        tvWidgetSky[j]= "??????";

                                    }else if (fcstValue.equals("3")){
                                        Log.d("WEATHERQ", "????????????");
                                        shortItems[j].imgSkyMax= R.drawable.weather_cloudy;
                                        imgWidgetSky[j]= R.drawable.weather_cloudy;
                                        tvWidgetSky[j]= "????????????";

                                    }else if (fcstValue.equals("4")){
                                        Log.d("WEATHERQ", "??????");
                                        shortItems[j].imgSkyMax=R.drawable.weather_blur;

                                        imgWidgetSky[j]= R.drawable.weather_blur; // ?????? ???????????????
                                        tvWidgetSky[j]= "??????"; // ?????? ???????????????

                                    }
                                }

                                // ?????? ????????????
                                if (fcstDate.equals(days[j]) && fcstTime.equals("1800")) {
                                    Log.d("trueSKYS", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , "+dayWeek(changeDays[2]));
                                    changeDays[3]+= 1; // ????????????

                                    // ????????? ???????????? ????????? ?????? ?????? ????????? ??????
                                    if (fcstValue.equals("1")){
                                        Log.d("WEATHERQ", "??????");
                                        shortItems[j].imgSkyMin= R.drawable.weather_sunny;

                                    }else if (fcstValue.equals("3")){
                                        Log.d("WEATHERQ", "????????????");
                                        shortItems[j].imgSkyMin= R.drawable.weather_cloudy;

                                    }else if (fcstValue.equals("4")){
                                        Log.d("WEATHERQ", "??????");
                                        shortItems[j].imgSkyMin=R.drawable.weather_blur;
                                    }
                                }

                                // ?????? ??????????????? ?????? ????????? ??????
                                if(fcstDate.equals(days[0]) && hour.equals(fcstTime) || fcstDate.equals(yesterday)){
                                    // ????????? ???????????? ????????? ?????? ?????? ????????? ??????
                                    if (fcstValue.equals("1")){
                                        imgSky.setImageResource(R.drawable.weather_sunny);
                                        tvSky.setText("??????");

                                    }else if (fcstValue.equals("3")){
                                        imgSky.setImageResource(R.drawable.weather_cloudy);
                                        tvSky.setText("????????????");

                                    }else if (fcstValue.equals("4")){
                                        imgSky.setImageResource(R.drawable.weather_blur);
                                        tvSky.setText("??????");
                                    }
                                }
                            }
                        } // if SKY ????????????

                        // ??????
                        if (category.equals("REH")){
                            String fcstValue= obj.getString("fcstValue");
                            String fcstDate= obj.getString("fcstDate");
                            String fcstTime= obj.getString("fcstTime");
                            Log.d("valueREH", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime);

                            // ??????????????? ????????????, ??????????????? ??????????????? ?????? ????????? ???????????? ????????? ????????????
                            if (fcstDate.equals(days[0]) && hour.equals(fcstTime) || fcstDate.equals(yesterday)) { // ?????? ??????
                                Log.d("trueREH", category+" , "+ fcstValue +" , "+fcstDate +" , "+fcstTime +" , ??????");
                                changeDays[4]+= 1; // ????????????
                                TextView tvReh;
                                tvReh= findViewById(R.id.tv_reh);
                                tvReh.setText(fcstValue+"%");
                            }
                        } // if REH ??????

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


                    // ?????? ?????????????????? base Url
                    String baseUrl2 = "http://apis.data.go.kr/1360000/MidFcstInfoService/";

                    // ?????? ???????????? json ????????????
                    Retrofit retrofit2= RetrofitHelper.getInstance(baseUrl2);
                    RetrofitService retrofitService2= retrofit2.create(RetrofitService.class);

                    // URL ???????????? ????????? getJson() ???????????? ??????
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

                                        // 3,4,5,6??? ??? ??????,?????? ????????????
                                        rnStAm[index]= obj.getString("rnSt"+j+"Am");
                                        rnStPm[index]= obj.getString("rnSt"+j+"Pm");

                                        // 3,4,5,6??? ??? ??????,?????? ????????????
                                        wfAm[index]= obj.getString("wf"+j+"Am");
                                        wfPm[index]= obj.getString("wf"+j+"Pm");
                                        Log.d("rnData", rnStAm[index]+","+rnStPm[index]+ ", index : "+ index + " j :" + j);
                                        Log.d("wfData", wfAm[index]+","+wfPm[index]+ ", index : "+ index + " j :" + j);

                                        shortItems[j].tvWeek= dayWeek(index+3);
                                        shortItems[j].tvPop= rnStPm[index]+"%";
                                        shortItems[j].imgSkyMax= R.drawable.weather_rain;

                                        // ?????? ????????????
                                        if (wfAm[index].equals("??????")){
                                            Log.d("WEATHERQ", "??????");
                                            shortItems[j].imgSkyMin= R.drawable.weather_sunny;
                                            imgWidgetSky[j]= R.drawable.weather_sunny; // ?????? ????????? ??????
                                            tvWidgetSky[j]= "??????"; // ?????? ????????? ??????

                                        }else if (wfAm[index].equals("????????????")){
                                            Log.d("WEATHERQ", "????????????");
                                            shortItems[j].imgSkyMin= R.drawable.weather_cloudy;
                                            imgWidgetSky[j]= R.drawable.weather_cloudy; // ?????? ????????? ??????
                                            tvWidgetSky[j]= "????????????"; // ?????? ????????? ??????

                                        }else if (wfAm[index].equals("???????????? ???")
                                                ||wfAm[index].equals("???????????? ???/???")
                                                ||wfAm[index].equals("???????????? ?????????")
                                                ||wfAm[index].equals("????????? ???")
                                                ||wfAm[index].equals("????????? ?????????")
                                                ||wfAm[index].equals("????????? ???/???")){
                                            Log.d("WEATHERQ", "???????????? ???");
                                            shortItems[j].imgSkyMin=R.drawable.weather_rain;
                                            imgWidgetSky[j]= R.drawable.weather_rain; // ?????? ????????? ??????
                                            tvWidgetSky[j]= "???????????? ???"; // ?????? ????????? ??????

                                        }else if (wfAm[index].equals("???????????? ???")||wfAm[index].equals("????????? ???")){
                                            Log.d("WEATHERQ", "??????");
                                            shortItems[j].imgSkyMin=R.drawable.weather_rain;
                                            imgWidgetSky[j]= R.drawable.weather_rain; // ?????? ????????? ??????
                                            tvWidgetSky[j]= "????????? ???"; // ?????? ????????? ??????

                                        }else if (wfAm[index].equals("??????")){
                                            Log.d("WEATHERQ", "??????");
                                            shortItems[j].imgSkyMin=R.drawable.weather_blur;
                                            imgWidgetSky[j]= R.drawable.weather_blur; // ?????? ????????? ??????
                                            tvWidgetSky[j]= "??????"; // ?????? ????????? ??????

                                        }

                                        // ?????? ????????????
                                        if (wfPm[index].equals("??????")){
                                            Log.d("WEATHERQ", "??????");
                                            shortItems[j].imgSkyMax= R.drawable.weather_sunny;

                                        }else if (wfPm[index].equals("????????????")){
                                            Log.d("WEATHERQ", "????????????");
                                            shortItems[j].imgSkyMax= R.drawable.weather_cloudy;

                                        }else if (wfPm[index].equals("???????????? ???")
                                                ||wfPm[index].equals("???????????? ???/???")
                                                ||wfPm[index].equals("???????????? ?????????")
                                                ||wfPm[index].equals("????????? ???")
                                                ||wfPm[index].equals("????????? ?????????")
                                                ||wfPm[index].equals("????????? ???/???")){
                                            Log.d("WEATHERQ", "???????????? ???");
                                            shortItems[j].imgSkyMax=R.drawable.weather_rain;

                                        }else if (wfPm[index].equals("???????????? ???")||wfPm[index].equals("????????? ???")){
                                            Log.d("WEATHERQ", "??????");
                                            shortItems[j].imgSkyMax=R.drawable.weather_rain;

                                        }else if (wfPm[index].equals("??????")){
                                            Log.d("WEATHERQ", "??????");
                                            shortItems[j].imgSkyMax=R.drawable.weather_blur;
                                        }
                                        index+=1;
                                    } // for

                                } // for

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }// catch

                            // ?????? ???????????? base Url
                            String baseUrl3 = "http://apis.data.go.kr/1360000/MidFcstInfoService/";

                            // ???????????? json ????????????
                            Retrofit retrofit3= RetrofitHelper.getInstance(baseUrl3);
                            RetrofitService retrofitService3= retrofit3.create(RetrofitService.class);

                            // URL ???????????? ????????? getJson() ???????????? ??????
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

                                            // 3,4,5,6??? ??? ????????????
                                            for (int j=3; j<=6; j++){
                                                taMax[index[0]]= obj.getString("taMax"+j);
                                                shortItems[j].tvTmpWeek= taMax[index[0]]+"??";
                                                widgetTmx[j]= taMax[index[0]]+"??"; // ?????? ????????? ??????

                                                index[0]+=1;
                                            } // for

                                            // 3,4,5,6??? ??? ????????????
                                            for (int j=3; j<=6; j++){
                                                taMax[index[1]]= obj.getString("taMin"+j);
                                                shortItems[j].tvTmnWeek= taMax[index[1]]+"??";
                                                widgetTmn[j]= taMax[index[1]]+"??"; // ?????? ????????? ??????

                                                index[1]+=1;
                                            } // for

                                            for (int j=3; j<=6; j++){
                                                weekItems.add(shortItems[j]);
                                                Log.d("SIZE", weekItems.size()+"");
                                            }
                                            adapter.notifyDataSetChanged();

                                            // ???????????? ?????? ???????????? ?????????????????? ??????
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
                    }); // call2 [ ???????????? ]

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }// ???????????? onResponse

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.v("TAG", t.getMessage());
            }
        }); // ???????????? callback

    } // retrofitParsing()


    // XML ??????
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
//            Date date = new Date(now); // ?????????????????? ?????? ????????? : new Date(now+(1000*60*60*24*2))
//
//            SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
//            SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
//
//            String getTime = sdf.format(date);
//            String getHour = sdfHour.format(date);
//
//            Log.d("dates", getTime+","+getHour);
//
//            // ????????????
//            String apiUrl= "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?"
//                    + "serviceKey=" + apiKey // api ???
//                    + "&pageNo=" + pageNo // ????????? ??????
//                    + "&numOfRows=" + numOfrows // ??? ????????? ?????? ???
//                    + "&dataType" + dataType // ??????????????????(XML/JSON)
//                    + "&base_date=" + getTime // ???????????? (ex.20230227)
//                    + "&base_time=" + baseTime // ???????????? (ex.0500)
//                    + "&nx=" + lat // ???????????? x??????
//                    + "&ny=" + lng; // ???????????? y??????
//            Log.d("values", regId);
//
//            // ????????????
//            String apiUrl2= "https://apis.data.go.kr/1360000/MidFcstInfoService/getMidTa?"
//                    + "serviceKey=" + apiKey // api ???
//                    + "&pageNo=" + pageNo // ????????? ??????
//                    + "&numOfRows=" + "10" // ??? ????????? ?????? ???
//                    + "&regId=" + regId // ?????? ????????????
//                    + "&tmFc=" + getTime + "0600"; // ????????????+???????????? (ex.202302270600)
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
//                    // ???????????? api
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
//                                if(xpp.getText().equals("TMP")){  // ???????????? : TMP(??????)
//                                    type = xpp.getText();
//
//                                }else if (xpp.getText().equals("SKY")&& skyFin==0){ // ???????????? : SKY(???????????? = 0~5 ??????, 6~8 ????????????, 9~10 ??????)
//                                    skyType = xpp.getText();
//                                }
//
//                            }else if (tagName.equals("fcstDate") && type.equals("TMP") && itemNum==0){  // ??????
//                                xpp.next();
//                                fcstDate= xpp.getText();
//
//                                if (fcstDate.equals(getTime)){
//                                    itemNum+=1;
//                                    fcstDate= xpp.getText();
//                                }
//
//                            }else if (tagName.equals("fcstTime") && type.equals("TMP")&& itemNum==1) {  // ??????
//                                xpp.next();
//                                fcstTime= xpp.getText();
//                                if (fcstTime.equals(getHour+"00")){
//                                    itemNum+=1;
//                                    fcstTime= xpp.getText();
//                                }
//
//                            // ?????? ???
//                            }else if (tagName.equals("fcstValue") && type.equals("TMP") && fcstTime.equals(getHour+"00") && fcstDate.equals(getTime) && itemNum==2) {
//                                xpp.next();
//                                weekItem[0].tvWeek= dayWeek(changeDay); // ??????
//                                weekItem[0].tvTmpWeek = xpp.getText() + "??"; // ??????
//                                type = "";
//                                itemNum += 1;
//                                Log.d("tmpData", xpp.getText()+","+lat+","+lng);
//
//                                tmpEditor.putString("tmp"+tmpNum, xpp.getText() + "??");
//                                tmpEditor.commit();
//                                tmpNum+=1;
//
//                            // ????????????
//                            }else if (tagName.equals("fcstValue") && skyType.equals("SKY") && skyFin==0){
//                                xpp.next();
//
//                                // api ???????????? ????????? ????????? ???????????? ( 0~5 ??????, 6~8 ????????????, 9~10 ?????? )
//                                int sky= Integer.parseInt(xpp.getText());
//
//                                // api????????? ????????? ???????????? ????????? ?????? ?????? ???????????? ???????????? ??????
//                                if (sky >= 0 || sky <= 5){
//                                    skyCurrent = "??????";
//                                }else if (sky >=6 || sky <=8){
//                                    skyCurrent = "????????????";
//                                }else if (sky >= 9 || sky <= 10){
//                                    skyCurrent = "??????";
//                                }
//
//                                // ???????????? ????????? ???????????? ???????????? ??????????????? ??????
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
//                                date = new Date(now+(1000*60*60*24*changeDay)); // ?????????????????? ?????? ????????? : new Date(now+(1000*60*60*24*1))
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
//                    // ???????????? api??? ???????????? ?????? ???????????? ??? ??????
//                    if (tmpResult== 1){
//                        // ???????????? api
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
//                                }else if(tagName.equals("taMin3")){ // 3??? ??? ????????????
//                                    xppWeek.next();
//
//                                    weekItem[1].tvWeek= dayWeek(changeDay); // ?????? ??????
//                                    weekItem[1].tvTmpWeek = xppWeek.getText() + "??"; // ??????
//
//                                    tmpEditor.putString("tmp"+tmpNum, xppWeek.getText() + "??");
//                                    tmpEditor.commit();
//                                    tmpNum+=1;
//
//                                    weekItems.add(weekItem[1]);
//                                    changeDay+=1;
//
//                                }else if(tagName.equals("taMin4")){ // 4??? ??? ????????????
//                                    xppWeek.next();
//                                    weekItem[2].tvWeek= dayWeek(changeDay); // ?????? ??????
//                                    weekItem[2].tvTmpWeek = xppWeek.getText() + "??"; // ??????
//
//                                    tmpEditor.putString("tmp"+tmpNum, xppWeek.getText() + "??");
//                                    tmpEditor.commit();
//                                    tmpNum+=1;
//
//                                    weekItems.add(weekItem[2]);
//                                    changeDay+=1;
//
//                                }else if(tagName.equals("taMin5")){ // 5??? ??? ????????????
//                                    xppWeek.next();
//                                    weekItem[3].tvWeek= dayWeek(changeDay); // ?????? ??????
//                                    weekItem[3].tvTmpWeek = xppWeek.getText() + "??"; // ??????
//
//                                    tmpEditor.putString("tmp"+tmpNum, xppWeek.getText() + "??");
//                                    tmpEditor.commit();
//                                    tmpNum+=1;
//
//                                    weekItems.add(weekItem[3]);
//                                    changeDay+=1;
//
//                                }else if(tagName.equals("taMin6")){ // 6??? ??? ????????????
//                                    xppWeek.next();
//                                    weekItem[4].tvWeek= dayWeek(changeDay); // ?????? ??????
//                                    weekItem[4].tvTmpWeek = xppWeek.getText() + "??"; // ??????
//
//                                    tmpEditor.putString("tmp"+tmpNum, xppWeek.getText() + "??");
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
//                                    tmpResult+=1; // END_TAG??? item??? ?????? break;
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

    // ???????????? ?????????
    public String dayWeek(int i){
        long now= System.currentTimeMillis();
        Date date = new Date(now);

        Calendar cal= Calendar.getInstance();
        cal.setTime(date);

        int dayWeeks = cal.get(Calendar.DAY_OF_WEEK);
        String strWeek= "";

        switch(dayWeeks+i){

            case 1: case 8: case 15:
                strWeek = "?????????";
                break;
            case 2: case 9: case 16:
                strWeek = "?????????";
                break;
            case 3: case 10: case 17:
                strWeek = "?????????";
                break;
            case 4: case 11: case 18:
                strWeek = "?????????";
                break;
            case 5: case 12: case 19:
                strWeek = "?????????";
                break;
            case 6: case 13: case 20:
                strWeek = "?????????";
                break;
            case 7: case 14: case 0:
                strWeek = "?????????";
                break;

        }
        return strWeek;
    }


}



