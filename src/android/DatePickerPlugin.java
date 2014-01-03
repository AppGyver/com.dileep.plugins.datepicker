/**
 *
 */
package com.dileep.plugins.datepicker;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

/**
 * @author ng4e
 * @author Daniel van 't Oever
 *
 *         Rewrote plugin so it it similar to the iOS datepicker plugin and it
 *         accepts prefilled dates and time
 */
public class DatePickerPlugin extends CordovaPlugin {

    private static final String ACTION_DATE = "date";
    private static final String ACTION_TIME = "time";
    private final String pluginName = "DatePickerPlugin";

    /*
     * (non-Javadoc)
     *
     * @see com.phonegap.api.Plugin#execute(java.lang.String, org.json.JSONArray, java.lang.String)
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(pluginName, "DatePicker called with method: '" + action + "' and options: " + args);

        show(args, callbackContext);
        return true;
    }

    public synchronized void show(final JSONArray data, final CallbackContext callbackContext) {
        final Context currentCtx = cordova.getActivity();
        final Calendar c = Calendar.getInstance();
        final Runnable runnable;

        String action = "date";

        /*
         * Parse information from data parameter and where possible, override above date fields
         */
        int month = -1, day = -1, year = -1, hour = -1, min = -1;
        try {
            JSONObject obj = data.getJSONObject(0);
            action = obj.getString("mode");

            String optionDate = obj.getString("date");

            if (!optionDate.isEmpty()) {
                String[] datePart = optionDate.split("/");
                month = Integer.parseInt(datePart[0]);
                day = Integer.parseInt(datePart[1]);
                year = Integer.parseInt(datePart[2]);
                hour = Integer.parseInt(datePart[3]);
                min = Integer.parseInt(datePart[4]);

                /* currently not handled in Android */
                // boolean optionAllowOldDates = obj.getBoolean("allowOldDates");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // By default initalize these fields to 'now'
        final int mYear = year == -1 ? c.get(Calendar.YEAR) : year;
        final int mMonth = month == -1 ? c.get(Calendar.MONTH) : month - 1;
        final int mDay = day == -1 ? c.get(Calendar.DAY_OF_MONTH) : day;
        final int mHour = hour == -1 ? c.get(Calendar.HOUR_OF_DAY) : hour;
        final int mMinutes = min == -1 ? c.get(Calendar.MINUTE) : min;

        if (ACTION_TIME.equalsIgnoreCase(action)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    final TimeSetListener timeSetListener = new TimeSetListener(callbackContext);
                    final TimePickerDialog timeDialog = new TimePickerDialog(currentCtx, timeSetListener, mHour, mMinutes, true);
                    timeDialog.show();
                }
            };

        } else if (ACTION_DATE.equalsIgnoreCase(action)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    final DateSetListener dateSetListener = new DateSetListener(callbackContext);
                    final DatePickerDialog dateDialog = new DatePickerDialog(currentCtx, dateSetListener, mYear, mMonth, mDay);
                    dateDialog.show();
                }
            };

        } else {
            Log.d(pluginName, "Unknown action. Only 'date' or 'time' are valid actions");
            return;
        }

        cordova.getActivity().runOnUiThread(runnable);
    }

    private final class DateSetListener implements OnDateSetListener {
        private final CallbackContext callBackContext;

        private DateSetListener(CallbackContext callbackContext) {
            callBackContext = callbackContext;
        }

        /**
         * Return a string containing the date in the format YYYY/MM/DD
         */
        @Override
        public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
            Date date = new Date(0);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DATE, dayOfMonth);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            String result = String.valueOf(cal.getTimeInMillis());

            callBackContext.success(result);
        }
    }

    private final class TimeSetListener implements OnTimeSetListener {
        private final CallbackContext callBackContext;

        private TimeSetListener(CallbackContext callbackContext) {
            callBackContext = callbackContext;
        }

        /**
         * Return the current date with the time modified as it was set in the time picker.
         */
        @Override
        public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
            Date date = new Date(0);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);

            String result = String.valueOf(cal.getTimeInMillis());

            callBackContext.success(result);
        }
    }

}