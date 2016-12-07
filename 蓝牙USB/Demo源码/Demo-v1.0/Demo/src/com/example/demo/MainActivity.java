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

	Button btgetsamid; //��ȡģ���Ű�ť
	TextView ViewRe ; //�����ʾ����
	Button btgetsamstatus ; //��ȡģ��״̬��ť
	
	
	Button btReadIdNum;  //�����֤���밴ť
	Button btReadBaseMsg ;//��������Ϣ��ť
	
	Button btfindloop ;  //ѭ��ɨ���ҿ���ť
	Button btstopfindloop ; //ֹͣѭ��ɨ�谴ť
	Button btcleartext;  //����Ҳ���ʾ��ť
	Button btcloseapp; //�ر�Ӧ�ó���ť
	
	Common common; //common���󣬴洢һЩ��Ҫ�Ĳ���
	
	/*�����б�*/
	String [] nation ={"��","�ɹ�","��","��","ά���","��","��","׳","����","����",
			"��","��","��","��","����","����","������","��","��","����",
			"��","�","��ɽ","����","ˮ","����","����","����","�˶�����","��",
			"���Ӷ�","����","Ǽ","����","����","ë��","����","����","����","����",
			"������","ŭ","���ȱ��","����˹","���¿�","�°�","����","ԣ��","��","������",
			"����","���״�","����","�Ű�","���","��ŵ"			
			};
	
	public boolean findloop=true;
	Sdtapi sdta;
	
	 Handler MyHandler = new Handler(){ //��Ϣ������������Ӧ�ó����ڲ�����Ϣ����
		
		public void handleMessage(Message msg){
			if(msg.what==1) //����ѭ��ɨ��
				setstop((String) msg.obj);	
			else if(msg.what==2) //��ʼѭ��ɨ���ҿ�
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
			else if(msg.what==3)//usb�豸�Ѿ�������Ȩ��Ӧ����������
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
			
		} catch (Exception e1) {//�����쳣��
			
			
			if(e1.getCause()==null) //USB�豸�쳣�������ӣ�Ӧ�ó��򼴽��رա�
			{
				
				new Thread(){
					public void run()
					{
						Message msg = new Message();
						msg.what=2;
						msg.obj = "USB�豸�쳣�������ӣ�Ӧ�ó��򼴽��رա�";
						MyHandler.sendMessage(msg);
					}
				}.start();
			}
			else //USB�豸δ��Ȩ����Ҫȷ����Ȩ
			{	
				ViewRe.setGravity(0);
				ViewRe.setTextSize(30);
				
				setallunclick("USB�豸δ��Ȩ������������Ȩ���ں�����\"ȷ��\"����");
				
			}
			
		}

      
		IntentFilter filter = new IntentFilter();//��ͼ������		
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);//USB�豸�γ�
        filter.addAction(common.ACTION_USB_PERMISSION);//�Զ����USB�豸������Ȩ
        registerReceiver(mUsbReceiver, filter);

      //��ȡģ��״̬
		btgetsamstatus.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				int ret;
				ret =sdta.SDT_GetSAMStatus();
				String show;
				if(ret==0x90)show = "ģ��״̬����";
				else 
					show ="ģ��״̬����:"+ String.format("0x%02x", ret);
				
				ViewRe.setText(show);
				
			}
			
		});
		
		
		//��ȡģ�����SAMID����ȫģ����
		btgetsamid.setOnClickListener(new OnClickListener()
		{
			 public void onClick(View v) {
			
				 char [] puSAMID = new char[36];			
				int ret= sdta.SDT_GetSAMIDToStr( puSAMID);
				
				if(ret==0x90)
					ViewRe.setText(puSAMID, 0, puSAMID.length);
				else 
				{
					String show ="����:"+ String.format("0x%02x", ret);
					ViewRe.setText(show);
					
				}
			 }
		});
		
	
		//�����֤����
		btReadIdNum.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				int ret;
				
				sdta.SDT_StartFindIDCard();//Ѱ�����֤
				sdta.SDT_SelectIDCard();//ѡȡ���֤
				
			
				IdCardMsg msg = new IdCardMsg();//���֤��Ϣ���󣬴洢���֤�ϵ�������Ϣ
				
				ret =ReadBaseMsgToStr(msg);
				
				String show;
				if(ret==0x90)
					show = "���֤����:"+'\n'+String.valueOf(msg.id_num)+'\n';
				else 
					show ="�����֤����ʧ��:"+String.format("0x%02x", ret);
				
				ViewRe.setText(show);
				
			}
		});
		
		
		//��������Ϣ
		btReadBaseMsg.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				
				int ret;
				String show = "";
				
				sdta.SDT_StartFindIDCard();//Ѱ�����֤
				sdta.SDT_SelectIDCard();//ѡȡ���֤
				
				IdCardMsg msg = new IdCardMsg();//���֤��Ϣ���󣬴洢���֤�ϵ�������Ϣ
				
				ret =ReadBaseMsgToStr(msg);
						
				if(ret==0x90)
				{
					show =   "����:"+msg.name+'\n'
							+"�Ա�:"+msg.sex+'\n'
							+"����:"+msg.nation_str+"��"+'\n'
							+"��������:"+msg.birth_year+"-"+msg.birth_month+"-"+msg.birth_day+'\n'
							+"סַ:"+msg.address+'\n'
							+"���֤����:"+msg.id_num+'\n'
							+"ǩ������:"+msg.sign_office+'\n'
							+"��Ч����ʼ����:"+msg.useful_s_date_year+"-"+msg.useful_s_date_month+"-"+msg.useful_s_date_day+'\n'
							+"��Ч�ڽ�ֹ����:"+msg.useful_e_date_year+"-"+msg.useful_e_date_month+"-"+msg.useful_e_date_day+'\n';

				}
				else
					show ="��������Ϣʧ��:"+String.format("0x%02x", ret);
			
					ViewRe.setText(show);
				}//end onclick()
			
		});
		
	
		//ѭ��ɨ���ҿ�
		btfindloop.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				setallunclick("ѭ�������У���\"ֹͣѭ��ɨ��\"��ť�����������ɲ�����");
				
				btstopfindloop.setClickable(true); //ֹͣѭ��ɨ�谴ť�ɵ��
				btstopfindloop.setBackgroundResource(R.drawable.ic_button); //ֹͣѭ��ɨ�谴ť����ɫ��ɿɲ�������ɫ
	
				findloop=true;
				t2 = new Thread(){
					public void run()
					{
						while(findloop)
						{
							int ret =sdta.SDT_StartFindIDCard();
							String show;
							if(ret==0x9f)//�ҿ��ɹ�
							{
								ret =sdta.SDT_SelectIDCard();
								if(ret==0x90) //ѡ���ɹ�
								{
									IdCardMsg cardmsg = new IdCardMsg();
									
									ret =ReadBaseMsgToStr(cardmsg);
											
									if(ret==0x90)
									{
										show =   "����:"+cardmsg.name+'\n'
												+"�Ա�:"+cardmsg.sex+'\n'
												+"����:"+cardmsg.nation_str+"��"+'\n'
												+"��������:"+cardmsg.birth_year+"-"+cardmsg.birth_month+"-"+cardmsg.birth_day+'\n'
												+"סַ:"+cardmsg.address+'\n'
												+"���֤����:"+cardmsg.id_num+'\n'
												+"ǩ������:"+cardmsg.sign_office+'\n'
												+"��Ч����ʼ����:"+cardmsg.useful_s_date_year+"-"+cardmsg.useful_s_date_month+"-"+cardmsg.useful_s_date_day+'\n'
												+"��Ч�ڽ�ֹ����:"+cardmsg.useful_e_date_year+"-"+cardmsg.useful_e_date_month+"-"+cardmsg.useful_e_date_day+'\n';

									}
									else
										show ="��������Ϣʧ��:"+String.format("0x%02x", ret);
															
									findloop=false;
									Message msg = new Message();
									msg.what=1;
									msg.obj = show;
									MyHandler.sendMessage(msg);
									break;
									
								}
								
							}//end if �ҿ��ɹ�
							
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
		
		
		//����ѭ��ɨ���ҿ�
		btstopfindloop.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {						
				findloop=false;
				setstop("����ѭ��������");
				
			}
			
		});
		
		
		//����Ҳ���ʾ
		btcleartext.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				ViewRe.setText("");
			}
			
		});
		
		
		//�ر�Ӧ�ó���
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

	
	//����ѭ��ɨ��ʱ������ť�ָ��ɲ���״̬����ɫ
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
	
	
	//��ʼɨ��ʱ������ť��Ϊ���ɲ���״̬����ɫ
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
	
	
	//�㲥������
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		 
	public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	      //USB�豸�γ��㲥  
	      if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                String deviceName = device.getDeviceName();
                if (device != null && device.equals(deviceName)) {
                  	Message msg = new Message();
					msg.what=2;
					msg.obj = "USB�豸�γ���Ӧ�ó��򼴽��رա�";
					MyHandler.sendMessage(msg);
                	
                }
                
            }
	     else if(common.ACTION_USB_PERMISSION.equals(action)){//USB�豸δ��Ȩ����SDTAPI�з����Ĺ㲥
	    	  Message msg = new Message();
				msg.what=3;
				msg.obj = "USB�豸��Ȩ��";
				MyHandler.sendMessage(msg);
	      }
	      
	        
	    }
	};
	
	
	 //�ֽڽ��뺯��
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
	
	 //��ȡ���֤�е�������Ϣ�����Ķ���ʽ�ģ�
	public int ReadBaseMsgToStr(IdCardMsg msg)
	{
		int ret;
		int []puiCHMsgLen=new int[1];
		int []puiPHMsgLen=new int[1];
		
		byte [] pucCHMsg = new byte[256];
		byte [] pucPHMsg = new byte[1024];
		
		//sdtapi�б�׼�ӿڣ�����ֽڸ�ʽ����Ϣ��
		ret =sdta.SDT_ReadBaseMsg( pucCHMsg, puiCHMsgLen, pucPHMsg, puiPHMsgLen);
			
		if(ret==0x90)
		{
			try {
				char [] pucCHMsgStr = new char[128];
				DecodeByte(pucCHMsg,pucCHMsgStr);//����ȡ�����֤�е���Ϣ�ֽڣ�����ɿ��Ķ�������
				PareseItem(pucCHMsgStr,msg); //����Ϣ������msg��
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		return ret;

	}
	
	
	//�ֶ���Ϣ��ȡ
	void  PareseItem(char []pucCHMsgStr,IdCardMsg msg)
	{
		msg.name = String.copyValueOf(pucCHMsgStr, 0, 15);
		String sex_code = String.copyValueOf(pucCHMsgStr, 15, 1);
		
		if(sex_code.equals("1")) msg.sex="��";
		else if(sex_code.equals("2"))msg.sex="Ů";
		else if(sex_code.equals("0"))msg.sex="δ֪";
		else if (sex_code.equals("9"))msg.sex="δ˵��";
							
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

