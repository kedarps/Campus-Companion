package kedarps.campuscompanion;

import java.util.Timer;
import java.util.TimerTask;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.Logger.LogLevel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectActivity extends AppCompatActivity{
	
	final public Context mContext = this;
	
	// All Times in milliseconds
	private static int BUTTON_PRESS_TIME = 1500; //in ms
	private static int BUTTON_PRESS_INTERVAL = 500; //im ms
	private static int SNOOZE_TIME = 5; // in mins
	private static String TAG = "ConnectActivity";
//	private static String DISARM_CODE = "1234";
	private static String USER_DISARM_CODE;
	
	// This is in seconds
	private int BUTTON_TIME_SPENT = 0;
	private int HRS_UNTIL_ALARM = 0;
	private int MINS_UNTIL_ALARM = 0;
	private int SECS_UNTIL_ALARM = 0;
	
	private String DISARM_CODE_ENTERED = "";
	
	private double BIG_TIME_IN_MS_UNTIL_ALARM;	
	
	public ImageView Help;
	public ImageView Disarm;
	public ImageView Snooze;
	public ImageView Disable;
	public TextView Status;
	public TextView TimeDisp;
	
//	public RollEventListener myRollEventListener;
	public HeadsetConnectionReceiver myHeadsetReceiver;
	public LocationBroadcaster myLocationBroadcaster;
	
	// For multi-threaded stuff
	Handler myLongPressHandler;
	Handler myButtonHandler;
	
	private static Timer myButtonTimer;
	private static CountDownTimer mCountDownTimer;
	private boolean isTimerFinished = false;
	
	// Dialog for disarming GPS data 
	AlertDialog.Builder myDisarmDialog;
	String myPassword = "1234";
	String UserValue = "";
	
    /*
     * Google Analytics configuration values.
     */
    
    private static GoogleAnalytics mGa;
    private static Tracker mTracker;
    
    private static final String SCREEN_LABEL = "Connect Screen";
    
    // Placeholder property ID.
    private static final String GA_PROPERTY_ID = "UA-48812272-1";

    // Dispatch period in seconds.
    private static final int GA_DISPATCH_PERIOD = 30;

    // Prevent hits from being sent to reports, i.e. during testing.
    private static final boolean GA_IS_DRY_RUN = false;

    // GA Logger verbosity.
    private static final LogLevel GA_LOG_VERBOSITY = LogLevel.VERBOSE;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Initialize Google Analytics
		initializeGa();
		mTracker.set(Fields.SCREEN_NAME, SCREEN_LABEL);
		
		//Get Disarm Code from Shared Prefs
		final SharedPreferences prefs = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
		USER_DISARM_CODE = prefs.getString("pref_disarm_code", "");
		
		// Check if called from CheckpointActivity or MainActivity
		Bundle extras = getIntent().getExtras();
		if(extras != null)
		{
			setContentView(R.layout.activity_connect_from_checkpoint_no_rotate);
			
			Disarm = (ImageView) findViewById(R.id.disarm);
			Status = (TextView) findViewById(R.id.status);
			Disable = (ImageView) findViewById(R.id.disable);
			Snooze = (ImageView) findViewById(R.id.snooze);
			TimeDisp = (TextView) findViewById(R.id.timeDisp);
			
			myLocationBroadcaster = new LocationBroadcaster(ConnectActivity.this, Disarm, Status, Disable, Snooze, TimeDisp);
						
			HRS_UNTIL_ALARM = extras.getInt("hrs_to_dest");
			MINS_UNTIL_ALARM = extras.getInt("mins_to_dest");
			SECS_UNTIL_ALARM = extras.getInt("secs_to_dest");
			BIG_TIME_IN_MS_UNTIL_ALARM = (HRS_UNTIL_ALARM*3600 + MINS_UNTIL_ALARM*60 + SECS_UNTIL_ALARM);
			
			Log.i(TAG,"HRS: "+HRS_UNTIL_ALARM+" MINS: "+MINS_UNTIL_ALARM+" SECS: "+SECS_UNTIL_ALARM);
			
			StartCountDownTimer();
			
			Snooze.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					int[] splitTimes = splitToComponentTimes(BIG_TIME_IN_MS_UNTIL_ALARM);
					splitTimes[1] += SNOOZE_TIME;
					BIG_TIME_IN_MS_UNTIL_ALARM = splitTimes[0]*3600 + splitTimes[1]*60 + splitTimes[2];
					StartCountDownTimer();
					Toast.makeText(getApplicationContext(),
							String.valueOf(SNOOZE_TIME)+" mins added until Alarm", Toast.LENGTH_SHORT)
							.show();
					
					return false;
				}
			});
			
			Disable.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					final Dialog mDialog = new Dialog(mContext);
					mDialog.setContentView(R.layout.disarm_dialog);
					mDialog.setTitle("Enter Disarm Code...");
					mDialog.setCancelable(false);
					
					final EditText disarmEntry = (EditText) mDialog.findViewById(R.id.disarmEntry);
					disarmEntry.requestFocus();
				    mDialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				    disarmEntry.setRawInputType(Configuration.KEYBOARD_12KEY);
					
					final Button dialogOK = (Button) mDialog.findViewById(R.id.OK_disarm);
									
					dialogOK.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							DISARM_CODE_ENTERED = disarmEntry.getText().toString().trim();
							Log.i(TAG,"Disarm code Entered: "+DISARM_CODE_ENTERED);
							
							if(DISARM_CODE_ENTERED.equals(USER_DISARM_CODE))
							{
								mDialog.dismiss();
								
								if(mCountDownTimer != null)
								{
									mCountDownTimer.cancel();
									mCountDownTimer = null;
								}
								
								if(myLocationBroadcaster.isStreaming)
									myLocationBroadcaster.StopBroadcast();
								
								setResult(Activity.RESULT_CANCELED);
								finish();
							}
							else
								Toast.makeText(getApplicationContext(),
										DISARM_CODE_ENTERED+" is the wrong code", Toast.LENGTH_SHORT)
										.show();
						}
					});
					
					Button dialogCancel = (Button) mDialog.findViewById(R.id.Cancel_disarm);
					dialogCancel.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							mDialog.dismiss();
						}
					});
					mDialog.show();
				}
			});

		}
		else
		{
			setContentView(R.layout.activity_connect_no_rotate);
			Disarm = (ImageView) findViewById(R.id.disarm);
			Status = (TextView) findViewById(R.id.status);
			
			myLocationBroadcaster = new LocationBroadcaster(ConnectActivity.this, Disarm, Status);
			isTimerFinished = true;
		}
		
		Help = (ImageView) findViewById(R.id.help);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(false);
		
