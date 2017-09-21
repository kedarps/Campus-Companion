package kedarps.campuscompanion;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class ReportToPoliceActivity extends ActionBarActivity{
	
	public String audioPath = "";
    public String imagePath = "";
    public String videoPath = "";
    public String txtMessage = "";   
    public String macAddress = "";
    public String alertType = "";
    
    public String mCurrentPhotoPath;
    
    public ImageView addAudio;
    public ImageView addImage;
    public ImageView addVideo;
	
    public static final int REQUEST_AUDIO_CAPTURE = 100;
    public static final int REQUEST_IMAGE_CAPTURE = 200;
    public static final int REQUEST_VIDEO_CAPTURE = 300;
    
    public static final int MEDIA_AUDIO = 1;
    public static final int MEDIA_IMAGE = 2;
    public static final int MEDIA_VIDEO = 3;
    
    private static final String TAG = "ReportActivity";
    
    private ProgressDialog SendDialog;
    
    /*
     * Google Analytics configuration values.
     */
    
    private static GoogleAnalytics mGa;
    private static Tracker mTracker;
    
    private static final String SCREEN_LABEL = "Report Screen";
    
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
		setContentView(R.layout.activity_report2police);
		
		// Initialize Google Analytics
		initializeGa();
		mTracker.set(Fields.SCREEN_NAME, SCREEN_LABEL);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(false);
		
		Spinner crimeSpinner = (Spinner) findViewById(R.id.CrimeSpinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.crime_array, R.layout.my_spinner_textview);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(R.layout.my_spinner_textview);
		// Apply the adapter to the spinner
		crimeSpinner.setAdapter(adapter);
				
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
   	 	WifiInfo wInfo = wifiManager.getConnectionInfo();
   	 	macAddress = wInfo.getMacAddress(); 
		
		addAudio = (ImageView) findViewById(R.id.addAudio);
		addImage = (ImageView) findViewById(R.id.addImage);
		addVideo = (ImageView) findViewById(R.id.addVideo);
		
		addAudio.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTracker.send(MapBuilder
					      .createEvent("ui_action",     // Event category (required)
					                   "button_press",  // Event action (required)
					                   "addAudio_button",   // Event label
					                   null)            // Event value
					      .build());
					      
				if(audioPath.isEmpty())
					openMedia(MEDIA_AUDIO);
				else
				{
					audioPath = "";
					addAudio.setImageResource(R.mipmap.ic_device_access_mic);
//					addVideo.setBackgroundResource(R.mipmap.rounded_corners_orange);
					Toast.makeText(getApplicationContext(), "Audio File Removed", Toast.LENGTH_SHORT).show();
				}
					
			}
		});
		
		addImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTracker.send(MapBuilder
					      .createEvent("ui_action",     // Event category (required)
					                   "button_press",  // Event action (required)
					                   "addImage_button",   // Event label
					                   null)            // Event value
					      .build());
				
				if(imagePath.isEmpty())
					openMedia(MEDIA_IMAGE);
				else
				{
					imagePath = "";
					addImage.setImageResource(R.mipmap.ic_device_access_camera);
//					addVideo.setBackgroundResource(R.mipmap.rounded_corners_orange);
					Toast.makeText(getApplicationContext(), "Image Removed", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		addVideo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTracker.send(MapBuilder
					      .createEvent("ui_action",     // Event category (required)
					                   "button_press",  // Event action (required)
					                   "addVideo_button",   // Event label
					                   null)            // Event value
					      .build());
				if(videoPath.isEmpty())
					openMedia(MEDIA_VIDEO);
				else
				{
					videoPath = "";
					addVideo.setImageResource(R.mipmap.ic_device_access_video);
//					addVideo.setBackgroundResource(R.mipmap.rounded_corners_orange);
					Toast.makeText(getApplicationContext(), "Video Removed", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		Button send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTracker.send(MapBuilder
					      .createEvent("ui_action",     // Event category (required)
					                   "button_press",  // Event action (required)
					                   "send_button",   // Event label
					                   null)            // Event value
					      .build());
				
				TextView tvTxtMsg = (TextView) findViewById(R.id.txtMessage);
				txtMessage = tvTxtMsg.getText().toString();
				Log.i(TAG, "Text Message: "+txtMessage);
				
				Spinner crimeSpinner = (Spinner) findViewById(R.id.CrimeSpinner);
				alertType = String.valueOf(crimeSpinner.getSelectedItem());
				
				if(audioPath.isEmpty() && imagePath.isEmpty() && videoPath.isEmpty() && txtMessage.isEmpty())
	            	Toast.makeText(getApplicationContext(), "Nothing to Upload",Toast.LENGTH_SHORT).show();
				else
					sendDataToServer();
			}
		});
	}
	
	private void sendDataToServer()
	{
		new AsyncTask<Void, Void, Boolean>() {
			
			@Override
			protected void onPreExecute(){
				super.onPreExecute();
				SendDialog = new ProgressDialog(ReportToPoliceActivity.this);
				SendDialog.setMessage("Sending Data...");
				SendDialog.show();
			}
			
			@Override
            protected Boolean doInBackground(Void... params)
			{
				boolean isSuccess = false;
				
				try
		        {
		             HttpClient client = new DefaultHttpClient();
//		             HttpPost post = new HttpPost("http://10.190.78.14/multimedia_upload/upload_multimedia.php");
		             HttpPost post = new HttpPost("http://www.campuscompanion.co/php/receiveAlerts.php");
		             
//		             {x,y,alert_type_id,user_unique_id,user_text_message,user_mac_address*},{attachment_image*,attachment_audio*,attachment_video*}
		             
		             MultipartEntity reqEntity = new MultipartEntity();
		             
		             if(!audioPath.isEmpty())
		            	 reqEntity.addPart("attachment_audio", new FileBody(new File(audioPath)));
		             
		             if(!imagePath.isEmpty())
		            	 reqEntity.addPart("attachment_image", new FileBody(new File(imagePath)));
		             
		             if(!videoPath.isEmpty())
		            	 reqEntity.addPart("attachment_video", new FileBody(new File(videoPath)));
		             
		             if(!txtMessage.isEmpty())
		            	 reqEntity.addPart("user_text_message", new StringBody(txtMessage));
		             
		             reqEntity.addPart("user_mac_address", new StringBody(macAddress));
//		             reqEntity.addPart("x", new StringBody(mLatitude));
//		             reqEntity.addPart("y", new StringBody(mLongitude));
		             reqEntity.addPart("user_unique_id", new StringBody("62"));
		             reqEntity.addPart("alert_type_id", new StringBody(alertType));
		             
		             post.setEntity(reqEntity);
		             HttpResponse response = client.execute(post);
		             HttpEntity resEntity = response.getEntity();
		             
		             final String response_str = EntityUtils.toString(resEntity);
		             
		             if (resEntity != null)
		             {
		            	 Log.i(TAG,response_str);
		            	 isSuccess = true;
		             }
		        }
		        catch (Exception ex)
		        {
		        	 Log.e(TAG, "Error: " + ex.getMessage(), ex);
		        	 isSuccess = false;
		        }
				
				return isSuccess;
			}
			
			@Override
			protected void onPostExecute(Boolean valid){
				super.onPostExecute(valid);
				SendDialog.dismiss();
				if(valid)
				{
					Toast.makeText(ReportToPoliceActivity.this.getApplicationContext(), "Uploaded Successfully!", Toast.LENGTH_SHORT).show();
					Log.i(TAG, "Upload Successful");
					audioPath = "";
					imagePath = "";
					videoPath = "";
					
					addAudio.setImageResource(R.mipmap.ic_device_access_mic);
					addImage.setImageResource(R.mipmap.ic_device_access_camera);
					addVideo.setImageResource(R.mipmap.ic_device_access_video);
					
//					addAudio.setBackgroundResource(R.mipmap.rounded_corners_orange);
//					addImage.setBackgroundResource(R.mipmap.rounded_corners_orange);
//					addVideo.setBackgroundResource(R.mipmap.rounded_corners_orange);
					
					TextView tvTxtMsg = (TextView) findViewById(R.id.txtMessage);
					tvTxtMsg.setText("");
				}
				else
				{
					Toast.makeText(ReportToPoliceActivity.this.getApplicationContext(), "There was an error in uploading.", Toast.LENGTH_SHORT).show();
					Log.e(TAG, "Error uploading!");
				}
					
					
			}
		}.execute(null,null,null);
	}
	
	private void openMedia(int mediaType)
	{
		switch(mediaType)
		{
		case MEDIA_AUDIO:
			Intent recordIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
			startActivityForResult(recordIntent, REQUEST_AUDIO_CAPTURE);
			break;
		case MEDIA_IMAGE:
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
			break;
		case MEDIA_VIDEO:
			Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
			break;
		}
	}
	
	public String getAudioPathFromURI(Uri contentUri) {
		
		String[] projection = { MediaStore.Audio.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(contentUri, projection, null, null, null);
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
		
		cursor.moveToFirst();
        return cursor.getString(column_index);
    }
	
	public String getImagePathFromURI(Uri contentUri)
	{
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(contentUri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        
        cursor.moveToFirst();
        return cursor.getString(column_index);
	}
	
	public String getVideoPathFromURI(Uri contentUri)
	{
		String[] projection = { MediaStore.Video.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(contentUri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        
        cursor.moveToFirst();
        return cursor.getString(column_index);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(resultCode, requestCode, data);
		
		if (requestCode == REQUEST_AUDIO_CAPTURE) {
	        if (resultCode == RESULT_OK) {
	        	Uri selectedMediaUri = data.getData();
	            // Audio captured and saved to fileUri specified in the Intent
	        	audioPath = getAudioPathFromURI(selectedMediaUri);
        		Log.i(TAG, "Audio: "+audioPath);
        		addAudio.setImageResource(R.mipmap.ic_content_remove);
        		Toast.makeText(getApplicationContext(), "Audio File Attached", Toast.LENGTH_SHORT).show();
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the audio capture
	        	Toast.makeText(getApplicationContext(), "Audio Attachment Cancelled", Toast.LENGTH_SHORT).show();
	        } else {
	            // Audio capture failed, advise user
	        	Toast.makeText(getApplicationContext(), "Audio Attachment Failed", Toast.LENGTH_SHORT).show();
	        }
	        return;
	    }
		
		if (requestCode == REQUEST_IMAGE_CAPTURE) {
	        if (resultCode == RESULT_OK) {
	        	Uri selectedMediaUri = data.getData();
	            // Image captured and saved to fileUri specified in the Intent
	        	imagePath = getImagePathFromURI(selectedMediaUri);
        		Log.i(TAG, "Image: "+imagePath);
        		addImage.setImageResource(R.mipmap.ic_content_remove);
        		Toast.makeText(getApplicationContext(), "Image Attached", Toast.LENGTH_SHORT).show();
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        	Toast.makeText(getApplicationContext(), "Image Attachment Cancelled", Toast.LENGTH_SHORT).show();
	        } else {
	            // Image capture failed, advise user
	        	Toast.makeText(getApplicationContext(), "Image Attachment Failed", Toast.LENGTH_SHORT).show();
	        }
	        return;
	    }
		
		if (requestCode == REQUEST_VIDEO_CAPTURE) {
	        if (resultCode == RESULT_OK) {
	        	Uri selectedMediaUri = data.getData();
	            // Video captured and saved to fileUri specified in the Intent
	        	videoPath = getVideoPathFromURI(selectedMediaUri);
        		Log.i(TAG, "Video: "+videoPath);
        		addVideo.setImageResource(R.mipmap.ic_content_remove);
        		Toast.makeText(getApplicationContext(), "Video Attached", Toast.LENGTH_SHORT).show();
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the video capture
	        	Toast.makeText(getApplicationContext(), "Video Attachment Cancelled", Toast.LENGTH_SHORT).show();
	        } else {
	            // Video capture failed, advise user
	        	Toast.makeText(getApplicationContext(), "Video Attachment Failed", Toast.LENGTH_SHORT).show();
	        }
	        return;
	    }
		
//        if (resultCode == RESULT_OK) {
//        	if(requestCode == 0 || requestCode == 1 || requestCode == 2)
//        	{
//        		Uri selectedMediaUri = data.getData();
//       	     
//            	switch(requestCode)
//            	{
//            	case REQUEST_AUDIO_CAPTURE:
//            		audioPath = getAudioPathFromURI(selectedMediaUri);
//            		Log.i(TAG, "Audio: "+audioPath);
//            		addAudio.setImageResource(R.mipmap.ic_content_remove);
////            		addAudio.setBackgroundResource(R.mipmap.rounded_corners_red);
//            		Toast.makeText(getApplicationContext(), "Audio File Attached", Toast.LENGTH_SHORT).show();
//            		break;
//            	case REQUEST_IMAGE_CAPTURE:
//            		imagePath = getImagePathFromURI(selectedMediaUri);
//            		Log.i(TAG, "Image: "+imagePath);
//            		addImage.setImageResource(R.mipmap.ic_content_remove);
////            		addImage.setBackgroundResource(R.mipmap.rounded_corners_red);
//            		Toast.makeText(getApplicationContext(), "Image Attached", Toast.LENGTH_SHORT).show();
//            		break;
//            	case REQUEST_VIDEO_CAPTURE:
//            		videoPath = getVideoPathFromURI(selectedMediaUri);
//            		Log.i(TAG, "Video: "+videoPath);
//            		addVideo.setImageResource(R.mipmap.ic_content_remove);
////            		addVideo.setBackgroundResource(R.mipmap.rounded_corners_red);
//            		Toast.makeText(getApplicationContext(), "Video Attached", Toast.LENGTH_SHORT).show();
//            		break;
//            	}
//        	}
//        }
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