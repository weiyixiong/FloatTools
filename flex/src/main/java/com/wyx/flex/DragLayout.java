package com.wyx.flex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by winney on 16/5/11.
 */
public class DragLayout extends FrameLayout {
  MyViewDragHelper dragHelper;

  private float x;
  private float y;

  public DragLayout(Context context) {
    super(context);
    initDragHelper();
  }

  private void initDragHelper() {
    dragHelper = MyViewDragHelper.create(this, new MyViewDragHelper.Callback() {

      @Override public int clampViewPositionHorizontal(View child, int left, int dx) {
        if (getPaddingLeft() > left) {
          return getPaddingLeft();
        }

        if (getWidth() - child.getWidth() < left) {
          return getWidth() - child.getWidth();
        }

        return left;
      }

      @Override public int clampViewPositionVertical(View child, int top, int dy) {
        if (getPaddingTop() > top) {
          return getPaddingTop();
        }

        if (getHeight() - child.getHeight() < top) {
          return getHeight() - child.getHeight();
        }
        return top;
      }

      @Override public int getViewHorizontalDragRange(View child) {
        return getMeasuredWidth() - child.getMeasuredWidth();
      }

      @Override public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        LayoutParams params = (LayoutParams) changedView.getLayoutParams();
        params.leftMargin = left;
        params.topMargin = top;
        changedView.setLayoutParams(params);
        super.onViewPositionChanged(changedView, left, top, dx, dy);
      }

      @Override public boolean tryCaptureView(View child, int pointerId) {
        return findBottomView(DragLayout.this, x, y) == child;
        //boolean b = child instanceof ViewGroup == false || (isContainsView((ViewGroup) child)
        //    && isHaveChildUnder((ViewGroup) child) == null);
        //if (b) {
        //  requestDisallowInterceptTouchEvent(true);
        //} else {
        //  requestDisallowInterceptTouchEvent(false);
        //}
        //return b;
        //return true;
      }

      @Override public int getViewVerticalDragRange(View child) {
        return getMeasuredHeight() - child.getMeasuredHeight();
      }
    });
  }

  @Override protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    PaintUtil.drawBorder(this, canvas);
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    return super.dispatchTouchEvent(ev);
  }

  private boolean isInside(float x, float y, View child) {
    Rect rect = new Rect();
    child.getGlobalVisibleRect(rect);
    if (x >= rect.left && x < rect.right &&
        y >= rect.top && y < rect.bottom) {
      L.e(child.getClass() + " " + rect.left + "  " + rect.right + "  " + rect.top + " " + rect.bottom);
    }
    return x >= rect.left && x < rect.right &&
        y >= rect.top && y < rect.bottom;
  }

  @Override public boolean onTouchEvent(MotionEvent ev) {
    dragHelper.processTouchEvent(ev);
    if (ev.getAction() == MotionEvent.ACTION_UP) dragHelper.abort();
    return false;
  }

  public View findBottomView(ViewGroup viewGroup, float x, float y) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View view = viewGroup.getChildAt(i);
      if (isInside(x, y, view)) {
        if (view instanceof ViewGroup && ((ViewGroup) view).getChildCount() != 0) {
          return findBottomView((ViewGroup) view, x, y);
        } else {
          return view;
        }
      }
    }
    return viewGroup;
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    x = ev.getRawX();
    y = ev.getRawY();
    //View haveChildUnder = findBottomView(this, x, y);
    //if (haveChildUnder.getParent() != this) {
    //  return false;
    //}
    return dragHelper.shouldInterceptTouchEvent(ev);
  }
}
