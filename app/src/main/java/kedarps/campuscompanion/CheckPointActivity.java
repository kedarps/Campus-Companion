package kedarps.campuscompanion;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CheckPointActivity extends AppCompatActivity{
	
	final static int DISABLE_CHECKPOINT = 5; 
	final static double PI = 3.141592653589793;
    final static double RADIUS = 6371;
	
	GoogleMap gMap;
	LatLng myLocation;
	LatLng destLocation;
	MarkerOptions DestMarker;
	GMapV2Direction gMapDir;
	
	NumberPicker hrsPicker;
	NumberPicker minPicker;
	ImageView set;
	ImageView clear;
	
	int hrsToDest;
	int minsToDest;
	int secsToDest;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checkpoint);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    	    
		try {
			// Loading map
			initilizeMap();
			
			gMapDir = new GMapV2Direction();
			
			hrsPicker = (NumberPicker) findViewById(R.id.hrsPicker);
			minPicker = (NumberPicker) findViewById(R.id.minPicker);
			set = (ImageView) findViewById(R.id.setCheckpoint);
			clear = (ImageView) findViewById(R.id.clearCheckpoint);
			
			hrsPicker.setVisibility(View.GONE);
			minPicker.setVisibility(View.GONE);
			
			set.setVisibility(View.GONE);
			clear.setVisibility(View.GONE);
			
			hrsPicker.setWrapSelectorWheel(true);
			minPicker.setWrapSelectorWheel(true);
			hrsPicker.setMaxValue(59);
			hrsPicker.setMinValue(0);
			
			minPicker.setMaxValue(59);
			minPicker.setMinValue(0);
			
			TextView hrs = (TextView) findViewById(R.id.hrs);
			TextView mins = (TextView) findViewById(R.id.mins);
			
			hrs.setVisibility(View.GONE);
			mins.setVisibility(View.GONE);
			
			hrsPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
				
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					// TODO Auto-generated method stub
					hrsToDest = newVal;
				}
			});
			
			minPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
				
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					// TODO Auto-generated method stub
					minsToDest = newVal;
				}
			});
			
			clear.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
//					clear.setImageResource(R.drawable.clear_checkpoint_clicked);
					clearCheckpoint();
				}
			});
			
			set.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
