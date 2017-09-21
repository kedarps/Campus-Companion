package kedarps.campuscompanion;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.Logger.LogLevel;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EmergencyActivity extends ActionBarActivity{
	final Context mContext = this;
	
	LocationBroadcaster mLocationBroadcaster;
	
	Button sosGPS;

	private static final String TAG = "Emergency Activity";
	
//	private String DISARM_CODE = "1234";
	private String USER_DISARM_CODE;
	private String DISARM_CODE_ENTERED = "";
	
    /*
     * Google Analytics configuration values.
     */
    
    private static GoogleAnalytics mGa;
    private static Tracker mTracker;
    
    private static final String SCREEN_LABEL = "Emergency Screen";
    
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
		setContentView(R.layout.activity_emergency);
		
		// Initialize Google Analytics
		initializeGa();
		mTracker.set(Fields.SCREEN_NAME, SCREEN_LABEL);
		
		//Get Disarm Code from Shared Prefs
		final SharedPreferences prefs = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
		USER_DISARM_CODE = prefs.getString("pref_disarm_code", "");
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(false);
	    
	    Button call911 = (Button) findViewById(R.id.call911);
	    Button callPolice = (Button) findViewById(R.id.callPolice);
	    sosGPS = (Button) findViewById(R.id.sosGps);
	    
	    mLocationBroadcaster = new LocationBroadcaster(EmergencyActivity.this, sosGPS);
	    
	    call911.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTracker.send(MapBuilder
					      .createEvent("ui_action",     // Event category (required)
					                   "button_press",  // Event action (required)
					                   "call911_button",   // Event label
					                   null)            // Event value
					      .build());
			}
		});
	    
	    callPolice.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTracker.send(MapBuilder
					      .createEvent("ui_action",     // Event category (required)
					                   "button_press",  // Event action (required)
					                   "callPolice_button",   // Event label
					                   null)            // Event value
					      .build());
			}
		});
	    
	    sosGPS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTracker.send(MapBuilder
					      .createEvent("ui_action",     // Event category (required)
					                   "button_press",  // Event action (required)
					                   "sosGPS_button",   // Event label
					                   null)            // Event value
					      .build());
				
				if(!mLocationBroadcaster.isStreaming)
				{
					mLocationBroadcaster.PrepToStartBroadcast();
//					mLocationBroadcaster.StartBroadcast();
				}
				else
				{
					final Dialog mDialog = new Dialog(mContext);
					mDialog.setContentView(R.layout.disarm_dialog);
					mDialog.setTitle("Enter Disarm Code...");
					mDialog.setCancelable(false);
					
					final EditText disarmEntry = (EditText) mDialog.findViewById(R.id.disarmEntry);
					disarmEntry.setHint("Disarm code to stop");
					disarmEntry.requestFocus();
				    mDialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				    disarmEntry.setRawInputType(Configuration.KEYBOARD_12KEY);
					
					final Button dialogOK = (Button) mDialog.findViewById(R.id.OK_disarm);
									
					dialogOK.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							DISARM_CODE_ENTERED = disarmEntry.getText().toString().trim();
							Log.d(TAG,"Disarm code Entered: "+DISARM_CODE_ENTERED);
							
							if(DISARM_CODE_ENTERED.equals(USER_DISARM_CODE))
							{
								mDialog.dismiss();
								mLocationBroadcaster.StopBroadcast();
							}
							else
								Toast.makeText(mContext,
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
			}
		});
	}
	
	@Override
	public void onBackPressed()
	{
		Log.i(TAG, "In Back Pressed");
		if(mLocationBroadcaster != null)
		{
			if(mLocationBroadcaster.isStreaming)
				Toast.makeText(getApplicationContext(), "Cannot exit without Stopping GPS", Toast.LENGTH_LONG).show();
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