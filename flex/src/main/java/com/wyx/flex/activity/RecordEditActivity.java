package com.wyx.flex.activity;

import android.app.AlertDialog;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.wyx.flex.R;
import com.wyx.flex.record.EventType;
import com.wyx.flex.record.Record;
import com.wyx.flex.record.RecordEvent;
import com.wyx.flex.util.MyArrayAdapter;
import com.wyx.flex.util.TimeUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author weiyixiong E-mail: weiyixiong@tigerbrokers.com
 * @version 创建时间: 2016/11/11 13:58
 */

public class RecordEditActivity extends AppCompatActivity {
  private static final String RECORD_ID = "record_id";
  private Map<Integer, String> actionType = new HashMap<>();
  private ListView editList;
  private EditAdapter eventAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record_edit);
    editList = (ListView) findViewById(R.id.edit_list);
    eventAdapter = new EditAdapter(getBaseContext());
    editList.setAdapter(eventAdapter);
    List<RecordEvent> allRecordEventByID = RecordEvent.getAllRecordEventByID(getRecordId());
    eventAdapter.addAll(allRecordEventByID);

    actionType.put(MotionEvent.ACTION_DOWN, "ACTION_DOWN");
    actionType.put(MotionEvent.ACTION_MOVE, "ACTION_MOVE");
    actionType.put(MotionEvent.ACTION_UP, "ACTION_UP");
    //actionType.put(MotionEvent.A, "ACTION_DOWN");
    //actionType.put(MotionEvent.ACTION_DOWN, "ACTION_DOWN");
    //actionType.put(MotionEvent.ACTION_DOWN, "ACTION_DOWN");

  }

  private long getRecordId() {
    return getIntent().getLongExtra(RECORD_ID, 0);
  }

  public static void putRecordId(Intent intent, long recordId) {
    intent.putExtra(RECORD_ID, recordId);
  }

  class EditAdapter extends MyArrayAdapter<RecordEvent> {
    private long startTime;

    public EditAdapter(Context context) {
      super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder viewHolder;
      if (convertView == null) {
        viewHolder = new ViewHolder();
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_edit_event, parent, false);
        viewHolder.actionType = (TextView) convertView.findViewById(R.id.text_action_type);
        viewHolder.adjustBtn = (Button) convertView.findViewById(R.id.btn_drag_event);
        convertView.setTag(viewHolder);
      } else {
        viewHolder = (ViewHolder) convertView.getTag();
      }
      RecordEvent item = getItem(position);
      long timeDiff = 0;
      if (position == 0) {
        startTime = item.getTime();
      } else {
        timeDiff = item.getTime() - startTime;
      }
      viewHolder.adjustBtn.setText(TimeUtils.getMinSecond(timeDiff));
      if (item.getType() == EventType.TOUCH) {
        viewHolder.actionType.setText("touch:" + actionType.get(item.getEventAction()));
      } else if (item.getType() == EventType.EDIT) {
        viewHolder.actionType.setText("edit");
      }

      return convertView;
    }
  }

  class ViewHolder {
    Button adjustBtn;
    TextView actionType;
  }
}
