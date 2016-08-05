package com.wyx.flex;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author winney E-mail: weiyixiong@tigerbrokers.com
 * @version 创建时间: 2016/08/05 上午11:29
 */

public class LogCatActivity extends Activity {
  TextView logcat;
  private static Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logcat);

    logcat = (TextView) findViewById(R.id.tv_loginfo);
    startLogCat();
  }

  private void startLogCat() {
    handler = new LogCatHandler(logcat);
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        handler.sendEmptyMessageDelayed(1, 500);
      }
    }, 0, 500);
  }

  static class LogCatHandler extends Handler {
    TextView textView;

    public LogCatHandler(TextView textView) {
      this.textView = textView;
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (msg.what == 0) {
        if (msg.obj != null) {
          ((Runnable) msg.obj).run();
        }
      } else {
        try {
          LogCatUtil.getLogcatInfo();
          textView.setText(LogCatUtil.getCacheLog());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
