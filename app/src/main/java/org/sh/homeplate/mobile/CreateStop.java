package org.sh.homeplate.mobile;

import org.sh.homeplate.data.Route;
import org.sh.homeplate.data.Client;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;


public class CreateStop extends Activity
{
	private Dialog dialog;
	private String name;
	private int type, weightType;
	private Route route;
	
	AutoCompleteTextView newStopNameAutocompletetextview;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_stop);
	
		setStopNameAdapter("donor");

	}
	
	public void setStopNameAdapter(String type){
		RouteControl routeControl = (RouteControl) getApplicationContext();
		route = routeControl.getRoute();
		String[] clientNames;
		if(type == "donor"){
			//clientNames = route.getDonors().toArray(new String[route.getDonors().size()]);
			clientNames = new String[route.getDonors().size()];
			
			int i = 0;
			for(Client s : route.getDonors()){
				if(s.getId() != "Publix food drive Beaufort"){
				clientNames[i] = s.getId();
				i++;
				}
			}
		}
		else{
			//clientNames = route.getRecipients().toArray(new String[route.getRecipients().size()]);
clientNames = new String[route.getRecipients().size()];
			
			int i = 0;
			for(Client s : route.getRecipients()){
				clientNames[i] = s.getId();
				i++;
			}
		}
		
		newStopNameAutocompletetextview = (AutoCompleteTextView)     findViewById(R.id.newStopNameAutocompletetextview);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, clientNames);
        newStopNameAutocompletetextview.setThreshold(1);

        newStopNameAutocompletetextview.setAdapter(adapter);
        
	}

	public void onRadioButtonClicked(View view) {    // Is the button now checked?    
		boolean checked = ((RadioButton) view).isChecked();       
		// Check which radio button was clicked    
		switch(view.getId()) {        
		case R.id.radioStopDropoff:           
			if (checked){// show recipients in autocomplete  
				setStopNameAdapter("recipient");
			}
				break;        
		case R.id.radioStopPickup:            
			if (checked)   {             // show donors in autocomplete    
				setStopNameAdapter("donor");
			}
				break;    
			}
	}


	

	public void confirmCreateStop(View view)
	{
		dialog = new Dialog(this);

		dialog.setContentView(R.layout.create_stop_dialog);
		dialog.setTitle("Confirm New Stop");

		Button yesButton = (Button) dialog.findViewById(R.id.dialog_confirm);
		Button noButton = (Button) dialog.findViewById(R.id.dialog_deny);

		// add functionality for the buttons
		yesButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				dialogConfirm();
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

		// textviews with information about the stop
		
		TextView stopName = (TextView) dialog
				.findViewById(R.id.new_stop_name_tag);
				
		

		
		TextView stopType = (TextView) dialog
				.findViewById(R.id.new_stop_type_tag);
		/*
		TextView stopWeightType = (TextView) dialog
				.findViewById(R.id.new_stop_weight_tag);
	*/
		
		// set name of stop based off the edittext info
		/*
		EditText editText = (EditText) this
				.findViewById(R.id.new_stop_entered_name_tag);
				String enteredName = editText.getText().toString();
				*/
		newStopNameAutocompletetextview = (AutoCompleteTextView)     this.findViewById(R.id.newStopNameAutocompletetextview);
		String enteredName = newStopNameAutocompletetextview.getText().toString();
		

		if (enteredName.equals(""))
			//default stop name
			stopName.setText("Unnamed Stop");
		else
			stopName.setText(enteredName);

		// set stop type based on corresponding selected radio button
		RadioGroup radioType = (RadioGroup) this
				.findViewById(R.id.radioStopType);
		RadioButton radioStopType =
				(RadioButton) findViewById(radioType.getCheckedRadioButtonId());
		stopType.setText(radioStopType.getText().toString());

		// set stop weight type based on corresponding selected radio button
		//option removed. all stops now by category
		/*
		RadioGroup radioWeight = (RadioGroup) findViewById(R.id.radioWeight);
		RadioButton radioWeightType =
				(RadioButton) findViewById(radioWeight
					.getCheckedRadioButtonId());
		stopWeightType.setText(radioWeightType.getText().toString());
	*/
		
		// grab information for new stop
		name = stopName.getText().toString();
		type = (stopType.getText().toString().equals("Pickup")) ?
			RouteControl.PICKUP : RouteControl.DROPOFF;
		/*
		weightType = (stopWeightType.getText().toString().equals("Total weight only")) ?
			RouteControl.TOTAL : RouteControl.BREAKDOWN;
*/
		
		weightType = (type == RouteControl.PICKUP ? RouteControl.BREAKDOWN : RouteControl.TOTAL);
		dialog.show();
	}


	public void cancelCreateStop(View view)
	{
		finish();
	}


	public void dialogConfirm()
	{
		// save the information entered as a new Stop
		// then go back to last activity
		RouteControl routeControl = (RouteControl) getApplication();
		routeControl.addNewStop(name, type, weightType);
		

		CharSequence text = "New stop successfully added";

		Toast toast = Toast.makeText(getApplicationContext(), text,
			Toast.LENGTH_LONG);
		toast.show();

		finish();
	}


	public void dialogDeny()
	{
		dialog.dismiss();
	}
}
