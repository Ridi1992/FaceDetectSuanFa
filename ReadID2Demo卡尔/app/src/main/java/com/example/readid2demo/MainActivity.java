package com.example.readid2demo;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.kaer.bean.ID2Data;
import com.kaer.bean.bmpmethod;
import com.kaer.common.impl.ReadCardFromSerialport;
import com.kaer.manage.ManageReadIDCard;
import com.kaer.service.ReadID2Card;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	MainActivityReceiver mActivity;
	private TextView idinfo,stationinfo,keyinfo;
	private ImageView photo;
	private Button btncard;
	private String TAG="info";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		idinfo=(TextView)findViewById(R.id.textView1);
		stationinfo=(TextView)findViewById(R.id.textView3);
		photo=(ImageView)findViewById(R.id.imageView1);
		btncard=(Button)findViewById(R.id.button1);

		mActivity = new MainActivityReceiver();
		IntentFilter filter = new IntentFilter();// 创建IntentFilter对象
		// 注册一个广播，用于接收Activity传送过来的命令，控制Service的行为，如：发送数据，停止服务等
		filter.addAction("com.kaer.activity");
		// 注册Broadcast Receiver
		registerReceiver(mActivity, filter);
		startService(new Intent(MainActivity.this, ReadID2Card.class));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.e("main","after oncreate");
	}

	public void onReadCard(View v) {
		if(btncard.getText().toString().equals("开启读卡")) {//ReadID2Card
			sendBroadcast(new Intent().setAction("com.kaer.ReadID2CardService").putExtra("command", 2).putExtra("code", ""));
			btncard.setText("关闭读卡");
		} else {
			sendBroadcast(new Intent().setAction("com.kaer.ReadID2CardService")
					.putExtra("command", 3));
			btncard.setText("开启读卡");
		}


	}

	private void showAll(ID2Data _ID2Data) {

		if (_ID2Data.getmID2NewAddress() != null)
			Log.i(TAG, "追加地址=" + _ID2Data.getmID2NewAddress());
		else
			Log.i(TAG, "无追加地址");
		// 指纹信息
		if (_ID2Data.getmID2FP() == null) {
			Log.i(TAG, "未发现指纹");
		} else {
			Log.i(TAG, "指位一:" + _ID2Data.getmID2FP().getFingerPosition(1));
			Log.i(TAG, "指位二:" + _ID2Data.getmID2FP().getFingerPosition(2));
		}
		idinfo.setText("\r\n姓名："+_ID2Data.getmID2Txt().getmName().trim()
				+"\r\n性别："+_ID2Data.getmID2Txt().getmGender().trim()
				+"\r\n民族："+_ID2Data.getmID2Txt().getmNational().trim()
				+"\r\n出生日期："+_ID2Data.getmID2Txt().getmBirthYear().trim()+_ID2Data.getmID2Txt().getmBirthMonth().trim()+_ID2Data.getmID2Txt().getmBirthDay().trim()
				+"\r\n住址："+_ID2Data.getmID2Txt().getmAddress().trim()
				+"\r\n身份证号："+_ID2Data.getmID2Txt().getmID2Num().trim()
				+"\r\n签发机关："+_ID2Data.getmID2Txt().getmIssue().trim()
				+"\r\n有效期："+_ID2Data.getmID2Txt().getmBegin().trim()+"--"+_ID2Data.getmID2Txt().getmEnd().trim()); // 住址



		Bitmap bmp1=  bmpmethod.createRgbBitmap(_ID2Data.getmID2Pic().getHeadFromCard(), 102, 126);
		photo.setImageBitmap(bmp1);

		if(_ID2Data.getmID2NewAddress()!=null)
			this.idinfo.setText(idinfo.getText().toString()+"\r\n"+_ID2Data.getmID2NewAddress()); // 追加住址

	}

	private class MainActivityReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("com.kaer.activity")) {
				Bundle bundle = intent.getExtras();
				int cmd = bundle.getInt("command");// 获取Extra信息
				switch (cmd) {
					case 1:
						stationinfo.setText( "读卡成功");
						showAll((ID2Data)bundle.getSerializable("id2data"));
						break;
					case 2:
						stationinfo.setText("正在读卡...");
						idinfo.setText("");
						photo.setImageBitmap(null);
						break;
					case 3:
						stationinfo.setText("读卡失败...");

						break;
					case 4://打开串口失败
						stationinfo.setText("打开串口失败");
						break;
					case 5:////上电失败

						break;
					case 6://初始化SAM模块失败
						stationinfo.setText( "初始化SAM模块失败");
						break;
					case 7://获取SAM模块号失败

						break;
					case 8://获取SAM模块号失败

						break;
					case 9://获取SAM模块号失败

						break;
					case 10://打开读卡成功
						stationinfo.setText( "打开读卡成功");
						break;
					case 11://打开读卡成功

						break;
					case 12://打开读卡成功
						String tmp=bundle.getString("keydecode");
						Log.e("facekey","return="+tmp);
						Toast.makeText(getApplicationContext(), "decode="+tmp, Toast.LENGTH_SHORT).show();
						break;
					default:
						break;
				}
			}
		}
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// mManageReadIDCard.readID2CardClose();
		sendBroadcast(new Intent().setAction("com.kaer.ReadID2CardService")
				.putExtra("command", 3));
		unregisterReceiver(mActivity);
		stopService(new Intent(MainActivity.this, ReadID2Card.class));
	}




}
