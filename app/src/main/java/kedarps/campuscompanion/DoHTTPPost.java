package kedarps.campuscompanion;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DoHTTPPost extends AsyncTask<String, Void, Boolean>{

		public final static int CXN_TIME_OUT = 5000;
		private static String TAG = "LocationHTTPPost";
		
		private String myLatitude = "";
		private String myLongitude = "";
						
		Exception exception;
		
		DoHTTPPost(Context context, String StrLatitude, String StrLongitude){
			myLatitude = StrLatitude;
			myLongitude = StrLongitude;
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			boolean isSuccess = false;
			try{
				//Create the HTTP request
				HttpParams httpParameters = new BasicHttpParams();
				
				//Setup timeouts
				HttpConnectionParams.setConnectionTimeout(httpParameters, CXN_TIME_OUT);
				HttpConnectionParams.setSoTimeout(httpParameters, CXN_TIME_OUT);			

				HttpClient httpclient = new DefaultHttpClient(httpParameters);
//				HttpPost httppost = new HttpPost("http://www.campuscompanion.co/php/receiveAlerts.php");
				HttpPost httppost = new HttpPost("http://10.190.78.14/new_gps_server_test/gps_server_test.php");
				
				MultipartEntity reqEntity = new MultipartEntity();
				
				reqEntity.addPart("user_unique_id", new StringBody("62"));
	            reqEntity.addPart("x", new StringBody(myLatitude));
	            reqEntity.addPart("y", new StringBody(myLongitude));
	             
//				HttpPost httppost = new HttpPost("http://"+IP_ADDRESS+"/gpsservertest/gpstest.php");
				
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
			catch (Exception ex)
			{
				 Log.e(TAG, "error: " + ex.getMessage(), ex);
	        	 isSuccess = false;
			}

			return isSuccess;
		}

		@Override
		protected void onPostExecute(Boolean isValid){
			if(isValid)
				Log.d(TAG, "GPS Data Sent to Server");
			else
				Log.e(TAG, "There was an error sending data to Server");
		}

	}
