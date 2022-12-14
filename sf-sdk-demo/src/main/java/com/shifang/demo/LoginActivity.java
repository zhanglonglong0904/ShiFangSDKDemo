package com.shifang.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.shifang.demo.utils.Utils;
import com.shifang.recognition.SFAiFreshManager;
import com.shifang.recognition.SFAiFreshParam;
import com.shifang.recognition.SFErrorCode;
import com.shifang.recognition.bean.SFActiveInfo;
import com.shifang.recognition.bean.SFAppInfo;
import com.shifang.weight.demo.R;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText etStoreNo;
    private EditText etLicenseNo;

    private TextView mTvTips;

    private LoginStatusController loginStatusController;

    private String mLicenseNo;
    private String mStoreNo;

    private SFAiFreshManager freshManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ss_login);

        checkPermissions();
        initData();
        initView();
    }

    protected void initData() {
        loginStatusController = new LoginStatusController();
    }

    protected void initView() {
        etLicenseNo = findViewById(R.id.al_et_license_no);
        etStoreNo = findViewById(R.id.al_et_store_no);
        mTvTips = findViewById(R.id.al_tv_tips);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.al_btn_login:
                login();
                break;

            case R.id.fl_btn_settings:
                enterSettings();
                break;
        }
    }

    private void updateTips(String message){
        Utils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvTips.setText(message);
                Log.i("shifang_demo", "message:" + message);
            }
        });
    }

    private void login(){
        Utils.showProgress(this, "?????????...");

        mLicenseNo = etLicenseNo.getText().toString().trim();
        mStoreNo = etStoreNo.getText().toString().trim();

        freshManager = SFAiFreshManager.getInstance(LoginActivity.this);

        loginStatusController.unAuth().work();
    }

    private void enterSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void enterHome(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    protected static final String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    /** ?????????????????? */
    private static int PERMISSION_REQUEST_CODE = 1000;  // ???????????????

    private void checkPermissions() {   // ?????????????????????????????????
        //?????????????????????????????????????????????
        List<String> needRequestPermissionList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissionList.add(perm);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                    needRequestPermissionList.add(perm);
                }
            }
        }
        //????????????
        if (null != needRequestPermissionList && needRequestPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    needRequestPermissionList.toArray(new String[needRequestPermissionList.size()]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            //??????????????????????????????????????????
            boolean flag = true;

            for (int result : paramArrayOfInt) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    flag = false;
                }
            }
            if (!flag) {
                updateTips("????????????????????????");
            }
        }
    }

    private class LoginStatusController implements LoginStatus{

        private final LoginStatusUnAuth loginStatusUnAuth = new LoginStatusUnAuth();
        private final LoginStatusAuthSuccess loginStatusAuthSuccess = new LoginStatusAuthSuccess();

        private LoginStatus mCurrentStatus;

        public LoginStatusController(){
            this.mCurrentStatus = loginStatusUnAuth;
        }

        public LoginStatusController setStatus(LoginStatus status){
            this.mCurrentStatus = status;
            return this;
        }

        public LoginStatusController unAuth(){
            setStatus(loginStatusUnAuth);
            return this;
        }

        public LoginStatusController authSuccess(){
            setStatus(loginStatusAuthSuccess);
            return this;
        }

        @Override
        public void work() {
            new Thread(){
                @Override
                public void run() {
                    try {
                        mCurrentStatus.work();
                    } catch(Exception e){
                        e.printStackTrace();
                        Utils.hideProgress();
                        updateTips("Exception:" + e.getMessage());
                    }
                }
            }.start();
        }
    }

    /**??????????????????*/
    private class LoginStatusAuthSuccess implements LoginStatus{
        @Override
        public void work() {
            updateTips("??????????????????...");
            Utils.showProgress(LoginActivity.this, "??????????????????...");

            initAlgoManager();
        }

        private void initAlgoManager() {
            SettingsConfig settingsConfig = SettingsConfig.getSavedConfigInfo(LoginActivity.this);

            SFAiFreshParam freshParam = new SFAiFreshParam();
            freshParam.forbidAbnormalRecognize = settingsConfig.forbidAbnormalRecognize;
            freshParam.forbidAbnormalFeedback = settingsConfig.forbidAbnormalFeedback;
            freshParam.compareThreshold = settingsConfig.compareThreshold;
            freshParam.onlineOptimizeServer = settingsConfig.onlineOptimizeServer;
            freshParam.onlineOptimizeEnabled = settingsConfig.onlineOptimizeEnabled;
            freshParam.recognitionServer = settingsConfig.recognitionServer;

            if(!TextUtils.isEmpty(freshParam.recognitionServer) || !TextUtils.isEmpty(freshParam.onlineOptimizeServer)){
                // TODO ??????????????????????????????????????????????????????????????????????????????????????????????????????
                SFAppInfo appInfo = new SFAppInfo();
                appInfo.setStoreName("???????????????-???????????????");
                appInfo.setAppVersion("1.0");
                freshManager.setAppInfo(appInfo);
            }

            SFErrorCode result = freshManager.init(freshParam);

            if(result.isOk()){
                updateTips("?????????????????????");
                enterHome();
            }else{
                updateTips("????????????????????????Code???" + result);
            }

            Utils.hideProgress();
        }
    }

    /**?????????????????????*/
    private class LoginStatusUnAuth implements LoginStatus{
        @Override
        public void work() {
            SFErrorCode checkResult = freshManager.checkLicense();    // 1.????????????
            updateTips("work checkresult:" + checkResult);

            if(!checkResult.isOk()){  // 2.1 ??????????????????????????????
                Utils.hideProgress();

                if(TextUtils.isEmpty(mStoreNo) || TextUtils.isEmpty(mLicenseNo)){
                    updateTips("????????????????????????????????????(" + checkResult + ")");
                    return;
                }

                try {
                    updateTips("auth work ??????????????????");

                    SFActiveInfo activeInfo = new SFActiveInfo();
                    activeInfo.storeNo = mStoreNo;
                    activeInfo.licenseNo = mLicenseNo;

                    SFErrorCode activeResult = freshManager.activeDevice(activeInfo);  // 3.????????????
                    if(!activeResult.isOk()){
                        updateTips("??????????????????, ????????????:" + activeResult);
                        return;
                    }

                    loginStatusController.authSuccess().work();

                } catch(Exception e){
                    updateTips("??????????????????, ????????????:" + e.getMessage());
                    e.printStackTrace();
                }

            }else{   // 2.2 ?????????????????????????????????
                loginStatusController.authSuccess().work();
            }
        }

    }

    private interface LoginStatus{
        void work();
    }
}
