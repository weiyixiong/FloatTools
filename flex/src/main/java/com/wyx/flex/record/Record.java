package com.wyx.flex.record;

import com.activeandroidlib.Model;
import com.activeandroidlib.annotation.Column;
import com.activeandroidlib.annotation.Table;
import com.activeandroidlib.query.Select;
import java.util.List;

/**
 * @author weiyixiong
 * @version 创建时间: 2016/10/24 15:08
 */
@Table(name = "Record")
public class Record extends Model {
  @Column(name = "startActivity") String activityName;
  @Column(name = "name") String name;
  @Column(name = "time") long time;

  public Record() {
    super();
  }

  public String getActivityName() {
    return activityName;
  }

  public String getName() {
    return name;
  }

  public long getTime() {
    return time;
  }

  public static List<Record> getAllRecord() {
    return new Select().all().from(Record.class).execute();
  }

  public static Record getRecordById(long recordId) {
    return new Select().from(Record.class).where("Id=?", recordId).executeSingle();
  }
}
