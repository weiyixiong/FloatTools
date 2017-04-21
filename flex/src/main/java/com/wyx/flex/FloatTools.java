package com.wyx.flex;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Configuration;
import com.activeandroid.TableInfo;
import com.activeandroid.util.SQLiteUtils;
import com.wyx.flex.record.EventInput;
import com.wyx.flex.record.Record;
import com.wyx.flex.record.RecordEvent;
import com.wyx.flex.util.AccessibilityUtil;
import com.wyx.flex.util.FloatConfig;
import com.wyx.flex.util.L;
import com.wyx.flex.util.LogCatUtil;
import com.wyx.flex.util.Navgation;
import com.wyx.flex.util.PrefUtil;
import com.wyx.flex.util.ShakeDetector;
import com.wyx.flex.util.ShakeDetectorUtil;
import com.wyx.flex.util.ViewUtil;
import com.wyx.flex.view.BorderImageView;
import com.wyx.flex.view.DragLayout;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by winney on 16/4/28.
 */
public class FloatTools {

  private static final String TAG = "FloatTools";
  public static final String RESET = "resetAction";

  private static final int RECORDING = 0;
  private static final int STOPPED = 1;
  private static final int INPUTTING = 2;

  private static Application application;
  private static FloatTools instance;
  private static RelativeLayout mFloatLayout;
  private WeakReference<Activity> currentActivity;
  private WeakReference<EditText> currentEditText;

  private FrameLayout touchLayer;
  private WindowManager.LayoutParams wmParams;
  private WindowManager mWindowManager;
  private Button btnDebug;
  private Button btnReset;
  private Button btnHide;
  private Button btnLogcat;
  private Button btnTrigger;
  private Button btnRecord;
  private Button btnReplay;
  private ImageView dragArea;
  private TextView logInfo;
  private ScrollView logCatWrapper;
  private static Handler autoRunControlHandler;

  private PointF touchDownPoint;

  private int floatViewStatus = View.INVISIBLE;
  private int touchLayerStatus = STOPPED;
  private static FloatConfig config = new FloatConfig();

  private static int recordingStatus = STOPPED;
  private static boolean startReplayed = false;

  private static OnActivityResumedListener onActivityResumedListener;

  public interface OnActivityResumedListener {
    void OnActivityResumed();
  }

  public static void init(Application application, @Nullable String dbName) {
    initWithDbName(application, dbName == null ? "Record.db" : dbName);
  }

