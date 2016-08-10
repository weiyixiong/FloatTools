package com.wyx.flex.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import com.wyx.flex.FloatTools;
import com.wyx.flex.R;
import com.wyx.flex.parser.ViewParser;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DetailInfoActivity extends Activity {
  private static final String VIEW_HASH_CODE = "hashcode";
  private ExpandableListView detailInfo;

  private InfoAdapter infoAdapter;
  private int hashCode;
  BroadcastReceiver finishReceiver;

  public static void putExtra(Intent intent, int hashcode) {
    intent.putExtra(VIEW_HASH_CODE, hashcode);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_info_detail);
    detailInfo = (ExpandableListView) findViewById(R.id.detail_info);
    infoAdapter = new InfoAdapter();
    detailInfo.setAdapter(infoAdapter);
    initData();

    registerResetRecevier();
  }

  private void registerResetRecevier() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(FloatTools.RESET);
    finishReceiver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        finish();
      }
    };
    registerReceiver(finishReceiver, filter);
  }

  @Override
  protected void onStop() {
    super.onStop();
    unregisterReceiver(finishReceiver);
  }

  @NonNull
  private void initData() {
    hashCode = getIntent().getIntExtra(VIEW_HASH_CODE, 0);
    Map<String, List<Pair<String, String>>> viewInfo = ViewParser.getViewInfo(hashCode);
    if (viewInfo == null) {
      Toast.makeText(getApplicationContext(), "无信息", Toast.LENGTH_SHORT).show();
    }

    String[] index = new String[viewInfo.size()];
    Iterator<Map.Entry<String, List<Pair<String, String>>>> iterator = viewInfo.entrySet().iterator();
    int i = 0;
    while (iterator.hasNext()) {
      index[i++] = iterator.next().getKey();
    }
    infoAdapter.setData(viewInfo, index);

    //Collections.sort(viewInfo, new Comparator<Pair<String, String>>() {
    //  @Override
    //  public int compare(Pair<String, String> lhs, Pair<String, String> rhs) {
    //    return Integer.compare(lhs.first.charAt(0), rhs.first.charAt(0));
    //  }
    //});
    //return viewInfo;
  }

  class InfoAdapter extends BaseExpandableListAdapter {
    Map<String, List<Pair<String, String>>> data;
    String[] index;

    public void setData(Map<String, List<Pair<String, String>>> data, String[] index) {
      setData(data);
      setIndex(index);
      notifyDataSetChanged();
    }

    private void setData(Map<String, List<Pair<String, String>>> data) {
      this.data = data;
    }

    private void setIndex(String[] index) {
      this.index = index;
    }

    @Override
    public int getGroupCount() {
      return data == null ? 0 : data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
      return data.get(index[groupPosition]).size();
    }

    @Override
    public List<Pair<String, String>> getGroup(int groupPosition) {
      return data.get(index[groupPosition]);
    }

    @Override
    public Pair<String, String> getChild(int groupPosition, int childPosition) {
      return getGroup(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
      return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
      return 0;
    }

    @Override
    public boolean hasStableIds() {
      return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
      ViewHolder viewHolder = null;
      if (convertView == null) {
        viewHolder = new ViewHolder();
        convertView = getLayoutInflater().inflate(R.layout.list_item_view_info_detail, null);
        viewHolder.textName = (TextView) convertView.findViewById(R.id.text_detail_name);
        viewHolder.textValue = (TextView) convertView.findViewById(R.id.text_detail_value);
        convertView.setTag(viewHolder);
      } else {
        viewHolder = (ViewHolder) convertView.getTag();
      }
      viewHolder.textName.setText(index[groupPosition]);
      return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
        ViewGroup parent) {
      ViewHolder viewHolder = null;
      if (convertView == null) {
        viewHolder = new ViewHolder();
        convertView = getLayoutInflater().inflate(R.layout.list_item_view_info_detail, null);
        viewHolder.textName = (TextView) convertView.findViewById(R.id.text_detail_name);
        viewHolder.textValue = (TextView) convertView.findViewById(R.id.text_detail_value);
        convertView.setTag(viewHolder);
      } else {
        viewHolder = (ViewHolder) convertView.getTag();
      }
      Pair<String, String> child = getChild(groupPosition, childPosition);
      viewHolder.textName.setText(child.first);
      viewHolder.textValue.setText(child.second);
      return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
      return false;
    }
  }

  class ViewHolder {
    TextView textName;
    TextView textValue;
  }
}
