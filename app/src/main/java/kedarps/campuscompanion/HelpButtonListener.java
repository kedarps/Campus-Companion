package kedarps.campuscompanion;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class HelpButtonListener implements View.OnTouchListener
{
	private Activity mActivity;
	private ImageView Help;
	private ImageView Disarm;
	private Boolean isEnabled;
		
	private String TAG = "Help Button Listener";
	
	public HelpButtonListener(Activity mActivity)
	{
		this.mActivity = mActivity;
		
		Help = (ImageView) mActivity.findViewById(R.id.help);
		Help.setOnTouchListener(this);
		Disarm = (ImageView) mActivity.findViewById(R.id.disarm);
	}
	
	public void Disable() {
		 synchronized(isEnabled) {
			 isEnabled = Boolean.FALSE;
		 }
	 }
	 
	 public void Enable() {
		 synchronized(isEnabled) {
			 isEnabled = Boolean.TRUE;
		 }
	 }
	 
	 public boolean onTouch(View v, MotionEvent event)
	 {
		 return true;
	 }
}