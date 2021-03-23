package com.aaa.new2;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;//预览摄像头
    private SurfaceHolder surfaceHolder;
    private Button button;//拍照按钮
    private Camera camera;//摄像头

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String content = this.readAssetsTxt(this, "aaa");
        TextView t = (TextView) findViewById(R.id.txtContext);
        t.setText(content);

        initView();
        initData();
        initListener();
    }

    //初始化View的方法,其实少的话都放到
    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.main_surface_view);
        button = (Button) findViewById(R.id.main_button);
    }

    private void initData() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void initListener() {
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "surfaceView", Toast.LENGTH_SHORT).show();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(safeToTakePicture){

                    Camera.Parameters param = camera.getParameters();
                    param.setPictureSize(1920, 1080);//如果不设置会按照系统默认配置最低160x120分辨率
                    param.set("orientation", "portrait");
                    param.set("rotation", 90);
                    camera.setParameters(param);
                    camera.takePicture(null, null, pictureCallback);
                    //Toast.makeText(MainActivity.this, "Yeah!", Toast.LENGTH_SHORT).show();
                    safeToTakePicture=false;
                }
            }
        });
    }

    private boolean safeToTakePicture = true;
    Bitmap mBitmap;
    public Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
// TODO Auto-generated method stub
            Log.i("ygy", "onPictureTaken");
            Toast.makeText(getApplicationContext(), "正在保存...", Toast.LENGTH_LONG).show();
            mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            String DCIM=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            String DIRECTORY = DCIM + "/Camera";
            String path=DIRECTORY+"/"+String.valueOf(System.currentTimeMillis()) + ".png";
            File file = new File(path);
            try {
                file.setWritable(true);
                file.createNewFile();
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                Toast.makeText(getApplicationContext(), "图像保存成功", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
            safeToTakePicture=true;
        }
    };

    private void initCamera() {
        camera.startPreview();
        camera.setDisplayOrientation(90);//将预览旋转90度
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            if (null != camera) {
                camera.release();
                camera = null;
            }
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "启动摄像头失败,请开启摄像头权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != camera) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    public String readAssetsTxt(Context context, String fileName){
        try {
            //Return an AssetManager instance for your application's package
            InputStream is = context.getAssets().open(fileName+".txt");
            int size = is.available();
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string.
            String text = new String(buffer, "utf-8");
            // Finally stick the string into the text view.
            return text;
        } catch (IOException e) {
            // Should never happen!
//            throw new RuntimeException(e);
            e.printStackTrace();
        }
        return "读取错误，请检查文件名";
    }
}
