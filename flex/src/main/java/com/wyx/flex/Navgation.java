package com.wyx.flex;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by winney on 16/5/11.
 */
public class Navgation {

  public static void startViewDetailActivity(Activity activity, int hashCode) {
    Intent intent = new Intent(activity, DetailInfoActivity.class);
    DetailInfoActivity.putExtra(intent, hashCode);
    activity.startActivity(intent);
  }
}
