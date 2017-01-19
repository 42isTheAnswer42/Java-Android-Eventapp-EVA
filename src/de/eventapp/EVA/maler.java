package de.eventapp.EVA;

import java.util.Timer;
import java.util.TimerTask;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

@SuppressLint({ "NewApi", "InlinedApi" })
public class maler extends Activity implements SensorEventListener {

	Context context;
	CustomizeDialog customizeDialog = null;
	ValueAnimator[] animations = new ValueAnimator[3];


	ValueAnimator rainbowAnim1 = ValueAnimator.ofObject(new ArgbEvaluator(), 0);
	ValueAnimator rainbowAnim2 = ValueAnimator.ofObject(new ArgbEvaluator(), 0);
	ValueAnimator rainbowAnim3 = ValueAnimator.ofObject(new ArgbEvaluator(), 0);

	private WakeLock wakeLock;
	private SensorManager mSensorManager;
	private Vibrator v;
	int dot = 200;
	int short_gap = 200;
	long[] pattern = { 0, dot, short_gap };
	boolean dialogExists = false;
	double helligkeitsGrenze = 5000;
	boolean blackScreen = false;
	int counter;
	double counters[] = new double[50];
	int value = 0;
	boolean calib = false;
	long startTime = System.currentTimeMillis();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT < 11) {
			finish();
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.malerstart);

		// Orientierung des Bildschirms: Portrait
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Verhindert Abdunkeln des Bildschirms
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"DoNotDimScreen");

		// Wann soll Bildschirm schwarz gesetzt werden ?

		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {

			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
					SensorManager.SENSOR_DELAY_NORMAL);

//		} else {
//			setContentView(R.layout.maler);
//			Toast.makeText(
//					this,
//					"Sorry, dein Smartphone besitzt keinen Helligkeits-Sensor !",
//					Toast.LENGTH_SHORT).show();
//
//			Intent intent = new Intent(this, start.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // beendet alle
//																// Activities
//																// außer die
//																// Startactivity
//			this.startActivity(intent);
//			((Activity) this).finish();

		}

		// =========================================
		// Die Farbanimationen
		// =========================================

		counter = 0;

		rainbowAnim1.setIntValues(0xFF2A0A29, 0xFF610B5E, 0xFFB404AE,
				0xFFDF01D7, 0xFFFF00FF, 0xFFFF0040, 0xFFDF013A, 0xFFB40431,
				0xFF190718);
		rainbowAnim2.setIntValues(0xFF071918, 0xFF0B615E, 0xFF00FFFF,
				0xFF00FF00, 0xFF0B610B, 0xFF071907);
		rainbowAnim3.setIntValues(0xFF181907, 0xFF868A08, 0xFFFFFF00,
				0xFFFFBF00, 0xFFFF8000, 0xFF8A4B08, 0xFF190B07);

		animations[0] = rainbowAnim1; // pink zu lila
		animations[1] = rainbowAnim2; // türkis zu grün
		animations[2] = rainbowAnim3; // gelb zu orange

		for (int k = 0; k < 3; k++) {
			animations[k].setDuration(25000);
			animations[k].setStartDelay(1000);

			animations[k].addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animator) {
					setActivityBackgroundColor((Integer) animator
							.getAnimatedValue());
				}
			});
		}

		for (int j = 0; j < 50; j++) {
			counters[j] = 0;
		}

		context = this;
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		// TODO Auto-generated method stub
		Sensor sensor = event.sensor;
		while (System.currentTimeMillis() - startTime < 5002 && System.currentTimeMillis() - startTime > 1000) {
			if (System.currentTimeMillis() - startTime > 5000) {
				setContentView(R.layout.maler);
				setActivityBackgroundColor(0xFF000000);
				blackScreen = true;
			}
		}
		if (sensor.getType() == Sensor.TYPE_LIGHT) {

			System.out.println("System.currentTimeMillis() - startTime "
					+ (startTime));

			new Timer().scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					getLights(event);
				}
			}, 0, 50);

			// Wenn Grenze erreicht => Animation
			if (event.values[0] > (helligkeitsGrenze + 200)
					&& !animations[0].isStarted() && !animations[1].isStarted()
					&& !animations[2].isStarted() && blackScreen) {

				// Bildschirmhelligkeit maximal setzen
				WindowManager.LayoutParams layout = getWindow().getAttributes();
				layout.screenBrightness = 1F;
				getWindow().setAttributes(layout);

				setContentView(R.layout.maler);
				animations[counter].start();
				counter++;

				if (counter == 3) {
					counter = 0;
				}
				v.vibrate(pattern, -1);

			}
		}
	}

	// Lebenszyklus Methoden (hier nicht reinpfuschen) !!!

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
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	// Methode für das Ändern der Hintergrundfarbe

	public void setActivityBackgroundColor(int color) {
		View myView = (View) findViewById(R.id.layout_id);
		GradientDrawable Shape = (GradientDrawable) myView.getBackground();
		Shape.setColor(color);
	}

	// Beenden des Maler-Effekts

	public void onBackPressed() {
		Toast.makeText(context, "Lange gedrückt halten!",Toast.LENGTH_SHORT).show();
	}

	public void getLights(SensorEvent event) {

		if (calib == false) {
			counters[value] = event.values[0];
			value++;
			System.out.println("Aufruf:" + value);

			if (value == 50) {
				value = 0;
				calib = true;
				double sum = 0;
				for (int k = 0; k < 50; k++) {
					sum = sum + counters[k];
				}
				helligkeitsGrenze = sum / 50;
				System.out.println("Summe:" + sum);
				System.out.println("HelligkeitsGrenze:" + helligkeitsGrenze);
				// v.vibrate(pattern, -1);

			}
		}
	}
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// When the search button is long pressed, quit
		if (keyCode == KeyEvent.KEYCODE_BACK) {			
			dialogExists = true;
			 Intent intent = new Intent(this, start.class);	
	       	 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   // beendet alle Activities außer die Startactivity
	       	 startActivity(intent);
			 finish();
			return true;
		}
		return false;
	}

}
