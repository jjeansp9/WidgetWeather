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
    String address= "";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);

        Intent intent = new Intent(context, WidgetProject.class).setAction(ACTION_BTN);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0);

        remoteViews.setOnClickPendingIntent(R.id.previous, pendingIntent); // 왼쪽 화살표 클릭시 작동
        remoteViews.setOnClickPendingIntent(R.id.widget_weather_today, pendingIntent);

        ComponentName componentName = new ComponentName(context, WidgetProject.class);

        // 디바이스에 저장된 주소 데이터 가져온 후 view에 데이터값으로 텍스트 변경
        SharedPreferences pref= context.getSharedPreferences("location", MODE_PRIVATE);
        address= pref.getString("address", address);
        remoteViews.setTextViewText(R.id.tv_loc, address+"11");
        Log.d("address", address+"11");

        appWidgetManager.updateAppWidget(componentName, remoteViews);



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

//        CharSequence widgetText = context.getString(R.string.appwidget_text);
//        // Construct the RemoteViews object
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_project);
//        views.setTextViewText(R.id.appwidget_text, widgetText);
//
//        // Instruct the widget manager to update the widget
//        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action.equals(ACTION_BTN)){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);
            ComponentName componentName = new ComponentName(context, WidgetProject.class);

            // 디바이스에 저장된 주소 데이터 가져온 후 view에 데이터값으로 텍스트 변경
            SharedPreferences pref= context.getSharedPreferences("location", MODE_PRIVATE);
            address= pref.getString("address", address);
            remoteViews.setTextViewText(R.id.tv_loc, address);
            Log.d("address", address+"1");

            appWidgetManager.updateAppWidget(componentName, remoteViews);



        }
    }
}