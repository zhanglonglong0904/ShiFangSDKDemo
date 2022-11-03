package com.shifang.demo.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

    private static final Handler UTIL_HANDLER       = new Handler(Looper.getMainLooper());

    public static void makeDir(String var0) {
        if (!TextUtils.isEmpty(var0)) {
            File var1 = new File(var0);
            if (!var0.endsWith("/")) {
                var1 = var1.getParentFile();
            }

            if (!var1.exists()) {
            }

            var1.mkdirs();
        }
    }

    public static void copyAssetsFile(Context var0, String var1, String var2) throws IOException {
        InputStream var3 = null;
        FileOutputStream var4 = null;

        try {
            AssetManager var5 = var0.getAssets();
            var3 = var5.open(var1);
            makeDir(var2);
            var4 = new FileOutputStream(var2, false);
            byte[] var6 = new byte[10240];
            boolean var7 = false;

            int var11;
            while((var11 = var3.read(var6)) > 0) {
                var4.write(var6, 0, var11);
            }
        } finally {
            if (var3 != null) {
                var3.close();
            }

            if (var4 != null) {
                var4.close();
            }

        }

    }


    /**
     * 将文本文件中的内容读入到buffer中
     *
     * @param buffer   buffer
     * @param filePath 文件路径
     * @throws IOException 异常
     */
    public static void readToBuffer(StringBuffer buffer, String filePath) throws IOException {
        InputStream is = new FileInputStream(filePath);
        String line; // 用来保存每行读取的内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        line = reader.readLine(); // 读取第一行
        while (line != null) { // 如果 line 为空说明读完了
            buffer.append(line); // 将读到的内容添加到 buffer 中
            buffer.append("\n"); // 添加换行符
            line = reader.readLine(); // 读取下一行
        }
        reader.close();
        is.close();
    }

    /**
     * 读取文本文件内容
     *
     * @param filePath 文件所在路径
     * @return 文本内容
     * @throws IOException 异常
     */
    public static String readFile(String filePath) throws IOException {
        StringBuffer sb = new StringBuffer();
        readToBuffer(sb, filePath);
        return sb.toString();
    }

    public static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            Utils.UTIL_HANDLER.post(runnable);
        }
    }

    private static ProgressDialog progressDialog;
    public static void showProgress(Context context, String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog != null && progressDialog.isShowing()){
                    progressDialog.setMessage(message) ;
                }else{
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage(message) ;		// 设置显示信息
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.onStart() ;			// 启动进度条
                    progressDialog.show() ;				// 显示对话框【使用new创建ProgressDialog需要 show()】
                }
            }
        });
    }

    public static void hideProgress(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
