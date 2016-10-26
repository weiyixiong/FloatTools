package com.wyx.flex.util;

import android.text.TextUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author dell E-mail: dell@tigerbrokers.com
 * @version 创建时间: 2016/10/25 11:36
 */

public class TimeUtils {
  private static final String FORMAT = "MM/dd hh:mm";

  public static String getDate(long milliSeconds) {
    // Create a DateFormatter object for displaying date in specified format.
    SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);

    // Create a calendar object that will convert the date and time value in milliseconds to date.
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(milliSeconds);
    return formatter.format(calendar.getTime());
  }
}
