package com.wyx.flex;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by winney on 16/4/28.
 */
public class FloatTools {

  private static final String TAG = "FloatTools";
  public static final String RESET = "resetAction";

  private static Application application;

  private static LinearLayout mFloatLayout;
  private static Handler handler;
  private WindowManager.LayoutParams wmParams;
  private WindowManager mWindowManager;
  private Button btnDebug;
  private Button btnReset;
  private Button btnHide;
  private Button btnLogcat;
  private ImageView dragArea;
  private TextView logInfo;
  private WeakReference<Activity> currentActivity;
  private static FloatTools instance;
  private Point touchDownPoint;
  private static SensorManager mSensorManager;
  private static Sensor mAccelerometer;
  private ShakeDetector mShakeDetector;
  private int floatViewStatus = View.INVISIBLE;

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
      }

      @Override
      public void onActivityPaused(Activity activity) {

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

  public static FloatTools getInstance(Activity activity) {
    if (instance == null) {
      instance = new FloatTools();
    }
    instance.setupActivity(activity);
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
          touchDownPoint = new Point();
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          touchDownPoint.set((int) event.getX(), (int) event.getY());
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
          wmParams.x += (int) event.getX() - touchDownPoint.x;
          wmParams.y += (int) event.getY() - touchDownPoint.y;
          mWindowManager.updateViewLayout(mFloatLayout, wmParams);
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
    showFloatTools();
    mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
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
    mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_tool_bar, null);
    btnDebug = (Button) mFloatLayout.findViewById(R.id.button);
    btnReset = (Button) mFloatLayout.findViewById(R.id.reset);
    btnHide = (Button) mFloatLayout.findViewById(R.id.hide);
    btnLogcat = (Button) mFloatLayout.findViewById(R.id.logcat);
    dragArea = (ImageView) mFloatLayout.findViewById(R.id.drag_area);
    logInfo = (TextView) mFloatLayout.findViewById(R.id.tv_loginfo);
    logInfo.setMovementMethod(new ScrollingMovementMethod());
    startLogCat();

    btnLogcat.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (logInfo.isShown()) {
          logInfo.setVisibility(View.GONE);
        } else {
          logInfo.setVisibility(View.VISIBLE);
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
        mWindowManager.removeView(mFloatLayout);
        floatViewStatus = View.INVISIBLE;
        btnReset.performClick();
      }
    });

    mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                         View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
  }

  private void startLogCat() {
    handler = new LogCatHandler(logInfo);
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        handler.sendEmptyMessageDelayed(1, 500);
      }
    }, 0, 500);
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
        view.destroyDrawingCache();
        view.buildDrawingCache();
        tmp.setImageBitmap(view.getDrawingCache());
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

  static class LogCatHandler extends Handler {
    TextView textView;

    public LogCatHandler(TextView textView) {
      this.textView = textView;
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (msg.what == 0) {
        if (msg.obj != null) {
          ((Runnable) msg.obj).run();
        }
      } else {
        try {
          String logcatInfo = LogCatUtil.getLogcatInfo();
          textView.setText(LogCatUtil.getCacheLog());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
