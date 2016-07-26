package com.wyx.flex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author weiyixiong
 * @version 创建时间: 2016/07/26 下午4:57
 */
public class BorderImageView extends ImageView {

  public BorderImageView(Context context) {
    super(context);
  }

  public BorderImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    PaintUtil.drawBorder(this, canvas);
  }
}
