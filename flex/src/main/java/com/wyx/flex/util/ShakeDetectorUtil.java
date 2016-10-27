package com.wyx.flex.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * @author weiyixiong
 * @version 创建时间: 2016/10/27 15:57
 */

public class ShakeDetectorUtil {
  private static SensorManager mSensorManager;
  private static Sensor mAccelerometer;

  public static void init(Context context) {
    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
  }

  public static void registerListener(ShakeDetector.OnShakeListener OnShakeListener) {
    // ShakeDetector initialization
    ShakeDetector mShakeDetector = new ShakeDetector();
    mShakeDetector.setOnShakeListener(OnShakeListener);
    mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
  }
}
