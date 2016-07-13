package com.wyx.flex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by winney on 16/5/24.
 */
public class LogCatUtil {
  public static String getLogcatInfo() throws IOException {
    String strLogcatInfo = "";
    try {
      ArrayList<String> commandLine = new ArrayList<String>();
      commandLine.add("logcat");
      commandLine.add("-d");

      //commandLine.add("*:E"); // 过滤所有的错误信息

      ArrayList<String> clearLog = new ArrayList<String>();  //设置命令  logcat -c 清除日志
      clearLog.add("logcat");
      clearLog.add("-c");

      Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line = null;
      while ((line = bufferedReader.readLine()) != null) {
        //Runtime.getRuntime().exec(clearLog.toArray(new String[clearLog.size()]));
        strLogcatInfo = strLogcatInfo + line + "\n";
      }

      bufferedReader.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      //process.destroy();
    }
    return strLogcatInfo;
  }
}
