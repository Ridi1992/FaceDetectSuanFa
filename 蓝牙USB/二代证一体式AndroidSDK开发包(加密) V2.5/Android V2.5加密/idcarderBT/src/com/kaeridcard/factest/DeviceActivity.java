package com.kaeridcard.factest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.kaeridcard.factest.R;
//import com.kaercontact.data.PreferData;
import com.kaeridcard.client.BtReaderClient;
import com.kaeridcard.client.IClientCallBack;
import com.kaeridcard.client.IdCardItem;

public class DeviceActivity extends Activity {

	private Context mContext = null;

	private final static String TAG = "BT_TEST";
	//final private String SET_FILE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
	private final String SET_FILE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
	private final String SET_FILE_NAME = "btmac";
	
	private LayoutInflater inflater;
	private String mDeviceAddress = "";
	private BluetoothAdapter btAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mmNewDevicesArrayAdapter;
	private TextView tv_devices_view = null;
	private Button save_button = null;
	private Button search_button = null;
	private static BluetoothDevice remoteDevice=null;

	private ArrayList<String> mPairedList;
	private ArrayList<String> mmPairedList;
	private ArrayList<Integer> mPairedCheck;
	
	private int select_index = 0xff;
	private ListView pairedListView;
	
	MyDetailAdapter mAdapter;

