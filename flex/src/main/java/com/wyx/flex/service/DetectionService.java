package com.wyx.flex.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.wyx.flex.FloatTools;
import com.wyx.flex.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.util.List;

public class DetectionService extends AccessibilityService {

  final static String TAG = "DetectionService";

  static String foregroundPackageName;
  long focusedEditTextId;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY; // 根据需要返回不同的语义值
  }

  @SuppressLint("NewApi")
  private void findAndPerformAction(String text) {
    // 查找当前窗口中包含“安装”文字的按钮
    if (getRootInActiveWindow() == null) return;
    //通过文字找到当前的节点
    List<AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(text);
    for (int i = 0; i < nodes.size(); i++) {
      AccessibilityNodeInfo node = nodes.get(i);
      // 执行按钮点击行为
      if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
      }
    }
  }

  /**
   * 重载辅助功能事件回调函数，对窗口状态变化事件进行处理
   */
  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    final CharSequence contentDescription = event.getContentDescription();
    if (contentDescription != null && contentDescription.equals(FloatTools.TAG)) {
      return;
    }
    switch (event.getEventType()) {
      case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
        foregroundPackageName = event.getPackageName().toString();
        ComponentName cName = new ComponentName(event.getPackageName().toString(), event.getClassName().toString());
        break;
      case AccessibilityEvent.TYPE_VIEW_FOCUSED:
      case AccessibilityEvent.TYPE_VIEW_CLICKED:

        final long viewSourceId = getViewSourceId(event);
        if (focusedEditTextId != 0 && focusedEditTextId != viewSourceId) {
          FloatTools.getInstance().completeInput(false);
        }
        focusedEditTextId = viewSourceId;

        if (event.getClassName().equals("android.widget.EditText")) {
          FloatTools.getInstance().startInput();
        }
        break;
      case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
      case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
      case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
      case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
        break;
      default:
        if (!event.getClassName().equals("android.widget.EditText")) {
          FloatTools.getInstance().completeInput(true);
        }
    }
  }

  private long getViewSourceId(AccessibilityEvent event) {
    final Field mSourceNodeId = ReflectionUtil.getField(event.getSource().getClass(), "mSourceNodeId", long.class);
    try {
      if (mSourceNodeId != null) {
        return (long) mSourceNodeId.get(event.getSource());
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  protected boolean onKeyEvent(KeyEvent event) {
    return super.onKeyEvent(event);
  }

  @Override
  protected boolean onGesture(int gestureId) {
    return super.onGesture(gestureId);
  }

  @Override
  public void onInterrupt() {
  }

  @Override
  protected void onServiceConnected() {
    super.onServiceConnected();
  }
}