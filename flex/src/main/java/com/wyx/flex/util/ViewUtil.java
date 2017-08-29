package com.wyx.flex.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.wyx.flex.parser.ViewParser;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
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
}
