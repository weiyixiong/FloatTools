package com.wyx.flex;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DetailInfoActivity extends Activity {
  private static final String VIEW_HASH_CODE = "hashcode";
  private ListView detailInfo;

  private InfoAdapter infoAdapter;
  private int hashCode;
  BroadcastReceiver finishReceiver;

  public static void putExtra(Intent intent, int hashcode) {
    intent.putExtra(VIEW_HASH_CODE, hashcode);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_info_detail);

    List<Pair<String, String>> viewInfo = initData();
    initView(viewInfo);

    registerResetRecevier();
  }

  private void registerResetRecevier() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(FloatTools.RESET);
    finishReceiver = new BroadcastReceiver() {

      @Override public void onReceive(Context context, Intent intent) {
        finish();
      }
    };
    registerReceiver(finishReceiver, filter);
  }

  @Override protected void onStop() {
    super.onStop();
    unregisterReceiver(finishReceiver);
  }

  private void initView(List<Pair<String, String>> viewInfo) {
    detailInfo = (ListView) findViewById(R.id.detail_info);
    infoAdapter = new InfoAdapter(getBaseContext());
    infoAdapter.addAll(viewInfo);
    detailInfo.setAdapter(infoAdapter);
  }

  @NonNull private List<Pair<String, String>> initData() {
    hashCode = getIntent().getIntExtra(VIEW_HASH_CODE, 0);
    List<Pair<String, String>> viewInfo = ViewParser.getViewInfo(hashCode);
    if (viewInfo == null) {
      Toast.makeText(getApplicationContext(), "无信息", Toast.LENGTH_SHORT).show();
      return new ArrayList<>();
    }
    Collections.sort(viewInfo, new Comparator<Pair<String, String>>() {
      @Override public int compare(Pair<String, String> lhs, Pair<String, String> rhs) {
        return Integer.compare(lhs.first.charAt(0), rhs.first.charAt(0));
      }
    });
    return viewInfo;
  }

  class InfoAdapter extends MyArrayAdapter<Pair<String, String>> {

    public InfoAdapter(Context context) {
      super(context, 0);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
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
      viewHolder.textName.setText(getItem(position).first);
      viewHolder.textValue.setText(getItem(position).second);
      return convertView;
    }
  }

  class ViewHolder {
    TextView textName;
    TextView textValue;
  }
}
