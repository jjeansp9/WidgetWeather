package kr.co.widgetweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import kr.co.widgetweather.activities.MainActivity;

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
        remoteViews.setOnClickPendingIntent(R.id.previous, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
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
            //버튼 클릭 결과를 로그로 확인합니다.
            Log.d("이벤트클릭 테스트 ","클릭!");
            //버튼 클릭 결과를 위젯 위의 텍스트뷰를 변경함으로 확인합니다.
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_project);
            ComponentName componentName = new ComponentName(context, WidgetProject.class);

            remoteViews.setTextViewText(R.id.previous,"이벤트발생!");
            appWidgetManager.updateAppWidget(componentName, remoteViews);
        }
    }
}