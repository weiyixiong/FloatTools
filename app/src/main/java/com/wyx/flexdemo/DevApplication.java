package com.wyx.flexdemo;

import com.wyx.flex.FloatTools;

/**
 * Created by winney on 16/5/11.
 */
public class DevApplication extends android.app.Application {
  @Override public void onCreate() {
    super.onCreate();
    FloatTools.init(this);
  }
}
