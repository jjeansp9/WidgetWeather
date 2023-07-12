package kr.co.widgetweather;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProject extends AppWidgetProvider {

    private final String ACTION_BTN_LEFT = "ButtonClick";
    private final String ACTION_BTN_RIGHT = "ButtonClick";
    //private final String REFESH = "refresh";

    //SwipeRefreshLayout swipeRefreshLayout;

    int tmpIndex = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);

        Intent intentLeft = new Intent(context, WidgetProject.class).setAction(ACTION_BTN_LEFT);
        Intent intentRight = new Intent(context, WidgetProject.class).setAction(ACTION_BTN_RIGHT);
        //Intent refresh = new Intent(context, WidgetProject.class).setAction(REFESH);

        PendingIntent pendingIntentLeft = PendingIntent.getBroadcast(context,0, intentLeft, 0);
        PendingIntent pendingIntentRight = PendingIntent.getBroadcast(context,0, intentRight, 0);
        //PendingIntent pendingIntentRefresh = PendingIntent.getBroadcast(context,0, refresh, 0);

        remoteViews.setOnClickPendingIntent(R.id.previous, pendingIntentLeft); // 왼쪽 화살표 클릭시 작동
        remoteViews.setOnClickPendingIntent(R.id.next, pendingIntentRight); // 오른쪽 화살표 클릭시 작동
        //remoteViews.setOnClickPendingIntent(R.id.refresh_widget, pendingIntentRefresh); // 새로고침

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        updateAppWidget(context, appWidgetManager, appWidgetIds[0]);


    }


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_project);

        String address="";
        String tmpCurrent="";
        String skyCurrent="";

        // 디바이스에 저장된 주소 데이터를 가져온 후 view에 데이터값으로 텍스트 변경.
        SharedPreferences prefLoc= context.getSharedPreferences("location", MODE_PRIVATE);
        address= prefLoc.getString("address", address);
        views.setTextViewText(R.id.tv_loc, address);

        // 디바이스에 저장된 온도 데이터 텍스트 변경
        SharedPreferences prefWeather= context.getSharedPreferences("weather", MODE_PRIVATE);
        tmpCurrent= prefWeather.getString("tmp"+0, tmpCurrent);
        views.setTextViewText(R.id.tv_tmp, tmpCurrent);

        // 디바이스에 저장된 문자열에 따라 어울리는 이미지로 변경.
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

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (action.equals(ACTION_BTN_LEFT)){ // 왼쪽 화살표 클릭시 동작

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);
            ComponentName componentName = new ComponentName(context, WidgetProject.class);

            SharedPreferences prefWeather= context.getSharedPreferences("weather", MODE_PRIVATE);
            String tmp = null;

            tmp= prefWeather.getString("tmp"+tmpIndex, tmp);
            remoteViews.setTextViewText(R.id.tv_tmp, tmp);
            Log.d("tmps", tmp);
            tmpIndex-=1;

            appWidgetManager.updateAppWidget(componentName, remoteViews);

        }else if (action.equals(ACTION_BTN_RIGHT)){ // 오른쪽 화살표 클릭시 동작

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);
            ComponentName componentName = new ComponentName(context, WidgetProject.class);

            SharedPreferences prefWeather= context.getSharedPreferences("weather", MODE_PRIVATE);
            String tmp = null;

            tmp= prefWeather.getString("tmp"+tmpIndex, tmp);
            remoteViews.setTextViewText(R.id.tv_tmp, tmp);
            Log.d("tmps", tmp);
            tmpIndex+=1;

            appWidgetManager.updateAppWidget(componentName, remoteViews);
        }

    }


}