package com.shifang.demo;

import android.content.Context;
import android.text.TextUtils;

import com.shifang.recognition.util.GsonUtils;
import com.shifang.recognition.util.SPUtils;

public class SettingsConfig {

    private static final String SP_NAME = "st_cfg";

    public float compareThreshold = 0.05f;
    public boolean forbidAbnormalRecognize = false;   // 异常图像不进行识别
    public boolean forbidAbnormalFeedback = false;   // 异常图像不进行学习
    public boolean onlineOptimizeEnabled = true;  // 是否开启联网模型优化：数据采集+自动推送新模型
    public String onlineOptimizeServer = null;  // onlineOptimizeEnabled为true时，值为null表示使用默认地址
    public String recognitionServer = null;   // 联网识别server,为null则使用离线识别

    private SettingsConfig() {
        super();
    }

    public void saveConfigInfo(Context context){
        SPUtils.getInstance(context).put(SP_NAME, GsonUtils.toJson(this, true));
    }

    public static SettingsConfig getSavedConfigInfo(Context context){
        String configJson = SPUtils.getInstance(context).getString(SP_NAME);

        SettingsConfig settingsConfig = Holder.instance;

        try {
            if(!TextUtils.isEmpty(configJson)){
                settingsConfig = GsonUtils.fromJson(configJson, SettingsConfig.class);
            }
        } catch(Exception e){
            e.printStackTrace();
        }

        return settingsConfig;
    }

    public String printConfigInfo() {
        return "SettingsConfig{" +
                "compareThreshold=" + compareThreshold +
                ", forbidAbnormalRecognize=" + forbidAbnormalRecognize +
                ", forbidAbnormalFeedback=" + forbidAbnormalFeedback +
                ", onlineOptimizeEnabled=" + onlineOptimizeEnabled +
                ", onlineOptimizeServer='" + onlineOptimizeServer + '\'' +
                ", recognitionServer='" + recognitionServer + '\'' +
                '}';
    }

    private static class Holder {
        private static SettingsConfig instance = new SettingsConfig();
    }
}
