package com.wyx.flex;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by winney on 16/5/11.
 */
public class DragLayout extends FrameLayout {
  ViewDragHelper dragHelper;

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
        params.leftMargin = left;//(int) (event.getRawX() - touchDownPoint.x);
        params.topMargin = top;// (int) (event.getRawY() - touchDownPoint.y);
        changedView.setLayoutParams(params);

        super.onViewPositionChanged(changedView, left, top, dx, dy);
      }

      @Override public boolean tryCaptureView(View child, int pointerId) {
        return true;
      }

      @Override public int getViewVerticalDragRange(View child) {
        return getMeasuredHeight() - child.getMeasuredHeight();
      }
    });
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    dragHelper.processTouchEvent(event);
    return true;
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    return dragHelper.shouldInterceptTouchEvent(ev);
  }
}
