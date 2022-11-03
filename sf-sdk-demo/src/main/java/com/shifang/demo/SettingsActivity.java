package com.shifang.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.shifang.weight.demo.R;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    //================================= 算法参数 =========================================//
    private EditText etRecognizeThreshold;
    private CheckBox cbForbidAbnormalFeedback;
    private CheckBox cbForbidAbnormalRecognize;

    //================================= 联网优化 =========================================//
    private CheckBox cbOnlineOptimizeEnabled;
    private EditText etOnlineOptimizeServer;

    //================================= 联网识别 =========================================//
    private EditText etOnlineRecognizeServer;

    private SettingsConfig settingsConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initBundleData(savedInstanceState);
        initView();
    }

    protected void initBundleData(Bundle bundle) {
        settingsConfig = SettingsConfig.getSavedConfigInfo(SettingsActivity.this);
    }

    protected void initView() {
        etRecognizeThreshold = findViewById(R.id.et_recognize_threshold);
        etRecognizeThreshold.setText(String.valueOf(settingsConfig.compareThreshold));

        cbForbidAbnormalRecognize = findViewById(R.id.cb_forbid_recognize);
        cbForbidAbnormalRecognize.setChecked(settingsConfig.forbidAbnormalRecognize);

        cbForbidAbnormalFeedback = findViewById(R.id.cb_forbid_feedback);
        cbForbidAbnormalFeedback.setChecked(settingsConfig.forbidAbnormalFeedback);

        cbOnlineOptimizeEnabled = findViewById(R.id.cb_online_optimize_enabled);
        cbOnlineOptimizeEnabled.setChecked(settingsConfig.onlineOptimizeEnabled);

        etOnlineOptimizeServer = findViewById(R.id.et_online_optimize_server);
        etOnlineOptimizeServer.setText(settingsConfig.onlineOptimizeServer);

        etOnlineRecognizeServer = findViewById(R.id.et_online_reconize_server);
        etOnlineRecognizeServer.setText(settingsConfig.recognitionServer);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            confirmClick();
        } else if (id == R.id.btn_back) {
            finish();
        }
    }

    private void confirmClick() {
        try {
            String thresholdStr = etRecognizeThreshold.getText().toString();
            if(TextUtils.isEmpty(thresholdStr)){
                settingsConfig.compareThreshold = 0.05f;
            }else{
                float threshold = Float.parseFloat(thresholdStr);
                if(threshold > 0 && threshold < 1){
                    settingsConfig.compareThreshold = threshold;
                }else{
                    throw new RuntimeException("阈值需要在0~1之间");
                }
            }

            boolean forbidAbnormalRecognizeChecked = cbForbidAbnormalRecognize.isChecked();
            settingsConfig.forbidAbnormalRecognize = forbidAbnormalRecognizeChecked;

            boolean forbidAbnormalFeedbackChecked = cbForbidAbnormalFeedback.isChecked();
            settingsConfig.forbidAbnormalFeedback = forbidAbnormalFeedbackChecked;

            boolean onlineOptimizeEnabledChecked = cbOnlineOptimizeEnabled.isChecked();
            settingsConfig.onlineOptimizeEnabled = onlineOptimizeEnabledChecked;
            if(onlineOptimizeEnabledChecked){
                String optimizeServer = etOnlineOptimizeServer.getText().toString();
                settingsConfig.onlineOptimizeServer = optimizeServer;
            }

            String onlineRecognizeServer = etOnlineRecognizeServer.getText().toString();
            settingsConfig.recognitionServer = onlineRecognizeServer;

            settingsConfig.saveConfigInfo(SettingsActivity.this);

            Log.d("shifang_demo", "SettingsActivity confirmClick params:" + settingsConfig.printConfigInfo());
            Toast.makeText(this, "参数设置成功", Toast.LENGTH_SHORT).show();

        } catch(Exception e){
            Toast.makeText(this, "参数设置失败:" + e.getMessage(), Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
