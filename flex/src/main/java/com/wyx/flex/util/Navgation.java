package com.wyx.flex.util;

import android.app.Activity;
import android.content.Intent;
import com.wyx.flex.activity.DetailInfoActivity;
import com.wyx.flex.activity.LogCatActivity;

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
}