	private static PowerManager.WakeLock mWakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device);
		pairedListView = (ListView) findViewById(R.id.paired_devices);

		mAdapter = new MyDetailAdapter();
		pairedListView.setOnItemClickListener(mDeviceClickListener);
		
		initData();
		newBt();
		doDiscovery();

		PowerManager pManager = ((PowerManager) getSystemService(POWER_SERVICE));  
        mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK  
                | PowerManager.ON_AFTER_RELEASE, "com.kaeridcard.factest");  
        mWakeLock.acquire();
	}
	
	private void initData() {
		mContext = DeviceActivity.this;
		
		search_button = (Button) findViewById(R.id.btn_search);
		search_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//pairedListView.setAdapter(mAdapter);
				newBt();
				doDiscovery();
			}
		});
		//preferData = new PreferData(mContext);
		inflater = LayoutInflater.from(mContext);
		mPairedList = new ArrayList<String>();
		mPairedCheck = new ArrayList<Integer>();
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!btAdapter.isEnabled()) {
			//Intent bt_Intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			//bt_Intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			//startActivityForResult(bt_Intent, 0x1);
		}
		listPairedBT();
	}
	
	private void newBt(){
		mmNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.newdevice_item);
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		
		newDevicesListView.setAdapter(mmNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mmDeviceClickListener);
	}
	
	private OnItemClickListener mmDeviceClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			btAdapter.cancelDiscovery();
			String info = ((TextView) v).getText().toString();
			if (info.length() < 17) {
				return;
			}
			String mac = info.substring(info.length() - 17);
			Toast.makeText(mContext, mac, Toast.LENGTH_SHORT).show();
			boolean a = pair(mac,"123456");
			
				//initData();
				//listPairedBT();
			
			//preferData.writeData("BtMac", mac);
			//BT.getInstance().stop();
			//startActivity(new Intent(mContext, MainActivity.class));
			//finish();
		}
	};
	
	
	 public static boolean pair(String strAddr, String strPsw)
		{
			boolean result = false;
			//蓝牙设备适配器
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			//取消发现当前设备的过程
			bluetoothAdapter.cancelDiscovery();
			if (!bluetoothAdapter.isEnabled())
			{
				bluetoothAdapter.enable();
			}
			if (!BluetoothAdapter.checkBluetoothAddress(strAddr))
			{ // 检查蓝牙地址是否有效
				Log.d(TAG, "devAdd un effient!");
			}
			//由蓝牙设备地址获得另一蓝牙设备对象
			BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);
			if (device.getBondState() != BluetoothDevice.BOND_BONDED)
			{
				try
				{
					Log.d(TAG, "NOT BOND_BONDED");
					ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
					ClsUtils.createBond(device.getClass(), device);
//					ClsUtils.cancelPairingUserInput(device.getClass(), device);
					remoteDevice = device; // 配对完毕就把这个设备对象传给全局的remoteDevice
					
					result = true;
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					Log.d(TAG, "setPiN failed!");
					e.printStackTrace();
				} //

			}
			else
			{
				Log.d(TAG, "HAS BOND_BONDED");
				try
				{
					//ClsUtils这个类的的以下静态方法都是通过反射机制得到需要的方法
					ClsUtils.createBond(device.getClass(), device);//创建绑定
					ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
					ClsUtils.createBond(device.getClass(), device);
//					ClsUtils.cancelPairingUserInput(device.getClass(), device);
					remoteDevice = device; // 如果绑定成功，就直接把这个设备对象传给全局的remoteDevice
					result = true;
					
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					Log.d(TAG, "setPiN failed!");
					e.printStackTrace();
				}
			}
			//initData();
			return result;
		}
	
	
	
	@Override
	protected void onResume() {
		if (!btAdapter.isEnabled()) {
			showBtDisableDialog();
		}
		super.onResume();
	}
	
	/**
	 * 蓝牙未连接
	 */
	private void showBtDisableDialog() {
		final OkOnlyDialog myDialog = new OkOnlyDialog(mContext);
		myDialog.setTitle("蓝牙未开启");
		myDialog.setMessage("请在手机设置中开启蓝牙，再次打开软件进行设置。");
		myDialog.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(View v) {
				myDialog.dismiss();
				finish();
			}
		});
	}
	
	/**
	 * 列出已配对的蓝牙
	 */
	public void listPairedBT() {
		//mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_item);
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_item);
		
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mAdapter);
	   
		
	/*	mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_item01);
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);
		*/
		
		
		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, intent);
		
		if(mPairedList!=null){
			mPairedList.clear();
		}
		
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				mPairedList.add(device.getName() + "\n" + device.getAddress());
				//System.out.println("listPairedBT mac : " + save_mac + " " + device.getAddress());
				mPairedCheck.add(1);
			}
			pairedListView.setSelection(0);
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			BluetoothDevice device = null;
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// 处理正在搜索设备
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					if (!mDeviceAddress.equals(device.getAddress())) {
						mDeviceAddress = device.getAddress();
						
						int i = 0;		//大于等于
						String new_device = device.getName() + "\n" + device.getAddress();
						for (i = 0;i < mmNewDevicesArrayAdapter.getCount();i ++){
							if (mmNewDevicesArrayAdapter.getItem(i).equals(new_device))
								break;
						}
						if (i >= mmNewDevicesArrayAdapter.getCount()){
							mmNewDevicesArrayAdapter.add(new_device);
							mmNewDevicesArrayAdapter.notifyDataSetChanged();
						}
					}
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// 处理搜索设备完毕
				//setProgressBarIndeterminateVisibility(false);
				if (mmNewDevicesArrayAdapter.getCount() == 0) {
					//tv_devices_view.setText(R.string.none_found);
				//	scanButton.setVisibility(View.VISIBLE);
				} else {
					//tv_devices_view.setText(R.string.select_device);
					//scanButton.setVisibility(View.GONE);
				} 
			}else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) { // 设备状态改变  
				//initData();
				pairedListView.setAdapter(mAdapter);
	            doStateChange(intent);  
	        }
			
		}
		
		 void doStateChange(Intent intent) {  
		        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
		        switch (device.getBondState()) {  
		        case BluetoothDevice.BOND_NONE:  
		        	Log.d(TAG,"取消配对");
		        	//Toast.makeText(DeviceActivity.this,"取消配对",Toast.LENGTH_SHORT);
		            break;  
		        case BluetoothDevice.BOND_BONDING:  
		        	Log.d(TAG,"正在配对");
		        	//Toast.makeText(DeviceActivity.this,"配对成功",Toast.LENGTH_SHORT);
		            break;
		        case BluetoothDevice.BOND_BONDED:  
		        	Log.d(TAG,"完成配对");
		        	//Toast.makeText(DeviceActivity.this,"完成配对",Toast.LENGTH_SHORT);
		        	
		        	
		        	changeList(intent);
		            break;  
		        }  
		    }  
		 void changeList(Intent intent) {  
		        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
			 	mPairedList.add(device.getName() + "\n" + device.getAddress());
			 	pairedListView.setAdapter(mAdapter);
	        	pairedListView.setSelection(0);
				newBt();
				btAdapter.cancelDiscovery();
	     }
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"onDestroy");
		this.unregisterReceiver(mReceiver);  
		if (btAdapter != null) {
			btAdapter.cancelDiscovery();
		}
		//this.unregisterReceiver(mReceiver);
		if(null != mWakeLock){
            mWakeLock.release();
		}
	}

	private void doDiscovery() {
		setProgressBarIndeterminateVisibility(true);
		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}
		btAdapter.startDiscovery();
	}
	
	private void saveMacSetting(String mac_str,String name_str)
	{
		PreferData prefer = new PreferData(mContext);
		prefer.saveBtMac(mac_str);
		prefer.saveBtName(name_str);
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			Log.d(TAG,"mDeviceClickListener");
			//mClient.connectBt();
			btAdapter.cancelDiscovery();
			
			String info =(String)av.getItemAtPosition(arg2);
			if (info.length() < 17) {
				return;
			}
			String mac = info.substring(info.length() - 17);
			String devicename = info.substring(0, info.length()-18);
			Log.d(TAG,"mac = " + mac);
			Log.d(TAG,"name = " + devicename);
		
			Intent intent = new Intent(mContext, BTsearchActivity.class);
			intent.putExtra("mac", mac);
			intent.putExtra("devicename", devicename);
			startActivity(intent);
		}
	};
	
	private String getDeviceMac()
	{	
		PreferData prefer = new PreferData(mContext);
		return prefer.readBtMac();
		
	}
	
	class MyDetailAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mPairedList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mPairedList.get(mPairedList.size()-arg0-1);
			//return mPairedList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.device_item, null);
				holder = new ViewHolder();
				
				holder.name = (TextView) convertView
						.findViewById(R.id.device_name);
				/*holder.check = (ImageView) convertView
						.findViewById(R.id.check_state);*/
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			//Log.i("TAG", "原来所在位置" +position+"" );
			int mposition = mPairedList.size() - position - 1;
			//Log.i("TAG", "后来所在位置" +mposition+"" );*/
			final String name_str = mPairedList.get(mposition);// 获取当前显示的实体
			// 
		//	int is_check = mPairedCheck.get(position);
			// 
			holder.name.setText(name_str);

			return convertView;
		}
	}

	static class ViewHolder {
		//ImageView check;
		TextView name;
	}
}
