package com.wyx.flex.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author weiyxiong
 * @version 创建时间: 2018/04/03 17:21
 */

public class FIleUtil {

  // Storage Permissions
  private static final int REQUEST_EXTERNAL_STORAGE = 1;
  private static String[] PERMISSIONS_STORAGE = {
      Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
  };

  /**
   * Checks if the app has permission to write to device storage
   *
   * If the app does not has permission then the user will be prompted to grant permissions
   */
  public static void verifyStoragePermissions(Activity activity) {
    // Check if we have write permission
    int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    if (permission != PackageManager.PERMISSION_GRANTED) {
      // We don't have permission so prompt the user
      ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
    }
  }

  public static void saveToSdCard(String content, String path) {
    File saved = new File(path);
    if (!saved.exists()) {
      try {
        saved.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      // BufferedWriter for performance, true to set append to file flag
      BufferedWriter buf = new BufferedWriter(new FileWriter(saved, true));
      buf.append(content);

      buf.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
