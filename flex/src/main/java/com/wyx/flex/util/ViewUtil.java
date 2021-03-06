package com.wyx.flex.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.wyx.flex.FloatTools;
import com.wyx.flex.parser.ViewParser;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author weiyixiong
 * @version 创建时间: 2016/10/24 16:36
 */

public class ViewUtil {
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
  public static ByteArrayOutputStream getByteArrayOutputStream(ViewGroup root)
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

  /**
   * dump view info and parse it
   *
   * @param root the View you want to dump view
   */
  public static void dumpView(ViewGroup root, Context context) {
    File dumpDir = StorageUtils.getIndividualCacheDirectory(context, "viewData");
    if (!dumpDir.exists()) {
      dumpDir.mkdirs();
    }
    File dump = new File(dumpDir.getAbsolutePath() + "/dump.txt");
    OutputStream outputStream = null;
    try {
      ByteArrayOutputStream stream = ViewUtil.getByteArrayOutputStream(root);
      ViewParser.parser(stream.toByteArray());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static List<View> getAllChildViews(Activity activity) {
    View view = activity.getWindow().getDecorView();
    return getAllChildViews(view);
  }

  public static List<View> getAllChildViews(View view) {
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

  public static boolean isInside(float x, float y, View child) {
    Rect rect = new Rect();
    child.getGlobalVisibleRect(rect);
    return x >= rect.left && x < rect.right && y >= rect.top && y < rect.bottom;
  }

  /**
   * Android 6.0 版本及之后的跳转权限申请界面
   *
   * @param context 上下文
   */
  private static void highVersionJump2PermissionActivity(Context context) {
    try {
      if (highVersionPermissionCheck(context)) {
        return;
      }
      Class clazz = Settings.class;
      Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
      Intent intent = new Intent(field.get(null).toString());
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setData(Uri.parse("package:" + context.getPackageName()));
      context.startActivity(intent);
    } catch (Exception e) {
    }
  }

  /**
   * Android 6.0 版本及之后的权限判断
   *
   * @param context 上下文
   * @return [ true, 有权限 ][ false, 无权限 ]
   */
  private static boolean highVersionPermissionCheck(Context context) {
    try {
      Class clazz = Settings.class;
      Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
      return (Boolean) canDrawOverlays.invoke(null, context);
    } catch (Exception e) {
    }
    return false;
  }

  @NonNull
  public static WindowManager.LayoutParams createWindowLayoutParams(int gravity) {
    highVersionJump2PermissionActivity(FloatTools.getInstance().getCurrentActivity());
    WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    }
    wmParams.format = PixelFormat.RGBA_8888;
    wmParams.gravity = gravity;
    wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    wmParams.x = 0;
    wmParams.y = 0;
    wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
    return wmParams;
  }

  public static ArrayList<View> getActivityAllViews(WindowManager windowManager) throws IllegalAccessException {
    final Class<?> windowManagerImpl = ReflectionUtil.getClass("android.view.WindowManagerImpl");
    final Class<?> windowManagerGlobal = ReflectionUtil.getClass("android.view.WindowManagerGlobal");
    final Field mGlobal = ReflectionUtil.getField(windowManagerImpl, "mGlobal", windowManagerGlobal);
    final Object global = mGlobal.get(windowManager);
    final Field mViews = ReflectionUtil.getField(windowManagerGlobal, "mViews", ArrayList.class);
    return (ArrayList<View>) mViews.get(global);
  }

  public static void hideIME(Context context, TextView view) {
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null && view != null) {
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public static void hideViews(View... btn) {
    for (View view : btn) {
      view.setVisibility(View.GONE);
    }
  }

  public static void showViews(View... btn) {
    for (View view : btn) {
      view.setVisibility(View.VISIBLE);
    }
  }
}
