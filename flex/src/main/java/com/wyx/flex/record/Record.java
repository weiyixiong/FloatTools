package com.wyx.flex.record;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import java.util.List;

/**
 * @author weiyixiong
 * @version 创建时间: 2016/10/24 15:08
 */
@Table(name = "Record")
public class Record extends Model {
  @Column(name = "name") String name;
  @Column(name = "time") long time;

  public Record() {
    super();
  }

  public static List<Record> getAllRecord() {
    return new Select().all().from(Record.class).execute();
  }
}