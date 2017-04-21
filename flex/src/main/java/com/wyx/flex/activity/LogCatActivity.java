package com.wyx.flex.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.wyx.flex.R;
import com.wyx.flex.util.LogCatUtil;

/**
 * @author winney E-mail: weiyixiong@tigerbrokers.com
 * @version 创建时间: 2016/08/05 上午11:29
 */

public class LogCatActivity extends AppCompatActivity {
  TextView logcat;
  Spinner logType;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logcat);

    logcat = (TextView) findViewById(R.id.tv_loginfo);
    logType = (Spinner) findViewById(R.id.spinner_log_type);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
    adapter.addAll("debug", "error", "warning", "verbose");
    logType.setAdapter(adapter);
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
