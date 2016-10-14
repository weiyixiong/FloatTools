package com.wyx.flexdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.wyx.flex.util.L;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.test_btn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(MainActivity.this, SecondActivity.class);
        startActivity(i);
      }
    });
  }

  //@Override
  //public boolean dispatchTouchEvent(MotionEvent ev) {
  //  L.e(ev.toString());
  //  return super.dispatchTouchEvent(ev);
  //}
}
