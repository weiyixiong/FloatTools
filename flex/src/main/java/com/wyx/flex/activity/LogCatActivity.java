package com.wyx.flex.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.wyx.flex.util.LogCatUtil;
import com.wyx.flex.R;

/**
 * @author winney E-mail: weiyixiong@tigerbrokers.com
 * @version 创建时间: 2016/08/05 上午11:29
 */

public class LogCatActivity extends Activity {
  TextView logcat;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logcat);

    logcat = (TextView) findViewById(R.id.tv_loginfo);
    startLogCat();
  }

  private void startLogCat() {
    LogCatUtil.addUpdateListener(new LogCatUtil.LogcatUpdateListener() {
      @Override
      public void onUpdate(final String log) {
        logcat.post(new Runnable() {
          @Override
          public void run() {
            logcat.setText(log);
          }
        });
      }
    });
  }
}
