package com.FRD.cn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.face.sv.FaceDetect;
import com.kaer.common.impl.ReadCardFromSerialport;
import com.kaer.manage.ManageReadIDCard;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class DetectTaskActivity extends Activity implements Callback, PreviewCallback, OnClickListener {
    private final static String TAG = "DetectActivity";
    private final static int PREVIEW_DELAY = 0x1001;
    private final static int DISPLAY_FACE = 0x1002;
    private final static int DELAY_TIME = 50;
    // ͼ���Ƿ���Ҫ��ת
    private final static boolean isRotate = false;
    // ͼ����ز���
    public final static int DEFAULT_ROTATE_VALUE = 90;
    private static int cameraIndex = 0;
    private static Camera camera;
    private byte[] camBuf;
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
    // SurfaceView���ڴ�С
    private static int width = 320;
    private static int height = 240;
    private boolean isPreview = false;
    private boolean isRun = true;
    private DetectAsyncTask mTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        sView = (SurfaceView) findViewById(R.id.surfaceView);
        // ���SurfaceView��SurfaceHolder
        surfaceHolder = sView.getHolder();
        // ΪsurfaceHolder���һ���ص�������
        surfaceHolder.addCallback(this);
        // ���ø�SurfaceView�Լ���ά������
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        sView.setKeepScreenOn(true);
  
        FrameLayout pLayout = (FrameLayout)this.findViewById(R.id.pLayout);
        mDrawFace =  new DrawFaceRect(this, getResources().getColor(R.color.face_position));
        // ��һ��activity������Ӷ����content
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
            
        // ����2   
        DisplayMetrics dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);  
        float width=dm.widthPixels*dm.density;   
        float height=dm.heightPixels*dm.density;  
        log("width:" + width + " height:" + height);
        txtScreenSize.setText(width + " x " + height);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRun = true;
        mTask = new DetectAsyncTask();
        mTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRun = false;
        mTask.cancel(false);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        log("surfaceCreated()");
        try {
            // ������ʾĿ�����
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
        // ������ͷ
        initCamera();
    }

    @SuppressLint("NewApi")
    private void initCamera() {
        long tm = System.currentTimeMillis();
        log("initCamera()");
        try {
        if (!isPreview) {
            //������ͷ
            log("initCamera() start1:" + (System.currentTimeMillis() - tm));
            camera = Camera.open(cameraIndex);
            log("initCamera() start2:" + (System.currentTimeMillis() - tm));
            if (isRotate) {
                // ������ת90��
                camera.setDisplayOrientation(DEFAULT_ROTATE_VALUE);
             }
            if (camera != null) {
                log("initCamera() start3:" + (System.currentTimeMillis() - tm));
                Camera.Parameters parameters = camera.getParameters();
                // ���÷ֱ���
                List<Camera.Size> list = parameters
                        .getSupportedPreviewSizes();
                Iterator<Camera.Size> its = list.iterator();
                int minWidth = 0;
                Camera.Size size = null;
                while (its.hasNext()) {
                    size = (Camera.Size) its.next();
                    if (size.width / 4 != size.height / 3) {
                        continue;
                    }
                    if (minWidth != 0 && minWidth < size.width) {
                        continue;
                    }
                    minWidth = size.width;
                    //if (320 == size.width && 240 == size.height) {
                    //    width = 320;
                    //    height = 240;
                    //} else
                        if (640 == size.width && 480 == size.height) {
                        width = 640;
                        height = 480;
                    }
                }

                log("width=" + width + " height=" + height);
                txtImageSize.setText(width + " x " + height);
                mDrawFace.setImageSize(width, height);
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
                // ͨ��SurfaceView��ʾȡ������
                camera.setPreviewDisplay(surfaceHolder);
                log("initCamera() start7:" + (System.currentTimeMillis() - tm));
                camera.addCallbackBuffer(camBuf);
                log("initCamera() start8:" + (System.currentTimeMillis() - tm));
                camera.setPreviewCallbackWithBuffer(this);
                //camera.setPreviewCallback(this);
                log("initCamera() start9:" + (System.currentTimeMillis() - tm));
                log("initCamera() start10:" + (System.currentTimeMillis() - tm));
                // ��ʼԤ��
                camera.startPreview();
                log("initCamera() start11:" + (System.currentTimeMillis() - tm));
                isPreview = true;
            }
            }
        } catch (RuntimeException ex) 
        {
            ex.printStackTrace();
            // �ͷ�����ͷ
            releaseCamera();
        } catch (Exception e) {
            e.printStackTrace();
            // �ͷ�����ͷ
            releaseCamera();
        }
    }

    // �ͷ�����ͷͼ����ʾ
    public void releaseCamera() {
        // �ͷ�����ͷ
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
        // �ͷ�����ͷ
        releaseCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {


    }

    /**
     * ����һ���࣬����̳�AsyncTask�����
     * Params: String���ͣ���ʾ���ݸ��첽����Ĳ���������String��ͨ��ָ������URL·��
     * Progress: Integer���ͣ��������ĵ�λͨ������Integer����
     * Result��byte[]���ͣ���ʾ�������غõ�ͼƬ���ֽ����鷵��
     * @author xiaoluo
     *
     */
    public class DetectAsyncTask extends AsyncTask<Void, Rect, Void>
    {
        private ManageReadIDCard mManageReadIDCard;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //��ʼ����д2016ģ��
            mManageReadIDCard = new ManageReadIDCard();
            mManageReadIDCard = new ManageReadIDCard();
            mManageReadIDCard.getID2DataController("com.kaer.bean.ID2Data");
            mManageReadIDCard.getID2CardReader("com.kaer.common.impl.ReadCardFromSerialport"); 
        }
        @Override
        protected Void doInBackground(Void... params)
        {
            boolean result = false;
            FaceDetect mDetect = new FaceDetect();
            boolean isValidLib = false;
            // 2016Ӳ����ʼ��
            ((ReadCardFromSerialport)mManageReadIDCard.getmIReadIDCard()).setmBaurd(115200);
            ((ReadCardFromSerialport)mManageReadIDCard.getmIReadIDCard()).setmComPath("/dev/ttyS0");
            //��ȡDM2016оƬ״̬
            int state = mManageReadIDCard.readID2CardOpen();
            System.out.println("state:" + state);
            if (state == 1) {
                // �����������㷨��KEY��֤����SDK��ȡ���������
                String str1 = mDetect.getDetectSN();
                Log.e("mDetect.getDetectSN() result=", "" + str1);
                // ����DM2016оƬ������Ӳ������
                String msg = mManageReadIDCard.ChipCalculate(str1);
                // Toast.makeText(getApplicationContext(), "msg="+msg,
                // Toast.LENGTH_SHORT).show();
                Log.e("ChipCalculate() result=", msg);
                // �����������������������㷨�������֤������1�ɹ���0ʧ��
                int retcheck = mDetect.CheckDetectSN(msg);
                if (retcheck == 1) {
                    isValidLib = true;
                }
                Log.e(TAG, "isValidLib:" + isValidLib);
            }
            mManageReadIDCard.readID2CardClose();
            if (isValidLib && mDetect != null) {
                mDetect.setDir(FaceApp.libDir, FaceApp.tempDir);
                result = mDetect.initFaceDetectLib(1);
                Log.e("initFaceDetectLib ret=",""+result);
            }
            if (isValidLib && result) {
                log("�㷨���ʼ���ɹ���");
            } else {
                log("�㷨���ʼ��ʧ�ܡ�");
            }

            //boolean res = false;
            int[] ret = null;
            Rect rect = null;
            YuvImage img;
            Bitmap bmp;
            Bitmap detBmp;
            ByteArrayOutputStream output;
            Matrix m = new Matrix();
            if (isRotate) {
                m.postScale(1, -1);   //����ֱ��ת
                m.postRotate(-90);
            } else {
                m.postScale(-1, 1); // ����ˮƽ��ת
            }
            log("init result:" + result);
            while (result && isRun) {
                log("=>>while() start");
                if (camera != null && camBuf != null) {
                    log("===>>YuvImage() start");
                    img = new YuvImage(camBuf, ImageFormat.NV21, width, height, null);
                    log("ByteArrayOutputStream()");
                    output = new ByteArrayOutputStream();
                    log("compressToJpeg()");
                    img.compressToJpeg(new Rect(0, 0, width, height), 100, output);
                    log("decodeByteArray()");
                    bmp = BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.size());
                    log("createBitmap()");
                    detBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, m, true);
                    log("bmp.recycle()");
                    bmp.recycle();
                    log("camera.addCallbackBuffer(camBuf)");
                    camera.addCallbackBuffer(camBuf);
                    log("addCallbackBuffer()");
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        //res = false;
                    }
                    log("===>>YuvImage() end");
                    if (detBmp != null) {
                        width = detBmp.getWidth();
                        height = detBmp.getHeight();
                        log("==>>getFacePositionFromBitmap() start");
                        ret = mDetect.getFacePositionFromBitmap((short) 0, detBmp);
                        log("==>>getFacePositionFromBitmap() end");
                        detBmp.recycle();
                        detBmp = null;
                        if (ret != null && ret[0] > 0) {
                            log("====>>||get position is success.");
                            rect = new Rect(ret[1], ret[2], ret[3], ret[4]);
                            //res = true;
                        } else {
                            log("====>>||get position is fail.");
                            rect = new Rect(0, 0, 0, 0);
                            //res = false;
                        }
                        log("publishProgress()");
                        publishProgress(new Rect[] { rect });
                        log("publishProgress()");
                    }
                }
                log("=>>while() end");
            }
            mDetect.releaseFaceDetectLib();
            return null;
        }
        @Override
        protected void onProgressUpdate(Rect... values)
        {
            super.onProgressUpdate(values);
            //    ����ProgressDialog�Ľ�����
            if (values != null && values.length  > 0) {
                mDrawFace.setPosition(values[0]);
                txtLeft.setText(String.valueOf(values[0].left));
                txtTop.setText(String.valueOf(values[0].top));
                txtRight.setText(String.valueOf(values[0].right));
                txtBottom.setText(String.valueOf(values[0].bottom));
            }
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
        }
    }

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
            //��������flag���ǱȽ� ��Ҫ��  
            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
            this.startActivity(mIntent);
            break;
        case R.id.action_Detect:
            mIntent = new Intent("frd.intent.action.FACE_DETECT");
            this.startActivity(mIntent);
            break;
        case R.id.action_Detect_Task:
            mIntent = new Intent("frd.intent.action.FACE_DETECT_TASK");
            this.startActivity(mIntent);
            break;
        case R.id.action_DM2016:
            mIntent = new Intent("frd.intent.action.DM2016");
            this.startActivity(mIntent);
            break;
        case R.id.action_Feature:
            mIntent = new Intent("frd.intent.action.FACE_FEATURE");
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
