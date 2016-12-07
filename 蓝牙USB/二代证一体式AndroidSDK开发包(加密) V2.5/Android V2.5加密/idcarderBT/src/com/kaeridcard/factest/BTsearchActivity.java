/**
 * 
 */
package com.kaeridcard.factest;

import com.kaeridcard.bluetooth.BT;
import com.kaeridcard.client.BtReaderClient;
import com.kaeridcard.client.IClientCallBack;
import com.kaeridcard.client.IdCardItem;
import com.kaeridcard.client.Value;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class BTsearchActivity extends Activity implements OnClickListener{

//	private final boolean is_unicom = true;
    private TextView connectrst;
    private TextView idcard_text;
   
    private TextView idcard_view = null;
	private TextView btstate_view = null;
	private Button connect_button = null;
	private Button disconnect_button = null;
	private Button read_button = null;
	private ImageView img_view = null;
	
	private boolean isRun = false;
	private boolean is_bt_connect = false;
	private Context mContext = null;
	
   private BtReaderClient mClient;
   private String mac;
   private String devicename;
   private int rssi_value;

   private boolean in_reading = false;
	private Button kaika_button;
	private Button read_ismi_button;
	private Button write_ismi_button;
	private Button write_msgcenter_button;
	private Button changeBt;
	private EditText deleytime;
	private EditText bytecounte;
	private Button read_iccid_button;
   
   private final static String TAG = "BT_TEST";
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "BTsearchActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.btsearch);

		mContext = BTsearchActivity.this;
		
		Intent intent = this.getIntent();
		mac = (String) intent.getStringExtra("mac");
		devicename = (String) intent.getStringExtra("devicename");
		Log.d(TAG, "name=" + devicename + "  mac=" + mac);
		
		idcard_view = (TextView) findViewById(R.id.idcard_text);
		deleytime = (EditText) findViewById(R.id.value);
		bytecounte = (EditText) findViewById(R.id.bytecounte);
        btstate_view = (TextView) findViewById(R.id.connect_state);
        disconnect_button = (Button) findViewById(R.id.disconnect_button);
        disconnect_button.setOnClickListener(this);
        connect_button = (Button) findViewById(R.id.connect_button);
        connect_button.setOnClickListener(this);
        read_button = (Button) findViewById(R.id.read_button);
        read_button.setOnClickListener(this);
        
        read_iccid_button = (Button) findViewById(R.id.read_iccid_button);
        read_iccid_button.setOnClickListener(this);
        read_ismi_button = (Button) findViewById(R.id.read_ismi_button);
        read_ismi_button.setOnClickListener(this);
        write_ismi_button = (Button) findViewById(R.id.write_ismi_button);
        write_ismi_button.setOnClickListener(this);
        write_msgcenter_button = (Button) findViewById(R.id.write_msgcenter_button);
        write_msgcenter_button.setOnClickListener(this);
        
        changeBt = (Button) findViewById(R.id.changeBt);
        changeBt.setOnClickListener(this);
        
        img_view = (ImageView) findViewById(R.id.idcard_image);	//身份证照片显示
        img_view.setVisibility(View.INVISIBLE);
        
        is_bt_connect = false;
        isRun = true;
        
        BTRssiThread connectThread = new BTRssiThread();
		connectThread.start();
		
		
		mClient = new BtReaderClient(this);
        mClient.setCallBack(mCallback);		//设置蓝牙连接状态回调，在回调接口onBtState 中显示蓝牙连接的状态
//        mClient.start();
    }
	
	 public IClientCallBack mCallback = new IClientCallBack(){
			
			@Override
			public void onBtState(final boolean is_connect){
				runOnUiThread(new Runnable() {
					public void run() {
						
						Log.d(TAG,"bt_state=" + is_connect);
						if (is_connect)
						{	
							String toast_text = getString(R.string.bt_connected_ts);
							
							Toast.makeText(BTsearchActivity.this, toast_text+devicename, Toast.LENGTH_SHORT).show();
							btstate_view.setText(toast_text+devicename);
						}
						else
						{	Toast.makeText(BTsearchActivity.this, getString(R.string.bt_lost_connect), Toast.LENGTH_SHORT).show();
							btstate_view.setText(R.string.bt_lost_connect);
						}
						is_bt_connect = is_connect;
					}
				});
			}
			
			@Override
			public void onIddataHandle(IdCardItem iddata){
				
			}
		};
	
		private Handler btHandler = new Handler(Looper.getMainLooper()) {
			
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
					case 1:
						if (!is_bt_connect)
							break;
						String dis_text;
						int rssi = rssi_value&0xff;
						
						if ((rssi & 0x80) != 0){
							rssi = 0x100 - rssi;
							dis_text = " -" +  rssi;
						}
						else if (rssi > 0){
							dis_text = " " +  rssi;
						}
						else
							dis_text = " 0";
						String connect_str = getString(R.string.bt_connected_ts) + devicename + "\n"+
								getString(R.string.bt_rssi_value) + dis_text;
						btstate_view.setText(connect_str);
						break;
					default:
						break;
				}
			}
		};
	private int timeTick = 0;
	// 后台服务线程
	private class BTRssiThread extends Thread {
		
		@Override
		public void run() {
			timeTick = 0;
			while (isRun) {
				try {
					sleep (100);
					timeTick ++;
					
					if (timeTick > 20) {
						timeTick = 0;
						if (in_reading)
							timeTick = 18;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		mClient.stop();			//断开蓝牙连接，退出读卡库，释放资源，退出时必须调用
		mClient.disconnectBt();
//		mClient.setCallBack(null);
		mClient = null;
		isRun = false;
		is_bt_connect = false;
	}
	
	/*
	 * 
	 * */
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case  R.id.connect_button:
				boolean connresult = mClient.connectBt(mac);// 连接设备   
				if(connresult)
					Log.d(TAG, "connect success");
				else
					Log.d(TAG, "connect failed");
				break;
			case  R.id.disconnect_button:
				mClient.disconnectBt(); // 断开连接
				Log.d(TAG,"disconnect!");
				break;
			case  R.id.read_iccid_button:
				Toast.makeText(mContext,"read_button", Toast.LENGTH_SHORT).show();
				if (!mClient.getBtState()){		//蓝牙没有连接
					Toast.makeText(mContext, getString(R.string.result_bt_no_link), Toast.LENGTH_SHORT).show();
					idcard_view.setText("");
					img_view.setVisibility(View.INVISIBLE);
					break;
				}
				String result = mClient.readICCID();
				if(result==null || result.equals(""))
					return;
				byte [] resultByte = hexStringToByte(result);
				int resultLen = (int)resultByte[6];
				if(resultLen==1){
					int error = (int)resultByte[7];
					switch (error) {
					case 1:
						Toast.makeText(mContext,"sim卡未插入", Toast.LENGTH_SHORT).show();
						break;
					case 2:
						Toast.makeText(mContext,"sim卡插入但是不识别", Toast.LENGTH_SHORT).show();
						break;
					case 4:
						Toast.makeText(mContext,"sim卡插入但还未成功读取", Toast.LENGTH_SHORT).show();
						break;

					default:
						break;
					}
				}else{
					byte []tmp = new byte[resultLen];
					for(int i=0;i<resultLen;i++){
						tmp[i] = resultByte[7+i];
					}
					idcard_view.setText("长度："+resultLen +"    卡号 ："+bytesToASCString(tmp));
				}

				break;
			case  R.id.read_ismi_button:
				Toast.makeText(mContext,"readimsi", Toast.LENGTH_SHORT).show();
				if (!mClient.getBtState()){		//蓝牙没有连接
					Toast.makeText(mContext, getString(R.string.result_bt_no_link), Toast.LENGTH_SHORT).show();
					idcard_view.setText("");
					img_view.setVisibility(View.INVISIBLE);
					break;
				}
				byte[] szCheckRes = new byte[1];
				byte[] szImsi2G = new byte[18];
				byte[] szImsiLen2G = new byte[1];
				byte[] szImsi3G = new byte[18];
				byte[] szImsiLen3G = new byte[1];
				// 第一个参数 0x00 表示读2G卡   0x01表示读3G 4G卡
				int readImsiRes = mClient.ReadIMSI((byte) 0x01, szCheckRes, szImsi2G, szImsiLen2G, szImsi3G, szImsiLen3G);
				if(readImsiRes!=1){
					int error = readImsiRes;
					switch (error) {
					case 0:
						Toast.makeText(mContext,"imsi读失败", Toast.LENGTH_SHORT).show();
						break;
					case 2:
						Toast.makeText(mContext,"卡未插入", Toast.LENGTH_SHORT).show();
						break;
					case 3:
						Toast.makeText(mContext,"不识别的SIM卡", Toast.LENGTH_SHORT).show();
						break;
					case 4:
						Toast.makeText(mContext,"sim卡插入但还未初始化", Toast.LENGTH_SHORT).show();
						break;

					default:
						break;
					}
				}else{
//					idcard_view.setText("3g："+bytesToASCString(szImsi3G) +"    2g："+bytesToASCString(szImsi2G));
					StringBuilder sb = new StringBuilder();
					sb.append("是否白卡 : ");
					sb.append(szCheckRes[0]==0?"是":"否");
					sb.append("\n");
					sb.append("3g: "+bytesToASCString(szImsi3G));
					sb.append("2g: "+bytesToASCString(szImsi2G));
					idcard_view.setText(sb.toString());
				}

				break;
			case  R.id.write_ismi_button:
				Toast.makeText(mContext,"writeimsi", Toast.LENGTH_SHORT).show();
				if (!mClient.getBtState()){		//蓝牙没有连接
					Toast.makeText(mContext, getString(R.string.result_bt_no_link), Toast.LENGTH_SHORT).show();
					idcard_view.setText("");
					img_view.setVisibility(View.INVISIBLE);
					break;
				}
				
				byte[] szImsi1 = new byte[15];
				for(int i=0;i<15;i++){
					szImsi1[i] = (byte) ((i%10)+0x30); // 例子数据 012345678901234
				}
				
				byte[] szImsi2 = new byte[15];
				for(int i=0;i<15;i++){
					szImsi2[i] = (byte) ( (i%10)+0x30);// 例子数据
				}		
				
				if(szImsi1==null || szImsi1.length!=15){// 判断输入参数1的格式是否正确。 必须不为空，且数据长度为15
					Toast.makeText(mContext,"2G 请输入正确的2G imsi", Toast.LENGTH_SHORT).show();
					return ;
				}
				if(szImsi2!=null&&szImsi2.length!=15){// 判断输入参数2的格式是否正确。 可以为null,此时此卡为2G卡。
													  // 但参数2若不为Null时，数据长度必须为15，此时此卡为3G或4G卡。
					Toast.makeText(mContext,"2G 请输入正确的 3G imsi", Toast.LENGTH_SHORT).show();
					return ;
				}
				
				int writeImsiRes = mClient.WriteIMSI(szImsi1, szImsi2);
				
				
				if(writeImsiRes!=1){
					int error = writeImsiRes;
					switch (error) {
					case 0:
						Toast.makeText(mContext,"imsi写失败", Toast.LENGTH_SHORT).show();
						break;
					case 2:
						Toast.makeText(mContext,"卡未插入", Toast.LENGTH_SHORT).show();
						break;
					case 3:
						Toast.makeText(mContext,"不识别的SIM卡", Toast.LENGTH_SHORT).show();
						break;
					case 4:
						Toast.makeText(mContext,"sim卡插入但还未初始化", Toast.LENGTH_SHORT).show();
						break;
					default:
						break;
					}
				}else{
					idcard_view.setText("写入 2g："+bytesToASCString(szImsi1) +"    3g："+bytesToASCString(szImsi2));
				}
				break;
			case  R.id.write_msgcenter_button:
				Toast.makeText(mContext,"writesmsc", Toast.LENGTH_SHORT).show();
				if (!mClient.getBtState()){		//蓝牙没有连接
					Toast.makeText(mContext, getString(R.string.result_bt_no_link), Toast.LENGTH_SHORT).show();
					idcard_view.setText("");
					img_view.setVisibility(View.INVISIBLE);
					break;
				}
				
				String abc = "01234567890";//短信中心号码
				if(abc.length()!=11){
					Toast.makeText(mContext,"请输入正确的短信中心号码", Toast.LENGTH_SHORT).show();
				}
				byte index = 1;//短信中心号索引（每个sim卡可以有几个短信中心线性文件，从1开始）
				int writeSmscRes = mClient.WriteSMSC(abc,index);
				
				
				if(writeSmscRes!=1){
					int error = writeSmscRes;
					switch (error) {
					case 0:
						Toast.makeText(mContext,"imsi读失败", Toast.LENGTH_SHORT).show();
						break;
					case 2:
						Toast.makeText(mContext,"卡未插入", Toast.LENGTH_SHORT).show();
						break;
					case 3:
						Toast.makeText(mContext,"不识别的SIM卡", Toast.LENGTH_SHORT).show();
						break;
					case 4:
						Toast.makeText(mContext,"sim卡插入但还未初始化", Toast.LENGTH_SHORT).show();
						break;

					default:
						break;
					}
				}else{
					
					idcard_view.setText("写入smsc："+abc);
				}

				break;
			case  R.id.read_button://  读卡
				
				in_reading = true;

				IdCardItem idcard;
				idcard = mClient.readIDCard();		
				if (idcard.result_code == 0){		//读取成功
					idcard_view.setText(idcard.name + "\n" + idcard.id_num + "\n" +
							idcard.getSexStr(idcard.sex_code) + "  " + idcard.getNationStr(idcard.nation_code) + "\n" + 
							idcard.birth_year +  "-" + idcard.birth_month + "-" + idcard.birth_day + "\n" + 
							idcard.address + "\n" + 
							idcard.sign_office + "\n" +
							idcard.useful_s_date_year+idcard.useful_s_date_month+idcard.useful_s_date_day + "--" + 
							idcard.useful_e_date_year + idcard.useful_e_date_month + idcard.useful_e_date_day
						);
					img_view.setImageBitmap(idcard.picBitmap);
					img_view.setVisibility(View.VISIBLE);
				}
				else if (idcard.result_code == 1){  //数据解析失败
					Toast.makeText(mContext, getString(R.string.result_data_err), Toast.LENGTH_SHORT).show();
				}
				else if (idcard.result_code == 2){	//读卡超时
					Toast.makeText(mContext, getString(R.string.result_read_overtime), Toast.LENGTH_SHORT).show();
				}
				else if (idcard.result_code == 3){	//蓝牙没有连接
					Toast.makeText(mContext, getString(R.string.result_bt_no_link), Toast.LENGTH_SHORT).show();
				}
				if (idcard.result_code != 0){
					idcard_view.setText("");
					img_view.setVisibility(View.INVISIBLE);
				}
				in_reading = false;
				break;
		}
	}
	
	
	public static String bytesToASCString(byte[] src) {
		int len = src.length;

		if (src == null || len <= 0) {
			return null;
		}
		char[] result = new char[len];
		for (int i = 0; i < len; i++) {
			result[i] = (char) src[i];
		}
		
		return String.valueOf(result);
	}
    public static byte HexToByte(char hex1, char hex2)
    {
	    byte result = 0;
	    for(int i = 0;i < 2;i++)
	    {
		    char c;
		    if (i == 0)
		    	c = hex1;
		    else
		    	c = hex2;
		    byte b = 0;
		    switch (c)
		    {
			    case '0':
			    case '1':
			    case '2':
			    case '3':
			    case '4':
			    case '5':
			    case '6':
			    case '7':
			    case '8':
			    case '9':
				    b = (byte)(c - '0');
				    break;
			    case 'A':
			    case 'B':
			    case 'C':
			    case 'D':
			    case 'E':
			    case 'F':
				    b = (byte)(10 + c - 'A');
				    break;
			    case 'a':
			    case 'b':
			    case 'c':
			    case 'd':
			    case 'e':
			    case 'f':
				    b = (byte)(10 + c - 'a');
				    break;
		    }
		    if (i == 0)
		    {
		    	b = (byte)(b * 16);
		    }
		    result += b;
	    }
	    return result;
    }
    
    public static byte[] hexStringToByte(String hex_str){
    
    	byte[] data_byte = new byte[hex_str.length()/2];
    	int byte_len = 0;
    	for (int i = 0;i < hex_str.length();i += 2){
    		data_byte[byte_len ++] = HexToByte(hex_str.charAt(i),hex_str.charAt(i+1));
    	}
    	return data_byte;
    }
}
