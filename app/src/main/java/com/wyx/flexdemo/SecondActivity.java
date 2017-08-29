package com.wyx.flexdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * @author dell E-mail: dell@tigerbrokers.com
 * @version 创建时间: 2016/10/14 19:16
 */

public class SecondActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.test_btn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(getBaseContext(), "sdfsd", Toast.LENGTH_SHORT).show();
      }
    });
  }

}
