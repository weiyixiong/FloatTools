package com.wyx.flex;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import java.io.IOException;

/**
 * @author winney E-mail: weiyixiong@tigerbrokers.com
 * @version 创建时间: 2016/08/05 上午11:29
 */

public class LogCatActivity extends Activity {
  TextView logcat;
  private static Handler handler;

  public static final int TIME_SCHEDULE = 1500;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logcat);

    logcat = (TextView) findViewById(R.id.tv_loginfo);
    startLogCat();
  }

  private void startLogCat() {
    handler = new LogCatHandler(logcat);
    handler.sendEmptyMessageDelayed(1, 0);
  }

  static class LogCatHandler extends Handler {
    TextView textView;

    public LogCatHandler(TextView textView) {
      this.textView = textView;
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      try {
        LogCatUtil.getLogcatInfo();
        textView.setText(LogCatUtil.getCacheLog());
        handler.sendEmptyMessageDelayed(1, TIME_SCHEDULE);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
