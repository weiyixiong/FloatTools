package com.wyx.flex;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.v4.view.InputDeviceCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.activeandroidlib.ActiveAndroid;
import com.activeandroidlib.Configuration;
import com.wyx.flex.Collection.AppModel;
import com.wyx.flex.Collection.EditEventCollection;
import com.wyx.flex.Collection.TouchEventCollection;
import com.wyx.flex.record.EventInput;
import com.wyx.flex.record.Record;
import com.wyx.flex.record.RecordEvent;
import com.wyx.flex.util.AccessibilityUtil;
import com.wyx.flex.util.FloatConfig;
import com.wyx.flex.util.L;
import com.wyx.flex.util.LogCatUtil;
import com.wyx.flex.util.Navgation;
import com.wyx.flex.util.PrefUtil;
import com.wyx.flex.util.ReflectionUtil;
import com.wyx.flex.util.ShakeDetector;
import com.wyx.flex.util.ShakeDetectorUtil;
import com.wyx.flex.util.ViewUtil;
import com.wyx.flex.view.BorderImageView;
import com.wyx.flex.view.DragLayout;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by winney on 16/4/28.
 */
public class FloatTools implements AppModel {

  public static final String TAG = "FloatTools";
  public static final String RESET = "resetAction";

  private static final int RECORDING = 0;
  private static final int STOPPED = 1;

  private static Application application;
  private static FloatTools instance;
  private static RelativeLayout mFloatLayout;
  private WeakReference<Activity> currentActivity;

  private TouchEventCollection touchEventCollection;
  private EditEventCollection editEventCollection;
  private WindowManager.LayoutParams wmParams;
  private WindowManager mWindowManager;
  private Button btnDebug;
  private Button btnHide;
  private Button btnLogcat;
  private Button btnTrigger;
  private Button btnRecord;
  private Button btnReplay;
  private TextView logInfo;
  private ScrollView logCatWrapper;
  private static Handler autoRunControlHandler;
  private LogCatUtil.LogcatUpdateListener logcatUpdateListener;
  private PointF touchDownPoint;

  private int floatViewStatus = View.INVISIBLE;

  private static FloatConfig config = new FloatConfig();
  private static int recordingStatus = STOPPED;
  private static boolean startReplayed = false;

  private static OnActivityResumedListener onActivityResumedListener;

  public interface OnActivityResumedListener {
    void OnActivityResumed();
  }

