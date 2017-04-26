package com.wyx.flex.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.wyx.flex.R;
import com.wyx.flex.util.LogCatUtil;

/**
 * @author winney E-mail: weiyixiong@tigerbrokers.com
 * @version 创建时间: 2016/08/05 上午11:29
 */

public class LogCatActivity extends AppCompatActivity {
  TextView logcat;
  LogCatUtil.LogcatUpdateListener logcatUpdateListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logcat);

    logcat = (TextView) findViewById(R.id.tv_loginfo);
    logcatUpdateListener = new LogCatUtil.LogcatUpdateListener() {
      @Override
      public void onUpdate(final String log) {
        logcat.post(new Runnable() {
          @Override
          public void run() {
            logcat.setText(log);
          }
        });
      }
    };
    startLogCat();
  }

  @Override
  protected void onPause() {
    LogCatUtil.removeUpdateListener(logcatUpdateListener);
    super.onPause();
  }

  private void startLogCat() {
    LogCatUtil.addUpdateListener(logcatUpdateListener);
  }
}
