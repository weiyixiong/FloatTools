package com.wyx.flex.record;

import com.activeandroidlib.Model;
import com.activeandroidlib.annotation.Column;
import com.activeandroidlib.annotation.Table;
import com.activeandroidlib.query.Delete;
import com.activeandroidlib.query.Select;
import java.util.List;

@Table(name = "RecordEvent")
public class RecordEvent extends Model {
  @Column(name = "recordId") long recordId;
  @Column(name = "EventType") EventType type;
  @Column(name = "eventAction") int eventAction;
  @Column(name = "String") String text;
  @Column(name = "x") float x;
  @Column(name = "y") float y;
  @Column(name = "time") long time;
  @Column(name = "resName") String resName;

  public RecordEvent() {
    super();
  }

  public RecordEvent(int action, float x, float y, long time) {
    this.type = EventType.TOUCH;
    this.eventAction = action;
    this.x = x;
    this.y = y;
    this.time = time;
  }

  public static List<RecordEvent> getAllRecordEventByID(long recordId) {
    return new Select().all().from(RecordEvent.class).where("recordId =?", recordId).orderBy("time ASC").execute();
  }

  public static List<RecordEvent> deleteAllRecordEventByID(long recordId) {
    return new Delete().from(RecordEvent.class).where("recordId =?", recordId).execute();
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

  public int getEventAction() {
    return eventAction;
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
