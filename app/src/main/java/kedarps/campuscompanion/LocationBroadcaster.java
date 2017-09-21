package kedarps.campuscompanion;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LocationBroadcaster
{
	private Ringtone mRingAlert;
	private Vibrator mVibrateAlert;
	private static Timer mBroadcastTimer;
	private LocationFinder mLocationFinder;
	private Activity mActivity;
	private static int GRACE_PERIOD = 10000;
	private static int PROGRESS_INCR = Math.round(((float) 360 / (GRACE_PERIOD / 1000))); 
	private static int GPS_SEND_INTERVAL = 5000;
	public final static int CXN_TIME_OUT = 5000;
	private static String TAG = "LocationBroadcaster";
	
	private Button mSosGps = null;
	
	private ImageView mDisarm = null;
	private TextView mStatus = null;
	
	private ImageView mDisable = null;
	private ImageView mSnooze = null;
	private TextView mTimeDisp = null;
	
	public Boolean isStreaming;
	public Boolean isGraceTimerRunning;
	
	private ProgressWheel mProgressWheel = null;
	private CountDownTimer mGraceTimer = null;
	private int progress = 0;
    
	private String DISARM_CODE_ENTERED = "";
	private static String USER_DISARM_CODE;
//	private static String DISARM_CODE = "1234";
	
	public LocationBroadcaster(Activity activity, Button sosGps)
	{
		this.mActivity = activity;
		this.mSosGps = sosGps;
		
		isStreaming = Boolean.FALSE;
		isGraceTimerRunning = Boolean.FALSE;
		
		//Get Disarm Code from Shared Prefs
		final SharedPreferences prefs = this.mActivity.getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
		USER_DISARM_CODE = prefs.getString("pref_disarm_code", "");
		
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	this.mRingAlert = RingtoneManager.getRingtone(mActivity.getApplicationContext(), notification);
    	this.mVibrateAlert = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
    	
		Log.d(TAG, "Initialised from Emergency");
	}
	
	public LocationBroadcaster(Activity activity, ImageView disarm, TextView status)
	{
		this.mActivity = activity;
		this.mDisarm = disarm;
		this.mStatus = status;
		
		isStreaming = Boolean.FALSE;
		isGraceTimerRunning = Boolean.FALSE;
				
		mDisarm.setVisibility(View.INVISIBLE);
		mStatus.setText("Connect Inactive");
    	mStatus.setTextColor(0xff99cc00);
    	
		//Get Disarm Code from Shared Prefs
		final SharedPreferences prefs = this.mActivity.getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
		USER_DISARM_CODE = prefs.getString("pref_disarm_code", "");
    	
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	this.mRingAlert = RingtoneManager.getRingtone(mActivity.getApplicationContext(), notification);
    	this.mVibrateAlert = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
		
    	Log.d(TAG, "Initialised Stand-Alone");
	}
	
	public LocationBroadcaster(Activity activity, ImageView disarm, TextView status, ImageView disable, ImageView snooze, TextView timeDisp)
	{
		this.mActivity = activity;
		this.mDisarm = disarm;
		this.mStatus = status;
		this.mDisable = disable;
		this.mSnooze = snooze;
		this.mTimeDisp = timeDisp;
		
		isStreaming = Boolean.FALSE;
		isGraceTimerRunning = Boolean.FALSE;
				
		mDisarm.setVisibility(View.INVISIBLE);
		
		mStatus.setText("Connect Inactive");
    	mStatus.setTextColor(0xff99cc00);
    	
		//Get Disarm Code from Shared Prefs
		final SharedPreferences prefs = this.mActivity.getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
		USER_DISARM_CODE = prefs.getString("pref_disarm_code", "");
    	
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	this.mRingAlert = RingtoneManager.getRingtone(mActivity.getApplicationContext(), notification);
    	this.mVibrateAlert = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
		
    	Log.d(TAG, "Initialised from Checkpoint");
	}
	
	public void PrepToStartBroadcast()
	{
		mRingAlert.play();
		mVibrateAlert.vibrate(250);
		
		final Dialog mDialog = new Dialog(mActivity);
		mDialog.setContentView(R.layout.disarm_dialog_w_countdown);
		mDialog.setTitle("Location Broadcast starts in...");
		mDialog.setCancelable(false);
		
		final EditText disarmEntry = (EditText) mDialog.findViewById(R.id.disarmEntry);
		disarmEntry.setHint("Disarm code to stop");
		disarmEntry.requestFocus();
	    mDialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	    disarmEntry.setRawInputType(Configuration.KEYBOARD_12KEY);
		
	    mProgressWheel = (ProgressWheel) mDialog.findViewById(R.id.progressWheel);
	    mProgressWheel.setRimColor(Color.GREEN);
	    
	    mGraceTimer = new CountDownTimer(GRACE_PERIOD+1000,1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				progress += PROGRESS_INCR;
				mProgressWheel.setProgress(progress);
				mProgressWheel.setText(Integer.toString((int) millisUntilFinished / 1000)+" s");
			}
			
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				progress = 0;
				mDialog.dismiss();
				StartBroadcast();
			}
		}.start();
	    
		synchronized (isGraceTimerRunning) {
			isGraceTimerRunning = Boolean.TRUE;
		}
		
	    final Button dialogOK = (Button) mDialog.findViewById(R.id.OK_disarm);
		dialogOK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DISARM_CODE_ENTERED = disarmEntry.getText().toString().trim();
				
				if(DISARM_CODE_ENTERED.equals(USER_DISARM_CODE))
				{
					if(isGraceTimerRunning)
					{
						progress = 0;
						mGraceTimer.cancel();
						synchronized (isGraceTimerRunning) {
							isGraceTimerRunning = Boolean.FALSE;
						}
					}
					mDialog.dismiss();
				}
				else
					Toast.makeText(mActivity.getApplicationContext(),
							DISARM_CODE_ENTERED+" is the wrong code", Toast.LENGTH_SHORT)
							.show();
			}
		});
		
		mDialog.show();
	}
    
	public void StartBroadcast()
	{
		Enable();
		
		mRingAlert.play();
		mVibrateAlert.vibrate(500);
		
		mLocationFinder = new LocationFinder(mActivity);
		mBroadcastTimer = new Timer();
		mBroadcastTimer.schedule(new TimerTask() {
	        @Override
	        public void run() {
	            sendDataToServer();
	        }
	    }, 0, GPS_SEND_INTERVAL);
		
		Log.d(TAG, "GPS Broadcast Started");
		
		Toast.makeText(mActivity.getApplicationContext(),
				"Location Tracking Started", Toast.LENGTH_SHORT)
				.show();
		
		if(mDisarm != null)
			mDisarm.setVisibility(View.VISIBLE);
		
		if(mStatus != null)
		{
			mStatus.setText("Connect Active");
	    	mStatus.setTextColor(Color.RED);
	    }
		
		if(mDisable != null)
			mDisable.setVisibility(View.INVISIBLE);
		
		if(mSnooze != null)
			mSnooze.setVisibility(View.INVISIBLE);
		
		if(mTimeDisp != null)
			mTimeDisp.setVisibility(View.INVISIBLE);
		
		if(mSosGps != null)
			mSosGps.setText("Stop GPS");
	}
	
	public void StopBroadcast()
	{
		Disable();
		
		if(mLocationFinder != null)
			mLocationFinder = null;
		
		if(mBroadcastTimer != null)
		{
			mBroadcastTimer.cancel();
			mBroadcastTimer.purge();
			mBroadcastTimer = null;
		}
			
		Log.d(TAG, "GPS Broadcast Stopped");
		
		Toast.makeText(mActivity.getApplicationContext(),
				"Location Tracking Stopped", Toast.LENGTH_SHORT)
				.show();
		
		if(mDisarm != null)
			mDisarm.setVisibility(View.INVISIBLE);
		
		if(mStatus != null)
		{
			mStatus.setText("Connect Inactive");
	    	mStatus.setTextColor(0xff99cc00);
	    }
		
		if(mDisable != null)
			mDisable.setVisibility(View.VISIBLE);
		
		if(mSnooze != null)
			mSnooze.setVisibility(View.VISIBLE);
		
		if(mTimeDisp != null)
			mTimeDisp.setVisibility(View.VISIBLE);
		
		if(mSosGps != null)
			mSosGps.setText("SOS GPS");
	}
	
	private void sendDataToServer()
	{
		new AsyncTask<Void, Void, Boolean>() {
			
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
			}
			
			@Override
			protected Boolean doInBackground(Void... params)
			{
				boolean isSuccess = false;
				try{
					//Create the HTTP request
					HttpParams httpParameters = new BasicHttpParams();
					
					//Setup timeouts
					HttpConnectionParams.setConnectionTimeout(httpParameters, CXN_TIME_OUT);
					HttpConnectionParams.setSoTimeout(httpParameters, CXN_TIME_OUT);			

					HttpClient httpclient = new DefaultHttpClient(httpParameters);
					HttpPost httppost = new HttpPost("http://www.campuscompanion.co/php/receiveAlerts.php");
//					HttpPost httppost = new HttpPost("http://10.190.78.14/new_gps_server_test/gps_server_test.php");
					
					MultipartEntity reqEntity = new MultipartEntity();
					
					reqEntity.addPart("user_unique_id", new StringBody("62"));
					reqEntity.addPart("alert_type_id", new StringBody("1"));
		            reqEntity.addPart("x", new StringBody(Double.valueOf(mLocationFinder.latitude).toString()));
		            reqEntity.addPart("y", new StringBody(Double.valueOf(mLocationFinder.longitude).toString()));
					
		            httppost.setEntity(reqEntity);
		            HttpResponse response = httpclient.execute(httppost);
		            HttpEntity resEntity = response.getEntity();
		             
		            final String response_str = EntityUtils.toString(resEntity);
		             
		            if (resEntity != null)
		            {
		            	Log.i(TAG,response_str);
		            	isSuccess = true;
		            }
		            
				}
				catch (ConnectTimeoutException ex)
				{
					 Log.e(TAG, "Connection timed out: " + ex.getMessage(), ex);
		        	 isSuccess = false;
		        }
				catch(Exception ex)
				{
					Log.e(TAG, "Error: " + ex.getMessage(), ex);
					isSuccess = false;
				}

				return isSuccess;
			}
			
			@Override
			protected void onPostExecute(Boolean isValid)
			{
				super.onPostExecute(isValid);
				if(isValid)
					Log.d(TAG, "GPS Data Sent to Server");
				else
				{
					Log.e(TAG, "There was an error sending data to Server");
					Toast.makeText(mActivity.getApplicationContext(), "Could not reach server. Please check your internet connection.", Toast.LENGTH_SHORT).show();
				}
					
			}
		}.execute(null,null,null);
	}
	
	private void Disable() {
		 synchronized(isStreaming) {
			 isStreaming = Boolean.FALSE;
		 }
	 }
	 
	private void Enable() {
		 synchronized(isStreaming) {
			 isStreaming = Boolean.TRUE;
		 }
	}

}