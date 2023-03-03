package kr.co.widgetweather;

import static android.content.Context.MODE_PRIVATE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProject extends AppWidgetProvider {

    private final String ACTION_BTN = "ButtonClick";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);

        Intent intent = new Intent(context, WidgetProject.class).setAction(ACTION_BTN);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0);

        remoteViews.setOnClickPendingIntent(R.id.previous, pendingIntent); // 왼쪽 화살표 클릭시 작동
        remoteViews.setOnClickPendingIntent(R.id.widget_weather_today, pendingIntent);


        updateAppWidget(context, appWidgetManager, appWidgetIds[0]);





    }

    @Override
    public void onEnabled(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        // Enter relevant functionality for when the first widget is created
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);


    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_project);

        String address="";
        String tmpCurrent="";
        String skyCurrent="";

        // 디바이스에 저장된 주소 데이터를 가져온 후 view에 데이터값으로 텍스트 변경
        SharedPreferences prefLoc= context.getSharedPreferences("location", MODE_PRIVATE);
        address= prefLoc.getString("address", address);
        views.setTextViewText(R.id.tv_loc, address);

        // 디바이스에 저장된 온도 데이터 텍스트 변경
        SharedPreferences prefWeather= context.getSharedPreferences("weather", MODE_PRIVATE);
        tmpCurrent= prefWeather.getString("tmp", tmpCurrent);
        views.setTextViewText(R.id.tv_tmp, tmpCurrent);

        // 디바이스에 저장된 문자열에 따라 어울리는 이미지로 변경
        skyCurrent= prefWeather.getString("sky", skyCurrent);
        if(skyCurrent == ""){
            views.setImageViewResource(R.id.img_sky, R.drawable.weather_sunny);
        }else if (skyCurrent.equals("맑음")){
            views.setImageViewResource(R.id.img_sky, R.drawable.weather_sunny);
        }else if (skyCurrent.equals("구름많음")){
            views.setImageViewResource(R.id.img_sky, R.drawable.weather_cloudy);
        }else if(skyCurrent.equals("흐림")){
            views.setImageViewResource(R.id.img_sky, R.drawable.weather_blur);
        }

        String tmpCurrents[]={"","","","","","",""};

        for (int i=0; i<=6; i++){
            tmpCurrents[i]= prefWeather.getString("tmp"+i, tmpCurrents[i]);
            Log.d("test", tmpCurrents[i]+i);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action.equals(ACTION_BTN)){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);
            ComponentName componentName = new ComponentName(context, WidgetProject.class);

            SharedPreferences prefWeather= context.getSharedPreferences("weather", MODE_PRIVATE);

            appWidgetManager.updateAppWidget(componentName, remoteViews);



        }
    }
}