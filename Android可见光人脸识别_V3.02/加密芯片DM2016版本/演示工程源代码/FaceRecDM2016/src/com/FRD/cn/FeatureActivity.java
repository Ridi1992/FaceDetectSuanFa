package com.FRD.cn;

import com.face.sv.FaceFeature;
import com.kaer.common.impl.ReadCardFromSerialport;
import com.kaer.manage.ManageReadIDCard;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FeatureActivity extends Activity implements OnClickListener {
    private final static String TAG = "MainActivity";
    private TextView txtMsg;
    private TextView txtSim;
    private FaceView fvLbb;
    private FaceView fvLdh;
    private Bitmap bmp1;
    private Bitmap bmp2;
    private int[] rect1 = {60, 108, 209, 243};
    private int[] rect2 = {17, 38, 83, 98};
    private byte[] feature1 = null;
    private byte[] feature2 = null;
    private boolean isRun = true;
    private FeatureAsyncTask mTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);
        
        Button btnFaceFeature = (Button) this.findViewById(R.id.btnFaceFeature);
        btnFaceFeature.setOnClickListener(this);
        Button btnClose = (Button) this.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);
        txtMsg = (TextView) this.findViewById(R.id.txtMsg);
        txtSim = (TextView) this.findViewById(R.id.txtSim);
        fvLbb = (FaceView) this.findViewById(R.id.ivLbb);
        
        fvLdh = (FaceView) this.findViewById(R.id.ivLdh);
        
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        bmp1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.lbb);
        fvLbb.setDisplayRect(rect1[0], rect1[1], rect1[2], rect1[3], bmp1.getWidth(), bmp1.getHeight());
        bmp2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.lt);
        fvLdh.setDisplayRect(rect2[0], rect2[1], rect2[2], rect2[3], bmp2.getWidth(), bmp2.getHeight());

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (bmp1 != null) {
            bmp1.recycle();
        }
        if (bmp2 != null) {
            bmp2.recycle();
        }
        if (mTask != null && isRun) {
            isRun = false;
            mTask.cancel(false);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnFaceFeature:
            if (!isRun) {
                isRun = true;
                mTask = new FeatureAsyncTask();
                mTask.execute();
                txtSim.setText("");
            }
            break;
        case R.id.btnClose:
            if (mTask != null && isRun) {
                isRun = false;
                mTask.cancel(false);
                txtSim.setText("");
            }
            this.finish();
            break;
        default:
            break;
        }
    }

    /**
     * 定义一个类，让其继承AsyncTask这个类
     * Params: String类型，表示传递给异步任务的参数类型是String，通常指定的是URL路径
     * Progress: Integer类型，进度条的单位通常都是Integer类型
     * Result：byte[]类型，表示我们下载好的图片以字节数组返回
     * @author xiaoluo
     *
     */
    public class FeatureAsyncTask extends AsyncTask<Void, Float, Void>
    {
        private ManageReadIDCard mManageReadIDCard;
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //初始化读写2016模块
            mManageReadIDCard = new ManageReadIDCard();
            mManageReadIDCard = new ManageReadIDCard();
            mManageReadIDCard.getID2DataController("com.kaer.bean.ID2Data");
            mManageReadIDCard.getID2CardReader("com.kaer.common.impl.ReadCardFromSerialport"); 
        }
        @Override
        protected Void doInBackground(Void... params)
        {
            boolean result = false;
            FaceFeature mFeature= new FaceFeature();
            boolean isValidLib = false;
            // 2016硬件初始化
            ((ReadCardFromSerialport)mManageReadIDCard.getmIReadIDCard()).setmBaurd(115200);
            ((ReadCardFromSerialport)mManageReadIDCard.getmIReadIDCard()).setmComPath("/dev/ttyS0");
            //获取DM2016芯片状态
            int state = mManageReadIDCard.readID2CardOpen();
            System.out.println("state:" + state);
            if (state == 1) {
                // 完成人脸比对算法的KEY验证，从SDK获取加密随机码
                String str1 = mFeature.getFeatureSN();
                Log.e("mFeature.getFeatureSN() result=", "" + str1);
                // 访问DM2016芯片，进行硬件解码
                String msg = mManageReadIDCard.ChipCalculate(str1);
                // Toast.makeText(getApplicationContext(), "msg="+msg,
                // Toast.LENGTH_SHORT).show();
                Log.e("ChipCalculate() result=",  msg);
                // 将解密随机码输入人脸比对算法库进行验证，返回1成功，0失败
                int retcheck = mFeature.CheckFeatureSN(msg);
                if (retcheck == 1) {
                    isValidLib = true;
                }
                Log.e(TAG, "isValidLib:" + isValidLib);
            }
            mManageReadIDCard.readID2CardClose();
                if (isValidLib && mFeature != null) {
                    mFeature.setDir(FaceApp.libDir, FaceApp.tempDir);
                    result = mFeature.initFaceFeatureLib(1);
                    Log.e("initFaceFeatureLib ret=",""+result);
                }
                if (result) {
                    log("算法库初始化成功。");
                } else {
                    log("算法库初始化失败。");
                }

            float ret = 0;
            log("init result:" + result);
            while (result && isRun) {
                log("=>>while() start");
                log("===>>YuvImage() end ");
                ret = -1;
                if (bmp1 != null && bmp2 != null) {
                    if (mFeature != null && rect1 != null) {
                        feature1 = mFeature.getFaceFeatureFromBitmap((short) 0,
                                bmp1, rect1);
                        log("btnFeature1 getFaceFeatureFromBitmap() ");
                        if (feature1 == null) {
                            log("feature1 == null");
                        }
                    }
                    if (mFeature != null && rect2 != null) {
                        feature2 = mFeature.getFaceFeatureFromBitmap((short) 0,
                                bmp2, rect2);
                        log("btnFeature1 getFaceFeatureFromBitmap() ");
                        if (feature2 == null) {
                            log("feature2 == null");
                        }
                    }
                    if (feature1 != null && feature2 != null) {
                        ret = mFeature.compareFeatures(feature1, feature2);
                        log("mFeature.compareFeatures(feature1, feature2) ret:" + ret);
                    }
                }
                log("publishProgress() ");
                publishProgress(new Float[] { ret });
                log("publishProgress() ");
                log("=>>while() end");
            }
            mFeature.releaseFaceFeatureLib();
            return null;
        }
        @Override
        protected void onProgressUpdate(Float... values)
        {
            super.onProgressUpdate(values);
            //    更新ProgressDialog的进度条
            if (values != null && values.length  > 0) {
                txtSim.setText(String.valueOf(values[0]) + "%");
            }
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
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
