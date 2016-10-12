package com.wyx.flex.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import java.util.UUID;

/**
 * @author dell E-mail: dell@tigerbrokers.com
 * @version 创建时间: 2016/10/11 10:34
 */

public class KeyBoardWatcher extends InputMethodService {
  BroadcastReceiver receiver;
  String switchAction = "com.wyx.flex." + UUID.randomUUID().toString() + ".SWITCH_KEYBOARD";

  @Override
  public void onCreate() {
    super.onCreate();
    this.receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
      }
    };
    IntentFilter intentFilter = new IntentFilter(switchAction);
    registerReceiver(this.receiver, intentFilter);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    InputConnection inputConnection=getCurrentInputConnection();
    inputConnection.commitText("sdf",1);
    return super.onStartCommand(intent, flags, startId);
  }
}
