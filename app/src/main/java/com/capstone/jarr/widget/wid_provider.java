package com.capstone.jarr.widget;
import com.capstone.jarr.concurrent.con_postsync;
import com.capstone.jarr.interfaces.ui_detail;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;
import com.capstone.jarr.R;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import com.capstone.jarr.interfaces.ui_list;

/**
 * Created on 09/09/2018.
 */


public class wid_provider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lout_widget);

            Intent intent = new Intent(context, ui_list.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.title, pendingIntent);

            views.setRemoteAdapter(R.id.list,
                    new Intent(context, wid_service.class));

            PendingIntent clickPendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(new Intent(context, ui_detail.class))
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.list, clickPendingIntent);

            views.setEmptyView(R.id.list, R.id.widget_empty);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                || intent.getAction().equals(con_postsync.int_dataupdate)) {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            widgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list);
        }
    }
}
