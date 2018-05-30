package com.wyx.flexdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((EditText) findViewById(R.id.textinput1)).setText("Hello World!");
        ((EditText) findViewById(R.id.textinput2)).setText("Hello World!");
      }
    });
    findViewById(R.id.test_btn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog();
      }
    });
    findViewById(R.id.test_btn).setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Intent i = new Intent(MainActivity.this, SecondActivity.class);
        startActivity(i);
        return false;
      }
    });
  }

  protected void dialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    builder.setMessage("确认退出吗？");
    builder.setTitle("提示");
    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Toast.makeText(getBaseContext(), "123123123", Toast.LENGTH_SHORT).show();
      }
    });
    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Toast.makeText(getBaseContext(), "cancel", Toast.LENGTH_SHORT).show();
      }
    });
    builder.create().show();
  }
}