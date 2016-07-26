package com.wyx.flex;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.MotionEventCompat;
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
  ViewDragHelper dragHelper;

  private float x;
  private float y;

  public DragLayout(Context context) {
    super(context);
    initDragHelper();
  }

  private void initDragHelper() {
    dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {

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
        return child instanceof ViewGroup == false;
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

  public boolean isHaveChildUnder(ViewGroup parent) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      View view = parent.getChildAt(i);
      if (isInside(x, y, view)) {
        return true;
      }
    }
    return false;
  }

  public View findBottomChildUnder(View parent, float x, float y) {
    ViewGroup viewGroup;
    if (parent instanceof ViewGroup) {
      viewGroup = (ViewGroup) parent;
      final int childCount = viewGroup.getChildCount();
      for (int i = childCount - 1; i >= 0; i--) {
        final View child = viewGroup.getChildAt(i);
        if (isInside(x, y, child)) {
          if (child instanceof ViewGroup && ((ViewGroup) child).getChildCount() != 0) {
            View bottomChildUnder = findBottomChildUnder(child, x, y);
            if (isInside(x, y, bottomChildUnder)) {
              return bottomChildUnder;
            }
            return parent;
          } else {
            return child;
          }
        }
      }
      return parent;
    } else {
      return null;
    }
  }

  private boolean isInside(float x, float y, View child) {
    return x >= child.getLeft() && x < child.getRight() &&
        y >= child.getTop() && y < child.getBottom();
  }

  @Override public boolean onTouchEvent(MotionEvent ev) {
    dragHelper.processTouchEvent(ev);
    return false;
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    x = ev.getX();
    y = ev.getY();
    return dragHelper.shouldInterceptTouchEvent(ev);
  }
}
