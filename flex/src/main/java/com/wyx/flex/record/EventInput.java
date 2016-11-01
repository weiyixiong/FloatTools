package com.wyx.flex.record;

import android.app.Activity;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.InputDeviceCompat;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.wyx.flex.FloatTools;
import com.wyx.flex.util.Navgation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventInput {

  private Method injectInputEventMethod;
  private InputManager im;
  private static List<RecordEvent> records = new ArrayList<>();
  private static ReplayHandler handler;
  private static final int TOUCH = 0;
  private static final int INPUT = 1;

  private static String startActivity;

  public EventInput() {

  }

  public EventInput(Object o) throws Exception {
    //Get the instance of InputManager class using reflection
    String methodName = "getInstance";
    Object[] objArr = new Object[0];
    im = (InputManager) InputManager.class.getDeclaredMethod(methodName, new Class[0]).invoke(null, objArr);

    //InputMethodManager
    //InputConnection
    //Make MotionEvent.obtain() method accessible
    methodName = "obtain";
    MotionEvent.class.getDeclaredMethod(methodName, new Class[0]).setAccessible(true);

    //Get the reference to injectInputEvent method
    methodName = "injectInputEvent";
    injectInputEventMethod = InputManager.class.getMethod(methodName, new Class[] { InputEvent.class, Integer.TYPE });
  }

  public void injectMotionEvent(int inputSource, int action, long when, float x, float y, float pressure)
      throws InvocationTargetException, IllegalAccessException {
    MotionEvent event = buildEvent(action, when, x, y, pressure);
    event.setSource(inputSource);
    injectInputEventMethod.invoke(im, new Object[] { event, Integer.valueOf(0) });
  }

  private static long getCurrentTime() {
    return SystemClock.uptimeMillis();
  }

  public static void recordMotionEvent(MotionEvent event) {
    records.add(
        new RecordEvent(MotionEvent.obtain(event).getAction(), event.getRawX(), event.getRawY(), getCurrentTime()));
  }

  public static void recordEditEvent(float x, float y, String text) {
    records.add(new RecordEvent(text, x, y, getCurrentTime()));
  }

  public static void recordEditEvent(String viewId, String s) {
    records.add(new RecordEvent(viewId, s, getCurrentTime()));
  }

  public static void replay(Activity context) {
    String startActivity = EventInput.getStartActivity();
    if (startActivity != null && !startActivity.isEmpty() && !context.getClass().getName().equals(startActivity)) {
      FloatTools.setOnActivityResumedListener(new FloatTools.OnActivityResumedListener() {
        @Override
        public void OnActivityResumed() {
          EventInput.replay(2000);
        }
      });
      try {
        Navgation.startActivity(context, Class.forName(startActivity));
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      EventInput.replay(0);
    }
  }

  public static void replay(long delayTime) {
    handler = new ReplayHandler();
    long startTime = getCurrentTime();
    for (int i = 0; i < records.size(); i++) {
      long timeDiff = delayTime;
      if (i != 0) {
        timeDiff = records.get(i).getTime() - records.get(0).getTime() + delayTime;
      }
      RecordEvent event = records.get(i);
      if (event.getType() == EventType.TOUCH) {
        MotionEvent event1 = buildEvent(event.getEventAction(), startTime + timeDiff, event.getX(), event.getY(), 1.0f);
        handler.sendMessageDelayed(handler.obtainMessage(TOUCH, event1), timeDiff);
      } else {
        handler.sendMessageDelayed(handler.obtainMessage(INPUT, event), timeDiff);
      }
    }
  }

  public static MotionEvent buildEvent(int action, long when, float x, float y, float pressure) {
    MotionEvent obtain = MotionEvent.obtain(when, when, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
    obtain.setSource(InputDeviceCompat.SOURCE_TOUCHSCREEN);
    return obtain;
  }

  private void injectKeyEvent(KeyEvent event) throws InvocationTargetException, IllegalAccessException {
    injectInputEventMethod.invoke(im, new Object[] { event, Integer.valueOf(0) });
  }

  public static void clear() {
    records.clear();
  }

  public static void saveRecord(String name) {
    Record record = new Record();
    record.time = System.currentTimeMillis();
    record.name = name;
    record.activityName = getStartActivity();
    Long recordId = record.save();
    for (RecordEvent recordEvent : records) {
      recordEvent.recordId = recordId;
      recordEvent.save();
    }
  }

  public static void installRecord(Record record) {
    if (record == null) {
      return;
    }
    records = RecordEvent.getAllRecordEventByID(record.getId());
    startActivity = record.getActivityName();
  }

  public static void setStartActivityName(String startActivityName) {
    if (startActivity == null) startActivity = startActivityName;
  }

  public static String getStartActivity() {
    return startActivity;
  }

  private static class ReplayHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case TOUCH:
          FloatTools.getInstance().onTouch((MotionEvent) msg.obj);
          break;
        case INPUT:
          FloatTools.getInstance().onEdit((RecordEvent) msg.obj);
          break;
      }
    }
  }
}
