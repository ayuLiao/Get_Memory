package com.ayu.showmethememory;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ayu.showmethememory.server.GetMemoryServer;
import com.ayu.showmethememory.server.LookServer;
import com.ayu.showmethememory.utils.IsServerRunning;
import com.ayu.showmethememory.utils.SharedPerferenceUtil;

import java.util.List;

public class MainActivity extends Activity {

    private Button bt;
    private Switch sw;
    private Context context;
    private TextView tvNum;
    private SharedPerferenceUtil sharedPerferenceUtil;
    private String HONBBO_SHU = "hongbao_shu";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_main);
        sw = (Switch) findViewById(R.id.onServer);
        tvNum = (TextView) findViewById(R.id.hongbaoshu);
        if (sharedPerferenceUtil == null) {
            sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int a = sharedPerferenceUtil.getInteger(HONBBO_SHU, 0);
        tvNum.setText(a+"");
        if (IsServerRunning.isStartAccessibilityService(this, "GetMemoryServer")) {
            sw.setChecked(true);
            sw.setText("红包服务已开启");
            //绿色
            sw.setTextColor(Color.rgb(26, 188, 156));
        }else{
            sw.setChecked(false);
            Toast.makeText(MainActivity.this, "红包鸟服务被系统强制关闭，请再次开启:)", Toast.LENGTH_SHORT).show();
            sw.setText("红包服务已关闭");
            //红色
            sw.setTextColor(Color.rgb(236, 73, 78));
        }
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {

                    Intent start_server = new Intent(context, LookServer.class);
                    startService(start_server);

                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);

                }else{
                    Intent stop_server = new Intent(context, LookServer.class);
                    stopService(stop_server);
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this,"关闭红包鸟服务可能会错过一个亿 :(",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
