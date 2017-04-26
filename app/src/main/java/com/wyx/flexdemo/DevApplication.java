package com.wyx.flexdemo;

import android.widget.Toast;
import com.wyx.flex.FloatTools;
import com.wyx.flex.util.FloatConfig;

/**
 * Created by winney on 16/5/11.
 */
public class DevApplication extends android.app.Application {
  @Override
  public void onCreate() {
    super.onCreate();

    FloatTools.init(this);
    FloatTools.setConfig(
        new FloatConfig.Builder().setLogCatEnabled(true).setShowLogCatWindow(false).setTriggerEnabled(false).create());
    FloatTools.setTriggerEvent(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DevApplication.this, FloatTools.getInstance().getCurrentActivity().toString(),
                       Toast.LENGTH_SHORT).show();
      }
    });
    FloatTools.setDebug(true);
  }
}
