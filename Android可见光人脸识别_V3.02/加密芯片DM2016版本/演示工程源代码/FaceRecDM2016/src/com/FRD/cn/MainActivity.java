package com.FRD.cn;


import com.face.sv.FaceDetect;
import com.face.sv.FaceFeature;

import com.kaer.common.impl.ReadCardFromSerialport;
import com.kaer.manage.ManageReadIDCard;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
    private final static String TAG = "MainActivity";
    private TextView txtMsg;
    private TextView txtSim;
    private FaceView fvLbb;
    private FaceView fvLt;

    private byte[] feature1 = null;
    private byte[] feature2 = null;
    private int[] rect1 = null;
    private int[] rect2  = null;
    
    private FaceDetect mDetect;
    private  FaceFeature mFeature;
    private ManageReadIDCard mManageReadIDCard;
    private boolean isValidLib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnConfig = (Button) this.findViewById(R.id.btnConfig);
        btnConfig.setOnClickListener(this);
        Button btnAlgAuth = (Button) this.findViewById(R.id.btnAlgAuth);
        btnAlgAuth.setOnClickListener(this);
        Button btnInit = (Button) this.findViewById(R.id.btnInit);
        btnInit.setOnClickListener(this);
        Button btnDetect1 = (Button) this.findViewById(R.id.btnDetect1);
        btnDetect1.setOnClickListener(this);
        Button btnDetect2 = (Button) this.findViewById(R.id.btnDetect2);
        btnDetect2.setOnClickListener(this);
        Button btnFeature1 = (Button) this.findViewById(R.id.btnFeature1);
        btnFeature1.setOnClickListener(this);
        Button btnFeature2 = (Button) this.findViewById(R.id.btnFeature2);
        btnFeature2.setOnClickListener(this);
        Button btnCompare = (Button) this.findViewById(R.id.btnCompare);
        btnCompare.setOnClickListener(this);
        Button btnClear = (Button) this.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);
        Button btnRelease = (Button) this.findViewById(R.id.btnRelease);
        btnRelease.setOnClickListener(this);

        txtMsg = (TextView) this.findViewById(R.id.txtMsg);
        txtSim = (TextView) this.findViewById(R.id.txtSim);
        fvLbb = (FaceView) this.findViewById(R.id.ivLbb);
        fvLt = (FaceView) this.findViewById(R.id.ivLt);
        
        mDetect =  new FaceDetect();
        mFeature= new FaceFeature();
        
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //初始化读写2016模块
        mManageReadIDCard = new ManageReadIDCard();
        mManageReadIDCard = new ManageReadIDCard();
        mManageReadIDCard.getID2DataController("com.kaer.bean.ID2Data");
        mManageReadIDCard.getID2CardReader("com.kaer.common.impl.ReadCardFromSerialport"); 
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        boolean ret = false;
        switch (v.getId()) {
        case R.id.btnConfig:
            // 设置人脸检测目录
            mDetect.setDir(FaceApp.libDir, FaceApp.tempDir);
            // 设置人脸比对目录
            mFeature.setDir(FaceApp.libDir, FaceApp.tempDir);
            log("tempDir:" + FaceApp.tempDir + " libDir:" + FaceApp.libDir);
            txtMsg.setText("算法库配置文件配制成功。");
            break;
        case R.id.btnAlgAuth:
            // 2016硬件初始化
            ((ReadCardFromSerialport)mManageReadIDCard.getmIReadIDCard()).setmBaurd(115200);
            ((ReadCardFromSerialport)mManageReadIDCard.getmIReadIDCard()).setmComPath("/dev/ttyS0");
            //获取DM2016芯片状态
            int state = mManageReadIDCard.readID2CardOpen();
            System.out.println("state:" + state);
            if (state == 1) {
                // 完成人脸检测算法的KEY验证，从SDK获取加密随机码
                String str1 = mDetect.getDetectSN();
                Log.e("mDetect.getDetectSN() result=", "" + str1);
                // 访问DM2016芯片，进行硬件解码
                String msg = mManageReadIDCard.ChipCalculate(str1);
                // Toast.makeText(getApplicationContext(), "msg="+msg,
                // Toast.LENGTH_SHORT).show();
                Log.e("ChipCalculate() result=", msg);
                // 将解密随机码输入人脸检测算法库进行验证，返回1成功，0失败
                int retcheck = mDetect.CheckDetectSN(msg);
                if (retcheck == 1) {
                    isValidLib = true;
                }

                // 完成人脸比对算法的KEY验证，从SDK获取加密随机码
                str1 = mFeature.getFeatureSN();
                Log.e("mFeature.getFeatureSN() result=", "" + str1);
                // 访问DM2016芯片，进行硬件解码
                msg = mManageReadIDCard.ChipCalculate(str1);
                // Toast.makeText(getApplicationContext(), "msg="+msg,
                // Toast.LENGTH_SHORT).show();
                Log.e("ChipCalculate() result=",  msg);
                // 将解密随机码输入人脸比对算法库进行验证，返回1成功，0失败
                retcheck = mFeature.CheckFeatureSN(msg);
                if (isValidLib && retcheck == 1) {
                    isValidLib = true;
                }
                Log.e(TAG, "isValidLib:" + isValidLib);
            }
            mManageReadIDCard.readID2CardClose();
            break;
        case R.id.btnInit:
            if(true || isValidLib)
            {
                if (mDetect != null) {
                    // 初始化人脸检测算法库通道
                    ret = mDetect.initFaceDetectLib(1);
                    Log.e("initFaceDetectLib ret=",""+ret);
                }

                
                if (mFeature != null) {
                 // 初始化人脸比对算法库通道
                    ret = mFeature.initFaceFeatureLib(1);
                }
                if (ret) {
                    txtMsg.setText("算法库初始化成功。");
                } else {
                    txtMsg.setText("算法库初始化失败。");
                }
            }
            
            
            //if()
           
            break;
        case R.id.btnDetect1:
            if (mDetect != null) {
                Bitmap mapLbb = BitmapFactory.decodeResource(this.getResources(), R.drawable.lbb);
                long tm = System.currentTimeMillis();
                // 获取人脸坐标
                int[] face1 = mDetect.getFacePositionFromBitmap((short)0, mapLbb);
                log("btnDetect1 getFacePositionFromBitmap() time:" + (System.currentTimeMillis() - tm));
                if (face1 != null && face1[0] > 0) {
                    fvLbb.setDisplayRect(face1[1], face1[2], face1[3], face1[4], mapLbb.getWidth(), mapLbb.getHeight());
                    rect1 = new int[]{face1[1], face1[2], face1[3], face1[4]};
                } else {
                    log("face1 == null || face1[0] <= 0, face1[0]:" + face1[0]);
                }
                mapLbb.recycle();
            }
            break;
        case R.id.btnDetect2:
            if (mDetect != null) {
                Bitmap mapLdh = BitmapFactory.decodeResource(this.getResources(), R.drawable.lt);
                long tm = System.currentTimeMillis();
                // 获取人脸坐标
                int[] face2 = mDetect.getFacePositionFromBitmap((short)0, mapLdh);
                log("btnDetect2 getFacePositionFromBitmap() time:" + (System.currentTimeMillis() - tm));
                if (face2 != null && face2[0] > 0) {
                    fvLt.setDisplayRect(face2[1], face2[2], face2[3], face2[4], mapLdh.getWidth(), mapLdh.getHeight());
                    rect2 = new int[]{face2[1], face2[2], face2[3], face2[4]};
                } else {
                    log("face2 == null || face2[0] <= 0, face2[0]:" + face2[0]);
                }
                mapLdh.recycle();
            }
            break;

        case R.id.btnFeature1:
            feature1 = null;
            if (mFeature != null && rect1 != null) {
                Bitmap mapLbb = BitmapFactory.decodeResource(this.getResources(), R.drawable.lbb);
                long tm = System.currentTimeMillis();
                // 根据人脸坐标，从对应图片中获取人脸模板
                byte[] result = mFeature.getFaceFeatureFromBitmap((short)0, mapLbb, rect1);
                log("btnFeature1 getFaceFeatureFromBitmap() time:" + (System.currentTimeMillis() - tm));
                log("mFeature.getFeatureFromBitmap(mapLbb) result[0]:" + result[0]);
                if (result != null && result.length >= 2008) {
                    int size = result.length;
                    feature1 = new byte[size];
                    System.arraycopy(result, 0, feature1, 0, size);
                }
                mapLbb.recycle();
                //rect1 = null;
            }
            break;
        case R.id.btnFeature2:
            feature2 = null;
            if (mFeature != null && rect2 != null) {
                Bitmap mapLdh = BitmapFactory.decodeResource(this.getResources(), R.drawable.lt);
                long tm = System.currentTimeMillis();
                // 根据人脸坐标，从对应图片中获取人脸模板
                byte[] result = mFeature.getFaceFeatureFromBitmap((short)0, mapLdh, rect2);
                log("btnFeature2 getFaceFeatureFromBitmap() time:" + (System.currentTimeMillis() - tm));
                log("mFeature.getFeatureFromBitmap(mapLdh) result[0]:" + result[0]);
                if (result != null && result.length >= 2008) {
                    int size = result.length;
                    feature2 = new byte[size];
                    System.arraycopy(result, 0, feature2, 0, size);
                }
                mapLdh.recycle();
                //rect2 = null;
            }
            break;
        case R.id.btnCompare:
            txtSim.setText("");
            if (mFeature != null && feature1 != null && feature2 != null) {
                long tm = System.currentTimeMillis();
                // 比对两个模板数据，返回相似度
                int sim = mFeature.compareFeatures(feature1, feature2);
                log("btnCompare compareFeatures() time:" + (System.currentTimeMillis() - tm));
                txtSim.setText(sim + "%");
            }
            break;
        case R.id.btnClear:
            txtMsg.setText("");
            txtSim.setText("");
            fvLbb.setDisplayRect(0, 0, 0, 0, 0, 0);
            fvLt.setDisplayRect(0, 0, 0, 0, 0, 0);
            feature1 = null;
            feature2 = null;
                    
            break;
        case R.id.btnRelease:
            if (mDetect != null) {
                // 释放人脸检测算法库
                mDetect.releaseFaceDetectLib();
            }
            if (mFeature != null) {
                // 释放人脸比对算法库
                mFeature.releaseFaceFeatureLib();
            }
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

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
