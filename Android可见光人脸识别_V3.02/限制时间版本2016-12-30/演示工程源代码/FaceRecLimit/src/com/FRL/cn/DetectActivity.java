package com.FRL.cn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.face.sv.FaceDetect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 演示人脸识别算法库在摄像头中人脸检测的使用。
 * @author 邹丰
 * @datetime 2016-05-03
 */
public class DetectActivity extends Activity implements Callback, PreviewCallback, OnClickListener {
    private final static String TAG = "DetectActivity";
    private final static int PREVIEW_DELAY = 0x1001;
    private final static int DISPLAY_FACE = 0x1002;
    // 图像是否需要旋转
    private final static boolean isRotate = false;
    // 图像相关参数
    private final static int DEFAULT_ROTATE_VALUE = 90;
    private final static int WIDTH = 320;
    private final static int HEIGHT = 240;
    private static int cameraIndex = 0;
    private static Camera camera;
    private static byte[] camBuf;
    private SurfaceView sView;
    private SurfaceHolder surfaceHolder;
    private static DrawFaceRect mDrawFace = null;  
    private TextView txtScreenSize;
    private TextView txtImageSize;
    private TextView txtDisplaySize;
    private TextView txtLeft;
    private TextView txtTop;
    private TextView txtRight;
    private TextView txtBottom;
    // SurfaceView窗口大小
    private boolean isPreview = false;
    private FaceDetect mDetect;
    private boolean initState = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        sView = (SurfaceView) findViewById(R.id.surfaceView);
        // 获得SurfaceView的SurfaceHolder
        surfaceHolder = sView.getHolder();
        // 为surfaceHolder添加一个回调监听器
        surfaceHolder.addCallback(this);
        // 设置该SurfaceView自己不维护缓冲
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        sView.setKeepScreenOn(true);
  
        FrameLayout pLayout = (FrameLayout)this.findViewById(R.id.pLayout);
        mDrawFace =  new DrawFaceRect(this, getResources().getColor(R.color.face_position));
        // 在一个activity上面添加额外的content
        //addContentView(mDrawFace, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        pLayout.addView(mDrawFace,  new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        mDrawFace.setVisibility(View.VISIBLE);
        
        txtScreenSize = (TextView)this.findViewById(R.id.txtScreenSize);
        txtImageSize = (TextView)this.findViewById(R.id.txtImageSize);
        txtDisplaySize = (TextView)this.findViewById(R.id.txtDisplaySize);
        txtLeft = (TextView)this.findViewById(R.id.txtLeft);
        txtTop = (TextView)this.findViewById(R.id.txtTop);
        txtRight = (TextView)this.findViewById(R.id.txtRight);
        txtBottom = (TextView)this.findViewById(R.id.txtBottom);
        Button btnBack = (Button)this.findViewById(R.id.btnBackD);
        btnBack.setOnClickListener(this);
        
        WindowManager windowManager = getWindowManager();    
        Display display = windowManager.getDefaultDisplay();    
        int screenWidth = screenWidth = display.getWidth();    
        int screenHeight = screenHeight = display.getHeight(); 
        log("screenWidth:" + screenWidth + " screenHeight:" + screenHeight);
            
