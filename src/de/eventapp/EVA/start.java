package de.eventapp.EVA;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class start extends Activity {

	Context context;
	CustomizeDialog customizeDialog = null;
	Button maler2, winker2, beatblitz2;
	ImageButton maler, winker, beatblitz, hilfe;
	MediaPlayer mp;
	int back;
	Intent start;
	Intent nextActivity;

	@SuppressLint({ "NewApi", "InlinedApi" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.start);

		// Orientierung des Bildschirms: Portrait
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		context = this;
		mp = MediaPlayer.create(context, R.raw.buttonsound);
		back = 1;

		addListenerOnButton();

		// Animation der Buttons

		final Animation animScale = AnimationUtils.loadAnimation(this,
				R.anim.anim_scale);
		final Animation animScale1 = AnimationUtils.loadAnimation(this,
				R.anim.anim_scale_1);
		final Animation animScale2 = AnimationUtils.loadAnimation(this,
				R.anim.anim_scale_2);
		maler.startAnimation(animScale1);
		winker.startAnimation(animScale);
		beatblitz.startAnimation(animScale2);
	}

	// ==============================================================================================================
	// Implemenierung der Buttons
	// ==============================================================================================================

	public void addListenerOnButton() {

		maler = (ImageButton) findViewById(R.id.maler_bild);
		winker = (ImageButton) findViewById(R.id.winker_bild);
		beatblitz = (ImageButton) findViewById(R.id.beatblitz_bild);
		hilfe = (ImageButton) findViewById(R.id.hilfe);

		// Auswahl des Winker-Buttons:

		winker.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), start.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mp.start();
				nextActivity = new Intent(context, waver.class);
				context.startActivity(nextActivity);
			}
		});

		// Auswahl des Maler-Buttons

		maler.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (Build.VERSION.SDK_INT < 11) {
					Toast.makeText(context, "Sorry, dein Handy unterstützt diesen Effekt nicht :(",Toast.LENGTH_SHORT).show();
				} else {
					Intent i = new Intent(getApplicationContext(), start.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mp.start();
					nextActivity = new Intent(context, maler.class);
					context.startActivity(nextActivity);
				}
			}
		});

		// Auswahl des Beatblitz-Buttons

		beatblitz.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mp.start();
				nextActivity = new Intent(context, BeatBlitz.class);
				context.startActivity(nextActivity);
			}
		});

		// Auswahl des Hilfe-Buttons

		hilfe.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				back = 2;
				mp.start();
				setContentView(R.layout.hilfe);

			}
		});
	}

	// ============================================================================================================
	// Beenden der App:
	// =============================================================================================================

	public void onBackPressed() {
		if (back == 1) {
			mp.start();
			customizeDialog = new CustomizeDialog(context, 1);
			customizeDialog.setTitle("EVA");
			customizeDialog.setMessage("Willst du EVA wirklich beenden?");
			customizeDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
			customizeDialog.show();
		} else {
			start = new Intent(this, start.class);
			start.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(start);

		}
	}

}
