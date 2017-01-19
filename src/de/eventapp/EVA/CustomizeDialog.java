package de.eventapp.EVA;

import de.eventapp.EVA.R;
import android.app.Activity;
import android.app.Dialog;  
import android.content.Context;  
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;  
import android.view.View;  
import android.view.View.OnClickListener;  
import android.view.Window;  
import android.widget.Button;  
import android.widget.TextView;  
	
	 public class CustomizeDialog extends Dialog implements OnClickListener {  
	     Button okButton;  
	     Button noButton;
	     Intent nextActivity;
	     Context mContext;  
	     TextView mTitle = null;  
	     TextView mMessage = null;  
	     View v = null;  
	     int whichEffect;
	    
	     public CustomizeDialog(Context context, int effect) {  
	         super(context);  
	         mContext = context;  
	         whichEffect = effect;
	         requestWindowFeature(Window.FEATURE_NO_TITLE);  
	         setContentView(R.layout.dialog);  
	         v = getWindow().getDecorView();  
	         v.setBackgroundResource(android.R.color.transparent);  
	         mTitle = (TextView) findViewById(R.id.dialogTitle);  
	         mMessage = (TextView) findViewById(R.id.dialogMessage);  
	         okButton = (Button) findViewById(R.id.OkButton);  
	         okButton.setOnClickListener(this);  
	         noButton = (Button) findViewById(R.id.NoButton);  
	         noButton.setOnClickListener(this);  
	     }  
	     @Override  
	     public void onClick(View v) {  

	      // case-1: von Startseite aus beenden   
	         
	         if (v == okButton && whichEffect == 1){  	   
	        	 ((Activity) mContext).finish();
	     }
	         
	      // case-2:  Back-Button aus Effekt raus
	         
	         if (v == okButton && whichEffect == 2){  	     	         
	        	 Intent intent = new Intent(mContext, start.class);
	        	 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   // beendet alle Activities auﬂer die Startactivity
	        	 mContext.startActivity(intent);
	        	 ((Activity) mContext).finish();
	     }
	         
	         // case: NO
	         
	         if(v == noButton)
	        	 dismiss();
	     }  
	     
	     @Override  
	     public void setTitle(CharSequence title) {  
	         super.setTitle(title);  
	         mTitle.setText(title);  
	     }  
	     @Override  
	     public void setTitle(int titleId) {  
	         super.setTitle(titleId);  
	         mTitle.setText(mContext.getResources().getString(titleId));  
	     }  
 
	     public void setMessage(CharSequence message) {  
	         mMessage.setText(message);  
	         mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());  
	     }  
 
	     public void setMessage(int messageId) {  
	         mMessage.setText(mContext.getResources().getString(messageId));  
	         mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());  
	     }  
	 }  
