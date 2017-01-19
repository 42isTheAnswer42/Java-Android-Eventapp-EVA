package de.eventapp.EVA;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.jtransforms.fft.DoubleFFT_1D;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class BeatBlitz extends Activity implements SurfaceHolder.Callback {
	int RGB_7_MinCounter = 0;
	int[] RGBColor = new int[20];
	private TextView tvfreq;

	int[] RGBColor_7Minutes_DifferentColors = new int[910];
	int colorCount = 0;
	int alphaValue = 255;
	int alphaCount = 0;
	int redV = 0;
	int greenV = 0;
	int blueV = 0;
	Random rnd = new Random();
	int color;
	public double Hit_Buffer;
	public double Hit_Long_Buffer;
	int longBufferCount = 0;
	int longBufferCount_twenty = 0;
	int longBufferCount_twenty_Hit = 0;
	int longBufferCount_twenty_HitLong = 0;
	long hitTimer = 0;
	boolean hit = false;
	double[] longBufferValues = new double[10];
	double[] longBufferValues_twenty = new double[20];
	double[] longBufferValues_twenty_Hit = new double[20];
	double[] longBufferValues_twenty_HitLong = new double[80];
	public double magnitude3;
	public double magnitude2;
	public double magnitude4;
	public int bufferArrayCount;
	boolean RMSMessureDone = false;
	boolean BPMMessureDone = false;
	double BPM = 0.0;
	double bpm2 = 128;
	double detectedRoomRMS = 4000;
	double detectedRoomRMS2 = 0.0;
	double[] roomRMSValues = new double[20];
	int rmsCounter = 0;
	int rmsCounter2 = 0;
	double magnitude_filtered_Signal2;
	double rms_Short_Buffer2;
	int blinkCounter = 0;
	int blinkCounter2 = 0;
	int[] rgb = new int[3];
	int bufferCount = 0;
	private int blockSize = 1024;
	int bufferWindowsOf1024Samples = 10;
	public double[] magnitudeLongBufferValues = new double[10];
	boolean noMoreStrobo = false;
	double magnitude_filtered_Signal;
	int cutFreqFilterLowPass = 400;
	public double rms_Short_Buffer;
	public double rms_Short_Buffer_M2;
	public double rms_Long_Buffer;
	public double rms_Long_Buffer_twenty;
	public double rms_Long_Buffer_twenty_Hit;
	public double rms_Long_Buffer_twenty_HitLong;
	public double rms_Long_Buffer_temp;
	public double rms_Long_Buffer_temp_twenty;
	public double rms_Long_Buffer_temp_twenty_Hit;
	public double rms_Long_Buffer_temp_twenty_HitLong;
	double[] rms_Long_Buffer_temp_array = new double[10];
	double initTime = System.currentTimeMillis();

	double deziAmplitude;
	double lastDecibelsAmplitude = 0;
	private static final int SAMPLING_RATE = 44100;
	public Camera camera;
	double amplitude_NonFilteredSignal;
	private TextView tvdb;
	private RecordingThread mRecordingThread;
	private int mBufferSize;
	private short[] mAudioBuffer;
	private short[] mAudioBuffer_filtered;
	double[] magnitude;
	double[] magnitudeValSquared = new double[blockSize];
	double[] magnitudeValSquared2 = new double[3];
	int iCount = 0;
	long[] timeMessured = new long[20];
	long sumTime = 0;
	long timeBetweenHits = 0;
	long nextHitInTime = 0;
	long lastHitinTime = 0;
	long systemTime = 0;
	long[] timeMessuredDifference = new long[16];
	long nextHit = 0;
	public double rms_Long_Buffer_a;
	// 4096 //1024
	double[] magnitudeValSquared_working = new double[blockSize];
	private int[] bufferDouble;
	CustomizeDialog customizeDialog;
	Context context;
	boolean mitBlitz;
	public static SurfaceView preview;
	public static SurfaceHolder mHolder;
	// private CameraPreview CamPreview;
	boolean dialogExists = false;
	boolean killMe = false;
	private CameraPreview mPreview;
	AudioRecord record;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		customizeDialog = null;
		context = this;
		super.onCreate(savedInstanceState);

		killMe = false;

		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FLASH) == false) {
			mitBlitz = false;
		} else {
			mitBlitz = true;
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.beatblitz);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Orientierung des Bildschirms: Portrait
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		tvdb = (TextView) findViewById(R.id.tv_decibels);
		tvfreq = (TextView) findViewById(R.id.tv_freq);

		// Compute the minimum required audio buffer size and allocate the
		// buffer.
		mBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE / 2,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

		mAudioBuffer = new short[1024];// [mBufferSize / 2];
		mAudioBuffer_filtered = new short[1024];// [mBufferSize / 2];
		bufferDouble = new int[(blockSize) * 2];

		SurfaceView preview = (SurfaceView) findViewById(R.id.PREVIEW);
		SurfaceHolder mHolder = preview.getHolder();
		mHolder.addCallback(this);
		camera = Camera.open();

		final Parameters c = camera.getParameters();
		c.setFlashMode(Parameters.FLASH_MODE_OFF);
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Bildschirmhelligkeit maximal setzen
		WindowManager.LayoutParams layout = getWindow().getAttributes();
		layout.screenBrightness = 1F;
		getWindow().setAttributes(layout);

		RGBColor[0] = Color.BLUE;
		RGBColor[1] = Color.CYAN;
		RGBColor[2] = Color.DKGRAY;
		RGBColor[3] = Color.GREEN;
		RGBColor[4] = Color.GRAY;
		RGBColor[5] = Color.MAGENTA;
		RGBColor[6] = Color.LTGRAY;
		RGBColor[7] = Color.RED;
		RGBColor[8] = Color.WHITE;
		RGBColor[9] = Color.YELLOW;
		RGBColor[10] = android.graphics.Color.rgb(120, 100, 122);
		RGBColor[11] = android.graphics.Color.rgb(233, 150, 122); // darksalmon
		RGBColor[12] = android.graphics.Color.rgb(255, 165, 0); // orange
		RGBColor[10] = android.graphics.Color.rgb(124, 252, 0); // LAWNgREEN
		RGBColor[11] = android.graphics.Color.rgb(64, 224, 208); // turquoise
		RGBColor[12] = android.graphics.Color.rgb(30, 144, 255); // dodgerblue
		RGBColor[13] = android.graphics.Color.rgb(138, 43, 226);// blueviolet
		RGBColor[14] = android.graphics.Color.rgb(186, 85, 211);// mediumorchid
		RGBColor[15] = android.graphics.Color.rgb(220, 20, 60); // crimson
		RGBColor[16] = android.graphics.Color.rgb(220, 220, 220); // gainsboro
		RGBColor[17] = android.graphics.Color.rgb(255, 127, 80); // coral
		RGBColor[18] = android.graphics.Color.rgb(255, 215, 0); // darkgoldenrose
		RGBColor[19] = android.graphics.Color.rgb(173, 255, 47); // greenyelllow
		int RGB_7_MinCounter = 0;
		int r = 15;
		int g = 100;
		int b = 255;

		for (int i = 0; i < RGBColor_7Minutes_DifferentColors.length; i++) {
			RGBColor_7Minutes_DifferentColors[i] = android.graphics.Color.rgb(
					r, g, b);
			r = r + 49;
			g = g - 27;
			b = b + 61;

			if (r > 255) {
				r = r - 255;
			}
			if (g < 0) {
				g = 0 + ((-1) * g) + 220;
			}
			if (b > 255) {
				b = b - 255;
			}

		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;
		mPreview = new CameraPreview(this); // ,camera, preview, null);
		killMe = false;
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		killMe = true;
		camera.stopPreview();
		camera.release();
		mPreview.getHolder().removeCallback(mPreview);
		mPreview.getHolder().removeCallback(this);
		if (mRecordingThread != null) {
			mRecordingThread.stopRunning();
			mRecordingThread = null;
		}

	}

	@Override
	public void onBackPressed() {
		Toast.makeText(context, "Lange gedrückt halten!", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		killMe = false;
		mRecordingThread = new RecordingThread();
		mRecordingThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();

		killMe = true;

		if (mRecordingThread != null) {
			mRecordingThread.stopRunning();
			mRecordingThread = null;
		}
		if (dialogExists == true) {
			customizeDialog.dismiss();
		}

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		killMe = true;
		if (mRecordingThread != null) {
			mRecordingThread.stopRunning();
			mRecordingThread = null;
		}

	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// When the search button is long pressed, quit
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return false;
	}

	private class RecordingThread extends Thread {

		private boolean mShallContinue = true;

		@Override
		public void run() {
			if (killMe)
				return;
			AudioRecord record = new AudioRecord(
					MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE / 2,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, mBufferSize);

			// short[] buffer = new short[blockSize];
			double[] audioDataDoubles = new double[(blockSize * 2)];
			double[] re = new double[blockSize];
			double[] im = new double[blockSize];
			double[] magnitude = new double[blockSize];

			// start collecting data
			record.startRecording();

			DoubleFFT_1D fft = new DoubleFFT_1D(blockSize);
			synchronized (this) {
				while (shallContinue()) {
					if (killMe) {
						if (mRecordingThread != null) {
							mRecordingThread.stopRunning();
							mRecordingThread = null;
						}
						record.release();
						return;
					}
					record.read(mAudioBuffer, 0, 1024);

					// LowPass Filtering - y[i] := y[i-1] + ALPHA * (x[i] -
					// y[i-1])
					// alpha between 0 ( 100% Filter) and 1 (no Filter)
					// 0.01 doesnt work
					// 0.05 seems to be fine
					float alpha = 0.2f;
					if (mAudioBuffer != null) {
						for (int i = 1; i < 1024; i++) {
							mAudioBuffer_filtered[i] = (short) (mAudioBuffer_filtered[i - 1] + alpha
									* (mAudioBuffer[i] - mAudioBuffer_filtered[i - 1]));

						}
					}

					// Windowing Henn-Window
					for (int i = 0; i < blockSize; i++) {
						double winValue = (1 - Math.cos(2 * Math.PI * i
								/ blockSize)) / 2.0;
						bufferDouble[i] = (int) (mAudioBuffer_filtered[i] * winValue);
					}

					// Prepering Data for FFT
					for (int i = 0; i < blockSize; i++) {
						audioDataDoubles[2 * i] = (double) bufferDouble[i]; // signed
																			// 16
																			// bit
						audioDataDoubles[(2 * i) + 1] = 0.0;
					}

					// FFT
					fft.complexForward(audioDataDoubles);

					for (int i = 0; i < blockSize; i++) {

						// real is stored in first part of array
						re[i] = audioDataDoubles[i * 2];
						// imaginary is stored in the sequential part
						im[i] = audioDataDoubles[(i * 2) + 1];

						// magnitude is calculated by the square root of
						// (imaginary^2 + real^2)
						magnitude[i] = Math.sqrt((re[i] * re[i])
								+ (im[i] * im[i]));
					}
					magnitude[0] = 0.0;

					magnitude2 = magnitude[2];
					magnitude3 = magnitude[3];
					magnitude4 = magnitude[4];

					updateShortBuffer();

					updateLongBuffer();

					// System.out.println(" 2TIME: "
					// + (System.currentTimeMillis() - initTime)
					// + " 4RMS2: " + (int) magnitude2 + " 6RMS3: "
					// + (int) rms_Short_Buffer + " 8M4: "
					// + (int) magnitude[4] + " 10M5: "
					// + (int) magnitude[5] + " 12M6: "
					// + (int) magnitude[6] + " 14M7: "
					// + (int) magnitude[7] + " 16M10: "
					// + (int) magnitude[10] + " 18M20: "
					// + (int) magnitude[20] + " 20M40: "
					// + (int) magnitude[40] + " 22LONG20: "
					// + (int) rms_Long_Buffer_twenty + " 24LONG10: "
					// + (int) rms_Long_Buffer + " 26HIT: "
					// + (int) Hit_Buffer + " 28HITBuffer: "
					// + (int) rms_Long_Buffer_twenty_Hit
					// + " 30HITBufferLONG: "
					// + (int) rms_Long_Buffer_twenty_HitLong);
				}
				record.stop(); // stop recording please.
				record.release(); // Destroy the recording.
			}
		}

		/**
		 * true if the thread should continue running or false if it should stop
		 */
		private synchronized boolean shallContinue() {
			return mShallContinue;
		}

		/**
		 * Notifies the thread that it should stop running at the next
		 * opportunity.
		 */
		public synchronized void stopRunning() {
			mShallContinue = false;
		}

	}

	private void updateShortBuffer() {

		rms_Short_Buffer = magnitude3;

		// //Gewichtung der versch. Buffers.
		// Hauptbuffer M3 - Nebenbuffer M2
		// Wenn Hauptbuffer kleiner 20000, dann wird M2 genommen und gewichtet
		// Hit_Buffer = rms_Short_Buffer;
		if (rms_Short_Buffer > rms_Long_Buffer)
			Hit_Buffer = rms_Short_Buffer;

		if (rms_Short_Buffer > 1.3 * rms_Long_Buffer)
			Hit_Buffer = rms_Short_Buffer * 4;

		if (rms_Short_Buffer < rms_Long_Buffer)
			Hit_Buffer = rms_Short_Buffer;

		longBufferValues[longBufferCount] = rms_Short_Buffer;
		longBufferValues_twenty[longBufferCount_twenty] = rms_Short_Buffer;
		longBufferValues_twenty_Hit[longBufferCount_twenty_Hit] = Hit_Buffer;

		longBufferCount++;
		longBufferCount_twenty++;
		longBufferCount_twenty_Hit++;

		if (longBufferCount == 10) {
			longBufferCount = 0;
		}

		if (longBufferCount_twenty == 20) {
			longBufferCount_twenty = 0;
		}

		if (longBufferCount_twenty_Hit == 20) {
			longBufferCount_twenty_Hit = 0;
		}

	}

	public void updateLongBuffer() {

		// ///////////LONGBUFFER mit 10 letzten Werten
		for (int i = 0; i < 10; i++) {
			rms_Long_Buffer_temp = rms_Long_Buffer_temp + longBufferValues[i];

		}
		rms_Long_Buffer_temp = rms_Long_Buffer_temp / (longBufferValues.length);
		rms_Long_Buffer = rms_Long_Buffer_temp;
		rms_Long_Buffer_temp = 0;

		// ////////////LONGBUFFER mit 20 letzten Werten
		for (int j = 0; j < 20; j++) {
			rms_Long_Buffer_temp_twenty = rms_Long_Buffer_temp_twenty
					+ longBufferValues_twenty[j];

		}
		rms_Long_Buffer_temp_twenty = rms_Long_Buffer_temp_twenty
				/ (longBufferValues_twenty.length);
		rms_Long_Buffer_twenty = rms_Long_Buffer_temp_twenty;
		rms_Long_Buffer_temp_twenty = 0;

		// //////////LONGBUFFER mit HitBuffer
		for (int j = 0; j < 20; j++) {
			rms_Long_Buffer_temp_twenty_Hit = rms_Long_Buffer_temp_twenty_Hit
					+ longBufferValues_twenty_Hit[j];

		}
		rms_Long_Buffer_temp_twenty_Hit = rms_Long_Buffer_temp_twenty_Hit
				/ (longBufferValues_twenty_Hit.length);
		rms_Long_Buffer_twenty_Hit = rms_Long_Buffer_temp_twenty_Hit;
		rms_Long_Buffer_temp_twenty_Hit = 0;

		// //////////LONGBUFFER mit 80 Werten HitBuffer

		longBufferValues_twenty_HitLong[longBufferCount_twenty_HitLong] = rms_Long_Buffer_twenty_Hit;
		longBufferCount_twenty_HitLong++;
		if (longBufferCount_twenty_HitLong == 80) {
			longBufferCount_twenty_HitLong = 0;
		}
		for (int j = 0; j < 80; j++) {
			rms_Long_Buffer_temp_twenty_HitLong = rms_Long_Buffer_temp_twenty_HitLong
					+ longBufferValues_twenty_HitLong[j];
		}
		rms_Long_Buffer_temp_twenty_HitLong = rms_Long_Buffer_temp_twenty_HitLong
				/ (longBufferValues_twenty_HitLong.length);
		rms_Long_Buffer_twenty_HitLong = rms_Long_Buffer_temp_twenty_HitLong;
		rms_Long_Buffer_temp_twenty_HitLong = 0;
		if (killMe)
			return;
		final Parameters c = camera.getParameters();

		synchronized (this) {
			tvdb.post(new Runnable() {

				public void camOn() {
					if (camera != null) {

						if (Build.VERSION.SDK_INT > 11) {
							try {
								camera.setPreviewTexture(new SurfaceTexture(0));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						if (c != null) {
							List<String> supportedFlashModes = c
									.getSupportedFlashModes();

							if (supportedFlashModes != null) {

								if (supportedFlashModes
										.contains(Parameters.FLASH_MODE_TORCH)) {
									c.setFlashMode(Parameters.FLASH_MODE_TORCH);
								} else if (supportedFlashModes
										.contains(Parameters.FLASH_MODE_ON)) {
									c.setFlashMode(Parameters.FLASH_MODE_ON);
								} else
									camera = null;
							}
						}

						// c.setFlashMode(Parameters.FLASH_MODE_TORCH);
						camera.setParameters(c);
						// camera.startPreview();

					}
				}

				// public void camOn() {
				// try {
				//
				// // try to open the camera to turn on the torch
				// camera.startPreview();
				// Camera.Parameters param = camera.getParameters();
				//
				// param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				// camera.setParameters(param);
				// // needed for some devices
				// Log.v("BSW torch", "Torch ON");
				// } catch (Exception e) {
				// // if open camera fails, try to release camera
				// Log.w("BSW torch",
				// "Camera is being used trying to turn Torch OFF");
				//
				// }
				//
				// // c.setFlashMode(Parameters.FLASH_MODE_TORCH);
				// // camera.setParameters(c);
				// // camera.startPreview();
				//
				// }

				public void camOff() {
					if (camera != null) {

						c.setFlashMode(Parameters.FLASH_MODE_OFF);
						camera.setParameters(c);
						// camera.startPreview();
					}
				}

				public void run() {
					// 1.3 1 1.8 = faktoren des longbuffers
					if (killMe)
						return;

					double Hit_LongBuffer = (1.3 * rms_Long_Buffer_twenty_Hit
							+ 1 * rms_Long_Buffer_temp_twenty + 1.8 * rms_Long_Buffer_twenty_HitLong);

					if (((Hit_Buffer) > (Hit_LongBuffer) * 0.9)
							&& ((Hit_Buffer) > 100000)
							&& (System.currentTimeMillis() - hitTimer > 150)) {

						if (hit == false) {
							System.out.println("HIT");
							if (mitBlitz == true && camera != null)
								camOn();

							hitTimer = System.currentTimeMillis();
							hit = true;

							color = Color.argb(alphaValue, redV, greenV, blueV);

							tvdb.setBackgroundColor(RGBColor_7Minutes_DifferentColors[RGB_7_MinCounter]);
							tvfreq.setBackgroundColor(RGBColor_7Minutes_DifferentColors[RGB_7_MinCounter]);
							setActivityBackgroundColor(RGBColor_7Minutes_DifferentColors[RGB_7_MinCounter]);

							alphaCount++;
							colorCount++;
							RGB_7_MinCounter++;

							if (RGB_7_MinCounter == 910) {
								RGB_7_MinCounter = 0;
							}

							if (colorCount == 20) {

								colorCount = 0;

							}

						}

					} else {

						setActivityBackgroundColor(Color.BLACK);

						tvdb.setBackgroundColor(Color.BLACK);
						tvfreq.setBackgroundColor(Color.BLACK);
						

						hit = false;
						if (mitBlitz == true && camera != null)
							camOff();
					}

				}

			});
		}

	}

	// Methode für das Ändern der Hintergrundfarbe

	public void setActivityBackgroundColor(int color) {
		View myView = (View) findViewById(R.id.layout_id);
		GradientDrawable Shape = (GradientDrawable) myView.getBackground();
		Shape.setColor(color);
	}

	// Methode die checkt ob es einen Camera-Blitz gibt:

	public boolean hasFlash() {

		Camera.Parameters parameters = camera.getParameters();

		if (parameters.getFlashMode() == null) {
			return false;
		}

		System.out.println("KAMERA = YOOOOOOOOO");
		return true;
	}

	// ===============================================================================================

	public class CameraPreview extends SurfaceView implements
			SurfaceHolder.Callback {

		public CameraPreview(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		public void surfaceCreated(SurfaceHolder holder) {
			mHolder = holder;
			try {
				camera.setPreviewDisplay(mHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// camera.stopPreview();
			mPreview.getHolder().removeCallback(mPreview);
			mHolder = null;

		}
	}

}
