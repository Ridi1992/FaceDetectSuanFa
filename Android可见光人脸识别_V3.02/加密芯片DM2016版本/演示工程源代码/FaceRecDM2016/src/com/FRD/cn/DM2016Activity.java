package com.FRD.cn;

import com.kaer.common.impl.ReadCardFromSerialport;
import com.kaer.manage.ManageReadIDCard;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DM2016Activity extends Activity implements OnClickListener {
    private final static String TAG = "DM2016Activity";
    private EditText edtEncode;
    private EditText edtUncode;
    ManageReadIDCard mManageReadIDCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm2016);
        
        edtEncode = (EditText)this.findViewById(R.id.edtEncode);
        edtUncode = (EditText)this.findViewById(R.id.edtUncode);
        Button btnDM2016 = (Button)this.findViewById(R.id.btnDM2016);
        btnDM2016.setOnClickListener(this);
        


    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
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
        switch(v.getId()) {
        case R.id.btnDM2016:
            // 硬件初始化
            // 设置串口读写波特率
            ((ReadCardFromSerialport)mManageReadIDCard.getmIReadIDCard()).setmBaurd(115200);
            // 设置串口设备地址
            ((ReadCardFromSerialport)mManageReadIDCard.getmIReadIDCard()).setmComPath("/dev/ttyS0");
            int b = mManageReadIDCard.readID2CardOpen();
            Log.e("DM2016Activity", "mManageReadIDCard.readID2CardOpen() =" + b);
            if(b==1)
            {
                //String msg=mManageReadIDCard.ChipCalculate("5500550000550055");
                //解密字符串，输入输出的都为16进制字符串
                String msg=mManageReadIDCard.ChipCalculate(edtEncode.getText().toString().trim());
                edtUncode.setText(msg);
                Log.e("ChipCalculate result=",""+msg);
            }
            mManageReadIDCard.readID2CardClose();
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

}