  public static void initWithDbName(Application application, String dbName) {
    if (Cache.isInitialized()) {
      SQLiteUtils.createTableDefinition(new TableInfo(Record.class));
      SQLiteUtils.createTableDefinition(new TableInfo(RecordEvent.class));
    } else {
      Configuration dbConfiguration = new Configuration.Builder(application).setDatabaseName(dbName)
                                                                            .setModelClasses(Record.class,
                                                                                             RecordEvent.class)
                                                                            .create();
      ActiveAndroid.initialize(dbConfiguration);
    }

    autoRunControlHandler = new Handler();

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
        Record recordById = Record.getRecordById(PrefUtil.getCurrentPlayID());
        EventInput.installRecord(recordById);
        if (!startReplayed && PrefUtil.isPlayRecordOnLaunch()) {
          autoRunControlHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              startReplayed = true;
              EventInput.replay(instance.getCurrentActivity());
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
        if (instance.touchLayerStatus == RECORDING) {
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

  private void setAndDumpActivity(Activity activity) {
    setupActivity(activity);
    createFloatView();
  }

  public static FloatTools getInstance() {
    return instance;
  }

  private void setupActivity(final Activity activity) {
    this.currentActivity = new WeakReference<>(activity);
  }

  public void showFloatTools() {
    if (floatViewStatus == View.INVISIBLE) {
      mWindowManager.addView(mFloatLayout, wmParams);
      floatViewStatus = View.VISIBLE;
    }
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

  public void hideIME(TextView view) {
    Activity activity = this.currentActivity.get();
    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  public void setEditorListener() {
    List<View> allChildViews = ViewUtil.getAllChildViews(this.currentActivity.get());
    for (View allChildView : allChildViews) {
      if (allChildView instanceof EditText) {
        final EditText editText = (EditText) allChildView;
        editText.addTextChangedListener(getWatcher(editText));
        editText.setOnEditorActionListener(getEditorActionListener());
      }
    }
    touchLayerStatus = INPUTTING;
    updateRecordBthText();
  }

  @NonNull
  private EditText.OnEditorActionListener getEditorActionListener() {
    return new EditText.OnEditorActionListener() {
      public boolean onEditorAction(TextView view, int action, KeyEvent event) {
        if (isSoftKeyboardFinishedAction(view, action, event)) {
          completeInput(view);
        }
        return false;
      }
    };
  }

  @NonNull
  private TextWatcher getWatcher(final EditText editText) {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (touchLayerStatus == STOPPED) {
          return;
        }
        if (currentEditText == null || currentEditText.get() != editText) {
          currentEditText = new WeakReference<>(editText);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    };
  }

  private void updateRecordBthText() {
    if (touchLayerStatus == STOPPED) {
      btnRecord.setText("Record");
    } else if (touchLayerStatus == RECORDING) {
      btnRecord.setText("stop");
    } else {
      btnRecord.setText("complete input");
    }
  }

  private void completeInput() {
    if (this.currentEditText == null) {
      return;
    }
    EditText view = this.currentEditText.get();
    completeInput(view);
  }

  private void completeInput(TextView view) {
    hideIME(view);
    if (view.getId() == View.NO_ID) {
      Rect rect = new Rect();
      view.getGlobalVisibleRect(rect);
      EventInput.recordEditEvent(rect.left + 1, rect.top + 1, view.getText().toString());
    } else {
      String viewId = getCurrentActivity().getResources().getResourceName(view.getId());
      EventInput.recordEditEvent(viewId, view.getText().toString());
    }
    startRecording();
  }

  public void startRecording() {
    Activity activity = getCurrentActivity();
    if (touchLayer == null) {
      touchLayer = new FrameLayout(activity.getApplicationContext());
      touchLayer.setBackgroundColor(Color.TRANSPARENT);
      touchLayer.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
          //解决触摸传递的误差问题
          MotionEvent ev =
              EventInput.buildEvent(motionEvent.getAction(), motionEvent.getEventTime(), motionEvent.getRawX(),
                                    motionEvent.getRawY(), motionEvent.getPressure());
          currentActivity.get().dispatchTouchEvent(ev);
          EventInput.recordMotionEvent(motionEvent);
          return false;
        }
      });
    }
    if (!AccessibilityUtil.isAccessibilitySettingsOn(activity)) {
      AccessibilityUtil.openSetting(activity);
    } else {
      if (touchLayerStatus == STOPPED || touchLayerStatus == INPUTTING) {
        //make floatTools on the touchLayer
        hideFloatTools();
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;

        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowManager.addView(touchLayer, wmParams);
        showFloatTools();
        touchLayerStatus = RECORDING;
        EventInput.setStartActivityName(activity.getClass().getName());
        Toast.makeText(this.currentActivity.get(), "started", Toast.LENGTH_SHORT).show();
        updateRecordBthText();
      }
    }
  }

  public void stopRecording() {
    if (touchLayerStatus == RECORDING) {
      mWindowManager.removeView(touchLayer);
      touchLayerStatus = STOPPED;
      updateRecordBthText();
      Activity context = this.currentActivity.get();
      Toast.makeText(context, "stopped", Toast.LENGTH_SHORT).show();
    }
  }

  private Activity showInputDialog() {
    Activity context = this.currentActivity.get();
    WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
    wmParams.format = PixelFormat.RGBA_8888;
    wmParams.gravity = Gravity.CENTER;
    wmParams.x = 0;
    wmParams.y = 0;
    wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    LayoutInflater inflater = LayoutInflater.from(context);
    final ViewGroup inputDialog = (ViewGroup) inflater.inflate(R.layout.dialog_name_input, null);
    inputDialog.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    Button cancel = (Button) inputDialog.findViewById(R.id.btn_cancel);
    Button ok = (Button) inputDialog.findViewById(R.id.btn_ok);
    final EditText recordName = (EditText) inputDialog.findViewById(R.id.text_record_name);

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
    return context;
  }

  public void startInputting() {
    if (touchLayerStatus == RECORDING) {
      stopRecording();
      setEditorListener();
    }
  }

  public static boolean isSoftKeyboardFinishedAction(TextView view, int action, KeyEvent event) {
    // Some devices return null event on editor actions for Enter Button
    return (action == EditorInfo.IME_ACTION_DONE || action == EditorInfo.IME_ACTION_GO ||
        action == EditorInfo.IME_ACTION_SEND) && (event == null || event.getAction() == KeyEvent.ACTION_DOWN);
  }

  private void initFloatView(final Activity activity) {
    wmParams = new WindowManager.LayoutParams();
    mWindowManager = (WindowManager) activity.getApplication().getSystemService(Context.WINDOW_SERVICE);
    wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
    wmParams.format = PixelFormat.RGBA_8888;
    wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    wmParams.gravity = Gravity.LEFT | Gravity.TOP;
    wmParams.x = 0;
    wmParams.y = 0;

    wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
    wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

    LayoutInflater inflater = LayoutInflater.from(activity.getApplication());
    mFloatLayout = (RelativeLayout) inflater.inflate(R.layout.float_tool_bar, null);
    btnDebug = (Button) mFloatLayout.findViewById(R.id.btn_debug);
    btnReset = (Button) mFloatLayout.findViewById(R.id.btn_reset);
    btnHide = (Button) mFloatLayout.findViewById(R.id.btn_hide);
    btnLogcat = (Button) mFloatLayout.findViewById(R.id.btn_logcat);
    btnTrigger = (Button) mFloatLayout.findViewById(R.id.btn_trigger_event);
    btnRecord = (Button) mFloatLayout.findViewById(R.id.btn_record);
    btnReplay = (Button) mFloatLayout.findViewById(R.id.btn_replay);
    dragArea = (ImageView) mFloatLayout.findViewById(R.id.drag_area);
    logInfo = (TextView) mFloatLayout.findViewById(R.id.tv_loginfo);
    logCatWrapper = (ScrollView) mFloatLayout.findViewById(R.id.layout_loginfo_wrapper);

    initWithConfig();

    btnLogcat.setOnClickListener(floatButtonsOnClickListener);
    logInfo.setOnClickListener(floatButtonsOnClickListener);
    btnHide.setOnClickListener(floatButtonsOnClickListener);
    btnRecord.setOnClickListener(floatButtonsOnClickListener);
    btnDebug.setOnClickListener(floatButtonsOnClickListener);
    btnReset.setOnClickListener(floatButtonsOnClickListener);
    btnReplay.setOnClickListener(floatButtonsOnClickListener);
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

          L.e(wmParams.x + "  " + x + " " + touchDownPoint.x);
          touchDownPoint.set(x, y);
        }
        return false;
      }
    });
    mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                         View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
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
      } else if (viewId == R.id.btn_reset) {
        onClickReset();
      }
    }

    private void onCLickDebug() {
      Activity activity = getCurrentActivity();
      ViewUtil.dumpView(activity);
      ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
      DragLayout parent = (DragLayout) root.findViewWithTag(TAG);
      if (parent == null) {
        parent = new DragLayout(activity);
        parent.setTag(TAG);
      }
      parent.removeAllViews();
      root.removeView(parent);
      parent.setBackgroundColor(Color.WHITE);
      parent.removeAllViews();
      addFakeView(activity, parent, root, 0, 0);
      root.addView(parent);
    }

    private void onClickReset() {
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
      FrameLayout parent = (FrameLayout) root.findViewWithTag(TAG);
      parent.setBackgroundColor(Color.WHITE);
      parent.removeAllViews();
      root.removeView(parent);
    }

    private void onClickReplay() {
      EventInput.replay(getCurrentActivity());
    }

    private void onClickRecord() {
      if (touchLayerStatus == RECORDING) {
        stopRecording();
        showInputDialog();
      } else if (touchLayerStatus == STOPPED) {
        startRecording();
      } else {
        completeInput();
      }
    }

    private void onClickHide() {
      hideFloatTools();
      btnReset.performClick();
    }

    private void onClickLogInfo() {
      Navgation.startLogCatActivity(getCurrentActivity());
    }

    private void onClickLogCat() {
      if (logInfo.isShown()) {
        logCatWrapper.setVisibility(View.GONE);
      } else {
        logCatWrapper.setVisibility(View.VISIBLE);
      }
    }
  };

  private void initWithConfig() {
    if (config.isLogCatEnabled()) {
      btnLogcat.setVisibility(View.VISIBLE);
      startLogCat();
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
    getCurrentActivity().dispatchTouchEvent(event);
  }

  public void onEdit(RecordEvent event) {
    Activity activity = this.currentActivity.get();
    if (event.getResName() != null) {
      int identifier = activity.getResources().getIdentifier(event.getResName(), null, null);
      EditText editText = (EditText) activity.findViewById(identifier);
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

  private void startLogCat() {
    LogCatUtil.addUpdateListener(new LogCatUtil.LogcatUpdateListener() {
      @Override
      public void onUpdate(final String log) {
        logInfo.post(new Runnable() {
          @Override
          public void run() {
            logInfo.setText(log);
          }
        });
      }
    });
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
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
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
