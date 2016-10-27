package com.wyx.flex.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author weiyixong
 * @version 创建时间: 2016/10/27 15:34
 */

public class PrefUtil {
  private static final String RECORD_PREFERENCE = "record_preference";

  private static final String PLAY_RECORD_ON_LAUNCH = "play_record_on_launch";
  private static final String CURRENT_RECORD_ID = "current_record_id";
  private static Context context;

  public static void init(Context contextParams) {
    context = contextParams;
  }

  public static void setPlayRecordOnLaunch(boolean playRecordOnLaunch, long recordID) {
    SharedPreferences preference = getSharedPreferences();
    SharedPreferences.Editor editor = preference.edit();
    editor.putBoolean(PLAY_RECORD_ON_LAUNCH, playRecordOnLaunch);
    editor.putLong(CURRENT_RECORD_ID, recordID);
    editor.apply();
  }

  public static long getCurrentPlayID() {
    return getSharedPreferences().getLong(CURRENT_RECORD_ID, -1);
  }

  private static SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences(RECORD_PREFERENCE, Context.MODE_PRIVATE);
  }

  public static boolean isPlayRecordOnLaunch() {
    return getSharedPreferences().getBoolean(PLAY_RECORD_ON_LAUNCH, false);
  }
}