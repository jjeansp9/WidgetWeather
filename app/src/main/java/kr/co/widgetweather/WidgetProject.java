package kr.co.widgetweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.widgetweather.activities.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class WidgetProject extends AppWidgetProvider {

    private static final String ACTION_BTN_LEFT = "Left";
    private static final String ACTION_BTN_RIGHT = "Right";
    private static final String ACTION_BTN_REFRESH = "Refresh";

    public int num= ((MainActivity)MainActivity.context_main).widgetNum;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for(int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);

        Intent intentLeft = new Intent(context, WidgetProject.class).setAction(ACTION_BTN_LEFT);
        PendingIntent pendingIntentLeft = PendingIntent.getBroadcast(context,0, intentLeft, 0);
        remoteViews.setOnClickPendingIntent(R.id.previous, pendingIntentLeft);

        Intent intentRight = new Intent(context, WidgetProject.class).setAction(ACTION_BTN_RIGHT);
        PendingIntent pendingIntentRight = PendingIntent.getBroadcast(context,0, intentRight, 0);
        remoteViews.setOnClickPendingIntent(R.id.next, pendingIntentRight);

        Intent intentRefresh = new Intent(context, WidgetProject.class).setAction(ACTION_BTN_REFRESH);
        PendingIntent pendingIntentRefresh = PendingIntent.getBroadcast(context,0, intentRefresh, 0);
        remoteViews.setOnClickPendingIntent(R.id.refresh_click, pendingIntentRefresh);

        long now= System.currentTimeMillis();
        Date date = new Date(now); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*2))

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");

        String getTime = sdf.format(((MainActivity)MainActivity.context_main).widgetDate);
        String getHour = sdfHour.format(date);


        String location1= ((MainActivity)MainActivity.context_main).address1;
        String location2= ((MainActivity)MainActivity.context_main).address2;
        String tmx= ((MainActivity) MainActivity.context_main).widgetTmx[0];
        String tmn= ((MainActivity)MainActivity.context_main).widgetTmn[0];
        String tvSky= ((MainActivity)MainActivity.context_main).tvWidgetSky[0];
        int imgSky = ((MainActivity)MainActivity.context_main).imgWidgetSky[0];


        remoteViews.setTextViewText(R.id.tv_tmx, tmx);
        remoteViews.setTextViewText(R.id.tv_tmn, tmn);
        remoteViews.setImageViewResource(R.id.img_sky, imgSky);
        remoteViews.setTextViewText(R.id.update, getHour);
        remoteViews.setTextViewText(R.id.tv_sky, tvSky);
        remoteViews.setTextViewText(R.id.tv_loc_1, location1);
        remoteViews.setTextViewText(R.id.tv_loc_2, location2);
        remoteViews.setTextViewText(R.id.widget_date, getTime);
