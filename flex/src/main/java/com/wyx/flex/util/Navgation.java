package com.wyx.flex.util;

import android.app.Activity;
import android.content.Intent;
import com.wyx.flex.activity.DetailInfoActivity;
import com.wyx.flex.activity.LogCatActivity;
import com.wyx.flex.activity.RecordActivity;
import com.wyx.flex.activity.RecordEditActivity;
import com.wyx.flex.record.Record;
import com.wyx.flex.record.RecordEvent;

/**
 * Created by winney on 16/5/11.
 */
public class Navgation {

  public static void startViewDetailActivity(Activity activity, int hashCode) {
    Intent intent = new Intent(activity, DetailInfoActivity.class);
    DetailInfoActivity.putExtra(intent, hashCode);
    activity.startActivity(intent);
  }

  public static void startLogCatActivity(Activity activity) {
    Intent intent = new Intent(activity, LogCatActivity.class);
    activity.startActivity(intent);
  }

  public static void startRecordActivity(Activity activity) {
    Intent intent = new Intent(activity, RecordActivity.class);
    activity.startActivity(intent);
  }

  public static void startEditEventActivity(Activity activity, long recordId) {
    Intent intent = new Intent(activity, RecordEditActivity.class);
    RecordEditActivity.putRecordId(intent, recordId);
    activity.startActivity(intent);
  }

  public static void startActivity(Activity activity, Class target) {
    if (target == null) {
      return;
    }
    Intent intent = new Intent(activity, target);
    activity.startActivity(intent);
  }
}
