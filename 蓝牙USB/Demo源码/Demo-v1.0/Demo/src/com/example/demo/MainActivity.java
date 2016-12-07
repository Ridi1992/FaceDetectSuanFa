package com.example.demo;






import com.example.demo.R;
import com.sdt.Common;
import com.sdt.Sdtapi;


import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;




public class MainActivity extends Activity {
	
	
	MainActivity mact;
	Thread t2;

	Button btgetsamid; //获取模块编号按钮
	TextView ViewRe ; //结果显示区域
	Button btgetsamstatus ; //获取模块状态按钮
	
	
	Button btReadIdNum;  //读身份证号码按钮
	Button btReadBaseMsg ;//读基本信息按钮
	
	Button btfindloop ;  //循环扫描找卡按钮
	Button btstopfindloop ; //停止循环扫描按钮
	Button btcleartext;  //情况右侧显示按钮
	Button btcloseapp; //关闭应用程序按钮
	
	Common common; //common对象，存储一些需要的参数
	
	/*民族列表*/
	String [] nation ={"汉","蒙古","回","藏","维吾尔","苗","彝","壮","布依","朝鲜",
			"满","侗","瑶","白","土家","哈尼","哈萨克","傣","黎","傈僳",
			"佤","畲","高山","拉祜","水","东乡","纳西","景颇","克尔克孜","土",
			"达斡尔","仫佬","羌","布朗","撒拉","毛南","仡佬","锡伯","阿昌","普米",
			"塔吉克","怒","乌兹别克","俄罗斯","鄂温克","德昂","保安","裕固","京","塔塔尔",
			"独龙","鄂伦春","赫哲","门巴","珞巴","基诺"			
			};
	
	public boolean findloop=true;
	Sdtapi sdta;
	
	 Handler MyHandler = new Handler(){ //消息处理函数，处理应用程序内部的消息传递
		
		public void handleMessage(Message msg){
			if(msg.what==1) //结束循环扫描
				setstop((String) msg.obj);	
			else if(msg.what==2) //开始循环扫描找卡
			{
				setallunclick((String) msg.obj);
				new Thread(){
					public void run()
					{
						try {
							sleep(2500);
							if(t2!=null && t2.isAlive())
								findloop=false;
							mact.finish();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();
			}
			else if(msg.what==3)//usb设备已经重新授权，应用重新启动
			{
				mact.recreate();
			}
			
		}
		
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		mact = this;
		
		common = new Common();
		
		
		
		
		btgetsamid = (Button) findViewById(R.id.GetSamID);
		ViewRe = (TextView) findViewById(R.id.result);
				
		btgetsamstatus = (Button) findViewById(R.id.GetSamStatus);
		
		btReadIdNum =(Button) findViewById(R.id.SDT_ReadIdNum);
		btReadBaseMsg = (Button)findViewById(R.id.SDT_ReadBaseMsg);
		
		btfindloop = (Button)findViewById(R.id.readloop);
		btstopfindloop = (Button)findViewById(R.id.stopreadloop);
		btcleartext = (Button)findViewById(R.id.cleartext);
		btcloseapp = (Button)findViewById(R.id.closeapp);
		
		
		try {
			sdta= new Sdtapi(this); 
			
		} catch (Exception e1) {//捕获异常，
			
			
			if(e1.getCause()==null) //USB设备异常或无连接，应用程序即将关闭。
			{
				
				new Thread(){
					public void run()
					{
						Message msg = new Message();
						msg.what=2;
						msg.obj = "USB设备异常或无连接，应用程序即将关闭。";
						MyHandler.sendMessage(msg);
					}
				}.start();
			}
			else //USB设备未授权，需要确认授权
			{	
				ViewRe.setGravity(0);
				ViewRe.setTextSize(30);
				
				setallunclick("USB设备未授权，弹出请求授权窗口后，请点击\"确定\"继续");
				
			}
			
		}

      
		IntentFilter filter = new IntentFilter();//意图过滤器		
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);//USB设备拔出
        filter.addAction(common.ACTION_USB_PERMISSION);//自定义的USB设备请求授权
        registerReceiver(mUsbReceiver, filter);

      //获取模块状态
		btgetsamstatus.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				int ret;
				ret =sdta.SDT_GetSAMStatus();
				String show;
				if(ret==0x90)show = "模块状态良好";
				else 
					show ="模块状态错误:"+ String.format("0x%02x", ret);
				
				ViewRe.setText(show);
				
			}
			
		});
		
		
		//获取模块编码SAMID即安全模块编号
		btgetsamid.setOnClickListener(new OnClickListener()
		{
			 public void onClick(View v) {
			
				 char [] puSAMID = new char[36];			
				int ret= sdta.SDT_GetSAMIDToStr( puSAMID);
				
				if(ret==0x90)
					ViewRe.setText(puSAMID, 0, puSAMID.length);
				else 
				{
					String show ="错误:"+ String.format("0x%02x", ret);
					ViewRe.setText(show);
					
				}
			 }
		});
		
	
		//读身份证号码
		btReadIdNum.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				int ret;
				
