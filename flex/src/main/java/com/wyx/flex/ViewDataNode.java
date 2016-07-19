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
  private Map<String, ViewDataNode> children;
  private Map<Short, String> originData;
  private Map<String, String> data;
  private ViewDataNode parent;

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

  public ViewDataNode getParent() {
    return parent;
  }
}
