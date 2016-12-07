package com.kaeridcard.factest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;



public class GetMacReceiver extends BroadcastReceiver {
	private final String GET_MAC_ACTION = "com.kaeridcard.client.GET_MAC";
	private final String RCV_MAC_ACTION = "com.kaeridcard.client.RCV_MAC";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction(); 
		if (action.equals(GET_MAC_ACTION)) {
			// 停止后台服务
			PreferData prefer = new PreferData(context);
			String mac_str = prefer.readBtMac();
			String name_str = prefer.readBtName();
			Intent mac_intent = new Intent(RCV_MAC_ACTION);
			mac_intent.putExtra("mac", mac_str);
			mac_intent.putExtra("name", name_str);
			context.sendBroadcast(mac_intent);
		}
	}
}
