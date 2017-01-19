package de.eventapp.EVA;

import java.util.Timer;
import java.util.TimerTask;

import de.eventapp.EVA.R;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.os.Vibrator;

public class waver extends Activity implements SensorEventListener{
	
	Context context;
	CustomizeDialog customizeDialog = null;
	private WakeLock wakeLock;
	private SensorManager mSensorManager;
	
	int i = 0;
	int counter = 0;
	int[] counters = new int[1000000];
	
	private Vibrator v;
	int dot = 200;    	
	int short_gap = 200;    
	long[] pattern = {0, dot, short_gap, dot,  short_gap, dot,};
	boolean vibri = false;	
	boolean winken = false;	
	int whichEffect;
	boolean dialogExists = false;
    Intent nextActivity;

	@SuppressLint({ "InlinedApi", "NewApi" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.winkerstart);
		
		//Verhindert Abdunkeln des Bildschirms
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
	
  		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
  		
  		
  		// checken ob Handy über einen Accelerometer verfügt
  		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

  			          mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);					
  			            
  			        } else { 			        	
  						setContentView(R.layout.winker);
  			        	Toast.makeText(this, "Sorry, dein Handy hat keinen Beschleunigungssensor !", Toast.LENGTH_SHORT).show();  
  			        	
  			     	 Intent intent = new Intent(this, start.class);
  		        	 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   // beendet alle Activities außer die Startactivity
  		        	 this.startActivity(intent);
  		        	 ((Activity) this).finish();
			        	
  			        }


		// für die Registrierung der Nicht-Bewegung benötigt (Effektbeendigung)
		
		for(int j=0; j<1000000; j++){
			counters[j] = 0;
		}
		
		new Timer().scheduleAtFixedRate(new TimerTask(){
	        @Override
	        public void run(){             	
	        	endListener();               
	        }         
	    }, 10000, 2000);
		
		context = this;
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {	
		 Sensor sensor = event.sensor;

		// Waver Sensorlistener
		 
		 if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){			
				waverHandler(event);					
			} 
		 
		}
		 
	// Lebenszyklus Methoden
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	protected void onPause() {
	super.onPause();
	wakeLock.release();
	mSensorManager.unregisterListener(this);

	}

	@Override
	protected void onResume() {
	super.onResume();
	wakeLock.acquire();
	mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}	
		
	// Methode zur Waver-Behandlung:
	
	public void waverHandler(SensorEvent event){

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	
		float position = event.values[0];
		
		if(position>8 && counter==0){				
			counter ++;	
			winken=true;
			setContentView(R.layout.winker);
		}
		
		if(position>8){				
			counter ++;		
		}
			 	
		if(winken==true){
			
		// Bildschirmhelligkeit maximal setzen
		WindowManager.LayoutParams layout = getWindow().getAttributes();
		layout.screenBrightness = 1F;
		getWindow().setAttributes(layout);
			
        int color = (int) (	5*position + 250);		 
		float[] hsv = {color, 1.f, 1.f};		
		int col = Color.HSVToColor(hsv);		
		setActivityBackgroundColor(col);	
		}
	}
	
	public void endListener(){		 
		 counters[i] = counter;        
		   	if(i>4 && counters[i] == counters[i-3] && counters[i]!=0 && vibri == false){	 			
		   		v.vibrate(pattern, -1);
		   		vibri = true;
		   		
		   	 this.runOnUiThread(new Runnable() {
		   		 public void run()
		   		    {
		   			    dialogExists = true;
		   			 	counter = 0;
				   		customizeDialog = new CustomizeDialog(context, 2);  
		                customizeDialog.setTitle("Exit Winker?");  
//		                customizeDialog.setMessage("Exit Winker?"); 
		                customizeDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		                customizeDialog.show();   
		   		    }
		   		 });            
	 		}		 
         i++;       
	}
	
	// Methode für das Ändern der Hintergrundfarbe
	
	public void setActivityBackgroundColor(int color) {				
		View myView = (View)findViewById(R.id.layout_id);
		GradientDrawable Shape = (GradientDrawable)myView.getBackground();
		Shape.setColor(color);
	}
	
	// Methode für den Hardware-Backbutton
	
	public void onBackPressed(){
		Toast.makeText(context, "Lange gedrückt halten!",Toast.LENGTH_SHORT).show();   
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// When the search button is long pressed, quit
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		 Intent intent = new Intent(this, start.class);	
       	 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   // beendet alle Activities außer die Startactivity
       	 startActivity(intent);
					
			dialogExists=true;
			counter = 0;
			
			finish();
			return true;
		}
		return false;
	}
}



	




 	