//					set.setImageResource(R.drawable.set_checkpoint_clicked);
					setCheckpoint();
				}
			});
			
			if (android.os.Build.VERSION.SDK_INT > 9) {
	            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	            StrictMode.setThreadPolicy(policy);
	        }
			
			// Changing map type
			gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//			 gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//			 gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			// gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			// gMap.setMapType(GoogleMap.MAP_TYPE_NONE);

			// Showing / hiding your current location
			gMap.setMyLocationEnabled(true);

			// Enable / Disable zooming controls
			gMap.getUiSettings().setZoomControlsEnabled(false);

			// Enable / Disable my location button
			gMap.getUiSettings().setMyLocationButtonEnabled(true);

			// Enable / Disable Compass icon
			gMap.getUiSettings().setCompassEnabled(true);

			// Enable / Disable Rotate gesture
			gMap.getUiSettings().setRotateGesturesEnabled(true);

			// Enable / Disable zooming functionality
			gMap.getUiSettings().setZoomGesturesEnabled(true);
			
			// Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            
            // Define a listener that responds to location updates
            LocationListener mLocationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                  // Called when a new location is found by the network location provider.
                	showLocationOnMap(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };
              
            // Register the listener with the Location Manager to receive location updates
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
              
            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            
            // Getting the name of the best provider
            String provider = mLocationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location location = mLocationManager.getLastKnownLocation(provider);

            if(location != null)
            {
            	showLocationOnMap(location);
            }
                        
			gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
				@Override
				public void onMapClick(LatLng DestLoc) {
					// TODO Auto-generated method stub
					if(destLocation == null)
					{
						destLocation = DestLoc;
						
						ImageView statText = (ImageView) findViewById(R.id.checkpointInstr);
						statText.setVisibility(View.GONE);
						
						TextView hrs = (TextView) findViewById(R.id.hrs);
						TextView mins = (TextView) findViewById(R.id.mins);
						
						hrs.setVisibility(View.VISIBLE);
						mins.setVisibility(View.VISIBLE);
						
	        	   		// Get distance using Haversine formula
						double dlon = (destLocation.longitude - myLocation.longitude)*PI/180;
				        double dlat = (destLocation.latitude - myLocation.latitude)*PI/180;
				        double a = (Math.sin(dlat / 2) * Math.sin(dlat / 2)) + Math.cos(myLocation.latitude*PI/180) * Math.cos(destLocation.latitude*PI/180) * (Math.sin(dlon / 2) * Math.sin(dlon / 2));
				        double angle = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
				        double HaversineDist = (angle * RADIUS)*1000;// in m
				        
				        int[] splitTimes = splitToComponentTimes(HaversineDist);
				        
				        hrsToDest = splitTimes[0];
				        minsToDest = splitTimes[1];
				        secsToDest = splitTimes[2];
				        
				        hrsPicker.setValue(hrsToDest);
				        minPicker.setValue(minsToDest);
				        
						MarkerOptions DestMarker = new MarkerOptions().position(
								new LatLng(DestLoc.latitude,DestLoc.longitude));
						
						DestMarker.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

						gMap.addMarker(DestMarker);
						
						hrsPicker.setVisibility(View.VISIBLE);
						minPicker.setVisibility(View.VISIBLE);
						
						set.setVisibility(View.VISIBLE);
						clear.setVisibility(View.VISIBLE);
					}
					else
					{
						Toast.makeText(getApplicationContext(),
								"Only one checkpoint can be set", Toast.LENGTH_SHORT)
								.show();
					}
				}
			});
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private void showLocationOnMap(Location location)
	{
    	// Getting latitude of the current location
    	double latitude = location.getLatitude();

    	// Getting longitude of the current location
    	double longitude = location.getLongitude();

    	myLocation = new LatLng(latitude, longitude);

    	// Move the camera to last position with a zoom level
		CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(myLocation.latitude,myLocation.longitude)).zoom(15).build();
		
		if(gMap != null)
			gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	
	private void setCheckpoint()
	{
		Intent mIntent = new Intent(getApplicationContext(), ConnectActivity.class);
		mIntent.putExtra("hrs_to_dest", hrsToDest);
		mIntent.putExtra("mins_to_dest", minsToDest);
		mIntent.putExtra("secs_to_dest", secsToDest);
		startActivityForResult(mIntent, DISABLE_CHECKPOINT);
		
		Toast.makeText(getApplicationContext(), "Checkpoint Set", Toast.LENGTH_SHORT).show();
	}
	
	private void clearCheckpoint()
	{
		gMap.clear();
		destLocation = null;
		
		TextView hrs = (TextView) findViewById(R.id.hrs);
		TextView mins = (TextView) findViewById(R.id.mins);
		
		hrs.setVisibility(View.GONE);
		mins.setVisibility(View.GONE);
		
		hrsPicker.setVisibility(View.GONE);
		minPicker.setVisibility(View.GONE);
		
		set.setVisibility(View.GONE);
		clear.setVisibility(View.GONE);
		
		ImageView statText = (ImageView) findViewById(R.id.checkpointInstr);
		statText.setVisibility(View.VISIBLE);
		
		Toast.makeText(getApplicationContext(), "Checkpoint Cleared", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_CANCELED)
		{
			if(requestCode == DISABLE_CHECKPOINT)
				clearCheckpoint();
		}
	}
	
	public static int[] splitToComponentTimes(double biggyInMs)
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
	protected void onResume() {
		super.onResume();
		initilizeMap();

//        set.setImageResource(R.drawable.set_checkpoint);
//        clear.setImageResource(R.drawable.clear_checkpoint);
	}
	
	private void initilizeMap() {
		if (gMap == null) {
			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.mapFragment);
			mapFragment.getMapAsync(this);

			// check if map is created successfully or not
			if (gMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}
}