package kedarps.campuscompanion;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

public class HeadsetConnectionReceiver extends BroadcastReceiver implements OnClickListener {
	
	public Boolean isEnabled;
	private Activity mActivity;
	private LocationBroadcaster mLocationBroadcaster;
	private Switch mHeadsetSwitch;
	private ImageView mHeadset;
	
	private static final String TAG = "HeadsetRxCx";
	
	public HeadsetConnectionReceiver(Activity activity, LocationBroadcaster locationBroadcaster, Switch headSetSwitch) {
		this.mActivity = activity;
		this.mLocationBroadcaster = locationBroadcaster;
		this.mHeadsetSwitch = headSetSwitch;
		mHeadsetSwitch.setOnClickListener(this);
		
		this.isEnabled = Boolean.FALSE;
		
    	
    	mHeadset = (ImageView) mActivity.findViewById(R.id.headset);
    }
	
	 public void onReceive(Context context, Intent intent) {
	     if (intent.hasExtra("state")){
	         if (isEnabled && intent.getIntExtra("state", 0) == 0){
	             Log.i(TAG, "Headset disconnected");
			     
			     if(!mLocationBroadcaster.isStreaming)
			     {
//			    	 mLocationBroadcaster.StartBroadcast();
			    	 mLocationBroadcaster.PrepToStartBroadcast();
			    	 Disable();
			     }
			    	 
			 }
	         else if (isEnabled && intent.getIntExtra("state", 0) == 1){
	        	 Log.i(TAG, "Headset connected");
	        	 
//	        	 ringAlert.play();
//	        	 vibrateAlert.vibrate(100);
	         }
	     }
	 }
	 
	 public void Disable() {
		 synchronized(isEnabled) {
			 isEnabled = Boolean.FALSE;
//			 mHeadset.setBackgroundResource(R.mipmap.border_white);
			 Toast.makeText(mActivity.getApplicationContext(), "Headset Mode Deactivated", Toast.LENGTH_SHORT).show();
			 Log.i(TAG, "Headset Mode deactivated");
		 }
	 }
	 
	 public void Enable() {
		 synchronized(isEnabled) {
			 if(!mLocationBroadcaster.isStreaming)
			 {
				 isEnabled = Boolean.TRUE;
//				 mHeadset.setBackgroundResource(R.mipmap.border_green);
//				 mHeadset.invalidate();
				 Toast.makeText(mActivity.getApplicationContext(), "Headset Mode Activated", Toast.LENGTH_SHORT).show();
				 Log.i(TAG, "Headset Mode Activated");
			 }
		 }
	 }
	 
	 public void onClick (View v)
	 {
		if(isEnabled)
		{
			mHeadset.setImageResource(R.mipmap.ic_headset_inactive);
			Disable();
		}
		else
		{
			mHeadset.setImageResource(R.mipmap.ic_headset_active);
			Enable();
		}
	}
}
