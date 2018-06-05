package com.wyx.flex.Collection;

import android.app.Activity;

/**
 * @author weiyixiong
 * @version 创建时间: 2018/06/01 17:10
 */
public class EventCollection {
  protected AppModel appModel;

  public EventCollection(AppModel appModel) {
    this.appModel = appModel;
  }

  public Activity getCurrentActivity() {
    return this.appModel.getCurrentActivity();
  }
}
