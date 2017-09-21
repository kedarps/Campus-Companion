package kedarps.campuscompanion;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.Logger.LogLevel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	
	private static String TAG = "RegisterActivity";
	
	private TextView tvEmerEmail1;
	private TextView tvEmerEmail2;
	private TextView tvEmerEmail3;
	
	private ImageView EmerEmailSel1;
	private ImageView EmerEmailSel2;
	private ImageView EmerEmailSel3;
	
	private boolean isEmerEmail1Sel = false;
	private boolean isEmerEmail2Sel = false;
	private boolean isEmerEmail3Sel = false;
	
    /*
     * Google Analytics configuration values.
     */
    
    private static GoogleAnalytics mGa;
    private static Tracker mTracker;
    
    private static final String SCREEN_LABEL = "Registration Screen";
    
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
		setContentView(R.layout.activity_register);
		
		// Initialize Google Analytics
		initializeGa();
		mTracker.set(Fields.SCREEN_NAME, SCREEN_LABEL);
		
		final TextView tvFirstName = (TextView) findViewById(R.id.firstName);
		final TextView tvLastName = (TextView) findViewById(R.id.lastName);
		final TextView tvEmail = (TextView) findViewById(R.id.email);
		final TextView tvPhNo = (TextView) findViewById(R.id.phone);
		
		tvEmerEmail1 = (TextView) findViewById(R.id.emerCont1);
		EmerEmailSel1 = (ImageView) findViewById(R.id.emerCont1Select);
		tvEmerEmail2 = (TextView) findViewById(R.id.emerCont2);
		EmerEmailSel2 = (ImageView) findViewById(R.id.emerCont2Select);
		tvEmerEmail3 = (TextView) findViewById(R.id.emerCont3);
		EmerEmailSel3 = (ImageView) findViewById(R.id.emerCont3Select);
		
		final TextView tvDisarmCode = (TextView) findViewById(R.id.disarmCode);
		
		Button submit = (Button) findViewById(R.id.submit);
		
		submit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String firstName = tvFirstName.getText().toString();
				String lastName = tvLastName.getText().toString();
				String email = tvEmail.getText().toString();
				String phNo  = tvPhNo.getText().toString();
				String emerEmail1 = tvEmerEmail1.getText().toString();
				String emerEmail2 = tvEmerEmail2.getText().toString();
				String emerEmail3 = tvEmerEmail3.getText().toString();
				String disarmCode = tvDisarmCode.getText().toString();
				
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        		WifiInfo wInfo = wifiManager.getConnectionInfo();
        		String macAddress = wInfo.getMacAddress(); 
				
				if(firstName.isEmpty() || lastName.isEmpty() || emerEmail1.isEmpty() || disarmCode.isEmpty())
				{
					Toast.makeText(RegisterActivity.this.getApplicationContext(), "Please enter all fields marked with *", Toast.LENGTH_SHORT).show();
					return;
				}
				else
				{
					Intent RegCredsToMain = new Intent();
					
					RegCredsToMain.putExtra("mac_address", macAddress);
					
					RegCredsToMain.putExtra("first_name", firstName);
					RegCredsToMain.putExtra("last_name", lastName);
					RegCredsToMain.putExtra("email_add", email);
					RegCredsToMain.putExtra("ph_no", phNo);
					
					RegCredsToMain.putExtra("emer_email_1", emerEmail1);
					RegCredsToMain.putExtra("emer_email_2", emerEmail2);
					RegCredsToMain.putExtra("emer_email_3", emerEmail3);
					
					RegCredsToMain.putExtra("disarm_code", disarmCode);
					
					Toast.makeText(RegisterActivity.this.getApplicationContext(), "Registered as "+firstName+" "+lastName, Toast.LENGTH_SHORT).show();
					setResult(RegisterActivity.RESULT_OK, RegCredsToMain);
					finish();
				}
				
			}
		});
		
		EmerEmailSel1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isEmerEmail1Sel)
				{
					tvEmerEmail1.setEnabled(true);
					tvEmerEmail1.setText("");
					isEmerEmail1Sel = false;
					EmerEmailSel1.setImageResource(R.mipmap.ic_social_add_person);
				}
				else
				{
					try
					{
						Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			            startActivityForResult(intent, 1);
					}
					catch (Exception e)
					{
			            e.printStackTrace();
			            Log.e(TAG,"Error in emergency contacts 1 intent : "+ e.toString());
					}
				}
			}
		});
		
		EmerEmailSel2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isEmerEmail2Sel)
				{
					tvEmerEmail2.setEnabled(true);
					tvEmerEmail2.setText("");
					isEmerEmail2Sel = false;
					EmerEmailSel2.setImageResource(R.mipmap.ic_social_add_person);
				}
				else				{
					try
					{
						Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			            startActivityForResult(intent, 2);
					}
					catch (Exception e)
					{
			            e.printStackTrace();
			            Log.e(TAG,"Error in emergency contacts 2 intent : "+ e.toString());
					}
				}
			}
		});
		
		EmerEmailSel3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isEmerEmail3Sel)
				{
					tvEmerEmail3.setEnabled(true);
					tvEmerEmail3.setText("");
					isEmerEmail3Sel = false;
					EmerEmailSel3.setImageResource(R.mipmap.ic_social_add_person);
				}
				else
				{
					try
					{
						Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			            startActivityForResult(intent, 3);
					}
					catch (Exception e)
					{
			            e.printStackTrace();
			            Log.e(TAG,"Error in emergency contacts 3 intent : "+ e.toString());
					}
				}
			}
		});
		
		tvEmerEmail1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				String emailHere = tvEmerEmail1.getText().toString();
				
				if(!emailHere.isEmpty())
				{
					isEmerEmail1Sel = true;
					EmerEmailSel1.setImageResource(R.mipmap.ic_content_remove);
				}
			}
		});
		
		tvEmerEmail2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				String emailHere = tvEmerEmail2.getText().toString();
				
				if(!emailHere.isEmpty())
				{
					isEmerEmail2Sel = true;
					EmerEmailSel2.setImageResource(R.mipmap.ic_content_remove);
				}
			}
		});
		
		tvEmerEmail3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				String emailHere = tvEmerEmail3.getText().toString();
				
				if(!emailHere.isEmpty())
				{
					isEmerEmail3Sel = true;
					EmerEmailSel3.setImageResource(R.mipmap.ic_content_remove);
				}
			}
		});
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data)
	{
		super.onActivityResult(reqCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK)
		{
			Uri contactData = data.getData();
            @SuppressWarnings("deprecation")
			Cursor cur = managedQuery(contactData, null, null, null, null);
            ContentResolver contect_resolver = getContentResolver();

            if (cur.moveToFirst()) {
                String id = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String email = "";
                
                Cursor phoneCur = contect_resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);

                if (phoneCur.moveToFirst()) {
                	email = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                }
                
                if(!email.isEmpty())
                {
                	switch(reqCode)
                    {
                    case 1:
                    	tvEmerEmail1.setText(email);
                    	tvEmerEmail1.setEnabled(false);
                    	EmerEmailSel1.setImageResource(R.mipmap.ic_content_remove);
                    	isEmerEmail1Sel = true;
                    	break;
                    case 2:
                    	tvEmerEmail2.setText(email);
                    	tvEmerEmail2.setEnabled(false);
                    	EmerEmailSel2.setImageResource(R.mipmap.ic_content_remove);
                    	isEmerEmail2Sel = true;
                    	break;
                    case 3:
                    	tvEmerEmail3.setText(email);
                    	tvEmerEmail3.setEnabled(false);
                    	EmerEmailSel3.setImageResource(R.mipmap.ic_content_remove);
                    	isEmerEmail3Sel = true;
                    	break;
                    default:
                    	break;
                    }
                    
                    Log.i(TAG,"Email "+reqCode+" : "+email);
                }
                else
                	Toast.makeText(getApplicationContext(), "No Email address exists for this contact", Toast.LENGTH_SHORT).show();
                

                id = null;
                email = null;
                phoneCur = null;
            }
            else
            	Toast.makeText(getApplicationContext(), "No Email address exists for this contact", Toast.LENGTH_SHORT).show();
            
            contect_resolver = null;
            cur = null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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