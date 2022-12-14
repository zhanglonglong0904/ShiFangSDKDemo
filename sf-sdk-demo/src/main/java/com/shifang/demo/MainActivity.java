package com.shifang.demo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.shifang.demo.bean.StoredProductBean;
import com.shifang.demo.camera.CameraWrapper;
import com.shifang.demo.utils.Utils;
import com.shifang.recognition.SFAiFreshCallback;
import com.shifang.recognition.SFAiFreshManager;
import com.shifang.recognition.SFAiFreshParam;
import com.shifang.recognition.SFErrorCode;
import com.shifang.recognition.bean.CropRect;
import com.shifang.recognition.bean.MatchResult;
import com.shifang.recognition.bean.SFProcessStatus;
import com.shifang.recognition.bean.SFProductInfo;
import com.shifang.recognition.bean.SFRecognizedResult;
import com.shifang.recognition.offline.IExtractServiceInterface;
import com.shifang.recognition.service.ExtractFeatureService;
import com.shifang.weight.demo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private final int ShowResultCount = 5;   // ????????????????????????

    //================================= ??????UI =========================================//
    private TextView mTvResult;
    private Button mBtnIden;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CameraWrapper mCamera;
    private Bitmap mCameraImage;

    private RenderScript renderScript;
    private Allocation allocationIn;
    private Allocation allocationOut;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;

    private Matrix mMatrix;
    private RecyclerView mRvProducts;
    private RecyclerAdapter mAdapter;
    private MyRecyclerItemClickListener itemClickListener;
    private HashMap<String, StoredProductBean> storedProductBeanList;

    private long startTime;
    //================================= ??????UI =========================================//

    private SettingsConfig settingsConfig;

    private SFAiFreshManager freshManager;
    /**??????????????????????????????????????????????????????????????????????????????*/
    private SFRecognizedResult recognizedResult;

    private IExtractServiceInterface extractBinder;
    private ExtractFeatureServiceConnection extractFeatureServiceConnection;
    private UpdateModelReceiver reloadFeatureListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
    }

    private void initData() {
        mMatrix = new Matrix();
        renderScript = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));
        mCamera = new CameraWrapper(MainActivity.this, mCamePreviewCallback, mSurfaceHolder);

        settingsConfig = SettingsConfig.getSavedConfigInfo(MainActivity.this);

        storedProductBeanList = readStoredProductBeanListFromSDCard();
        itemClickListener = new MyRecyclerItemClickListener();
        mAdapter = new RecyclerAdapter(this);
        mAdapter.setData(storedProductBeanList);
        mAdapter.setOnItemClickListener(itemClickListener);

        freshManager = SFAiFreshManager.getInstance(this.getApplicationContext());

        // TODO ??????getParam????????????param???????????????????????????new SFAiFreshParam???????????????????????????
        SFAiFreshParam freshParam = freshManager.getParam();
        // FIXME ???????????????????????????????????????????????????????????????????????????320:240????????????????????????
        freshParam.cropRect = new CropRect(0, 0, 640, 480);
        freshManager.setParam(freshParam);

        freshManager.setProcessCallback(sfProcessCallback);
        freshManager.loadFeature();   // ??????SDK?????????????????????????????????SDK?????????????????????????????????????????????

        extractFeatureServiceConnection = new ExtractFeatureServiceConnection();
        reloadFeatureListener = new UpdateModelReceiver();

        // TODO ???????????????????????????????????????&??????????????????????????????
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_ACTION_FINISH_EXTRACT);
        intentFilter.addAction(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_ACTION_START_EXTRACT);
        intentFilter.addAction(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_ACTION_PROCESS_EXTRACT);
        intentFilter.addAction(SFAiFreshManager.ModelUpgradeBroadcast.BROADCAST_ACTION_EXCEPTION_EXTRACT);
        registerReceiver(reloadFeatureListener, intentFilter);

        /** {@link UpdateModelReceiver} */
        // TODO ExtractFeatureService ???SDK??????????????????????????????????????????????????????????????????????????????????????????????????? ReloadFeatureListener ?????????
        startService(new Intent(this, ExtractFeatureService.class));
        Intent intent = new Intent(this, ExtractFeatureService.class);
        bindService(intent, extractFeatureServiceConnection, Service.BIND_AUTO_CREATE);
    }

    private void initView() {
        mTvResult = findViewById(R.id.tv_result);
        mBtnIden = findViewById(R.id.btn_iden);
        mRvProducts = findViewById(R.id.rv_content);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRvProducts.setLayoutManager(manager);
        mRvProducts.setAdapter(mAdapter);

        mSurfaceView = findViewById(R.id.surface);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(surfaceCallback);
        mSurfaceHolder.setKeepScreenOn(true);
    }

    public void idenClick(View view){
        recognize();
    }

    @Override
    protected void onDestroy() {
        mCamera.closeCamera();

        if(extractFeatureServiceConnection != null && extractBinder != null){
            try {
                extractBinder.stopExtract();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(extractFeatureServiceConnection);
        }

        stopService(new Intent(this, ExtractFeatureService.class));

        if(reloadFeatureListener != null){
            unregisterReceiver(reloadFeatureListener);
        }

        if(freshManager != null){
            freshManager.release();
        }

        super.onDestroy();

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private synchronized void recognize(){
        startTime = System.currentTimeMillis();

        updateResultTips("", false);

        if(mCameraImage == null){
            updateResultTips("????????????????????????????????????????????????", false);
            return;
        }

        Bitmap processImage = mCameraImage;

        try {
            SFRecognizedResult sfRecognizedResult = new SFRecognizedResult();
            SFErrorCode recognizeResultCode = freshManager.recognize(processImage, ShowResultCount, sfRecognizedResult);
            if(recognizeResultCode.isOk()){
                processRecognizedResult(sfRecognizedResult);
            }else{
                updateResultTips("???????????????" + recognizeResultCode, true);
            }
        } catch(Throwable e){
            updateResultTips("???????????????" + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void processRecognizedResult(SFRecognizedResult sfRecognizedResult) {
        if(sfRecognizedResult == null){
            return;
        }

        ArrayList<MatchResult> finalMatchResults = processMatchResult(sfRecognizedResult.getMatchResults());  // ??????????????????????????????????????????????????????
        sfRecognizedResult.setMatchResults(finalMatchResults);   // processMatchResult?????????????????????????????????????????????recognizedResult???matchResult?????????????????????finalMatchResults

        recognizedResult = sfRecognizedResult;
    }

    private ArrayList<MatchResult> processMatchResult(List<MatchResult> matchResults) {
        ArrayList<MatchResult> finalMatchResults = new ArrayList<>();
        ArrayList<StoredProductBean> recognizedProductResults = new ArrayList<>();

        for(MatchResult mr: matchResults) {
            if(mr.score < settingsConfig.compareThreshold){
                continue;
            }
            finalMatchResults.add(mr);
        }

        for (int i = 0; i < finalMatchResults.size() && recognizedProductResults.size() <= ShowResultCount; i++) {
            MatchResult matchResult = finalMatchResults.get(i);

            StoredProductBean storedProductBean = storedProductBeanList.get(matchResult.code);
            if (storedProductBean != null) {
                recognizedProductResults.add(storedProductBean);
            }
        }

        String recognizedResultStr = getRecognizedResultStr(recognizedProductResults);
        String message = "???????????????" + recognizedResultStr + "??? ?????????" + (System.currentTimeMillis() - startTime) + "ms" + ", SCORE:" + finalMatchResults;
        updateResultTips(message, false);

        Log.i("shifang_demo", "MainActivity.java MainActivity matchFeature :" + recognizedResultStr);
        return finalMatchResults;   // ??????????????????????????????feedback
    }

    private void feedback(StoredProductBean storedProductBean) {
        if(recognizedResult == null){
            updateResultTips("???????????????????????????", true);
            return;
        }

        SFProductInfo sfProductInfo = new SFProductInfo();
        sfProductInfo.setCode(storedProductBean.getCode());
        sfProductInfo.setName(storedProductBean.getName());
        sfProductInfo.setPrice(storedProductBean.getPrice());
        sfProductInfo.setWeight(storedProductBean.getWeight());

        SFErrorCode sfResultInfo = freshManager.feedback(recognizedResult, sfProductInfo);
        updateResultTips(sfResultInfo.toString(), false);
        Log.i("shifang_demo", "feedback result sync sfResultInfo:" + sfResultInfo);
    }

    // ??????learn???????????????????????????????????????????????????????????? ?????????????????????????????????????????????????????????????????????learn???????????????(recognize)+??????(feedback)?????????
    private void learn(StoredProductBean storedProductBean) {
        updateResultTips("", false);

        if(mCameraImage == null){
            updateResultTips("????????????????????????????????????????????????", false);
            return;
        }

        Bitmap processImage = mCameraImage;

        SFProductInfo sfProductInfo = new SFProductInfo();
        sfProductInfo.setCode(storedProductBean.getCode());
        sfProductInfo.setName(storedProductBean.getName());
        sfProductInfo.setPrice(storedProductBean.getPrice());
        sfProductInfo.setWeight(storedProductBean.getWeight());

        SFErrorCode sfResultInfo = freshManager.learn(processImage, sfProductInfo);
        updateResultTips(sfProductInfo.getName() + ":" + sfResultInfo.toString() + "-" + System.currentTimeMillis(), false);
        Log.i("shifang_demo", "learn result sfResultInfo:" + sfResultInfo);
    }

    private void updateResultTips(String message, boolean showToast){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvResult.setText(message);

                if(showToast){
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String getRecognizedResultStr(List<StoredProductBean> storedProductBeanList) {
        String result = "";
        for (int i = 0; i < storedProductBeanList.size(); i++) {
            result += storedProductBeanList.get(i).name + "  ";
        }
        return "[" + result + "]";
    }

    private HashMap<String, StoredProductBean> convertStoredProductBeanListFromJson(String json){
        HashMap<String, StoredProductBean> map = new LinkedHashMap<>();

        try {
            Gson gson = new Gson();
            JsonArray arry = new JsonParser().parse(json).getAsJsonArray();
            for (JsonElement jsonElement : arry) {
                StoredProductBean storedProductBean = gson.fromJson(jsonElement, StoredProductBean.class);
                map.put(storedProductBean.code, storedProductBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    private HashMap<String, StoredProductBean> readStoredProductBeanListFromSDCard() {
        String file = "/sdcard/products.json";
        String readJson = "";

        try {
            Utils.copyAssetsFile(this, "products.json", file);
            readJson = Utils.readFile(file);

            if(TextUtils.isEmpty(readJson)){
                String message = "????????????????????????";
                updateResultTips(message, true);
            }
        } catch(Exception e){
            String message = "?????????????????????????????????" + e.getMessage();
            updateResultTips(message, true);
        }

        return convertStoredProductBeanListFromJson(readJson);
    }

    private float getSurfaceViewScaleX(){
        int surfaceViewWidth = mSurfaceView.getMeasuredWidth();
        float scaleX = 1.0f * surfaceViewWidth / CameraWrapper.WIDTH;
        return scaleX;
    }

    private float getSurfaceViewScaleY(){
        int surfaceViewHeight = mSurfaceView.getMeasuredHeight();
        float scaleY = 1.0f * surfaceViewHeight / CameraWrapper.HEIGHT;
        return scaleY;
    }

    private void drawCameraImage() {
        if(mCameraImage == null){
            updateResultTips("????????????????????????????????????????????????", false);
            return;
        }

        Canvas canvas = null;

        try {
            canvas = mSurfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }

            canvas.drawBitmap(mCameraImage, mMatrix, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null && mSurfaceHolder != null) {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private class MyRecyclerItemClickListener implements RecyclerAdapter.OnItemClickListener {
        @Override
        public void onItemClick(View view, StoredProductBean productBean, int position, String tag) {
            StoredProductBean storedProductBean = new StoredProductBean(productBean);
            feedback(storedProductBean);  // TODO ?????????????????????????????????
        }
    }

    /**
     * Android Camera????????????
     */
    private Camera.PreviewCallback mCamePreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (allocationIn == null) {
                Type.Builder yuvType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(data.length);
                allocationIn = Allocation.createTyped(renderScript, yuvType.create(), Allocation.USAGE_SCRIPT);

                Type.Builder rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript))
                        .setX(CameraWrapper.WIDTH)
                        .setY(CameraWrapper.HEIGHT);
                allocationOut = Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SCRIPT);
            }

            mCameraImage = Bitmap.createBitmap(CameraWrapper.WIDTH, CameraWrapper.HEIGHT, Bitmap.Config.ARGB_8888);

            allocationIn.copyFrom(data);
            yuvToRgbIntrinsic.setInput(allocationIn);
            yuvToRgbIntrinsic.forEach(allocationOut);
            allocationOut.copyTo(mCameraImage);

            try {
                drawCameraImage();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    /**
     * SurfaceView??????
     */
    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mMatrix.postScale(getSurfaceViewScaleX(), getSurfaceViewScaleY());
            mCamera.openCamera(0, CameraWrapper.WIDTH, CameraWrapper.HEIGHT);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.closeCamera();
        }
    };

    private SFAiFreshCallback sfProcessCallback = new SFAiFreshCallback() {
        @Override
        public void onFeatureLoadStart() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showProgress(MainActivity.this, "???????????????");
                }
            });

            Log.i("shifang_demo", "MainActivity onLoadStart :");
        }

        @Override
        public void onFeatureLoadComplete() {
            freshManager.optimizeFeatures();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.hideProgress();
                }
            });
            Log.i("shifang_demo", "MainActivity onLoadComplete ");
        }

        @Override
        public void onLoadFeatureError(SFProcessStatus processStatus) {
            Log.i("shifang_demo", "MainActivity onLoadError processStatus:" + processStatus);
        }
    };


    private class ExtractFeatureServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            extractBinder = IExtractServiceInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

}