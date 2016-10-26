package com.wyx.flex.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.wyx.flex.R;
import com.wyx.flex.record.EventInput;
import com.wyx.flex.record.Record;
import com.wyx.flex.record.RecordEvent;
import com.wyx.flex.util.TimeUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @author weiyixiong
 * @version 创建时间: 2016/10/25 10:33
 */

public class RecordActivity extends Activity {
  private ListView recordList;
  recordAdapter adapter;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record);

    recordList = (ListView) findViewById(R.id.list_record);
    adapter = new recordAdapter();
    adapter.setData(Record.getAllRecord());
    adapter.notifyDataSetChanged();
    recordList.setAdapter(adapter);

    recordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        List<RecordEvent> allRecordEventByID = RecordEvent.getAllRecordEventByID(adapter.getItem(position).getId());
        EventInput.installEvnet(allRecordEventByID);
      }
    });
  }

  class recordAdapter extends BaseAdapter {
    List<Record> data;

    public void setData(List<Record> data) {
      this.data = data;
    }

    @Override
    public int getCount() {
      return data == null ? 0 : data.size();
    }

    @Override
    public Record getItem(int position) {
      return data.get(position);
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder viewHolder;
      if (convertView == null) {
        convertView = LayoutInflater.from(RecordActivity.this).inflate(R.layout.list_item_record, null);
        viewHolder = new ViewHolder();
        viewHolder.name = (TextView) convertView.findViewById(R.id.text_record_name);
        viewHolder.time = (TextView) convertView.findViewById(R.id.text_record_time);
        viewHolder.startActivity = (TextView) convertView.findViewById(R.id.text_record_activity);
      } else {
        viewHolder = (ViewHolder) convertView.getTag();
      }
      Record item = getItem(position);
      viewHolder.name.setText(item.getName());
      viewHolder.time.setText(TimeUtils.getDate(item.getTime()));
      viewHolder.startActivity.setText(item.getActivityName());
      return convertView;
    }
  }

  class ViewHolder {
    TextView name;
    TextView time;
    TextView startActivity;
  }
}
