package com.wyx.flex.util;

import android.content.Context;

public class StatusBarHeightUtil {

  private static final String STATUS_BAR_DEF_PACKAGE = "android";
  private static final String STATUS_BAR_DEF_TYPE = "dimen";
  private static final String STATUS_BAR_NAME = "status_bar_height";
  private static boolean init = false;
  private static int statusBarHeight = 50;

  public static synchronized int getStatusBarHeight(final Context context) {
    if (!init) {
      int resourceId = context.getResources().
          getIdentifier(STATUS_BAR_NAME, STATUS_BAR_DEF_TYPE, STATUS_BAR_DEF_PACKAGE);
      if (resourceId > 0) {
        statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        init = true;
      }
    }

    return statusBarHeight;
  }
}