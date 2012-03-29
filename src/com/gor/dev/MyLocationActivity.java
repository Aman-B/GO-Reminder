package com.gor.dev;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.gor.dev.util.CommonUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**This include the views which does "Map Visualization"
 * @author Thushan
 *
 */
public class MyLocationActivity extends MapActivity{

	//LocationManager lManager;	//location manager to obtain GPS service
	//LocationListener lListener;	//get user's current location periodically
	Location location;
	MapController mControl;	//Control properties of the map view
	GeoPoint gPoint;	//Geo point represent a single pair of longitude, lattitude
	GeoPoint selectedLoc;
	String selectedLocAddress;
	MapView mView;	//Responsible for displaying the content of google maps
	Button selectB;
	TextView positionTag;
	PopupWindow pw;	//A window which contains 2 buttons appears when user clicks on the map


	private class MapOverlay extends com.google.android.maps.Overlay
	{


		@Override
		public boolean draw(Canvas canvas, MapView mapView, 
				boolean shadow, long when) 
		{
			super.draw(canvas, mapView, shadow);                   

			//---translate the GeoPoint to screen pixels---
			Point screenPts = new Point();
			if(selectedLoc!=null){
				mapView.getProjection().toPixels(selectedLoc, screenPts);

				//---add the marker---
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pin);            
				canvas.drawBitmap(bmp, screenPts.x, screenPts.y-50, null);
			}
			return true;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event,MapView mView){
			if(event.getAction()==1){
				GeoPoint p=mView.getProjection().fromPixels((int)event.getX(),(int)event.getY());
				//If currently in the "marker mode"
				if(mapModeTB.isChecked()){
					//get pixels of the selected location
					selectedLoc=((MapView) mView).getProjection().fromPixels((int)event.getX(),(int)event.getY());
					Toast.makeText(getBaseContext(), 
							getAddress(p)+";"+p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() /1E6 , 
							Toast.LENGTH_SHORT).show();

					//pw.showAtLocation(mView, Gravity.CENTER_HORIZONTAL, (int)event.getX(), (int)event.getY());
				}
			}

			return false;

		}
	} 

	private class ButtonHandler implements View.OnClickListener
	{
		public void onClick(View v)
		{	

			if(v.getId()==selectB.getId()){
				double[] coordinates={selectedLoc.getLatitudeE6(),selectedLoc.getLongitudeE6()};
				Intent createMemoIntent=new Intent(getApplicationContext(),CreateMemoActivity.class);
				createMemoIntent.putExtra("selectedCoordinates", coordinates);
				createMemoIntent.putExtra("selectedLocAddress", getAddress(selectedLoc));
				startActivity(createMemoIntent); 										
			}//Mode changing 'Zoom/Pan' and 'Marker'
			else if(v.getId()==mapModeTB.getId()){
				if(!mapModeTB.isChecked()){
					mView.setOnTouchListener(null);
				}else{
					mView.setOnTouchListener(new MyTouchListener());
				}
			}else if(v.getId()==saveLocB.getId()){
				double[] coordinates={selectedLoc.getLatitudeE6(),selectedLoc.getLongitudeE6()};
				Intent saveLoc=new Intent(v.getContext(), SaveLocationActivity.class);
				saveLoc.putExtra("selectedCoordinates", coordinates);
				saveLoc.putExtra("selectedLocAddress", getAddress(selectedLoc));
				startActivity(saveLoc);
			}else if(v.getId()==tagFriendsB.getId()){
				double[] coordinates={selectedLoc.getLatitudeE6(),selectedLoc.getLongitudeE6()};
				Intent tagFriends=new Intent(v.getContext(),TagFriendsActivity.class);
				tagFriends.putExtra("selectedCoordinates", coordinates);
				tagFriends.putExtra("selectedLocAddress", getAddress(selectedLoc));
				startActivity(tagFriends);
			}
		}
	}

	private String getAddress(GeoPoint p){
		selectedLocAddress="";
		Geocoder gCoder=new Geocoder(getBaseContext(),Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses=gCoder.getFromLocation(p.getLatitudeE6()/1E6,p.getLongitudeE6()/1E6,1);
			Thread.sleep(500);
			for (Address address : addresses) {
		        selectedLocAddress+=("\n" + address.getAddressLine(0));
		      }
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return selectedLocAddress;
	}
	
	/**Listens to popup window events
	 * @author Thushan
	 *
	 */
	private class PopupListener implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent ev) {
			//if user clicks on outside the window hide the window
			if(ev.getAction()==MotionEvent.ACTION_OUTSIDE){
				pw.dismiss();
			}
			return false;
		}

	}
	private class MyTouchListener implements OnTouchListener{

		@Override
		public boolean onTouch(View mV, MotionEvent event) {
			//If user clicks on the mapview
			if(mV.getId()==mView.getId()){
				if(event.getAction()==MotionEvent.ACTION_UP){
					//If marker mode is selected
					if(mapModeTB.isChecked()){
						selectedLoc=((MapView) mV).getProjection().fromPixels((int)event.getX(),(int)event.getY());
						Toast.makeText(getBaseContext(), 
								selectedLoc.getLatitudeE6() / 1E6 + "," + selectedLoc.getLongitudeE6() /1E6 , 
								Toast.LENGTH_SHORT).show();
						selectB.setEnabled(true);
						pw.showAsDropDown(positionTag);
					}
				}
			}
			return true;
		}

	}

	ToggleButton mapModeTB;
	Button saveLocB;
	Button tagFriendsB;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);


		mapModeTB=(ToggleButton) findViewById(R.id.mapModeTB);
		mapModeTB.getBackground().setAlpha(80);
		mapModeTB.setOnClickListener(new ButtonHandler());

		mView=(MapView) findViewById(R.id.MapView);

		mView.displayZoomControls(true);
		mView.setBuiltInZoomControls(true);

		//lListener=new MyLocationListener(getBaseContext(),mView); //subscribe to myLocationListener

		
		//lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CommonUtils.TIME_TO_UPDATE_LOCATION,CommonUtils.DISTANCE_TO_UPDATE_LOCATION, lListener);	//Get the GPS provider

		positionTag=(TextView) findViewById(R.id.textView1);

		selectB=(Button) findViewById(R.id.selectB);
		selectB.setOnClickListener(new ButtonHandler());
		selectB.setEnabled(false);
		
		mControl=mView.getController();

		createPopup();	//creates to popup window for displaying later

		//Create a set of mapoverlays to show various views on top of the mapview
		MapOverlay mapOverlay = new MapOverlay();
		List<Overlay> listOfOverlays = mView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(mapOverlay);


	}


	/**Creates a popup window with specified attributes
	 * 
	 */
	public void createPopup(){
		//We're using this to get the device width, so the popup window will
		//fit just rit.
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		pw = new PopupWindow(
				inflater.inflate(R.layout.popup, null, false), 
				metrics.widthPixels, 100, true);
		
		saveLocB=(Button) pw.getContentView().findViewById(R.id.selectLocB);
		saveLocB.setOnClickListener(new ButtonHandler());
		
		tagFriendsB=(Button)pw.getContentView().findViewById(R.id.tagB);
		tagFriendsB.setOnClickListener(new ButtonHandler());
		// The code below assumes that the root container has an id called 'main'
		pw.setBackgroundDrawable(new BitmapDrawable());
	
		//pw.setOutsideTouchable(true);
		//pw.showAsDropDown(mView);
		pw.setTouchInterceptor(new PopupListener());
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}





}
