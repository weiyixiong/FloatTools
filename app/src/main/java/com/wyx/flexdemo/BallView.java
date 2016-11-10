package com.wyx.flexdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

class BallView extends View {
  private static final String TAG = "BallView";

  private Paint paint;        //定义画笔
  private int radius = 50;
  private Canvas mcanvas;
  private List<Point> listPoint;
  private int w;
  private int h;

  public BallView(Context context) {
    this(context, null);
  }

  public BallView(Context context, AttributeSet attrs) {
    super(context, attrs);
    listPoint = new ArrayList<Point>();
    //初始化画笔
    initPaint();
    mcanvas = new Canvas();
  }

  private void initPaint() {
    paint = new Paint();
    //设置消除锯齿
    paint.setAntiAlias(true);
    //设置画笔颜色
    paint.setColor(Color.RED);
  }

  //重写onDraw方法实现绘图操作
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    h = getHeight();
    w = getWidth();
    Log.d(TAG, "BallView: h:" + h + ",w:" + w);
    // 设置画布颜色
    canvas.drawColor(Color.GRAY);
    // 画圆
    if (listPoint.size() > 0) {
      for (int i = 0; i < listPoint.size(); i++) {
        if (listPoint.get(i) != null) {
          canvas.drawCircle(listPoint.get(i).x, listPoint.get(i).y, radius, paint);
        }
      }
    }
  }

  /**
   * 修正圆点坐标，让圆不会跑出边界
   */
   /* private void revise() {
        if (cx <= radius) {
            cx = radius;
        } else if (cx >= (w - radius)) {
            cx = w - radius;
        }
        if (cy <= radius) {
            cy = radius;
        } else if (cy >= (h - radius)) {
            cy = h - radius;
        }
    }*/

  /**
   * @param x
   * @param y
   */
  public void addPoint(int x, int y) {
    Point point = new Point(x, y);
    listPoint.add(point);
    invalidate();
    if (listPoint.size() > 10) {
      listPoint.remove(0);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    Point point = new Point((int) event.getX(), (int) event.getY());
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        listPoint.add(point);
        //该方法会调用onDraw方法，重新绘图
        postInvalidate();
        break;
      case MotionEvent.ACTION_MOVE:
        listPoint.add(point);
        postInvalidate();
        if (listPoint.size() > 10) {
          listPoint.remove(0);
        }
        break;
      case MotionEvent.ACTION_UP:
        listPoint.clear();
        postInvalidate();
        break;
    }

    return true;
  }
}