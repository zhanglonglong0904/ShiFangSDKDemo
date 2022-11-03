package com.shifang.demo.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.util.List;

@SuppressWarnings("deprecation")
public class CameraWrapper {
    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;

    private Camera mCamera;
    private SurfaceTexture mPreviewSurface;
    private SurfaceHolder mSurfaceHolder;
    private Camera.PreviewCallback mPreviewCallback;

    public Context mContext;

    public boolean isPreviewing = false;

    public CameraWrapper(Context context, Camera.PreviewCallback previewCallback, SurfaceHolder surfaceHolder) {
        this.mContext = context;
        this.mPreviewSurface = new SurfaceTexture(0);
        this.mPreviewCallback = previewCallback;
        this.mSurfaceHolder = surfaceHolder;
    }

    public void openCamera(int cameraID, int imgWidth, int imgHeight) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraID);
            } catch (Exception e) {
                Toast.makeText(mContext, "Camera ID:" + cameraID + " 打开失败，请重试", Toast.LENGTH_LONG).show();
                mCamera = null;
                e.printStackTrace();
                return;
            }
        }

        updateCameraParameters(imgWidth, imgHeight);
        startPreview();
    }

    public void closeCamera() {
        if (mCamera != null) {
            stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public boolean startPreview() {
        if (mCamera == null) {
            return false;
        }
        mCamera.setPreviewCallback(mPreviewCallback);
        mCamera.startPreview();
        isPreviewing = true;

        return true;
    }

    public boolean stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
        }
        isPreviewing = false;

        return true;
    }

    private void updateCameraParameters(int imgWidth, int imgHeight) {
        if (mCamera == null) {
            return;
        }

        try {
            Parameters params = mCamera.getParameters();
            params.setPreviewSize(imgWidth, imgHeight);
            params.setPreviewFormat(ImageFormat.NV21);
            List<String> supportedFlashModes = params.getSupportedFlashModes();
            if (supportedFlashModes != null && supportedFlashModes.contains(Parameters.FLASH_MODE_OFF)) {
                params.setFlashMode(Parameters.FLASH_MODE_OFF); // 设置闪光模式
            }

            List<String> supportedFocusModes = params.getSupportedFocusModes();
            if(supportedFocusModes != null && supportedFocusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                params.setFocusMode(Parameters.FOCUS_MODE_AUTO);  // 设置聚焦模式
            }

            List<String> supportedWhiteBalance = params.getSupportedWhiteBalance();
            if(supportedWhiteBalance != null && supportedWhiteBalance.contains(Parameters.WHITE_BALANCE_AUTO)){
                params.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
            }

            mCamera.setParameters(params);
            mCamera.setPreviewTexture(mPreviewSurface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
