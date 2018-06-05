package com.wyx.flex.Collection;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.wyx.flex.record.EventInput;
import com.wyx.flex.util.ViewUtil;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 文字输入收集器
 *
 * @author weiyixiong
 * @version 创建时间: 2018/06/01 17:06
 */
public class EditEventCollection extends EventCollection {
  private WeakReference<EditText> currentEditText;
  private boolean isEditText = false;

  public EditEventCollection(AppModel appModel) {
    super(appModel);
  }

  private static boolean isSoftKeyboardFinishedAction(TextView view, int action, KeyEvent event) {
    // Some devices return null event on editor actions for Enter Button
    return (action == EditorInfo.IME_ACTION_DONE || action == EditorInfo.IME_ACTION_GO ||
        action == EditorInfo.IME_ACTION_SEND) && (event == null || event.getAction() == KeyEvent.ACTION_DOWN);
  }

  /**
   * 为所有EditText加上输入监听
   */
  public void setEditorListener() {
    List<View> allChildViews = ViewUtil.getAllChildViews(getCurrentActivity());
    for (View allChildView : allChildViews) {
      if (allChildView instanceof EditText) {
        final EditText editText = (EditText) allChildView;
        editText.addTextChangedListener(getWatcher(editText));
        editText.setOnEditorActionListener(getEditorActionListener());
      }
    }
    isEditText = true;
  }

  @NonNull
  private EditText.OnEditorActionListener getEditorActionListener() {
    return new EditText.OnEditorActionListener() {
      public boolean onEditorAction(TextView view, int action, KeyEvent event) {
        if (isSoftKeyboardFinishedAction(view, action, event)) {
          appModel.completeInput(true, false);
        }
        return false;
      }
    };
  }

  public void hideSoftKeyBoard() {
    final EditText view = this.currentEditText.get();
    if (view == null) {
      return;
    }
    ViewUtil.hideIME(getCurrentActivity(), view);
  }

  private EditText getCurrentEditText() {
    return this.currentEditText.get();
  }

  public boolean isEditText() {
    return this.currentEditText != null && isEditText;
  }

  public void recordInputEvent() {
    final EditText view = getCurrentEditText();
    if (view == null) {
      return;
    }
    if (view.getId() == View.NO_ID) {
      Rect rect = new Rect();
      view.getGlobalVisibleRect(rect);
      EventInput.recordEditEvent(rect.left + 1, rect.top + 1, view.getText().toString());
    } else {
      String viewId = getCurrentActivity().getResources().getResourceName(view.getId());
      EventInput.recordEditEvent(viewId, view.getText().toString());
    }
    this.currentEditText = null;
  }

  @NonNull
  private TextWatcher getWatcher(final EditText editText) {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!appModel.isCollectingTouch()) {
          return;
        }
        if (currentEditText == null || currentEditText.get() != editText) {
          currentEditText = new WeakReference<>(editText);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    };
  }
}
