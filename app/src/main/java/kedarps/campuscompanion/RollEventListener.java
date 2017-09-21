package kedarps.campuscompanion;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class RollEventListener implements SensorEventListener, OnClickListener {
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private Ringtone ringAlert;
	private Vibrator vibrateAlert;
	public Boolean isEnabled;
	
	private Activity mActivity;	
	private LocationBroadcaster mLocationBroadcaster;
	private ImageView mRotate;
	
	//Angle integration variables
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp = 0;
    private LinkedList<Double> dts;
    private LinkedList<Double> omegas;
    private double theta = 0.0;
    private double time = 0.0;
    private double timeWindow;
    
    private static final String TAG = "RollOverRxCx";
    
    public RollEventListener(Activity activity, LocationBroadcaster locationBroadcaster, ImageView rotate) {
    	this.mActivity = activity;
    	this.mLocationBroadcaster = locationBroadcaster;
    	this.mRotate = rotate;
    	
    	mRotate.setOnClickListener(this);
    	
    	mActivity.getApplicationContext();
		mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mSensor, Sensor.TYPE_GYROSCOPE);
        
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	this.ringAlert = RingtoneManager.getRingtone(mActivity.getApplicationContext(), notification);
    	this.vibrateAlert = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
    	
    	dts = new LinkedList<Double>();
    	omegas = new LinkedList<Double>();
        timeWindow = 3.0;
        isEnabled = Boolean.FALSE;
    }
    
    public void onSensorChanged(SensorEvent event) {
	  // This timestep's delta rotation to be multiplied by the current rotation
	  // after computing it from the gyro sample data.
	  if (timestamp != 0) {
	    float dT = (event.timestamp - timestamp) * NS2S;
	    float omega = event.values[1];
	    theta = theta + omega*dT;
	    time = time + dT;
	    dts.addLast(Double.valueOf(dT));
	    omegas.addLast(Double.valueOf(omega));
	    if (Math.abs(theta) > 3*Math.PI) {
	    	synchronized(isEnabled) {
	    		if (isEnabled.booleanValue()) {
			    	Log.i(TAG, "Rolled Over");
			    	//Reset theta and time to properly detect the next rotation
			    	resetAngle();
			    	ringAlert.play();
			    	vibrateAlert.vibrate(500);
			    	
			    	if(!mLocationBroadcaster.isStreaming)
			    	{
			    		mLocationBroadcaster.StartBroadcast();
			    		Disable();
			    	}
			    }
	    	}
	    }
	    if (time > timeWindow) {
	    	//Only integrate within a two second window
	    	double dT0 = dts.remove().doubleValue();
	    	double omega0 = omegas.remove().doubleValue();
	    	time = time - dT0;
	    	theta = theta - dT0*omega0;
	    }
	  }
	  timestamp = event.timestamp;
    }
    
    public void resetAngle() {
    	theta = 0;
    	time = 0;
    	dts.clear();
    	omegas.clear();
    }
    
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	
	protected void onResume() {
        mSensorManager.registerListener(this, mSensor, Sensor.TYPE_GYROSCOPE);
    }

    protected void onPause() {
        mSensorManager.unregisterListener(this);
    }
    
	 public void Disable() {
		 synchronized(isEnabled) {
			 isEnabled = Boolean.FALSE;
			 mRotate.setBackgroundResource(R.mipmap.border_white);
			 Toast.makeText(mActivity.getApplicationContext(), "Rotation Mode Deactivated", Toast.LENGTH_SHORT).show();
			 Log.i(TAG, "Rotation Mode deactivated");
		 }
	}
	 
	 public void Enable() {
		 synchronized(isEnabled) {
			 if(!mLocationBroadcaster.isStreaming)
			 {
				 isEnabled = Boolean.TRUE;
				 resetAngle();
				 mRotate.setBackgroundResource(R.mipmap.border_green);
				 mRotate.invalidate();
				 Toast.makeText(mActivity.getApplicationContext(), "Rotation Mode Activated", Toast.LENGTH_SHORT).show();
				 Log.i(TAG, "Rotation Mode Activated");
			 }
		 }
	 }
	 
	 public void onClick (View v)
	 {
		if(isEnabled)
			Disable();
		else
			Enable();
	}
}
