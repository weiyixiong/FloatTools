package com.wyx.flex;

import android.os.Handler;
import android.os.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by winney on 16/5/24.
 */
public class LogCatUtil {
  private static final int LIMIT_LINE = 100;
  public static final int TIME_SCHEDULE = 1000;
  static Queue<String> cache = new ArrayDeque<>();

  static ArrayList<String> commandLine = new ArrayList<String>();
  //commandLine.add("*:E"); // 过滤所有的错误信息

  static ArrayList<String> clearLog = new ArrayList<String>();  //设置命令  logcat -c 清除日志

  static {
    commandLine.add("logcat");
    commandLine.add("-d");
    clearLog.add("logcat");
    clearLog.add("-c");
  }

  public static String getLogcatInfo() throws IOException {
    String strLogcatInfo = "";
    try {
      Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = null;
      while (true) {
        line = bufferedReader.readLine();
        if (line == null) {
          break;
        }
        strLogcatInfo = strLogcatInfo + line + "\n";
        if (!cache.contains(line)) {
          cache.add(line);
        }
        if (cache.size() > LIMIT_LINE) {
          cache.poll();
        }
      }
      Runtime.getRuntime().exec(clearLog.toArray(new String[clearLog.size()]));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return strLogcatInfo;
  }

  public static String getCacheLog() {
    StringBuilder res = new StringBuilder();
    for (String s : cache) {
      res.append(s).append("\n");
    }
    return res.toString();
  }

  public static void addUpdateListener(LogcatUpdateListener logcatUpdateListener) {
    if (handler == null) {
      handler = new InnerLogCatHandler();
    }
    if (!handler.hasMessages(1)) {
      handler.sendEmptyMessageDelayed(1, TIME_SCHEDULE);
    }
    logcatUpdateListeners.add(logcatUpdateListener);
  }

  public interface LogcatUpdateListener {
    void onUpdate(String log);
  }

  private static List<LogcatUpdateListener> logcatUpdateListeners = new ArrayList<>();
  private static InnerLogCatHandler handler;

  private static class InnerLogCatHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      try {
        LogCatUtil.getLogcatInfo();
        String cacheLog = LogCatUtil.getCacheLog();
        for (LogcatUpdateListener logcatUpdateListener : logcatUpdateListeners) {
          logcatUpdateListener.onUpdate(cacheLog);
        }
        if (logcatUpdateListeners.size() != 0) {
          sendEmptyMessageDelayed(1, TIME_SCHEDULE);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
