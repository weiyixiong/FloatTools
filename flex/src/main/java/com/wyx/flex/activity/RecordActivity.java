package com.wyx.flex.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.wyx.flex.R;
import com.wyx.flex.record.EventInput;
import com.wyx.flex.record.Record;
import com.wyx.flex.record.RecordEvent;
import com.wyx.flex.util.Navgation;
import com.wyx.flex.util.PrefUtil;
import com.wyx.flex.util.TimeUtils;
import java.util.List;

/**
 * @author weiyixiong
 * @version 创建时间: 2016/10/25 10:33
 */

public class RecordActivity extends AppCompatActivity {
  private ListView recordList;
  recordAdapter adapter;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record);

    recordList = (ListView) findViewById(R.id.list_record);
    adapter = new recordAdapter();
    updateList();
    recordList.setAdapter(adapter);
  }

  private void updateList() {
    adapter.setData(Record.getAllRecord());
    adapter.notifyDataSetChanged();
  }

  class recordAdapter extends BaseAdapter {
    List<Record> data;

    long currentRecordId;

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

    View.OnClickListener cancelAutoRun = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Record item = getItemByViewTag(v);
        PrefUtil.setPlayRecordOnLaunch(false, item.getId());
        updateList();
      }
    };

    private Record getItemByViewTag(View v) {
      return adapter.getItem((Integer) v.getTag());
    }

    View.OnClickListener cancelUse = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PrefUtil.setCurrentRecordId(-1);
        updateList();
      }
    };
    View.OnClickListener installOnClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final Record item = getItemByViewTag(v);
        EventInput.installRecord(item);
        AlertDialog.Builder builder = new AlertDialog.Builder(RecordActivity.this);
        builder.setMessage("是否在应用启动时自动运行？");
        builder.setTitle("提示");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            PrefUtil.setPlayRecordOnLaunch(true, item.getId());
            updateList();
            dialog.dismiss();
          }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
          @Override

          public void onClick(DialogInterface dialog, int which) {
            PrefUtil.setPlayRecordOnLaunch(false, item.getId());
            updateList();
            dialog.dismiss();
          }
        });
        builder.create().show();
      }
    };
    View.OnClickListener deleteOnClick = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Record item = getItemByViewTag(v);
        RecordEvent.deleteAllRecordEventByID(item.getId());
        item.delete();
        updateList();
      }
    };
    View.OnClickListener editEventOnClick = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Record item = getItemByViewTag(v);
        Navgation.startEditEventActivity(RecordActivity.this, item.getId());
      }
    };

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      ViewHolder viewHolder;
      if (convertView == null || convertView.getTag() == null) {
        convertView = LayoutInflater.from(RecordActivity.this).inflate(R.layout.list_item_record, null);
        viewHolder = new ViewHolder();
        viewHolder.name = (TextView) convertView.findViewById(R.id.text_record_name);
        viewHolder.time = (TextView) convertView.findViewById(R.id.text_record_time);
        viewHolder.startActivity = (TextView) convertView.findViewById(R.id.text_record_activity);
        viewHolder.btnDelete = (ImageButton) convertView.findViewById(R.id.btn_delete);
        viewHolder.btnInstall = (Button) convertView.findViewById(R.id.btn_record_install);
        viewHolder.btnEdit = (Button) convertView.findViewById(R.id.btn_record_edit);
      } else {
        viewHolder = (ViewHolder) convertView.getTag();
      }
      Record item = getItem(position);
      viewHolder.name.setText(item.getName());
      viewHolder.time.setText(TimeUtils.getDate(item.getTime()));
      viewHolder.startActivity.setText(item.getActivityName());

      viewHolder.updatePosition(position);
      if (currentRecordId == item.getId()) {
        if (PrefUtil.isPlayRecordOnLaunch()) {
          viewHolder.btnInstall.setText("取消自启");
          viewHolder.btnInstall.setOnClickListener(cancelAutoRun);
        } else {
          viewHolder.btnInstall.setText("正在使用");
          viewHolder.btnInstall.setOnClickListener(cancelUse);
        }
      } else {
        viewHolder.btnInstall.setText("装载");
        viewHolder.btnInstall.setOnClickListener(installOnClickListener);
      }
      viewHolder.btnEdit.setOnClickListener(editEventOnClick);
      viewHolder.btnDelete.setOnClickListener(deleteOnClick);
      return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
      super.notifyDataSetChanged();
      currentRecordId = PrefUtil.getCurrentPlayID();
    }
  }

  class ViewHolder {
    TextView name;
    TextView time;
    TextView startActivity;
    Button btnInstall;
    Button btnEdit;
    ImageButton btnDelete;

    public void updatePosition(int position) {
      btnInstall.setTag(position);
      btnEdit.setTag(position);
      btnDelete.setTag(position);
    }
  }
}
