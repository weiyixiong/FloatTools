package com.wyx.flex.Collection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.wyx.flex.record.EventInput;
import com.wyx.flex.util.ReflectionUtil;
import com.wyx.flex.util.StatusBarHeightUtil;
import com.wyx.flex.util.ViewUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 触摸事件收集层
 *
 * @author weiyixiong
 * @version 创建时间: 2018/06/01 16:38
 */
public class TouchEventCollection extends EventCollection {
  private static final int COLLECT_TOUCH = 0;
  private static final int STOPPED = 1;

  private FrameLayout touchLayer;
  /**
   * 当软键盘弹出时调整收集层的高度
   */
  private final ViewTreeObserver.OnGlobalLayoutListener adjustLayerByKeyBoardHeight =
      new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
          if (touchLayer == null || touchLayer.getParent() == null) {
            return;
          }
          final InputMethodManager systemService =
              (InputMethodManager) appModel.getCurrentActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
          final Method getInputMethodWindowVisibleHeight =
              ReflectionUtil.getMethod("getInputMethodWindowVisibleHeight", InputMethodManager.class);
          try {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            final ViewGroup.LayoutParams layoutParams = touchLayer.getLayoutParams();
            final int keyboardHeight;
            if (getInputMethodWindowVisibleHeight == null) {
              return;
            }
            keyboardHeight = (int) getInputMethodWindowVisibleHeight.invoke(systemService);
            layoutParams.height = displaymetrics.heightPixels - keyboardHeight -
                StatusBarHeightUtil.getStatusBarHeight(appModel.getCurrentActivity());
            getWindowManager().updateViewLayout(touchLayer, layoutParams);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      };
  private int touchLayerStatus = STOPPED;

  @SuppressLint("ClickableViewAccessibility")
  public TouchEventCollection(AppModel appModel) {
    super(appModel);
    touchLayer = new FrameLayout(appModel.getCurrentActivity());
    touchLayer.setBackgroundColor(Color.TRANSPARENT);
    touchLayer.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        //解决触摸传递的误差问题
        MotionEvent ev =
            EventInput.buildEvent(motionEvent.getAction(), motionEvent.getEventTime(), motionEvent.getRawX(),
                                  motionEvent.getRawY(), motionEvent.getPressure());
        TouchEventCollection.this.appModel.dispatchTouchEvents(ev);
        EventInput.recordMotionEvent(ev);
        return false;
      }
    });
  }

  public View getLayer() {
    return touchLayer;
  }

  public boolean isCollecting() {
    return touchLayerStatus != STOPPED;
  }

  public void startCollectEvent() {
    WindowManager.LayoutParams wmParams = ViewUtil.createWindowLayoutParams(Gravity.START | Gravity.TOP);
    wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
    wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    getWindowManager().addView(touchLayer, wmParams);
    touchLayerStatus = COLLECT_TOUCH;
    Toast.makeText(appModel.getCurrentActivity(), "started", Toast.LENGTH_SHORT).show();
  }

  public void stopCollectEvent() {
    if (touchLayer.getParent() != null) {
      getWindowManager().removeView(touchLayer);
    }
    touchLayerStatus = STOPPED;
  }

  private WindowManager getWindowManager() {
    return (WindowManager) appModel.getCurrentActivity().getApplication().getSystemService(Context.WINDOW_SERVICE);
  }

  public void enterNewActivity() {
    appModel.getCurrentActivity()
            .getWindow()
            .getDecorView()
            .getViewTreeObserver()
            .addOnGlobalLayoutListener(adjustLayerByKeyBoardHeight);
  }
}
