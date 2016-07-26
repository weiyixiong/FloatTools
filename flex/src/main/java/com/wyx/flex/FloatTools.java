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
  private Button bthHide;
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
      @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

      }

      @Override public void onActivityStarted(Activity activity) {

      }

      @Override public void onActivityResumed(Activity activity) {
        instance.setAndDumpActivity(activity);
      }

      @Override public void onActivityPaused(Activity activity) {

      }

      @Override public void onActivityStopped(Activity activity) {

      }

      @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

      }

      @Override public void onActivityDestroyed(Activity activity) {

      }
    });
  }

  private void setAndDumpActivity(Activity activity) {
    setupActivity(activity);
    createFloatView();
    //dumpView(activity);
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
      @Override public boolean onTouch(View v, MotionEvent event) {
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
    btnDebug.setOnClickListener(getDragOnClickListener(activity));
    btnReset.setOnClickListener(getResetClickListener(activity));

    // ShakeDetector initialization
    mShakeDetector = new ShakeDetector();
    mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

      @Override public void onShake(int count) {
        showFloatTools();
      }
    });
    showFloatTools();
    mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
  }

  private void initFloatView(Activity activity) {
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
    bthHide = (Button) mFloatLayout.findViewById(R.id.hide);
    dragArea = (ImageView) mFloatLayout.findViewById(R.id.drag_area);
    logInfo = (TextView) mFloatLayout.findViewById(R.id.tv_loginfo);

    handler = new LogCatHandler(logInfo);
    //new Timer().schedule(new TimerTask() {
    //  @Override public void run() {
    //    handler.sendEmptyMessageDelayed(1, 500);
    //  }
    //}, 0, 500);
    //handler.sendMessageDelayed(msg, 1000);
    bthHide.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mWindowManager.removeView(mFloatLayout);
        floatViewStatus = View.INVISIBLE;
        btnReset.performClick();
      }
    });

    mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
  }

  @NonNull private View.OnClickListener getResetClickListener(final Activity activity) {
    return new View.OnClickListener() {
      @Override public void onClick(View v) {
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

  @NonNull private View.OnClickListener getDragOnClickListener(final Activity activity) {
    return new View.OnClickListener() {
      @Override public void onClick(final View v) {
        dumpView(activity);
        ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
        List<View> allChildViews = getAllChildViews(activity);
        DragLayout parent = (DragLayout) root.findViewWithTag(TAG);

        if (parent == null) {
          parent = new DragLayout(activity);
          parent.setTag(TAG);
          //root.addView(parent);
        }

        parent.setBackgroundColor(Color.WHITE);
        parent.removeAllViews();
        addView(activity, parent, root, 0, 0);

        root.addView(parent);
        //
        //for (final View view : allChildViews) {
        //  //if (!(view instanceof ViewGroup)) {
        //  ImageView tmp = new ImageView(activity);
        //  FrameLayout.LayoutParams params =
        //      new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //  view.destroyDrawingCache();
        //  view.buildDrawingCache();
        //  tmp.setImageBitmap(view.getDrawingCache());
        //  parent.addView(tmp);
        //  int[] location = new int[2];
        //  view.getLocationOnScreen(location);
        //  params.topMargin = location[1];
        //  params.leftMargin = location[0];
        //  tmp.setLayoutParams(params);
        //  tmp.setOnClickListener(new View.OnClickListener() {
        //    @Override public void onClick(View v) {
        //      view.callOnClick();
        //    }
        //  });
        //  tmp.setOnLongClickListener(new View.OnLongClickListener() {
        //    @Override public boolean onLongClick(View v) {
        //      Navgation.startViewDetailActivity(activity, view.hashCode());
        //      return true;
        //    }
        //  });
        //  //}
        //}
      }
    };
  }

  private void addView(final Activity activity, ViewGroup parent, ViewGroup origin, int top, int left) {
    for (int i = 0; i < origin.getChildCount(); i++) {
      final View view = origin.getChildAt(i);
      L.e(view.getClass());
      if (view instanceof ViewGroup) {
        DragLayout frameLayout = new DragLayout(activity);
        //FrameLayout.LayoutParams params =
        //    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(view.getMeasuredWidth(), view.getMeasuredHeight());
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        params.topMargin = location[1] - top;
        params.leftMargin = location[0] - left;
        frameLayout.setLayoutParams(params);
        parent.addView(frameLayout);
        addView(activity, frameLayout, (ViewGroup) view, location[1], location[0]);
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
          @Override public boolean onLongClick(View v) {
            Navgation.startViewDetailActivity(activity, view.hashCode());
            return true;
          }
        });
      }
    }
  }

  private void dumpView(Activity activity) {
    ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();//.getRootView();
    //root = (ViewGroup) root.getChildAt(0);
    File dumpDir = StorageUtils.getIndividualCacheDirectory(activity, "viewData");
    if (!dumpDir.exists()) {
      dumpDir.mkdirs();
    }
    File dump = new File(dumpDir.getAbsolutePath() + "/dump.txt");
    OutputStream outputStream = null;
    try {
      //public static void dump(View root, boolean skipChildren, boolean includeProperties,
      //OutputStream clientStream) throws IOException {
      //}
      //public static void dumpv2(@NonNull final View view, @NonNull ByteArrayOutputStream out)
      ByteArrayOutputStream stream = getByteArrayOutputStream(root);
      ViewParser.parser(stream.toByteArray());
      //Method dispatch = ViewDebug.class.getDeclaredMethod("dumpv2", View.class,
      //    ByteArrayOutputStream.class);//, boolean.class, OutputStream.class);
      //dispatch.setAccessible(true);
      //
      ////outputStream = new FileOutputStream(dump);
      //dispatch.invoke(null, root, stream);

      //BufferedReader reader = null;
      //reader = new BufferedReader(new FileReader(dump));
      //String tempString = null;
      //int line = 1;
      //// 一次读入一行，直到读入null为文件结束
      //while ((tempString = reader.readLine()) != null) {
      //  // 显示行号
      //  String str = "line " + line + ": " + tempString;
      //  Log.e(TAG, str);
      //  line++;
      //}
      //reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @NonNull private ByteArrayOutputStream getByteArrayOutputStream(ViewGroup root)
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

  //private static List<ViewGroup> getAllGroupView(View view) {
  //  List<ViewGroup> allChildren = new ArrayList<>();
  //  if (view instanceof ViewGroup) {
  //    ViewGroup vp = (ViewGroup) view;
  //    for (int i = 0; i < vp.getChildCount(); i++) {
  //      View viewChild = vp.getChildAt(i);
  //      allChildren.add(viewChild);
  //      allChildren.addAll(getAllChildViews(viewChild));
  //    }
  //  }
  //  return allChildren;
  //}

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

    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (msg.what == 0) {
        if (msg.obj != null) {
          ((Runnable) msg.obj).run();
        }
        //  Message msg1 = new Message();
        //  msg1.what = 1;
        //  Bundle data = new Bundle();
        //  try {
        //    data.putString("log", LogCatUtil.getLogcatInfo());
        //  } catch (IOException e) {
        //    e.printStackTrace();
        //  }
        //  msg1.setData(data);
        //  sendMessageDelayed(msg1, 500);
        //} else {
        //  textView.setText(msg.getData().getString("log"));
      } else {
        try {
          textView.setText(LogCatUtil.getLogcatInfo());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