//	    ImageView Rotate = (ImageView) findViewById(R.id.rotate);
//		ImageView Headset = (ImageView) findViewById(R.id.headset);
	    
	    Switch headsetSwitch = (Switch) findViewById(R.id.headSetSwitch);
	    
//		myRollEventListener = new RollEventListener(ConnectActivity.this, myLocationBroadcaster, Rotate);
		myHeadsetReceiver = new HeadsetConnectionReceiver(ConnectActivity.this, myLocationBroadcaster, headsetSwitch);
    	registerReceiver(myHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
	    		    	
	    Disarm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Dialog mDialog = new Dialog(mContext);
				mDialog.setContentView(R.layout.disarm_dialog);
				mDialog.setTitle("Enter Disarm Code...");
				mDialog.setCancelable(false);
				
				final EditText disarmEntry = (EditText) mDialog.findViewById(R.id.disarmEntry);
				disarmEntry.setRawInputType(Configuration.KEYBOARD_12KEY);
				disarmEntry.requestFocus();
			    mDialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			    
				final Button dialogOK = (Button) mDialog.findViewById(R.id.OK_disarm);
								
				dialogOK.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						DISARM_CODE_ENTERED = disarmEntry.getText().toString().trim();
						Log.i(TAG,"Disarm code Entered: "+DISARM_CODE_ENTERED);
						
						if(DISARM_CODE_ENTERED.equals(USER_DISARM_CODE))
						{
							mDialog.dismiss();

							if(myLocationBroadcaster.isStreaming)
							{
								myLocationBroadcaster.StopBroadcast();
								ConnectActivity.this.Help.setImageResource(R.mipmap.help_static);
								
								TextView TimeDisp = (TextView) findViewById(R.id.timeDisp);
								if(TimeDisp != null && !isTimerFinished)
									StartCountDownTimer();
								
								if(TimeDisp != null && isTimerFinished)
								{
									setResult(Activity.RESULT_CANCELED);
									finish();
								}
							}
						}
						else
							Toast.makeText(getApplicationContext(),
									DISARM_CODE_ENTERED+" is the wrong code.", Toast.LENGTH_SHORT)
									.show();
					}
				});
				
				Button dialogCancel = (Button) mDialog.findViewById(R.id.Cancel_disarm);
				dialogCancel.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mDialog.dismiss();
					}
				});
				mDialog.show();
			}
		});
	    
	    Help = (ImageView) findViewById(R.id.help);
		Help.setOnTouchListener(new View.OnTouchListener() {
		final Handler myLongPressHandler = new Handler(); 
		Runnable myLongPressCallback = new Runnable() { 
			@Override
			public void run() {
		    	myButtonTimer.cancel();
		    	myButtonTimer.purge();
		    	myButtonHandler.removeCallbacks(myButtonCallback);
//		    	myLocationBroadcaster.StartBroadcast();
		    	myLocationBroadcaster.PrepToStartBroadcast();
		    	
		    	if(mCountDownTimer != null)
		    		mCountDownTimer.cancel();
		    	
		    	BUTTON_TIME_SPENT = 0;
		    }   
		};
		
		final Handler myButtonHandler = new Handler();
		Runnable myButtonCallback = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				HelpButtonPoll();
			}
		};
		
		@Override
		public boolean onTouch(View v, MotionEvent myEvent) {
			// TODO Auto-generated method stub
			if (!myLocationBroadcaster.isStreaming)
			{
				switch(myEvent.getAction()){
				case MotionEvent.ACTION_DOWN:
					myLongPressHandler.postDelayed(myLongPressCallback, BUTTON_PRESS_TIME);
					myButtonHandler.post(myButtonCallback);
					break;
				case MotionEvent.ACTION_UP:
					myLongPressHandler.removeCallbacks(myLongPressCallback);
					myButtonHandler.removeCallbacks(myButtonCallback);
					myButtonTimer.cancel();
					BUTTON_TIME_SPENT = 0;
					Help.setImageResource(R.mipmap.help_static);
					break;
				default:
					break;
				}
			}
			return true;
		}
	});
	}
	
	public void HelpButtonPoll()
	{
		myButtonTimer = new Timer();
		myButtonTimer.schedule(new TimerTask() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			BUTTON_TIME_SPENT++;
			UpdateButtonImg();
		}
	},0, BUTTON_PRESS_INTERVAL);
	}
	
	private void UpdateButtonImg()
	{
		this.runOnUiThread(UpdateImg);
	}
	
	private Runnable UpdateImg = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			switch (BUTTON_TIME_SPENT)
			{
				case 1: 
					ConnectActivity.this.Help.setImageResource(R.mipmap.help_1);
					break;
				case 2:
					ConnectActivity.this.Help.setImageResource(R.mipmap.help_2);
					break;
				case 3:
					ConnectActivity.this.Help.setImageResource(R.mipmap.help_3);
					break;
			}
		}
	};
	
	private void StartCountDownTimer()
	{
		UpdateTimeDisplay();
		
		if(mCountDownTimer != null)
			mCountDownTimer.cancel();
		
		mCountDownTimer = new CountDownTimer((long)BIG_TIME_IN_MS_UNTIL_ALARM*1000,500) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				BIG_TIME_IN_MS_UNTIL_ALARM -= 0.5;
				UpdateTimeDisplay();
			}
			
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone ringAlert = RingtoneManager.getRingtone(getApplicationContext(), notification);
				Vibrator vibrateAlert = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				
				ringAlert.play();
	        	vibrateAlert.vibrate(250);
				
	        	mCountDownTimer.cancel();
	        	isTimerFinished = true;
	        	
	        	if(!myLocationBroadcaster.isStreaming)
	        	{
//	        		myLocationBroadcaster.StartBroadcast();
	        		myLocationBroadcaster.PrepToStartBroadcast();
	        	}
	        		
	        }
		}.start();
	}
	
	private void UpdateTimeDisplay()
	{
		int[] splitTimes = splitToComponentTimes(BIG_TIME_IN_MS_UNTIL_ALARM);
		
		TextView TimeDisp = (TextView) findViewById(R.id.timeDisp);
				
		if((splitTimes[0] == 0) && (splitTimes[1] == 0))
			TimeDisp.setText("for "+splitTimes[2]+" s ");
		
		if((splitTimes[0] == 0) && (splitTimes[1] != 0))
			TimeDisp.setText("for "+splitTimes[1]+" mins "+splitTimes[2]+" s ");
		
		if(splitTimes[0] != 0)
			TimeDisp.setText("for "+splitTimes[0]+" hrs "+splitTimes[1]+" mins "+splitTimes[2]+" s ");
	}
	
	private static int[] splitToComponentTimes(double biggyInMs)
	{
	    int hours = (int) biggyInMs / 3600;
	    int remainder = (int) biggyInMs - hours * 3600;
	    int mins = remainder / 60;
	    remainder = remainder - mins * 60;
	    int secs = remainder;

	    int[] ints = {hours , mins , secs};
	    
	    return ints;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		
	}
	
	@Override 
	public void onPause() {
	    super.onPause();
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		if(myHeadsetReceiver != null)
			unregisterReceiver(myHeadsetReceiver);
		
		if(myLocationBroadcaster != null)
		{
			if(myLocationBroadcaster.isStreaming)
				myLocationBroadcaster.StopBroadcast();
			
			myLocationBroadcaster = null;
		}
	}
	
	@Override
	public void onBackPressed()
	{
		if(myLocationBroadcaster != null)
		{
			if(myLocationBroadcaster.isStreaming || !isTimerFinished)
				Toast.makeText(getApplicationContext(), "Cannot exit without Disarming/Disabling", Toast.LENGTH_LONG).show();
			else
				finish();
		}
	}
	
	@Override
	  public void onStart() {
	    super.onStart();

	    // Send a screen view when the Activity is displayed to the user.
	    mTracker.send(MapBuilder.createAppView().build());
	  }
	
/*
 * Method to handle basic Google Analytics initialization. This call will not
 * block as all Google Analytics work occurs off the main thread.
 */
	@SuppressWarnings("deprecation")
	private void initializeGa() {
	    mGa = GoogleAnalytics.getInstance(this);
	    mTracker = mGa.getTracker(GA_PROPERTY_ID);
	
	    // Set dispatch period.
	    GAServiceManager.getInstance().setLocalDispatchPeriod(GA_DISPATCH_PERIOD);
	
	    // Set dryRun flag.
	    mGa.setDryRun(GA_IS_DRY_RUN);
	
	    // Set Logger verbosity.
	    mGa.getLogger().setLogLevel(GA_LOG_VERBOSITY);
	}
}