				sdta.SDT_StartFindIDCard();//寻找身份证
				sdta.SDT_SelectIDCard();//选取身份证
				
			
				IdCardMsg msg = new IdCardMsg();//身份证信息对象，存储身份证上的文字信息
				
				ret =ReadBaseMsgToStr(msg);
				
				String show;
				if(ret==0x90)
					show = "身份证号码:"+'\n'+String.valueOf(msg.id_num)+'\n';
				else 
					show ="读身份证号码失败:"+String.format("0x%02x", ret);
				
				ViewRe.setText(show);
				
			}
		});
		
		
		//读基本信息
		btReadBaseMsg.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				
				int ret;
				String show = "";
				
				sdta.SDT_StartFindIDCard();//寻找身份证
				sdta.SDT_SelectIDCard();//选取身份证
				
				IdCardMsg msg = new IdCardMsg();//身份证信息对象，存储身份证上的文字信息
				
				ret =ReadBaseMsgToStr(msg);
						
				if(ret==0x90)
				{
					show =   "姓名:"+msg.name+'\n'
							+"性别:"+msg.sex+'\n'
							+"民族:"+msg.nation_str+"族"+'\n'
							+"出生日期:"+msg.birth_year+"-"+msg.birth_month+"-"+msg.birth_day+'\n'
							+"住址:"+msg.address+'\n'
							+"身份证号码:"+msg.id_num+'\n'
							+"签发机关:"+msg.sign_office+'\n'
							+"有效期起始日期:"+msg.useful_s_date_year+"-"+msg.useful_s_date_month+"-"+msg.useful_s_date_day+'\n'
							+"有效期截止日期:"+msg.useful_e_date_year+"-"+msg.useful_e_date_month+"-"+msg.useful_e_date_day+'\n';

				}
				else
					show ="读基本信息失败:"+String.format("0x%02x", ret);
			
					ViewRe.setText(show);
				}//end onclick()
			
		});
		
	
		//循环扫描找卡
		btfindloop.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				setallunclick("循环读卡中，除\"停止循环扫描\"按钮，其他都不可操作。");
				
				btstopfindloop.setClickable(true); //停止循环扫描按钮可点击
				btstopfindloop.setBackgroundResource(R.drawable.ic_button); //停止循环扫描按钮背景色变成可操作的颜色
	
				findloop=true;
				t2 = new Thread(){
					public void run()
					{
						while(findloop)
						{
							int ret =sdta.SDT_StartFindIDCard();
							String show;
							if(ret==0x9f)//找卡成功
							{
								ret =sdta.SDT_SelectIDCard();
								if(ret==0x90) //选卡成功
								{
									IdCardMsg cardmsg = new IdCardMsg();
									
									ret =ReadBaseMsgToStr(cardmsg);
											
									if(ret==0x90)
									{
										show =   "姓名:"+cardmsg.name+'\n'
												+"性别:"+cardmsg.sex+'\n'
												+"民族:"+cardmsg.nation_str+"族"+'\n'
												+"出生日期:"+cardmsg.birth_year+"-"+cardmsg.birth_month+"-"+cardmsg.birth_day+'\n'
												+"住址:"+cardmsg.address+'\n'
												+"身份证号码:"+cardmsg.id_num+'\n'
												+"签发机关:"+cardmsg.sign_office+'\n'
												+"有效期起始日期:"+cardmsg.useful_s_date_year+"-"+cardmsg.useful_s_date_month+"-"+cardmsg.useful_s_date_day+'\n'
												+"有效期截止日期:"+cardmsg.useful_e_date_year+"-"+cardmsg.useful_e_date_month+"-"+cardmsg.useful_e_date_day+'\n';

									}
									else
										show ="读基本信息失败:"+String.format("0x%02x", ret);
															
									findloop=false;
									Message msg = new Message();
									msg.what=1;
									msg.obj = show;
									MyHandler.sendMessage(msg);
									break;
									
								}
								
							}//end if 找卡成功
							
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}								
							
						}//end while 
					}
				};
				t2.start();
			}	//end onclick
		});
		
		
		//结束循环扫描找卡
		btstopfindloop.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {						
				findloop=false;
				setstop("结束循环读卡。");
				
			}
			
		});
		
		
		//清空右侧显示
		btcleartext.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				ViewRe.setText("");
			}
			
		});
		
		
		//关闭应用程序
		btcloseapp.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if(t2!=null && t2.isAlive())
					findloop=false;
				
				MainActivity.this.finish();
				
			}
			
		});
		
		
	
		
	}      
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	//结束循环扫描时，将按钮恢复可操作状态和颜色
	public void setstop(String show)
	{
		
		btstopfindloop.setClickable(false);
		btstopfindloop.setBackgroundResource(R.drawable.ic_button_unclickable);
			
		btfindloop.setClickable(true);
		btfindloop.setBackgroundResource(R.drawable.ic_button);
			
		btgetsamid.setClickable(true);
		btgetsamid.setBackgroundResource(R.drawable.ic_button);
		btgetsamstatus.setClickable(true);
		btgetsamstatus.setBackgroundResource(R.drawable.ic_button);
			
		
		btReadIdNum.setClickable(true);
		btReadIdNum.setBackgroundResource(R.drawable.ic_button);
		
		btReadBaseMsg.setClickable(true);
		btReadBaseMsg.setBackgroundResource(R.drawable.ic_button);
			
		btcleartext.setClickable(true);
		btcleartext.setBackgroundResource(R.drawable.ic_button);
		
		ViewRe.setText(show);
		
	}
	
	
	//开始扫描时，将按钮设为不可操作状态和颜色
	public void setallunclick(String show)
	{
		btstopfindloop.setClickable(false);
		btstopfindloop.setBackgroundResource(R.drawable.ic_button_unclickable);

		btfindloop.setClickable(false);
		btfindloop.setBackgroundResource(R.drawable.ic_button_unclickable);

		
		ViewRe.setText(show);
		btgetsamid.setClickable(false);
		btgetsamid.setBackgroundResource(R.drawable.ic_button_unclickable);
		btgetsamstatus.setClickable(false);
		btgetsamstatus.setBackgroundResource(R.drawable.ic_button_unclickable);
		
		btReadIdNum.setClickable(false);
		btReadIdNum.setBackgroundResource(R.drawable.ic_button_unclickable);
		
		btReadBaseMsg.setClickable(false);
		btReadBaseMsg.setBackgroundResource(R.drawable.ic_button_unclickable);
		
		btcleartext.setClickable(false);
		btcleartext.setBackgroundResource(R.drawable.ic_button_unclickable);
	
	}
	
	
	//广播接收器
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		 
	public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	      //USB设备拔出广播  
	      if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                String deviceName = device.getDeviceName();
                if (device != null && device.equals(deviceName)) {
                  	Message msg = new Message();
					msg.what=2;
					msg.obj = "USB设备拔出，应用程序即将关闭。";
					MyHandler.sendMessage(msg);
                	
                }
                
            }
	     else if(common.ACTION_USB_PERMISSION.equals(action)){//USB设备未授权，从SDTAPI中发出的广播
	    	  Message msg = new Message();
				msg.what=3;
				msg.obj = "USB设备无权限";
				MyHandler.sendMessage(msg);
	      }
	      
	        
	    }
	};
	
	
	 //字节解码函数
	 void DecodeByte(byte[] msg,char []msg_str) throws Exception
	{
		byte[] newmsg = new byte[msg.length+2];
		
		newmsg[0]=(byte) 0xff;
		newmsg[1] =(byte) 0xfe;
		
		for(int i=0;i<msg.length;i++)
			newmsg[i+2]= msg[i];
		
		String s=new String(newmsg,"UTF-16");
		for(int i=0;i<s.toCharArray().length;i++)
			msg_str[i]=s.toCharArray()[i];
		
	
	}
	
	 //读取身份证中的文字信息（可阅读格式的）
	public int ReadBaseMsgToStr(IdCardMsg msg)
	{
		int ret;
		int []puiCHMsgLen=new int[1];
		int []puiPHMsgLen=new int[1];
		
		byte [] pucCHMsg = new byte[256];
		byte [] pucPHMsg = new byte[1024];
		
		//sdtapi中标准接口，输出字节格式的信息。
		ret =sdta.SDT_ReadBaseMsg( pucCHMsg, puiCHMsgLen, pucPHMsg, puiPHMsgLen);
			
		if(ret==0x90)
		{
			try {
				char [] pucCHMsgStr = new char[128];
				DecodeByte(pucCHMsg,pucCHMsgStr);//将读取的身份证中的信息字节，解码成可阅读的文字
				PareseItem(pucCHMsgStr,msg); //将信息解析到msg中
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		return ret;

	}
	
	
	//分段信息提取
	void  PareseItem(char []pucCHMsgStr,IdCardMsg msg)
	{
		msg.name = String.copyValueOf(pucCHMsgStr, 0, 15);
		String sex_code = String.copyValueOf(pucCHMsgStr, 15, 1);
		
		if(sex_code.equals("1")) msg.sex="男";
		else if(sex_code.equals("2"))msg.sex="女";
		else if(sex_code.equals("0"))msg.sex="未知";
		else if (sex_code.equals("9"))msg.sex="未说明";
							
		String nation_code = String.copyValueOf(pucCHMsgStr, 16, 2);
		msg.nation_str = nation[Integer.valueOf(nation_code)-1];
				
				
		msg.birth_year = String.copyValueOf(pucCHMsgStr, 18, 4);
		msg.birth_month = String.copyValueOf(pucCHMsgStr, 22, 2);
		msg.birth_day = String.copyValueOf(pucCHMsgStr, 24, 2);
		msg.address = String.copyValueOf(pucCHMsgStr, 26, 35);
		msg.id_num = String.copyValueOf(pucCHMsgStr, 61, 18);
		msg.sign_office = String.copyValueOf(pucCHMsgStr, 79, 15);
				
		msg.useful_s_date_year = String.copyValueOf(pucCHMsgStr, 94, 4);
		msg.useful_s_date_month = String.copyValueOf(pucCHMsgStr, 98, 2);
		msg.useful_s_date_day = String.copyValueOf(pucCHMsgStr, 100, 2);
				
		msg.useful_e_date_year = String.copyValueOf(pucCHMsgStr, 102, 4);
		msg.useful_e_date_month = String.copyValueOf(pucCHMsgStr, 106, 2);
		msg.useful_e_date_day = String.copyValueOf(pucCHMsgStr, 108, 2);			
		
	}   

	
}
class IdCardMsg{
	public String name;
	public String sex;	
	public String nation_str;
			
			
	public String birth_year ;
	public String birth_month ;
	public String birth_day ;
	public String address ;
	public String id_num ;
	public String sign_office;
			
	public String useful_s_date_year ;
	public String useful_s_date_month ;
	public String useful_s_date_day ;
			
	public String useful_e_date_year ;
	public String useful_e_date_month;
	public String useful_e_date_day ;
			
}

