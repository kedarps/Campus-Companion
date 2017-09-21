package kedarps.campuscompanion;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";
	
	Button Emergency;
	Button Report;
	Button Connect;
//	Button CheckPoint;
	
	String[] RegisterCredentials = new String[9];
    private static final int CODE_REG_TO_MAIN = 5;
    
//    private static final String REG_URL_ADDRESS = "http://10.190.73.4/new_register_test/register_test.php";
    private static final String REG_URL_ADDRESS = "http://www.campuscompanion.co/php/testing/test_insertRegistration.php";
    private static Boolean REG_SENT_TO_SERVER = false;
    /*
     * Google Analytics configuration values.
     */
    
    private static GoogleAnalytics mGa;
    private static Tracker mTracker;
    
    private static final String SCREEN_LABEL = "Home Screen";
    
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
		setContentView(R.layout.activity_main_no_checkpoint);
		
		// Initialize Google Analytics
		initializeGa();
		mTracker.set(Fields.SCREEN_NAME, SCREEN_LABEL);
		
		getRegisterCredentials(this);
		
		if(RegisterCredentials[1].isEmpty() || RegisterCredentials[2].isEmpty() || RegisterCredentials[5].isEmpty() || RegisterCredentials[8].isEmpty())
		{
			Intent mIntent = new Intent(this, RegisterActivity.class);
			startActivityForResult(mIntent, CODE_REG_TO_MAIN);
		}
		else
			Log.i(TAG, "Registered as: "+RegisterCredentials[1]+" "+RegisterCredentials[2]+"! Mac Address: "+RegisterCredentials[0]);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(R.layout.action_bar_title_layout);
		((TextView) findViewById(R.id.action_bar_title)).setText(
		    "Campus Companion");
		
		Emergency = (Button) findViewById(R.id.Emergency);
		Report = (Button) findViewById(R.id.Report);
		Connect = (Button) findViewById(R.id.Connect);
//		CheckPoint = (Button) findViewById(R.id.CheckPoint);
					
		Emergency.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Emergency.setImageResource(R.drawable.emergency_clicked);
				Emergency.setBackgroundResource(R.mipmap.border_blue_select);
				Intent intent = new Intent(MainActivity.this, EmergencyActivity.class);
				startActivity(intent);
			}
		});
		
		
		Report.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Report.setImageResource(R.mipmap.report_to_police_clicked);
				Report.setBackgroundResource(R.mipmap.border_orange_select);
				Intent intent = new Intent(MainActivity.this, ReportToPoliceActivity.class);
				startActivity(intent);
			}
		});
				
		Connect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Connect.setImageResource(R.mipmap.connect_clicked);
				Connect.setBackgroundResource(R.mipmap.border_green_select);
				Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
				startActivity(intent);
			}
		});
		
