package com.wyx.flex.util;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

/**
 * @author dell E-mail: dell@tigerbrokers.com
 * @version 创建时间: 2016/10/11 14:26
 */

public class AccessibilityUtil {
  final static String TAG = "AccessibilityUtil";

  // 此方法用来判断当前应用的辅助功能服务是否开启
  public static boolean isAccessibilitySettingsOn(Context context) {
    int accessibilityEnabled = 0;
    try {
      accessibilityEnabled =
          Settings.Secure.getInt(context.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
    } catch (Settings.SettingNotFoundException e) {
      Log.i(TAG, e.getMessage());
    }

    if (accessibilityEnabled == 1) {
      String services =
          Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
      if (services != null) {
        return services.toLowerCase().contains(context.getPackageName().toLowerCase());
      }
    }

    return false;
  }

  public static void openSetting(Context context) {
    // 判断辅助功能是否开启
    if (!isAccessibilitySettingsOn(context)) {
      // 引导至辅助功能设置页面
      context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    } else {
      // 执行辅助功能服务相关操作
    }
  }
}
