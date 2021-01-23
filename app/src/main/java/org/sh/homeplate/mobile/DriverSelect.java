package org.sh.homeplate.mobile;

import java.util.ArrayList;
import java.util.List;

import org.sh.homeplate.data.Client;
import org.sh.homeplate.data.Route;
import org.sh.homeplate.data.Volunteer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class DriverSelect extends Activity
{
	private ListView					listView;
	private ArrayList<Driver>		drivers;
	private ArrayAdapter<Driver>	listAdapter;
	private Route						route;
	private Dialog dialog;
	private List<String> driverNames = new ArrayList<String>();
	private AutoCompleteTextView otherDriverNameAutocompletetextview;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drivers);
		
		// get the global route
		RouteControl routeControl = (RouteControl) getApplicationContext();
		route = routeControl.getRoute();
		
		setAreaText();
		
		// Find the ListView resource.   
		listView = (ListView) findViewById(R.id.driver_list);
		
		List<String> driverNames = route.getDrivers();
		drivers = new ArrayList<Driver>();
		
		int i = 0;
		for (String name : driverNames)
		{
			String ogName = name;
			char marker = name.charAt(name.length() - 1);
			name = name.substring(0, name.length() - 1);
			
			if(marker != '#' && marker != '*'){
				name = ogName;
			}
			
		//	Log.d("Drivers", driverNames.get(i++));
		//	Log.d("Drivers", name);
		//	Log.d("Drivers", Character.toString(marker));
			
			if (marker == '#' && name.indexOf("Other") < 0)
				drivers.add(new Driver(name, true));
			else{
				
				drivers.add(new Driver(name));
			}
		}
		
		// Set our custom array adapter as the ListView's adapter.  
		listAdapter = new DriverArrayAdapter(this, drivers);
		listView.setAdapter(listAdapter);
		/*
		listView
		.setOnItemClickListener(new OnItemClickListener() {
			
			   public void onItemClick(AdapterView<?> parent, View view,
			     int position, long id) {
				   Log.d("Check changing.", "Check changed");
				   Driver driver = listAdapter.getItem(position);
				   driver.setChecked(! driver.checked);
	       
				   Log.d("Drivers", "Check changed");
	                
			   }
			  });
*/
		
		/*
         .setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View item,
                     int position, long id) {
                 Driver driver = listAdapter.getItem(position);
                driver.checked = ! driver.checked;
                
             }
         }
         */
         

		 
	}
	
	
	// called by button in the XML
	public void scrollUp(View view)
	{
		listView.smoothScrollByOffset(-3);
	}
	
	
	// called by button in the XML
	public void scrollDown(View view)
	{
		listView.smoothScrollByOffset(3);
	}
	
	
	private void setAreaText()
	{
		Intent i = getIntent();
		String area = i.getStringExtra("AREA");
		
		TextView areaTag = (TextView) findViewById(R.id.area_tag);
		areaTag.setText(area);
	}
	
	
	public void routeScreenLaunch(View view)
	{
		
		int checkedCount = 0;
		boolean otherChecked = false;
		
		for (Driver driver : drivers)
		{
			driverNames.add(driver.markedName());
			if(driver.markedName().indexOf("#") > -1){
				checkedCount++;
				if(driver.getName().indexOf("Other") > -1){
					otherChecked = true;
				}
			}
		}
		
		if(checkedCount < 1)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Please select a driver!")
					.setTitle("Driver selection required")
			       .setCancelable(false)
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   return;
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
			return;
		}
		
		if(otherChecked){
			//show dialog
			confirmCreateOther(view);
		}
		else{
ContinueToRoute();
		}
		
	}
	
	
	private void ContinueToRoute(){

		
		// update the route with the drivers selected
		route.setDrivers(driverNames);
		Intent i = new Intent(this, RouteView.class);
		finish();
		startActivity(i);
	}
	
	/** Holds driver row data. */
	private static class Driver
	{
		private String		name			= "";
		private String		markedName	= "";
		private boolean	checked;
		
		
		public Driver(String name)
		{
			this.name = name;
			this.checked = false;
			this.markedName = name + "*";
		}
		
		
		public Driver(String name, boolean checked)
		{
			this.name = name;
			this.checked = checked;
			this.markedName = (checked) ? 
				name + "#" : name + "*";
		}
		
		
		public String getName()
		{
			return name;
		}
		
		
		public String markedName()
		{
			return markedName;
		}
		
		
		public boolean isChecked()
		{
			return checked;
		}
		
		
		public void setChecked(boolean checked)
		{
			this.checked = checked;
			this.markedName = (checked) ? name + "#" : name + "*";
		}
	}
	
	/** Custom adapter for displaying an array of Driver objects. */
	private static class DriverArrayAdapter extends ArrayAdapter<Driver>
	{
		private LayoutInflater	inflater;
		
		
		public DriverArrayAdapter(Context context, List<Driver> driverList)
		{
			super(context, R.layout.drivers_row, R.id.text, driverList);
			
			// Cache the LayoutInflate to avoid asking for a new one each time.
			inflater = LayoutInflater.from(context);
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			// Driver to display
			Driver driver = (Driver) this.getItem(position);
			
			// The child views in each row.
			CheckBox checkBox;
			TextView textView;
			
			// Create a new row view
			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.drivers_row, null);
				
				textView = (TextView) convertView.findViewById(R.id.text);
				checkBox = (CheckBox) convertView.findViewById(R.id.check);
				
				convertView.setTag(new DriverViewHolder(textView, checkBox));
				
				convertView.setOnClickListener(new View.OnClickListener()
				{
					// If CheckBox is toggled, update the driver it is tagged with.
					public void onClick(View v)
					{
						CheckBox cb = ((DriverViewHolder) v.getTag()).getCheckBox();
						
						cb.toggle();
						Driver driver = (Driver) cb.getTag();
						driver.setChecked(cb.isChecked());
					}
				});
				
				// If CheckBox is toggled, update the planet it is tagged with.  
		        checkBox.setOnClickListener( new View.OnClickListener() {  
		          public void onClick(View v) {  
		        	  CheckBox cb = (CheckBox) v ;  
						
						Driver driver = (Driver) cb.getTag();
						driver.setChecked(cb.isChecked());
		          }  
		        });          
		        
			}
			
			else
			{ //reuse existing row
				DriverViewHolder viewHolder = (DriverViewHolder) convertView
						.getTag();
				checkBox = viewHolder.getCheckBox();
				textView = viewHolder.getTextView();
				
				convertView.setOnClickListener(new View.OnClickListener()
				{
					// If CheckBox is toggled, update the driver it is tagged with.
					public void onClick(View v)
					{
						CheckBox cb = ((DriverViewHolder) v.getTag()).getCheckBox();
						
						cb.toggle();
						Driver driver = (Driver) cb.getTag();
						driver.setChecked(cb.isChecked());
					}
				});
				
				
			}
			
			// Tag the CheckBox with the Driver it is displaying, so that we can
			// access the driver in onClick() when the CheckBox is toggled.
			checkBox.setTag(driver);
			
			// Display driver data
			//don't check other option by default
			if(driver.getName().indexOf( "Other") < 0){
				
				checkBox.setChecked(driver.isChecked());
			}
			textView.setText(driver.getName());
			
			View view = View.inflate(this.getContext(), R.layout.drivers_row, null);
		    view.setClickable(true);
		    view.setFocusable(true);

			
			return convertView;
		}
		
	}
	
	/** Holds child views for one row. */
	private static class DriverViewHolder	
	{
		private CheckBox	checkBox;
		private TextView	textView;
		
		
		public DriverViewHolder(TextView textView, CheckBox checkBox)
		{
			this.checkBox = checkBox;
			this.textView = textView;
		}
		
		
		public CheckBox getCheckBox()
		{
			return checkBox;
		}
		
		
		@SuppressWarnings("unused")
		public void setCheckBox(CheckBox checkBox)
		{
			this.checkBox = checkBox;
		}
		
		
		public TextView getTextView()
		{
			return textView;
		}
		
		
		@SuppressWarnings("unused")
		public void setTextView(TextView textView)
		{
			this.textView = textView;
		}
		
		
	}
	
	public void confirmCreateOther(View view)
	{
		dialog = new Dialog(this);

		dialog.setContentView(R.layout.drivers_other_dialog);
		dialog.setTitle("Select name for Other...");
		
		

		Button yesButton = (Button) dialog.findViewById(R.id.other_dialog_confirm);
		Button noButton = (Button) dialog.findViewById(R.id.other_dialog_deny);
		Button yesAnotherButton = (Button) dialog.findViewById(R.id.other_dialog_confirm_and_another);
		// add functionality for the buttons
				yesButton.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						dialogConfirm();
					}
				});
				
				yesAnotherButton.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						dialogConfirmAndAdd();
					}
				});

				// add functionality for the buttons
				noButton.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						dialogDeny();
					}
				});
		
		String[] driverNames = new String[route.getVolunteers().size()];
	
		int i = 0;
		for(Volunteer s : route.getVolunteers()){
			driverNames[i] = s.getFullName(); //.substring(0, s.getFullName().length() - 1);
			//Log.d("Homeplate", "Volunteer:" + driverNames[i]);
			i++;
		}
		
		// ArrayList<Driver>	 volunteers = new ArrayList<Driver>();
		
		otherDriverNameAutocompletetextview = (AutoCompleteTextView)     dialog.findViewById(R.id.otherDriverNameAutocompletetextview);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, driverNames);
        otherDriverNameAutocompletetextview.setThreshold(1);

        otherDriverNameAutocompletetextview.setAdapter(adapter);

        dialog.show();
       // finish();
	}
	
	private void AddOtherDriver(){
		// save the information entered as a new Stop
				// then go back to last activity
				RouteControl routeControl = (RouteControl) getApplicationContext();
				route = routeControl.getRoute();
				
				
				
				String newDriverName = otherDriverNameAutocompletetextview.getText().toString();
				if(newDriverName == null || newDriverName.isEmpty()){
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("Please enter a driver name.")
							.setTitle("Driver name for Other is required")
					       .setCancelable(false)
					       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   return;
					           }
					       });
					AlertDialog alert = builder.create();
					alert.show();
					finish();
					
					dialog.dismiss();
					
					
					return;
				}
				
				String[] lines = newDriverName.split( "\n" );
				for(int i=0; i < lines.length; i++){
					//route.addDriver(newDriverName + "#");
					if(driverNames.contains(lines[i] + "*")){
						
						driverNames.set(driverNames.indexOf(lines[i] + "*"), lines[i] + "#");
						/*
						for(int j=0; j < driverNames.size(); j++){
							driverNames.
							if(driverNames[j] == lines[i] + "*"){
								break;
							}
						}
						*/
					}
					else if(!driverNames.contains(lines[i] + "#")){
						driverNames.add(lines[i] + "#");
					}
				}
				
				

				CharSequence text = "Volunteer Driver successfully added";

				Toast toast = Toast.makeText(getApplicationContext(), text,
					Toast.LENGTH_LONG);
				toast.show();
	}
	
	public void dialogConfirmAndAdd()
	{
		AddOtherDriver();
		
		otherDriverNameAutocompletetextview.setText("");
	}
	
	public void dialogConfirm()
	{
		
		
		AddOtherDriver();
		dialog.dismiss();
		ContinueToRoute();
		//finish();
	}


	public void dialogDeny()
	{
		dialog.dismiss();
	}
	
	
}