//		CheckPoint.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
////				CheckPoint.setImageResource(R.mipmap.checkpoint_clicked);
//				CheckPoint.setBackgroundResource(R.mipmap.border_yellow_select);
//				Intent intent = new Intent(MainActivity.this, CheckPointActivity.class);
//				startActivity(intent);
//			}
//		});
	}
	
	private void getRegisterCredentials(Context context)
	{
		final SharedPreferences mPrefs = getSharedPreferences("my_shared_prefs",Context.MODE_PRIVATE);
		
		RegisterCredentials[0] = mPrefs.getString("pref_mac_address", ""); 
		
		RegisterCredentials[1] = mPrefs.getString("pref_first_name", "");
		RegisterCredentials[2] = mPrefs.getString("pref_last_name", "");
		RegisterCredentials[3] = mPrefs.getString("pref_email_address", "");
		RegisterCredentials[4] = mPrefs.getString("pref_phone_number", "");
		
		RegisterCredentials[5] = mPrefs.getString("pref_email_1", "");
		RegisterCredentials[6] = mPrefs.getString("pref_email_2", "");
		RegisterCredentials[7] = mPrefs.getString("pref_email_3", "");
		
		RegisterCredentials[8] = mPrefs.getString("pref_disarm_code", "");
		
    }
	
	private void sendRegCredsToServer()
	{
		new AsyncTask<Void, Void, Boolean>()
		{
			private ProgressDialog SendDialog = null;
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				SendDialog = new ProgressDialog(MainActivity.this);
				SendDialog.setMessage("Sending registration info to server...");
				SendDialog.show();
			}
			
			@Override
            protected Boolean doInBackground(Void... params) {
				boolean isSuccess = false;
                try 
                {
                     HttpClient client = new DefaultHttpClient();
					 HttpPost post = new HttpPost(REG_URL_ADDRESS);
					 
					 MultipartEntity reqEntity = new MultipartEntity();
					 
					 getRegisterCredentials(MainActivity.this.getApplicationContext());
					 
					 reqEntity.addPart("user_mac_address", new StringBody(RegisterCredentials[0]));
					 
					 reqEntity.addPart("user_first_name", new StringBody(RegisterCredentials[1]));
					 reqEntity.addPart("user_last_name", new StringBody(RegisterCredentials[2]));
					 reqEntity.addPart("user_email_address", new StringBody(RegisterCredentials[3]));
					 reqEntity.addPart("user_phone_number", new StringBody(RegisterCredentials[4]));
					 
					 reqEntity.addPart("emergency_email_1", new StringBody(RegisterCredentials[5]));
					 reqEntity.addPart("emergency_email_2", new StringBody(RegisterCredentials[6]));
					 reqEntity.addPart("emergency_email_3", new StringBody(RegisterCredentials[7]));
					 
					 reqEntity.addPart("user_disarm_code", new StringBody(RegisterCredentials[8]));
					 
					 post.setEntity(reqEntity);
					 HttpResponse response = client.execute(post);
					 HttpEntity resEntity = response.getEntity();
					 
					 final String response_str = EntityUtils.toString(resEntity);
					 
					 if (resEntity != null) {
					     Log.i("Server Response ",response_str);
					 }
					
					 return true;    
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
            protected void onPostExecute(Boolean isSuccess)
            {
            	SendDialog.setMessage("Done !");
            	SendDialog.dismiss();
            	if(isSuccess)
            	{
            		Log.d(TAG, "Registration Info Sent to Server");
            		synchronized (REG_SENT_TO_SERVER) {
            			REG_SENT_TO_SERVER = Boolean.TRUE;
					}
            	}
				else
				{
					Log.e(TAG, "There was an error sending registration info to Server");
					Toast.makeText(MainActivity.this.getApplicationContext(), "Could not reach server. Please check your internet connection.", Toast.LENGTH_SHORT).show();
					synchronized (REG_SENT_TO_SERVER) {
            			REG_SENT_TO_SERVER = Boolean.FALSE;
					}
				}
            }
            
		}.execute(null,null,null);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
//        Emergency.setImageResource(R.mipmap.emergency);
//        Report.setImageResource(R.mipmap.report_to_police);
//        Connect.setImageResource(R.mipmap.connect);
//        CheckPoint.setImageResource(R.mipmap.checkpoint);
        
        Emergency.setBackgroundResource(R.mipmap.border_blue);
        Report.setBackgroundResource(R.mipmap.border_orange);
        Connect.setBackgroundResource(R.mipmap.border_green);
//        CheckPoint.setBackgroundResource(R.mipmap.border_yellow);
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if(resultCode == RESULT_OK)
		{
			if(requestCode == CODE_REG_TO_MAIN)
			{
				// Data from Register Activity
        		final SharedPreferences prefs = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
        		SharedPreferences.Editor editor = prefs.edit();
        		
        		editor.putString("pref_mac_address", data.getExtras().getString("mac_address"));
        		
        		editor.putString("pref_first_name", data.getExtras().getString("first_name"));
        		editor.putString("pref_last_name", data.getExtras().getString("last_name"));
        		editor.putString("pref_email_address", data.getExtras().getString("email_add"));
        		editor.putString("pref_phone_number", data.getExtras().getString("ph_no"));
        		
        		editor.putString("pref_email_1", data.getExtras().getString("emer_email_1"));
        		editor.putString("pref_email_2", data.getExtras().getString("emer_email_2"));
        		editor.putString("pref_email_3", data.getExtras().getString("emer_email_3"));
        		
        		editor.putString("pref_disarm_code", data.getExtras().getString("disarm_code"));
        		
        		editor.commit();
        		
        		sendRegCredsToServer();
        	}
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
