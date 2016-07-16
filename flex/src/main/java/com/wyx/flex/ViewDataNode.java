package com.wyx.flex;

import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author weiyixiong
 * @version 创建时间: 2016/07/16 下午2:36
 */
public class ViewDataNode {
  List<ViewDataNode> child;
  Map<String, ViewDataNode> children;
  Map<Short, String> originData;
  Map<String, String> data;
  ViewDataNode parent;

  public ViewDataNode(ViewDataNode parent) {
    this.parent = parent;
  }

  public Map<String, String> getData() {
    return data;
  }

  public void setData(Map<String, String> data) {
    this.data = data;
  }

  public void setOriginData(Map<Short, String> originData) {
    this.originData = originData;
  }

  public Map<Short, String> getOriginData() {
    return originData;
  }

  public void addChild(ViewDataNode viewDataNode) {
    if (child == null) {
      child = new ArrayList<>();
    }
    child.add(viewDataNode);
  }
}
