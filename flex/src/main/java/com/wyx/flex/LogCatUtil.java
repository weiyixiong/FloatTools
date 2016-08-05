package com.wyx.flex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by winney on 16/5/24.
 */
public class LogCatUtil {
  private static final int LIMIT_LINE = 100;

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
}