//
//        Log.d("testWidgetTmn", tmn[0]+""+ location);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }


    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        long now= System.currentTimeMillis();
        Date date = new Date(now); // 현재시간에서 하루 더하기 : new Date(now+(1000*60*60*24*2))

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");

        String getTime = sdf.format(((MainActivity)MainActivity.context_main).widgetDate);
        String getHour = sdfHour.format(date);

        // 왼쪽클릭
        if(action.equals(ACTION_BTN_LEFT) && num > 0 && num < 7){

//            Log.d("testWidgetTmn", tmn[0]+"");

            //버튼 클릭 결과를 로그로 확인.
            Log.d("이벤트클릭 테스트 ","왼쪽클릭!");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);
            ComponentName componentName = new ComponentName(context, WidgetProject.class);

            ((MainActivity)MainActivity.context_main).widgetNum-=1;
            ((MainActivity)MainActivity.context_main).widgetChangeDays-=1;
            ((MainActivity)MainActivity.context_main).widgetDate= new Date(((MainActivity)MainActivity.context_main).widgetNow+(1000*60*60*24*((MainActivity)MainActivity.context_main).widgetChangeDays));
            getTime = sdf.format(((MainActivity)MainActivity.context_main).widgetDate);

            Log.d("testNumber", ((MainActivity)MainActivity.context_main).widgetNum+"");
            Log.d("testChangeDay", ((MainActivity)MainActivity.context_main).widgetChangeDays+"");

            String tmx= ((MainActivity) MainActivity.context_main).widgetTmx[((MainActivity)MainActivity.context_main).widgetNum];
            String tmn= ((MainActivity)MainActivity.context_main).widgetTmn[((MainActivity)MainActivity.context_main).widgetNum];
            String tvSky= ((MainActivity)MainActivity.context_main).tvWidgetSky[((MainActivity)MainActivity.context_main).widgetNum];
            int imgSky = ((MainActivity)MainActivity.context_main).imgWidgetSky[((MainActivity)MainActivity.context_main).widgetNum];

            Log.d("values.", tmx+","+tmn+","+tvSky+","+imgSky);

            remoteViews.setTextViewText(R.id.tv_tmx, tmx);
            remoteViews.setTextViewText(R.id.tv_tmn, tmn);
            remoteViews.setTextViewText(R.id.tv_sky, tvSky);
            remoteViews.setImageViewResource(R.id.img_sky, imgSky);
            remoteViews.setTextViewText(R.id.widget_date, getTime);
            remoteViews.setTextViewText(R.id.update, getHour);


            appWidgetManager.updateAppWidget(componentName, remoteViews);
        }

        // 오른쪽 클릭
        if(action.equals(ACTION_BTN_RIGHT) && num >= 0 && num < 6){

            //버튼 클릭 결과를 로그로 확인.
            Log.d("이벤트클릭 테스트 ","오른쪽클릭!");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);
            ComponentName componentName = new ComponentName(context, WidgetProject.class);

            ((MainActivity)MainActivity.context_main).widgetNum+=1;
            ((MainActivity)MainActivity.context_main).widgetChangeDays+=1;
            ((MainActivity)MainActivity.context_main).widgetDate= new Date(((MainActivity)MainActivity.context_main).widgetNow+(1000*60*60*24*((MainActivity)MainActivity.context_main).widgetChangeDays));
            getTime = sdf.format(((MainActivity)MainActivity.context_main).widgetDate);

            Log.d("testNumber", ((MainActivity)MainActivity.context_main).widgetNum+"");
            Log.d("testChangeDay", ((MainActivity)MainActivity.context_main).widgetChangeDays+","+ ((MainActivity)MainActivity.context_main).widgetDate);

            String tmx= ((MainActivity) MainActivity.context_main).widgetTmx[((MainActivity)MainActivity.context_main).widgetNum];
            String tmn= ((MainActivity)MainActivity.context_main).widgetTmn[((MainActivity)MainActivity.context_main).widgetNum];
            String tvSky= ((MainActivity)MainActivity.context_main).tvWidgetSky[((MainActivity)MainActivity.context_main).widgetNum];
            int imgSky = ((MainActivity)MainActivity.context_main).imgWidgetSky[((MainActivity)MainActivity.context_main).widgetNum];

            Log.d("values.", tmx+","+tmn+","+tvSky+","+imgSky);

            remoteViews.setTextViewText(R.id.tv_tmx, tmx);
            remoteViews.setTextViewText(R.id.tv_tmn, tmn);
            remoteViews.setTextViewText(R.id.tv_sky, tvSky);
            remoteViews.setImageViewResource(R.id.img_sky, imgSky);
            remoteViews.setTextViewText(R.id.update, getHour);
            remoteViews.setTextViewText(R.id.widget_date, getTime);

            appWidgetManager.updateAppWidget(componentName, remoteViews);
        }

        // 새로고침버튼 클릭
        if (action.equals(ACTION_BTN_REFRESH)){
            Log.d("refresh","클릭");

            ((MainActivity)MainActivity.context_main).widgetDate= new Date(((MainActivity)MainActivity.context_main).widgetNow);
            getTime = sdf.format(((MainActivity)MainActivity.context_main).widgetDate);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);
            ComponentName componentName = new ComponentName(context, WidgetProject.class);

            ((MainActivity)MainActivity.context_main).widgetNum=0;
            ((MainActivity)MainActivity.context_main).widgetChangeDays=0;

            String location1= ((MainActivity)MainActivity.context_main).address1;
            String location2= ((MainActivity)MainActivity.context_main).address2;
            String tmx= ((MainActivity) MainActivity.context_main).widgetTmx[0];
            String tmn= ((MainActivity)MainActivity.context_main).widgetTmn[0];
            String tvSky= ((MainActivity)MainActivity.context_main).tvWidgetSky[0];
            int imgSky = ((MainActivity)MainActivity.context_main).imgWidgetSky[0];

            remoteViews.setTextViewText(R.id.tv_tmx, tmx);
            remoteViews.setTextViewText(R.id.tv_tmn, tmn);
            remoteViews.setImageViewResource(R.id.img_sky, imgSky);
            remoteViews.setTextViewText(R.id.update, getHour);
            remoteViews.setTextViewText(R.id.widget_date, getTime);
            remoteViews.setTextViewText(R.id.tv_sky, tvSky);
            remoteViews.setTextViewText(R.id.tv_loc_1, location1);
            remoteViews.setTextViewText(R.id.tv_loc_2, location2);

            appWidgetManager.updateAppWidget(componentName, remoteViews);
        }

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}