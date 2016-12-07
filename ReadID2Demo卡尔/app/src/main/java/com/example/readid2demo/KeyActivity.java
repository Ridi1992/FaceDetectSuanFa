package com.example.readid2demo;

import com.kaer.common.impl.ReadCardFromSerialport;
  
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class KeyActivity  extends Activity{
	private ReadCardFromSerialport mCom;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key);
		mCom=new ReadCardFromSerialport();
	}

	 
	public void OnKey(View e){ 
		if(mCom.openCom()==1){
			Toast.makeText(getApplicationContext(), "打开串口结果=="+mCom.openCom()+"/关闭串口结果==" + this.mCom.closeCom(), Toast.LENGTH_SHORT).show();
			//mCom.closeCom();
			//return;
		}else if(mCom.openCom()==2){
			Toast.makeText(getApplicationContext(), "打开串口结果=="+mCom.openCom()+"/关闭串口结果==" + this.mCom.closeCom(), Toast.LENGTH_SHORT).show();
//			String str=mCom.ChipCalculate("5500550000550055");
//			mCom.closeCom();
//			Toast.makeText(getApplicationContext(), "解密"+str, Toast.LENGTH_SHORT).show();
		    
		}else if(mCom.openCom()==3){
			Toast.makeText(getApplicationContext(), "打开串口结果=="+mCom.openCom()+"/关闭串口结果==" + this.mCom.closeCom(), Toast.LENGTH_SHORT).show();
		}else if(mCom.openCom()==4){
			Toast.makeText(getApplicationContext(), "打开串口结果=="+mCom.openCom()+"/关闭串口结果==" + this.mCom.closeCom(), Toast.LENGTH_SHORT).show();
		}
		
	}
}
