package com.wyx.flex.record;

import android.view.MotionEvent;

public class RecordEvent {
  private EventType type;
  private MotionEvent event;
  private String text;
  private float x;
  private float y;
  private long time;
  private String resName;

  public RecordEvent(MotionEvent event, long time) {
    this.event = event;
    this.type = EventType.TOUCH;
    this.time = time;
  }

  public RecordEvent(String text, float x, float y, long time) {
    this.type = EventType.EDIT;
    this.text = text;
    this.x = x;
    this.y = y;
    this.time = time;
  }

  public RecordEvent(String viewId, String s, long currentTime) {
    this.resName = viewId;
    this.text = s;
    this.time = currentTime;
  }

  public EventType getType() {
    return type;
  }

  public MotionEvent getEvent() {
    return event;
  }

  public long getTime() {
    return time;
  }

  public String getText() {
    return text;
  }

  public String getResName() {
    return resName;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }
}
