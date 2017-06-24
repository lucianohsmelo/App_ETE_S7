package com.example.ete_s7;

import java.text.SimpleDateFormat;
import java.util.Timer;

import Moka7.*;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;
import java.util.TimerTask;



@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {
	private Button btnConnect;
	private Button btnLiga;
	private Button btnDesl;
	private Button btnModo2;
	private Button btnModo1;
	private Button btnTempSet;
	private Button btnRun;
	private Button btnStop;
	
	private TextView outputText;
	private TextView loopText;
	private TextView tempAtualText;
	private TextView tempDeslText;
	private TextView statusText;
	private TextView modoText;
	private TextView AtvS7ClientText;
	
	private EditText ipAddrEText;
	private EditText tempDeslEText;
	private EditText dbEText;
	
	private CheckBox checkBoxM1;
	private CheckBox checkBoxM2;
	
	public boolean S7ClientConnected = false;
	public int AtvS7Client, tempDesl;
	boolean modoM, atvS7Client, statusSig;
	public String strMotorSel;
	public String tempM1Str, tempM2Str, tempDeslStr, modoMStr, statusMStr,
				AtvS7ClientStr; 
	public double tempM1, tempM2;
	
	private Handler uiHandler = new Handler();
    private MyTimerTask myTask = new MyTimerTask();
    private Timer myTimer = new Timer();    
    final Handler handler = new Handler();
	
    private static byte[] Buffer = new byte[65536]; // 64K buffer (maximum for S7400 systems)
    private static final S7Client Client = new S7Client();   
    private static String IpAddress = "192.168.1.10";
    private static int Rack = 0; // Default 0 for S7300
    private static int Slot = 2; // Default 2 for S7300 
    private static int DBSample; // Sample DB that must be present in the CPU
   

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		myTimer.schedule(myTask, 1000, 1500);

		checkBoxM1 = (CheckBox)findViewById(R.id.checkBox1);
		checkBoxM2 = (CheckBox)findViewById(R.id.checkBox2);
		checkBoxM1.setChecked(false);
		checkBoxM2.setChecked(false);
			
		checkBoxM1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date1 = new Date();
					setOutputText(dateFormat.format(date1)
							+" - Atualizando informa��es Motor 1");
					strMotorSel = "Motor 1";
					if(checkBoxM1.isChecked() == false){
						checkBoxM2.setChecked(true);
					}else{
						checkBoxM2.setChecked(false);
					}
					
				}
			});
			
		checkBoxM2.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date1 = new Date();
					setOutputText(dateFormat1.format(date1)
							+" - Atualizando informa��es Motor 2");
					strMotorSel = "Motor 2";
					if(checkBoxM2.isChecked() == false){
						checkBoxM1.setChecked(true);
					}else{
						checkBoxM1.setChecked(false);
					}
					
				}
			});

		outputText = (TextView)findViewById(R.id.textView7);
		outputText.setTextColor(Color.DKGRAY);
		
		loopText = (TextView)findViewById(R.id.textView1);
		loopText.setTextColor(Color.DKGRAY);
			
		tempAtualText = (TextView)findViewById(R.id.textView5);
		tempDeslText = (TextView)findViewById(R.id.textView8);
		statusText = (TextView)findViewById(R.id.textView9);
		modoText = (TextView)findViewById(R.id.textView10);
		AtvS7ClientText = (TextView)findViewById(R.id.textView11);
		ipAddrEText = (EditText)findViewById(R.id.editText1);
		tempDeslEText = (EditText)findViewById(R.id.editText3);
		dbEText = (EditText)findViewById(R.id.editText2);
		
		btnConnect = (Button)findViewById(R.id.button1);
		btnConnect.setOnClickListener(new View.OnClickListener() {
				
				@Override
			public void onClick(View v) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				try{
					Date date1 = new Date();
					setOutputText(dateFormat.format(date1)
							+" - Conectando ao dispositivo: "
							+ ipAddrEText.getText().toString() +". Aguarde...");
					checkBoxM1.setChecked(true);
					checkBoxM2.setChecked(false);
					new Disconnect().execute("");
					new Connect().execute("");
				}catch (Exception e){
						setOutputText("ERRO");
				}
					
			}
		});
	
		btnLiga = (Button)findViewById(R.id.button3);
		btnLiga.setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				if (Client.Connected){
					modoM = S7.GetBitAt(Buffer, 10, 0);
					if (modoM == true){
						 S7.SetBitAt(Buffer, 10, 1, true);
						 new WriteDB().execute("");
						 Date date1 = new Date();
						 setOutputText(dateFormat.format(date1) + " - "+
								"Enviando comando para sinal 5V");
						 outputText.setTextColor(Color.DKGRAY);
					}else{
						Date date = new Date();
						setOutputText(dateFormat.format(date)+
							" - Opera��o n�o realizada: Modo 2 n�o selecionado");
						outputText.setTextColor(Color.RED);
					}
				}else{
					Date date1 = new Date();
					 setOutputText(dateFormat.format(date1) + " - "+
								"Erro de conex�o");
				}
			}
		});
			
		btnDesl = (Button)findViewById(R.id.button4);
		btnDesl.setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				if (Client.Connected){
					modoM = S7.GetBitAt(Buffer, 10, 0);
					if (modoM == true){
						 S7.SetBitAt(Buffer, 10, 1, false);
						 new WriteDB().execute("");
						 Date date1 = new Date();
						 setOutputText(dateFormat.format(date1) + " - "+
								"Enviando comando para sinal 0V");
						 outputText.setTextColor(Color.DKGRAY);
					}else{
						Date date = new Date();
						setOutputText(dateFormat.format(date)+
								" - Opera��o n�o realizada: Modo 2 n�o selecionado");
						outputText.setTextColor(Color.RED);
					}
				}else{
					Date date1 = new Date();
					 setOutputText(dateFormat.format(date1) + " - "+
								"Erro de conex�o");
					
				}
				
			}
		});
			
		btnModo1 = (Button)findViewById(R.id.button5);
		btnModo1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				if (Client.Connected){
						 S7.SetBitAt(Buffer, 10, 0, false);
						 new WriteDB().execute("");
						 Date date1 = new Date();
						 setOutputText(dateFormat.format(date1) + " - "+
								"Enviando comando para Modo 1");
						 outputText.setTextColor(Color.DKGRAY);
					}else{
						 Date date1 = new Date();
						 setOutputText(dateFormat.format(date1) + " - "+
									"Erro conex�o");
						 outputText.setTextColor(Color.RED);
					}
				}
				
		});
			
		btnModo2 = (Button)findViewById(R.id.button6);
		btnModo2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				if (Client.Connected){
						 S7.SetBitAt(Buffer, 10, 0, true);
						 new WriteDB().execute("");
						 Date date1 = new Date();
						 setOutputText(dateFormat.format(date1) + " - "+
								"Enviando comando Modo2");
						 outputText.setTextColor(Color.DKGRAY);
				}else{
					Date date1 = new Date();
					setOutputText(dateFormat.format(date1) + " - "+
							"Erro conex�o");
					 outputText.setTextColor(Color.RED);
					}
				}
					
		});
			
		btnTempSet = (Button)findViewById(R.id.button7);
		btnTempSet.setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				if (Client.Connected){
					if (checkInt(tempDeslEText.getText().toString()) == true){
						tempDesl = Integer.valueOf(tempDeslEText.getText().toString());
						S7.SetWordAt(Buffer, 8, tempDesl);
						new WriteDB().execute("");
						outputText.setTextColor(Color.DKGRAY);
					}else{
						Date date = new Date();
						setOutputText(dateFormat.format(date)+
								" - Opera��o n�o realizada: Valor de temperatura n�o v�lido");
						outputText.setTextColor(Color.RED);
					}
				}else{
					Date date = new Date();
					setOutputText(dateFormat.format(date)+
							" - Opera��o n�o realizada: client desconectado");
					outputText.setTextColor(Color.RED);
						
				}
			}
		});
			
			
		btnRun = (Button)findViewById(R.id.button8);
		btnRun.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new runMode().execute("");
				// TODO Auto-generated method stub
					
			}
		});
			
		btnStop = (Button)findViewById(R.id.button2);
		btnStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new stopMode().execute("");
				// TODO Auto-generated method stub
			
			}
		});
	}
	
    class MyTimerTask extends TimerTask {
   	  public void run() {
   		if (Client.Connected){
   			try{
   				new ReadDB().execute("");
   				tempM1 = S7.GetFloatAt(Buffer, 0);
   				tempM2 = S7.GetFloatAt(Buffer, 4);
   				tempDesl = S7.GetWordAt(Buffer, 8);
   				modoM = S7.GetBitAt(Buffer, 10, 0);
   				statusSig = S7.GetBitAt(Buffer, 10, 1);
   				atvS7Client = S7.GetBitAt(Buffer, 10, 2);
   				if(checkBoxM1.isChecked()){
   					setOutputValue(String.format("%.2f", tempM1), String.format("%d", tempDesl), Boolean.toString(modoM),
   							Boolean.toString(statusSig), Boolean.toString(atvS7Client));
   				}else if(checkBoxM2.isChecked()){
   					setOutputValue(String.format("%.2f", tempM2), String.format("%d", tempDesl), Boolean.toString(modoM),
   							Boolean.toString(statusSig), Boolean.toString(atvS7Client));
   				}else{
   					setOutputValue("***","***","***","***","***");
   				}
    	
   			}catch (Exception e){
   				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
   				Date date1 = new Date();
   				setTextLoop(dateFormat.format(date1)+
   						" Erro - MyTimerTask");	
   				new Disconnect().execute("");
   			}
   		}else{
			setTextLoop("");
			new Disconnect().execute("");
   		}
  		
   	  }
  	}

	class Connect extends AsyncTask<String, Void, String>{

    	String ret = "";
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			try{
				Client.SetConnectionType(S7.OP);
				DBSample = Integer.parseInt(dbEText.getText().toString());
				IpAddress = (ipAddrEText.getText().toString());
		    	int Result = Client.ConnectTo(IpAddress, Rack, Slot);
		    	if (Result == 0){
		    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date1 = new Date();
					setOutputText(dateFormat.format(date1) + " - "+
							"Conectado ao CLP");
					outputText.setTextColor(Color.DKGRAY);
					
		    		
		    	}else{
		    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		        	Date date = new Date();
					setOutputText(dateFormat.format(date)+ " - "+
								S7Client.ErrorText(Result));
					outputText.setTextColor(Color.RED);
		    	}
			}catch(Exception e){
				Thread.interrupted();
			}
			return null;
		}
    }
	
	class Disconnect extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try{
				
				Client.Disconnect();
				
			}catch(Exception e){
				Thread.interrupted();
			}
			return null;
		}
    	
    }
	
	class ReadDB extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			DBSample = Integer.parseInt(dbEText.getText().toString());
    		int Result = Client.ReadArea(S7.S7AreaDB, DBSample, 0, 12, Buffer);
    		if (Result == 0){
				Date date1 = new Date();
    			setTextLoop(dateFormat.format(date1)); 
    		}else{
    			Date date = new Date();
				setOutputText(dateFormat.format(date)+ " - "+
						S7Client.ErrorText(Result));
				outputText.setTextColor(Color.RED);
				new Disconnect().execute("");
				Notifi("","","","");
    		}
			return null;
		}
		
	}

	class WriteDB extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				try{
					DBSample = Integer.parseInt(dbEText.getText().toString());
					int Result = Client.WriteArea(S7.S7AreaDB, DBSample, 0, 12, Buffer);
					if (Result == 0){
						Date date1 = new Date();
						 setOutputText(dateFormat.format(date1) + " - "+
								"Escrita no CLP OK");
						 outputText.setTextColor(Color.DKGRAY);
						
					}else{
						Date date = new Date();
						setOutputText(dateFormat.format(date)+ " - "+
								S7Client.ErrorText(Result));
						outputText.setTextColor(Color.RED);
						new Disconnect().execute("");
						
					}
				}catch(Exception e){
					setOutputText(e.toString());
				 }
			return null;
		}
		
	}
	
	class stopMode extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try{
				Client.PlcStop();
				
			}catch(Exception e){
				
			}
			return null;
		}
		
	}
	
	class runMode extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try{
				Client.PlcHotStart();
				
			}catch(Exception e){
				
			}
			return null;
		}
		
	}
	
	public void setOutputText(String str1) {
		
		final String outText = str1;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
               outputText.setText(outText);
            }
        });
	}

	public void setOutputValue(String str1, String str2, String str3, String str4, String str5) {
		final String tempAtual = str1;
		final String tempDesl = str2;
		final String modo = str3;
		final String status = str4;
		final String atv = str5;
		uiHandler.post(new Runnable() {
        @Override
        public void run() {
           tempAtualText.setText("Temperatura atual: " + tempAtual + "�C");
           tempDeslText.setText("Temperatura del: " + tempDesl + "�C");
           if (modo == "false"){
        	   modoText.setText("Modo 1 selecionado");
           }else{
        	   modoText.setText("Modo 2 selecionado");   
           }
           if (status == "false"){
        	   statusText.setText("Estado sinal: 0V");
           }else{
        	   statusText.setText("Estado sinal: 5V");
           }
           AtvS7ClientText.setText(atv);
        }
    });
}

	public void setTextLoop(String str){
		final String outText = str;
		uiHandler.post(new Runnable() {
        @Override
        public void run() {
            // This gets executed on the UI thread so it can safely modify Views
            loopText.setText("Última atualiza��o: "+outText);
            if (Client.Connected){
            	loopText.setTextColor(Color.DKGRAY);
            }else{
            	loopText.setTextColor(Color.RED);
            }
        }
    });
	
}
	
	public boolean checkInt(String str) {
		boolean isInt = true;
		try {
			int i = Integer.parseInt(str);
		}catch(NumberFormatException nfe) {
			isInt = false;
		}
		return isInt;    
	}
	
	public void Notifi(String str1, String str2, String str3, String str4){
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		PendingIntent p = PendingIntent.getActivity(this, 0, new Intent(this, NotifiActivity.class), 0);
		
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setTicker(str1);
		builder.setContentTitle(str1);
		//builder.setContentText("Descri��o"); 
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentIntent(p);
		
		
		NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
		String [] descs = new String [] {"", "","", ""};
		for(int i = 0; i < descs.length; i++){
			style.addLine(descs[i]);
		}
		builder.setStyle(style);
		
		Notification n = builder.build();
		n.vibrate = new long[]{150, 350, 200, 350};
		n.flags = Notification.FLAG_AUTO_CANCEL;
		nm.notify(R.drawable.ic_launcher, n);
		
		try{
			Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone toque = RingtoneManager.getRingtone(this, som);
			toque.play();
			
		}catch(Exception e){
			
		}
		
	}
}
