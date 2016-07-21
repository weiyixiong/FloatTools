package com.wyx.flex;

import android.content.Context;
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

  private MotionEvent currentEvent;

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
        if (currentEvent == null) {
          return true;
        }
        return DragLayout.this.equals(findBottomChildUnder(child, currentEvent.getX(), currentEvent.getY()));
      }

      @Override public int getViewVerticalDragRange(View child) {
        return getMeasuredHeight() - child.getMeasuredHeight();
      }
    });
  }

  public View findBottomChildUnder(View parent, float x, float y) {
    ViewGroup viewGroup;
    if (parent instanceof ViewGroup) {
      viewGroup = (ViewGroup) parent;
      final int childCount = viewGroup.getChildCount();
      boolean found = false;
      for (int i = childCount - 1; i >= 0; i--) {
        final View child = viewGroup.getChildAt(i);
        if (x >= child.getLeft() && x < child.getRight() &&
            y >= child.getTop() && y < child.getBottom()) {
          found = true;
          if (child instanceof ViewGroup && ((ViewGroup) child).getChildCount() != 0) {
            return findBottomChildUnder(child, x, y);
          } else {
            return child;
          }
        }
      }
      if (!found) {
        return parent;
      }
    } else {
      return this;
    }
    return parent;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    currentEvent = event;
    dragHelper.processTouchEvent(event);
    return false;
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    return dragHelper.shouldInterceptTouchEvent(ev);
  }
}
