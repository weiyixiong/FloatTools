package com.wyx.flex.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * @author weiyixiong
 * @version 创建时间: 2016/07/26 下午5:32
 */
public class PaintUtil {
  private static Paint borderPaint;

  public static Paint getBorderPaint() {
    if (borderPaint != null) {
      return borderPaint;
    }
    borderPaint = new Paint();
    borderPaint.setStyle(Paint.Style.STROKE);
    borderPaint.setColor(Color.RED);
    return borderPaint;
  }

  public static void drawBorder(View view, Canvas canvas) {
    Rect rect = new Rect();
    view.getLocalVisibleRect(rect);
    canvas.drawRect(rect, getBorderPaint());
  }
}
