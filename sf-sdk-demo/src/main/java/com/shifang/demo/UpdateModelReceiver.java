package com.shifang.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shifang.recognition.SFAiFreshManager;

public class UpdateModelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null){
            return;
        }

        Log.i("shifang", "reload feature listener onReceive action:" + intent.getAction());

        if(intent.getAction().equalsIgnoreCase(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_ACTION_START_EXTRACT)){

            // EventBus 可以用来给主界面发送message，然后在主界面给出相关提示；
//            EventBus.getDefault().post(new UpgradeModelEvent(UpgradeModelEvent.CODE_START, "开始升级模型"));
            Log.i("shifang", "开始升级模型");

        } else if(intent.getAction().equalsIgnoreCase(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_ACTION_FINISH_EXTRACT)){
            // 收到重新加载模型&算法通知；
//            EventBus.getDefault().post(new UpgradeModelEvent(UpgradeModelEvent.CODE_FINISHED, "升级模型完成"));

            Log.i("shifang", "模型升级完成，重启后生效");

            // TODO 模型升级完成，在应用重启后生效

        }else if(intent.getAction().equalsIgnoreCase(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_ACTION_PROCESS_EXTRACT)){
            int total = intent.getIntExtra(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_EXTRA_PROCESS_EXTRACT_TOTAL, 0);
            int current = intent.getIntExtra(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_EXTRA_PROCESS_EXTRACT_CURRENT, 0);

            Log.i("shifang", "模型升级中【请勿退出软件】，共:" + total + "个，当前进度:" + current);

        }else if(intent.getAction().equalsIgnoreCase(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_ACTION_EXCEPTION_EXTRACT)){
            String message = intent.getStringExtra(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_EXTRA_EXCEPTION_EXTRACT_MESSAGE);
            Log.i("shifang", "模型升级出现异常，异常信息：" + message);
        }
    }
}
