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
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.wyx.flex.record.RecordEvent;
import com.wyx.flex.util.AccessibilityUtil;
import com.wyx.flex.record.EventInput;
import com.wyx.flex.util.FloatConfig;
import com.wyx.flex.util.L;
import com.wyx.flex.util.LogCatUtil;
import com.wyx.flex.util.Navgation;
import com.wyx.flex.util.ShakeDetector;
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

  private PointF touchDownPoint;
  private static SensorManager mSensorManager;
  private static Sensor mAccelerometer;
  private ShakeDetector mShakeDetector;
  private int floatViewStatus = View.INVISIBLE;
  private int touchLayerStatus = STOPPED;
  private static FloatConfig config = new FloatConfig();
  private static EventInput eventInput = new EventInput();

  private static int recordingStatus = STOPPED;

  public static void init(Application application) {
    ActiveAndroid.initialize(application);
    FloatTools.application = application;
    instance = new FloatTools();
    mSensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
    final Activity activity = this.currentActivity.get();
    if (activity == null) {
      throw new RuntimeException("have not setup activity or activity had recycled");
    }
    if (floatViewStatus == View.INVISIBLE) {
      mWindowManager.addView(mFloatLayout, wmParams);
      floatViewStatus = View.VISIBLE;
    }
  }

  private void createFloatView() {
    final Activity activity = this.currentActivity.get();
    if (activity == null) {
      return;
    }
    if (mFloatLayout == null) {
      initFloatView(activity);
    }
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
    btnDebug.setOnClickListener(buildDebugClickListener(activity));
    btnReset.setOnClickListener(buildResetClickListener(activity));

    // ShakeDetector initialization
    mShakeDetector = new ShakeDetector();
    mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

      @Override
      public void onShake(int count) {
        showFloatTools();
      }
    });
    if (config.isStartOnLaunch()) {
      showFloatTools();
    }
    mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
  }

  private void installLayer(final Activity activity) {
    if (touchLayer == null) {
      touchLayer = new FrameLayout(activity.getApplicationContext());
      touchLayer.setBackgroundColor(Color.TRANSPARENT);
      touchLayer.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
          currentActivity.get().dispatchTouchEvent(motionEvent);
          eventInput.recordMotionEvent(motionEvent);
          return false;
        }
      });
    }
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
        editText.addTextChangedListener(new TextWatcher() {
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
        });
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
          public boolean onEditorAction(TextView view, int action, KeyEvent event) {
            if (isSoftKeyboardFinishedAction(view, action, event)) {
              completeInput(view);
            }
            return false;
          }
        });
      }
    }
    touchLayerStatus = INPUTTING;
    updateRecordBthText();
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
    EditText view = this.currentEditText.get();
    completeInput(view);
  }

  private void completeInput(TextView view) {
    hideIME(view);
    if (view.getId() == View.NO_ID) {
      Rect rect = new Rect();
      view.getGlobalVisibleRect(rect);
      eventInput.recordEditEvent(rect.left + 1, rect.top + 1, view.getText().toString());
    } else {
      String viewId = this.currentActivity.get().getResources().getResourceName(view.getId());
      eventInput.recordEditEvent(viewId, view.getText().toString());
    }
    startRecording();
  }

  public void startRecording() {
    Activity activity = this.currentActivity.get();
    if (touchLayer == null) {
      touchLayer = new FrameLayout(activity.getApplicationContext());
      touchLayer.setBackgroundColor(Color.TRANSPARENT);
      touchLayer.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
          currentActivity.get().dispatchTouchEvent(motionEvent);
          eventInput.recordMotionEvent(motionEvent);
          return false;
        }
      });
    }
    if (!AccessibilityUtil.isAccessibilitySettingsOn(activity)) {
      AccessibilityUtil.openSetting(activity);
    } else {
      if (touchLayerStatus == STOPPED || touchLayerStatus == INPUTTING) {
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
          eventInput.saveRecord(recordName.getText().toString());
          mWindowManager.removeView(inputDialog);
        }
      });

      mWindowManager.addView(inputDialog, wmParams);
      Toast.makeText(context, "stopped", Toast.LENGTH_SHORT).show();
    }
  }

  public void startInputing() {
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
    mWindowManager =
        (WindowManager) activity.getApplication().getSystemService(activity.getApplication().WINDOW_SERVICE);
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
    btnDebug = (Button) mFloatLayout.findViewById(R.id.button);
    btnReset = (Button) mFloatLayout.findViewById(R.id.reset);
    btnHide = (Button) mFloatLayout.findViewById(R.id.hide);
    btnLogcat = (Button) mFloatLayout.findViewById(R.id.logcat);
    btnTrigger = (Button) mFloatLayout.findViewById(R.id.trigger_event);
    btnRecord = (Button) mFloatLayout.findViewById(R.id.install_layer);
    btnReplay = (Button) mFloatLayout.findViewById(R.id.replay);
    dragArea = (ImageView) mFloatLayout.findViewById(R.id.drag_area);
    logInfo = (TextView) mFloatLayout.findViewById(R.id.tv_loginfo);
    logCatWrapper = (ScrollView) mFloatLayout.findViewById(R.id.tv_loginfo_wrapper);

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

    btnLogcat.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (logInfo.isShown()) {
          logCatWrapper.setVisibility(View.GONE);
        } else {
          logCatWrapper.setVisibility(View.VISIBLE);
        }
      }
    });
    logInfo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Navgation.startLogCatActivity(activity);
      }
    });

    btnHide.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFloatTools();
        btnReset.performClick();
      }
    });

    btnRecord.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (touchLayerStatus == RECORDING) {
          stopRecording();
        } else if (touchLayerStatus == STOPPED) {
          startRecording();
        } else {
          completeInput();
        }
      }
    });
    btnRecord.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        eventInput.clear();
        Toast.makeText(activity, "cleared", Toast.LENGTH_SHORT).show();
        return true;
      }
    });
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

    btnReplay.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        eventInput.replay();
      }
    });
    mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                         View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
  }

  private void hideFloatTools() {
    mWindowManager.removeView(mFloatLayout);
    floatViewStatus = View.INVISIBLE;
  }

  public void onTouch(MotionEvent event) {
    this.currentActivity.get().dispatchTouchEvent(event);
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

  @NonNull
  private View.OnClickListener buildResetClickListener(final Activity activity) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View v) {
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
    };
  }

  @NonNull
  private View.OnClickListener buildDebugClickListener(final Activity activity) {
    return new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        ViewUtil.dumpView(activity);
        ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
        List<View> allChildViews = ViewUtil.getAllChildViews(activity);
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
    };
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
