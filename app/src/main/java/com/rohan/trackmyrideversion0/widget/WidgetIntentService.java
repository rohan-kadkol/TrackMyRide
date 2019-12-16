package com.rohan.trackmyrideversion0.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.rohan.trackmyrideversion0.R;

public class WidgetIntentService extends IntentService {
    private boolean isDriver;

    public static final String ACTION_SHOW_RIDE_CODE = "com.rohan.trackmyrideversion0.action.show_ride_code";

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    public static void startActionUpdateText(Context context) {
        Intent intent = new Intent(context, WidgetIntentService.class);
        intent.setAction(ACTION_SHOW_RIDE_CODE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SHOW_RIDE_CODE.equals(action)) {
                handleActionUpdateIngredientStringSet(intent);
            }
        }
    }

    private void handleActionUpdateIngredientStringSet(Intent intent) {
        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.shared_preferences_rides_string), Context.MODE_PRIVATE);
        String widgetString = sharedpreferences.getString(getString(R.string.shared_preferences_widget_string_key), getString(R.string.widget_string_default));

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, AppWidget.class));

        AppWidget.updateAllWidgets(this, appWidgetManager, widgetString, appWidgetIds);
    }
}