        // 方法2   
        DisplayMetrics dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);  
        float width=dm.widthPixels*dm.density;   
        float height=dm.heightPixels*dm.density;  
        log("screenWidth:" + width + " screenHeight:" + height);
        txtScreenSize.setText(width + " x " + height);
        
        mDetect =  new FaceDetect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (mDetect != null) {
            mDetect.setDir(FaceApp.libDir, FaceApp.tempDir);
            initState = mDetect.initFaceDetectLib(1);
            Log.e("initFaceDetectLib ret=",""+initState);
            if (initState) {
                log("算法库初始化成功。");
            } else {
                log("算法库初始化失败。");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDetect != null) {
            mDetect.releaseFaceDetectLib();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        log("surfaceCreated()");
        try {
            // 设置显示目标界面
            if (camera != null) {
                camera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        log("surfaceChanged() width:" + width + " height:" + height);
        mDrawFace.setViewSize(width, height);
        txtDisplaySize.setText(width + " x " + height);
        if (camera != null) {
            camera.stopPreview();
            isPreview = false;
            //camera.setPreviewCallback(this);
            camera.setPreviewCallbackWithBuffer(this);
            camera.setAutoFocusMoveCallback(null);
            camera.startPreview();
            isPreview = true;
        }
        // 打开摄像头
        initCamera();
    }

    @SuppressLint("NewApi")
    private void initCamera() {
        long tm = System.currentTimeMillis();
        log("initCamera()");
        try {
        if (!isPreview) {
            //打开摄像头
            log("initCamera() start1:" + (System.currentTimeMillis() - tm));
            camera = Camera.open(cameraIndex);
            log("initCamera() start2:" + (System.currentTimeMillis() - tm));
            if (isRotate) {
                // 设置旋转90度
                camera.setDisplayOrientation(DEFAULT_ROTATE_VALUE);
             }
            if (camera != null) {
                int width = 320;
                int height = 240;
                log("initCamera() start3:" + (System.currentTimeMillis() - tm));
                Camera.Parameters parameters = camera.getParameters();
                // 设置分辨率
                List<Camera.Size> list = parameters
                        .getSupportedPreviewSizes();
                Iterator<Camera.Size> its = list.iterator();
                Camera.Size size = null;
                int w, h = 0;
                while (its.hasNext()) {
                    size = (Camera.Size) its.next();
                    w = size.width;
                    h = size.height;
                    if (320 == w && 240 == h) {
                        width = 320;
                        height = 240;
                        break;
                    } else if (640 == w&& 480 == w) {
                        width = 640;
                        height = 480;
                    } else {
                        width = w;
                        height = h;
                    }
                }

                log("width=" + width + " height=" + height);
                txtImageSize.setText(width + " x " + height);
                parameters.setPictureSize(width, height);
                parameters.setPreviewSize(width, height);
                log("initCamera() start4:" + (System.currentTimeMillis() - tm));
                if (camBuf == null) {
                    camBuf = new byte[width * height * 3 / 2];
                }
                log("initCamera() start5:" + (System.currentTimeMillis() - tm));
                camera.setParameters(parameters);
                log("initCamera() start6:" + (System.currentTimeMillis() - tm));
                //Size cSize = camera.getParameters().getPreviewSize();
                //camera.enableShutterSound(true);
                // 通过SurfaceView显示取景画面
                camera.setPreviewDisplay(surfaceHolder);
                log("initCamera() start7:" + (System.currentTimeMillis() - tm));
                camera.addCallbackBuffer(camBuf);
                log("initCamera() start8:" + (System.currentTimeMillis() - tm));
                camera.setPreviewCallbackWithBuffer(this);
                //camera.setPreviewCallback(this);
                log("initCamera() start9:" + (System.currentTimeMillis() - tm));
                log("initCamera() start10:" + (System.currentTimeMillis() - tm));
                // 开始预览
                camera.startPreview();
                log("initCamera() start11:" + (System.currentTimeMillis() - tm));
                isPreview = true;
            }
            }
        } catch (RuntimeException ex) 
        {
            ex.printStackTrace();
            // 释放摄像头
            releaseCamera();
        } catch (Exception e) {
            e.printStackTrace();
            // 释放摄像头
            releaseCamera();
        }
    }

    // 释放摄像头图像显示
    public void releaseCamera() {
        // 释放摄像头
        if (camera != null) {
            camera.setPreviewCallbackWithBuffer(null);
            if (isPreview) {
                camera.stopPreview();
                isPreview = false;
            }
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        log("surfaceDestroyed()");
        // 释放摄像头
        releaseCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mDetect != null && camera != null) {
            Parameters param = camera.getParameters();
            Size sz = param.getPictureSize();
            YuvImage img = new YuvImage(data, param.getPreviewFormat(), sz.width, sz.height, null);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            img.compressToJpeg(new Rect(0, 0, sz.width, sz.height), 100, output);
            Matrix m = new Matrix();
            if (isRotate) {
                m.postScale(1, -1);   //镜像垂直翻转
                m.postRotate(-90);
            } else {
                m.postScale(-1, 1); // 镜像水平翻转
            }
            // 缩放图片大小到320x240
            Options opt = new Options();
            if (isRotate) {
                opt.outWidth = HEIGHT;
                opt.outHeight = WIDTH;
                mDrawFace.setImageSize(HEIGHT, WIDTH);
            } else {
                opt.outWidth = WIDTH;
                opt.outHeight = HEIGHT;
                mDrawFace.setImageSize(WIDTH, HEIGHT);
            }

            Bitmap bmp = BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.size(), opt);
            final Bitmap detBmp = Bitmap.createBitmap(bmp, 0, 0, sz.width, sz.height, m, true);
            bmp.recycle();
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 检测人脸，显示人脸坐标
            int[] ret  = mDetect.getFacePositionFromBitmap((short)0, detBmp);
            if (ret != null && ret[0] > 0) {
                txtLeft.setText(String.valueOf(ret[1]));
                txtTop.setText(String.valueOf(ret[2]));
                txtRight.setText(String.valueOf(ret[3]));
                txtBottom.setText(String.valueOf(ret[4]));
                mDrawFace.setPosition(ret[1], ret[2], ret[3], ret[4]);
                //Bundle bundle = new Bundle();
                //bundle.putInt("left", ret[1]);
                //bundle.putInt("top", ret[2]);
                //bundle.putInt("right", ret[3]);
                //bundle.putInt("bottom", ret[4]);
                //Message msg = new Message();
                //msg.what = DISPLAY_FACE;
                //msg.setData(bundle);
                //mHandler.sendMessage(msg);
            } else {
                log("The image have none face.");
                //mHandler.sendEmptyMessageDelayed(PREVIEW_DELAY, DELAY_TIME);
                txtLeft.setText("");
                txtTop.setText("");
                txtRight.setText("");
                txtBottom.setText("");
                mDrawFace.setPosition(0, 0, 0, 0);
            }
        }
        if (camera != null) {
            camera.addCallbackBuffer(camBuf);
        }
    }

    private static Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch(msg.what) {
            case PREVIEW_DELAY:
                if (camera != null) {
                    camera.addCallbackBuffer(camBuf);
                }
            break;
            case DISPLAY_FACE:
                Bundle bundle = msg.getData();
                if (bundle != null) {
                    Rect face = new Rect();
                    face.left = bundle.getInt("left");
                    face.top = bundle.getInt("top");
                    face.right = bundle.getInt("right");
                    face.bottom = bundle.getInt("bottom");
                    mDrawFace.setPosition(face);
                }
                break;
            default:
                log("receive Message: default");
                break;
            }
            
        };
    };

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
        case R.id.btnBackD:
            this.finish();
            break;
        default:
            break;
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent mIntent = null;
        switch(item.getItemId()) {
        case R.id.action_Main:
            mIntent = new Intent();  
            mIntent.setClass(this, MainActivity.class);  
            //这里设置flag还是比较 重要的  
            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
            this.startActivity(mIntent);
            break;
        case R.id.action_Detect:
            mIntent = new Intent("frl.intent.action.FACE_DETECT");
            this.startActivity(mIntent);
            break;
        case R.id.action_Detect_Task:
            mIntent = new Intent("frl.intent.action.FACE_DETECT_TASK");
            this.startActivity(mIntent);
            break;
        case R.id.action_Feature:
            mIntent = new Intent("frl.intent.action.FACE_FEATURE");
            this.startActivity(mIntent);
            break;
        case R.id.action_Exit:
            this.finish();
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
