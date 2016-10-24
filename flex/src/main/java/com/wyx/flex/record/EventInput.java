package com.wyx.flex.record;

import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.InputDeviceCompat;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.wyx.flex.FloatTools;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by omerjerk on 19/9/15.
 *
 * Class to create seamless input/touch events on your Android device without root
 */
public class EventInput {

  private Method injectInputEventMethod;
  private InputManager im;
  private ArrayList<RecordEvent> record = new ArrayList<>();
  private ReplayHandler handler;
  private static final int TOUCH = 0;
  private static final int INPUT = 1;

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

  private long getCurrentTime() {
    return SystemClock.uptimeMillis();
  }

  public void recordMotionEvent(MotionEvent event) {
    record.add(new RecordEvent(MotionEvent.obtain(event), getCurrentTime()));
  }

  public void recordEditEvent(float x, float y, String text) {
    record.add(new RecordEvent(text, x, y, getCurrentTime()));
  }

  public void recordEditEvent(String viewId, String s) {
    record.add(new RecordEvent(viewId, s, getCurrentTime()));
  }

  public void replay() {
    handler = new ReplayHandler();
    long startTime = getCurrentTime();
    for (int i = 0; i < record.size(); i++) {
      long timeDiff = 0;
      if (i != 0) {
        timeDiff = record.get(i).getTime() - record.get(0).getTime();
      }
      RecordEvent event = record.get(i);
      if (event.getType() == EventType.TOUCH) {
        MotionEvent motionEvent = event.getEvent();
        MotionEvent event1 =
            buildEvent(motionEvent.getAction(), startTime + timeDiff, motionEvent.getRawX(), motionEvent.getRawY(),
                       1.0f);
        handler.sendMessageDelayed(handler.obtainMessage(TOUCH, event1), timeDiff);
      } else {
        handler.sendMessageDelayed(handler.obtainMessage(INPUT, event), timeDiff);
      }
    }
  }

  public MotionEvent buildEvent(int action, long when, float x, float y, float pressure) {
    MotionEvent obtain = MotionEvent.obtain(when, when, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
    obtain.setSource(InputDeviceCompat.SOURCE_TOUCHSCREEN);
    return obtain;
  }

  private void injectKeyEvent(KeyEvent event) throws InvocationTargetException, IllegalAccessException {
    injectInputEventMethod.invoke(im, new Object[] { event, Integer.valueOf(0) });
  }

  public void clear() {
    record.clear();
  }

  public void saveRecord(String name) {
    Record record = new Record();
    record.time = SystemClock.uptimeMillis();
    record.name=name;
    Long recordId = record.save();
    for (RecordEvent recordEvent : this.record) {
      recordEvent.recordId = recordId;
      recordEvent.save();
    }
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