  private View.OnClickListener floatButtonsOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      int viewId = v.getId();
      if (viewId == R.id.btn_logcat) {
        onClickLogCat();
      } else if (viewId == R.id.tv_loginfo) {
        onClickLogInfo();
      } else if (viewId == R.id.btn_hide) {
        onClickHide();
      } else if (viewId == R.id.btn_record) {
        onClickRecord();
      } else if (viewId == R.id.btn_replay) {
        onClickReplay();
      } else if (viewId == R.id.btn_debug) {
        onCLickDebug();
      }
    }

    private void onCLickDebug() {
      Activity activity = getCurrentActivity();
      ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
      DragLayout parent = root.findViewWithTag(TAG);
      if (parent == null) {
        parent = new DragLayout(activity);
        parent.setTag(TAG);
      }
      if (parent.getChildCount() != 0) {
        resetDebug();
        btnDebug.setText(R.string.text_debug);
        return;
      }
      btnDebug.setText(R.string.text_reset);
      parent.removeAllViews();
      root.removeView(parent);
      parent.setBackgroundColor(Color.WHITE);
      parent.removeAllViews();
      try {
        final ArrayList<View> views = ViewUtil.getActivityAllViews(mWindowManager);
        for (View view : views) {
          if (view == mFloatLayout) {
            continue;
          }
          ViewUtil.dumpView((ViewGroup) view, activity);
          addFakeView(activity, parent, (ViewGroup) view, 0, 0);
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
      root.addView(parent);
    }

    private void resetDebug() {
      Activity activity = getCurrentActivity();
      Intent intent = new Intent();
      intent.setAction(RESET);
      application.sendBroadcast(intent);

      ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
      if (root.findViewWithTag(TAG) == null) {
        FrameLayout parent = new FrameLayout(activity);
        parent.setTag(TAG);
        root.addView(parent);
      }
      FrameLayout parent = root.findViewWithTag(TAG);
      parent.setBackgroundColor(Color.WHITE);
      parent.removeAllViews();
      root.removeView(parent);
    }

    private void onClickReplay() {
      EventInput.replay(getCurrentActivity());
    }

    private void onClickRecord() {
      Activity activity = getCurrentActivity();
      if (!AccessibilityUtil.isAccessibilitySettingsOn(activity)) {
        AccessibilityUtil.openSetting(activity);
        return;
      }
      if (touchEventCollection.isCollecting()) {
        stopRecording();
        showInputDialog();
        ViewUtil.showViews(btnDebug, btnHide, btnLogcat, btnReplay, btnTrigger);
        updateViewVisible();
      } else {
        EventInput.setStartActivityName(activity.getClass().getName());
        startRecording();
        ViewUtil.hideViews(btnDebug, btnHide, btnLogcat, btnReplay, btnTrigger);
      }
    }

    private void onClickHide() {
      hideFloatTools();
      resetDebug();
    }

    private void onClickLogInfo() {
      onClickLogCat();
      Navgation.startLogCatActivity(getCurrentActivity());
    }

    private void onClickLogCat() {
      if (logInfo.isShown()) {
        logCatWrapper.setVisibility(View.GONE);
        LogCatUtil.removeUpdateListener(logcatUpdateListener);
      } else {
        logCatWrapper.setVisibility(View.VISIBLE);
        LogCatUtil.addUpdateListener(logcatUpdateListener);
      }
    }
  };

  public static void init(Application application) {
    Configuration dbConfiguration = new Configuration.Builder(application).setDatabaseName("Record.db")
                                                                          .setModelClasses(Record.class,
                                                                                           RecordEvent.class)
                                                                          .create();
    autoRunControlHandler = new Handler();
    ActiveAndroid.initialize(dbConfiguration);
    PrefUtil.init(application);
    ShakeDetectorUtil.init(application);
    FloatTools.application = application;
    instance = new FloatTools();
    application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

      }

      @Override
      public void onActivityStarted(Activity activity) {

      }

      @Override
      public void onActivityResumed(Activity activity) {
        instance.setAndDumpActivity(activity);
        if (recordingStatus == RECORDING) {
          instance.installLayer(activity);
        }
        if (!startReplayed && PrefUtil.isPlayRecordOnLaunch()) {
          Record recordById = Record.getRecordById(PrefUtil.getCurrentPlayID());
          EventInput.installRecord(recordById);

          autoRunControlHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                @Override
                public boolean queueIdle() {
                  startReplayed = true;
                  EventInput.replay(instance.getCurrentActivity());
                  return false;
                }
              });
            }
          }, 3000);
        }
        if (onActivityResumedListener != null) {
          onActivityResumedListener.OnActivityResumed();
          onActivityResumedListener = null;
        }
      }

      @Override
      public void onActivityPaused(Activity activity) {
        if (instance.touchEventCollection.isCollecting()) {
          recordingStatus = RECORDING;
          instance.hideFloatTools();
          instance.stopRecording();
        } else {
          recordingStatus = STOPPED;
        }
        if (autoRunControlHandler.hasMessages(0)) {
          autoRunControlHandler.removeCallbacksAndMessages(0);
        }
      }

      @Override
      public void onActivityStopped(Activity activity) {

      }

      @Override
      public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

      }

      @Override
      public void onActivityDestroyed(Activity activity) {

      }
    });
  }

  public static FloatTools getInstance() {
    return instance;
  }

  private void setAndDumpActivity(Activity activity) {
    this.currentActivity = new WeakReference<>(activity);
    if (touchEventCollection == null) {
      this.editEventCollection = new EditEventCollection(FloatTools.this);
      this.touchEventCollection = new TouchEventCollection(FloatTools.this);
    }
    createFloatView();
    touchEventCollection.enterNewActivity();
  }

  private void createFloatView() {
    Activity activity = this.currentActivity.get();
    if (activity == null || mFloatLayout != null) {
      return;
    }
    initFloatView(activity);
    ShakeDetectorUtil.registerListener(new ShakeDetector.OnShakeListener() {
      @Override
      public void onShake(int count) {
        showFloatTools();
      }
    });
    if (config.isStartOnLaunch()) {
      showFloatTools();
    }
  }

  private void installLayer(final Activity activity) {
    if (!AccessibilityUtil.isAccessibilitySettingsOn(activity)) {
      AccessibilityUtil.openSetting(activity);
    } else {
      startRecording();
    }
  }

  private void showFloatTools() {
    if (floatViewStatus == View.INVISIBLE) {
      mWindowManager.addView(mFloatLayout, wmParams);
      floatViewStatus = View.VISIBLE;
    }
  }

  private void updateRecordBthText() {
    if (!touchEventCollection.isCollecting()) {
      btnRecord.setText(R.string.text_record);
    } else {
      btnRecord.setText(R.string.text_stop);
    }
  }

  public void completeInput(boolean hideIME, boolean completeRecord) {
    if (!editEventCollection.isEditText()) {
      return;
    }
    if (hideIME) {
      editEventCollection.hideSoftKeyBoard();
    }
    editEventCollection.recordInputEvent();
    if (!completeRecord) {
      startRecording();
    }
  }

  private Rect touchArea = new Rect();

  @SuppressLint("ClickableViewAccessibility")
  private void startRecording() {
    if (!touchEventCollection.isCollecting()) {
      //make floatTools above the touchLayer
      hideFloatTools();
      touchEventCollection.startCollectEvent();
      updateRecordBthText();
      showFloatTools();
    }
  }

  public void dispatchTouchEvents(MotionEvent ev) {
    try {
      int rawX = (int) ev.getRawX();
      int rawY = (int) ev.getRawY();
      final ArrayList<View> views = ViewUtil.getActivityAllViews(mWindowManager);
      final Class<?> viewRootImpl = ReflectionUtil.getClass("android.view.ViewRootImpl");
      final Field mWinFrame = ReflectionUtil.getField(viewRootImpl, "mWinFrame", Rect.class);
      for (int i = views.size() - 1; i >= 0; i--) {
        final View view = views.get(i);
        final String name = view.getClass().getName();
        if (isPhoneWindowOrPopupWindow(name)) {
          final Rect rect = (Rect) mWinFrame.get(view.getParent());
          MotionEvent obtain =
              MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), ev.getAction(), rawX - rect.left, rawY - rect.top,
                                 1.0f, 1.0f, 0, 1.0f, 1.0f, 0, 0);
          obtain.setSource(InputDeviceCompat.SOURCE_TOUCHSCREEN);
          ev = obtain;
        }
        view.getGlobalVisibleRect(touchArea);
        rawX = (int) ev.getRawX();
        rawY = (int) ev.getRawY();

        if (editEventCollection.isEditText()) {
          completeInput(false, false);
        }
        if (view != touchEventCollection.getLayer() && view != mFloatLayout && touchArea.contains(rawX, rawY)) {
          view.dispatchTouchEvent(ev);
          break;
        }
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private boolean isPhoneWindowOrPopupWindow(String name) {
    return name.equals("com.android.internal.policy.PhoneWindow$DecorView") ||
        name.equals("android.widget.PopupWindow$PopupDecorView");
  }

  @Override
  public boolean isCollectingTouch() {
    return touchEventCollection.isCollecting();
  }

  private void stopRecording() {
    if (touchEventCollection.isCollecting()) {
      touchEventCollection.stopCollectEvent();
      completeInput(true, true);
      updateRecordBthText();
    }
  }

  private void showInputDialog() {
    Activity context = this.currentActivity.get();
    WindowManager.LayoutParams wmParams = ViewUtil.createWindowLayoutParams(Gravity.CENTER);
    wmParams.flags = 0;
    LayoutInflater inflater = LayoutInflater.from(context);
    final ViewGroup inputDialog = (ViewGroup) inflater.inflate(R.layout.dialog_name_input, null);
    inputDialog.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    Button cancel = inputDialog.findViewById(R.id.btn_cancel);
    Button ok = inputDialog.findViewById(R.id.btn_ok);
    final EditText recordName = inputDialog.findViewById(R.id.text_record_name);
    recordName.setContentDescription(TAG);
    cancel.setContentDescription(TAG);
    ok.setContentDescription(TAG);

    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mWindowManager.removeView(inputDialog);
      }
    });
    ok.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        EventInput.saveRecord(recordName.getText().toString());
        mWindowManager.removeView(inputDialog);
      }
    });
    mWindowManager.addView(inputDialog, wmParams);
  }

  public void startInput() {
    if (touchEventCollection.isCollecting()) {
      editEventCollection.setEditorListener();
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private void initFloatView(final Activity activity) {
    wmParams = ViewUtil.createWindowLayoutParams(Gravity.START | Gravity.TOP);
    mWindowManager = (WindowManager) activity.getApplication().getSystemService(Context.WINDOW_SERVICE);

    LayoutInflater inflater = LayoutInflater.from(activity.getApplication());
    mFloatLayout = (RelativeLayout) inflater.inflate(R.layout.float_tool_bar, null);
    btnDebug = mFloatLayout.findViewById(R.id.btn_debug);
    btnHide = mFloatLayout.findViewById(R.id.btn_hide);
    btnLogcat = mFloatLayout.findViewById(R.id.btn_logcat);
    btnTrigger = mFloatLayout.findViewById(R.id.btn_trigger_event);
    btnRecord = mFloatLayout.findViewById(R.id.btn_record);
    btnReplay = mFloatLayout.findViewById(R.id.btn_replay);
    ImageView dragArea = mFloatLayout.findViewById(R.id.drag_area);
    logInfo = mFloatLayout.findViewById(R.id.tv_loginfo);
    logCatWrapper = mFloatLayout.findViewById(R.id.layout_loginfo_wrapper);

    updateViewVisible();

    btnLogcat.setOnClickListener(floatButtonsOnClickListener);
    logInfo.setOnClickListener(floatButtonsOnClickListener);
    btnHide.setOnClickListener(floatButtonsOnClickListener);
    btnRecord.setOnClickListener(floatButtonsOnClickListener);
    btnDebug.setOnClickListener(floatButtonsOnClickListener);
    btnReplay.setOnClickListener(floatButtonsOnClickListener);
    logcatUpdateListener = new LogCatUtil.LogcatUpdateListener() {
      @Override
      public void onUpdate(final String log) {
        logInfo.post(new Runnable() {
          @Override
          public void run() {
            logInfo.setText(log);
          }
        });
      }
    };
    btnRecord.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        EventInput.clear();
        Toast.makeText(activity, "cleared", Toast.LENGTH_SHORT).show();
        return true;
      }
    });
    btnReplay.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Navgation.startRecordActivity(getCurrentActivity());
        return true;
      }
    });
    dragArea.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (touchDownPoint == null) {
          touchDownPoint = new PointF();
        }
        float x = event.getRawX();
        float y = event.getRawY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          touchDownPoint.set(x, y);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
          wmParams = (WindowManager.LayoutParams) mFloatLayout.getLayoutParams();
          wmParams.x += (x - touchDownPoint.x);
          wmParams.y += (y - touchDownPoint.y);
          mWindowManager.updateViewLayout(mFloatLayout, wmParams);
          touchDownPoint.set(x, y);
        }
        return false;
      }
    });
    mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                         View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
  }

  private void updateViewVisible() {
    if (config.isLogCatEnabled()) {
      btnLogcat.setVisibility(View.VISIBLE);
    } else {
      btnLogcat.setVisibility(View.GONE);
    }
    if (!config.isShowLogCatWindow()) {
      logCatWrapper.setVisibility(View.GONE);
    } else {
      logCatWrapper.setVisibility(View.VISIBLE);
    }
    if (config.isTriggerEnabled()) {
      btnTrigger.setVisibility(View.VISIBLE);
      btnTrigger.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (triggerEvent != null) {
            triggerEvent.run();
          }
        }
      });
    } else {
      btnTrigger.setVisibility(View.GONE);
    }
  }

  private void hideFloatTools() {
    if (mFloatLayout.getParent() != null) {
      mWindowManager.removeView(mFloatLayout);
    }
    floatViewStatus = View.INVISIBLE;
  }

  public void onTouch(MotionEvent event) {
    dispatchTouchEvents(event);
  }

  public void onEdit(RecordEvent event) {
    Activity activity = this.currentActivity.get();
    if (event.getResName() != null) {
      int identifier = activity.getResources().getIdentifier(event.getResName(), null, null);
      EditText editText = activity.findViewById(identifier);
      if (editText == null) {
        Toast.makeText(activity, "未找到相应的输入框，回放异常终止", Toast.LENGTH_LONG).show();
        return;
      }
      editText.setText(event.getText());
    } else {
      List<View> allChildViews = ViewUtil.getAllChildViews(activity);
      float x = event.getX();
      float y = event.getY();
      for (View item : allChildViews) {
        if (item instanceof EditText) {
          if (ViewUtil.isInside(x, y, item)) {
            ((EditText) item).setText(event.getText());
          }
        }
      }
    }
  }

  private static Runnable triggerEvent;

  public static void setOnActivityResumedListener(OnActivityResumedListener onActivityResumedListener) {
    FloatTools.onActivityResumedListener = onActivityResumedListener;
  }

  public static void setTriggerEvent(Runnable runnable) {
    triggerEvent = runnable;
  }

  public Activity getCurrentActivity() {
    Activity activity = this.currentActivity.get();
    if (activity == null) {
      throw new RuntimeException("have not setup activity or activity had recycled");
    }
    return activity;
  }

  private void addFakeView(final Activity activity, ViewGroup parent, ViewGroup origin, int top, int left) {
    for (int i = 0; i < origin.getChildCount(); i++) {
      final View view = origin.getChildAt(i);
      L.e(view.getClass());
      if (!view.isShown()) {
        continue;
      }
      if (view instanceof ViewGroup) {
        DragLayout frameLayout = new DragLayout(activity);
        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(view.getMeasuredWidth(), view.getMeasuredHeight());
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        params.topMargin = location[1] - top;
        params.leftMargin = location[0] - left;
        frameLayout.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          frameLayout.setBackground(view.getBackground());
        }
        parent.addView(frameLayout);
        addFakeView(activity, frameLayout, (ViewGroup) view, location[1], location[0]);
        frameLayout.setOnLongClickListener(new View.OnLongClickListener() {
          @Override
          public boolean onLongClick(View v) {
            Navgation.startViewDetailActivity(activity, view.hashCode());
            return true;
          }
        });
      } else {
        BorderImageView tmp = new BorderImageView(activity);
        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (view.getMeasuredWidth() == 0 || view.getMeasuredHeight() == 0) {
          continue;
        }
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_4444);
        view.draw(new Canvas(bitmap));
        tmp.setImageBitmap(bitmap);
        parent.addView(tmp);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        params.topMargin = location[1] - top;
        params.leftMargin = location[0] - left;
        tmp.setLayoutParams(params);
        tmp.setOnLongClickListener(new View.OnLongClickListener() {
          @Override
          public boolean onLongClick(View v) {
            Navgation.startViewDetailActivity(activity, view.hashCode());
            return true;
          }
        });
      }
    }
  }

  public static void setDebug(boolean debug) {
    L.setDebug(debug);
  }

  public static void setConfig(FloatConfig con) {
    config = con;
  }
}
