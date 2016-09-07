package com.wyx.flexdemo;

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
        new FloatConfig.Builder().setLogCatEnabled(false).setTriggerEnabled(false).setShowLogCatWindow(false).create());
  }
}
