package com.wyx.flex.record;

import android.view.MotionEvent;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "RecordEvent")
public class RecordEvent extends Model {
  @Column(name = "recordId") long recordId;
  @Column(name = "EventType") EventType type;
  @Column(name = "event") MotionEvent event;
  @Column(name = "String") String text;
  @Column(name = "x") float x;
  @Column(name = "y") float y;
  @Column(name = "time") long time;
  @Column(name = "resName") String resName;

  public RecordEvent() {
    super();
  }

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
