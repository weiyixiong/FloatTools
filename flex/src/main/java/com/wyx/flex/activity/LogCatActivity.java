package com.wyx.flex.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.wyx.flex.R;
import com.wyx.flex.util.FIleUtil;
import com.wyx.flex.util.LogCatUtil;
import java.io.File;

/**
 * @author winney
 * @version 创建时间: 2016/08/05 上午11:29
 */

public class LogCatActivity extends AppCompatActivity {
  TextView logcat;
  EditText filter;
  LogCatUtil.LogcatUpdateListener logcatUpdateListener;
  Button save;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logcat);
    logcat = findViewById(R.id.tv_loginfo);
    save = findViewById(R.id.btn_save);
    filter = findViewById(R.id.tv_filter);
    logcatUpdateListener = new LogCatUtil.LogcatUpdateListener() {
      @Override
      public void onUpdate(final String log) {
        logcat.post(new Runnable() {
          @Override
          public void run() {
            logcat.setText(log);
          }
        });
      }
    };
    startLogCat();

    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LogCatActivity.this);
        builder.setTitle("文件名");

        final EditText input = new EditText(LogCatActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            FIleUtil.verifyStoragePermissions(LogCatActivity.this);
            final String downloadPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String fileName = input.getText().toString();
            File saved = new File(downloadPath + "/" + fileName + ".txt");
            Toast.makeText(LogCatActivity.this, "Saved at " + saved.getPath(), Toast.LENGTH_LONG).show();
            FIleUtil.saveToSdCard(logcat.getText().toString(), saved.getPath());
          }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });

        builder.show();
      }
    });
    filter.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        LogCatUtil.setKeyword(charSequence.toString());
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
  }

  @Override
  protected void onPause() {
    LogCatUtil.removeUpdateListener(logcatUpdateListener);
    super.onPause();
  }

  private void startLogCat() {
    LogCatUtil.addUpdateListener(logcatUpdateListener);
  }
}
