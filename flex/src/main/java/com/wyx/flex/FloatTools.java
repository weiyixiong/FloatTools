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
import com.wyx.flex.parser.ViewParser;
import com.wyx.flex.util.AccessibilityUtil;
import com.wyx.flex.util.EventInput;
import com.wyx.flex.util.FloatConfig;
import com.wyx.flex.util.L;
import com.wyx.flex.util.LogCatUtil;
import com.wyx.flex.util.Navgation;
import com.wyx.flex.util.ShakeDetector;
import com.wyx.flex.util.StorageUtils;
import com.wyx.flex.view.BorderImageView;
import com.wyx.flex.view.DragLayout;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by winney on 16/4/28.
 */
public class FloatTools {

  private static final String TAG = "FloatTools";
  public static final String RESET = "resetAction";

  private static final int RECORDING = 0;
  private static final int STOPPED = 1;
  private static final int INPUTING = 2;

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
    List<View> allChildViews = getAllChildViews(this.currentActivity.get());
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
    touchLayerStatus = INPUTING;
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
    //String viewId = this.currentActivity.get().getResources().getResourceName(view.getId());
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
      if (touchLayerStatus == STOPPED || touchLayerStatus == INPUTING) {
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
      Toast.makeText(this.currentActivity.get(), "stopped", Toast.LENGTH_SHORT).show();
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

  public void onEdit(EventInput.RecordEvent event) {
    Activity activity = this.currentActivity.get();
    if (event.getResName() != null) {
      int identifier = activity.getResources().getIdentifier(event.getResName(), null, null);
      EditText editText = (EditText) activity.findViewById(identifier);
      editText.setText(event.getText());
    } else {
      List<View> allChildViews = getAllChildViews(activity);
      float x = event.getX();
      float y = event.getY();
      for (View item : allChildViews) {
        if (item instanceof EditText) {
          if (isInside(x, y, item)) {
            ((EditText) item).setText(event.getText());
          }
        }
      }
    }
  }

  private boolean isInside(float x, float y, View child) {
    Rect rect = new Rect();
    child.getGlobalVisibleRect(rect);
    return x >= rect.left && x < rect.right &&
        y >= rect.top && y < rect.bottom;
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
        dumpView(activity);
        ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
        List<View> allChildViews = getAllChildViews(activity);
        DragLayout parent = (DragLayout) root.findViewWithTag(TAG);

        if (parent == null) {
          parent = new DragLayout(activity);
          parent.setTag(TAG);
        }
        parent.removeAllViews();
        root.removeView(parent);

        parent.setBackgroundColor(Color.WHITE);
        parent.removeAllViews();
        addView(activity, parent, root, 0, 0);

        root.addView(parent);
      }
    };
  }

  private void addView(final Activity activity, ViewGroup parent, ViewGroup origin, int top, int left) {
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
        addView(activity, frameLayout, (ViewGroup) view, location[1], location[0]);
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

  /**
   * dump view info and parse it
   *
   * @param activity the activity you want to dump view
   */
  private void dumpView(Activity activity) {
    ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
    File dumpDir = StorageUtils.getIndividualCacheDirectory(activity, "viewData");
    if (!dumpDir.exists()) {
      dumpDir.mkdirs();
    }
    File dump = new File(dumpDir.getAbsolutePath() + "/dump.txt");
    OutputStream outputStream = null;
    try {
      ByteArrayOutputStream stream = getByteArrayOutputStream(root);
      ViewParser.parser(stream.toByteArray());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * use reflection to call the ViewHierarchyEncoder and dump view as Stream
   *
   * @throws ClassNotFoundException
   * @throws NoSuchMethodException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws java.lang.reflect.InvocationTargetException
   */
  @NonNull
  private ByteArrayOutputStream getByteArrayOutputStream(ViewGroup root)
      throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
             java.lang.reflect.InvocationTargetException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream(32 * 1024);
    Class<?> viewHierarchyEncoder = Class.forName("android.view.ViewHierarchyEncoder");
    Constructor viewDumpCon = viewHierarchyEncoder.getConstructor(ByteArrayOutputStream.class);

    Method dumpV2 = View.class.getDeclaredMethod("encode", viewHierarchyEncoder);
    dumpV2.setAccessible(true);

    Object o = viewDumpCon.newInstance(stream);

    Method endStream = viewHierarchyEncoder.getDeclaredMethod("endStream");
    dumpV2.invoke(root, o);
    endStream.invoke(o);
    return stream;
  }

  private static List<View> getAllChildViews(Activity activity) {
    View view = activity.getWindow().getDecorView();
    return getAllChildViews(view);
  }

  private static List<View> getAllChildViews(View view) {
    List<View> allChildren = new ArrayList<View>();
    if (view instanceof ViewGroup) {
      ViewGroup vp = (ViewGroup) view;
      for (int i = 0; i < vp.getChildCount(); i++) {
        View viewChild = vp.getChildAt(i);
        allChildren.add(viewChild);
        allChildren.addAll(getAllChildViews(viewChild));
      }
    }
    return allChildren;
  }

  public static void setDebug(boolean debug) {
    L.setDebug(debug);
  }

  public static void setConfig(FloatConfig con) {
    config = con;
  }
}
