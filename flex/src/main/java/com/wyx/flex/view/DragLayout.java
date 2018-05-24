package com.wyx.flex.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.wyx.flex.util.PaintUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by winney on 16/5/11.
 */
public class DragLayout extends FrameLayout {
  ViewDragHelper dragHelper;

  private float x;
  private float y;

  private float initialX;
  private float initialY;

  public DragLayout(Context context) {
    super(context);
    initDragHelper();
  }

  private void initDragHelper() {
    dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {

      @Override
      public int clampViewPositionHorizontal(View child, int left, int dx) {
        return left;
      }

      @Override
      public int clampViewPositionVertical(View child, int top, int dy) {
        return top;
      }

      @Override
      public int getViewHorizontalDragRange(View child) {
        return getMeasuredWidth() - child.getMeasuredWidth();
      }

      @Override
      public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        LayoutParams params = (LayoutParams) changedView.getLayoutParams();
        params.leftMargin = left;
        params.topMargin = top;
        changedView.setLayoutParams(params);
      }

      @Override
      public boolean tryCaptureView(View child, int pointerId) {
        return true;
      }

      @Override
      public int getViewVerticalDragRange(View child) {
        return getMeasuredHeight() - child.getMeasuredHeight();
      }
    });
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    PaintUtil.drawBorder(this, canvas);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    return super.dispatchTouchEvent(ev);
  }

  private boolean isInside(float x, float y, View child) {
    Rect rect = new Rect();
    child.getGlobalVisibleRect(rect);
    return x >= rect.left && x < rect.right &&
        y >= rect.top && y < rect.bottom;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    super.onTouchEvent(ev);
    dragHelper.processTouchEvent(ev);
    View bottomView = findBottomView(this, x, y);
    final ViewGroup parent = (ViewGroup) bottomView.getParent();

    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      initialX = ev.getX();
      initialY = ev.getY();
    } else if (ev.getAction() == MotionEvent.ACTION_MOVE && isTouchSlop(ev, initialX, initialY)) {
      try {
        Method removeLongPressCallback = View.class.getDeclaredMethod("removeLongPressCallback");
        removeLongPressCallback.setAccessible(true);
        removeLongPressCallback.invoke(bottomView);
        while (bottomView.getParent() != null && bottomView.getParent() instanceof View) {
          removeLongPressCallback.invoke(bottomView.getParent());
          bottomView = (View) bottomView.getParent();
        }
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return parent == this;
  }

  public boolean isTouchSlop(MotionEvent event, float initialX, float initialY) {
    final float dx = MotionEventCompat.getX(event, 0) - initialX;
    final float dy = MotionEventCompat.getY(event, 0) - initialY;
    int touchSlop = getTouchSlop();
    return dx * dx + dy * dy > touchSlop * touchSlop;
  }

  //灵敏度过高 导致对于单击行为判断不准确
  public int getTouchSlop() {
    return ViewConfiguration.get(getContext()).getScaledTouchSlop();
  }

  public View findBottomView(ViewGroup viewGroup, float x, float y) {
    for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
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

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    x = ev.getRawX();
    y = ev.getRawY();
    View haveChildUnder = findBottomView(this, x, y);
    if (haveChildUnder.getParent() != this) {
      return false;
    } else {
      return dragHelper.shouldInterceptTouchEvent(ev);
    }
  }
}
