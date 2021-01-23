package org.sh.homeplate.mobile;

import org.sh.homeplate.data.Route;
import org.sh.homeplate.data.Stop;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Weight extends Activity
{
	private MediaPlayer mp	= null;
	
	private Stop		stop;
	private Route		route;
	private String		stopName;
	private boolean	breakdownMode;
	
	private static final String[] foodTypes = Stop.getFoodTypes();
	
	private static final String EMPTY_TRUCK = "Empty the truck!";
	
	private static final int[] editIds = 
		{ R.id.breakdown_meat_tag, R.id.breakdown_frozen_tag,
		R.id.breakdown_bakery_tag, R.id.breakdown_grocery_tag,
		R.id.breakdown_dairy_tag, R.id.breakdown_produce_tag };


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);


	android.view.WindowManager.LayoutParams wmlp = getWindow().getAttributes();

		wmlp.gravity = android.view.Gravity.TOP;



		initRouteInfo();

		Bundle extras = getIntent().getExtras();
		boolean intentBreakdownMode = true;

		if(extras != null){
			intentBreakdownMode = extras.getBoolean("breakdownMode");
			//breakdownMode set in initRouteInfo based on stop type.
			//breakdownMode = true;// intentBreakdownMode;
		}
		
		if (breakdownMode)
		{
			setContentView(R.layout.weight_breakdown);
			weightBreakdownSetup();

		}
		else
		{
			setContentView(R.layout.weight);
			weightTotalSetup();
		}
		
		((TextView)findViewById(R.id.weight_name_tag)).setText(stopName);
		
		if (stop.getType().equals("dropoff") &&
			(route.getTotalWeightOnTruck() > 0))
			setEmptyTruckButton();
		if (stop.getId().startsWith("Piggly"))
			oink(1);
	//	else if (stop.getId().startsWith("Food Lion"))
	//		oink(2);
		else if (stop.getId().startsWith("Wild Wings"))
			oink(3);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ftp_sync, menu);
		return true;
	}
	
	
	
	private void setEmptyTruckButton()
	{
		Button emptyTruck = (Button) findViewById(R.id.empty_truck_button);
		emptyTruck.setVisibility(View.VISIBLE);
	}

	
	private void initRouteInfo()
	{
		RouteControl routeControl = (RouteControl) getApplicationContext();
		route = routeControl.getRoute();
		
		Intent i = getIntent();
		stopName = i.getStringExtra("STOP_NAME");
		
		stop = (route.getPickup(stopName) != null) ? 
			route.getPickup(stopName) : route.getDropoff(stopName);
		
		// set the appropriate layout based on the weight type
		//breakdownMode = true;
		// 2017-01-10 default all stops to breakdown
		//2017-01-27 client asked the drop offs be total weight
		breakdownMode = (stop.getType() == "pickup");
		//  stop.getWeightType().equals("foodtype");
	}
	

	private void weightTotalSetup()
	{
		EditText weightTag = (EditText) findViewById(R.id.weight_entry_tag);
		
		int thisWeight = stop.getWeight();
		
		if(thisWeight < 0){
			thisWeight = 0;
		}
		
		weightTag.setText(Integer.toString(thisWeight));
		weightTag.selectAll();
	}


	private void weightBreakdownSetup()
	{
		// order : Meat, Frozen, Bakery, Grocery, Dairy, Produce
		int[] foodWeights = stop.getFoodTypeWeights();

		for (int i = 0; i < foodWeights.length; i++)
		{
			EditText edit = (EditText) findViewById(editIds[i]);
			
			if(foodWeights[i] > 0){
				edit.setText(Integer.toString(foodWeights[i]));
			}
			else{
				edit.setText("0");
			}
			
		}
	}

	public void emptyTheTruck(View view)
	{
		
		Button record = (Button) findViewById(R.id.record_weight_button);
		Button tapHere = (Button) findViewById(R.id.empty_truck_button);
		//Button recordWeightBreakdown = (Button)findViewById(R.id.record_weight_button);
		
		record.setText(EMPTY_TRUCK);
		tapHere.setVisibility(View.GONE);
		
		EditText weightTag = (EditText) findViewById(R.id.weight_entry_tag);
		if(weightTag != null){
			weightTag.setText(Integer.toString(route.getTotalWeightOnTruck()));
			weightTag.selectAll();
		}

		if(breakdownMode){
			recordWeightBreakdown(view);
		}
		else{
			recordWeight(view);
		}

	}

	public void recordWeight(View view)
	{
		EditText weightEntry;
		int currentWeight = 0;
		String weight = "";



			weightEntry = (EditText) this.findViewById(R.id.weight_entry_tag);

			if (weightEntry != null) {
				weight = weightEntry.getText().toString();
			}


		// the ternary is to catch an error when parse tries to parse ""
		currentWeight = weight.equals("") ? 0 : Integer.parseInt(weight);

		// get the current stop
		stop = (route.getPickup(stopName) != null) ?
			route.getPickup(stopName) : route.getDropoff(stopName);

		stop.setWeight(route, currentWeight);

		finish();
	}
	
	public void recordNoItems(View view)
	{
		
		int currentWeight = -1;
		
		stop = (route.getPickup(stopName) != null) ?
				route.getPickup(stopName) : route.getDropoff(stopName);
				
if(this.breakdownMode){
	int[] foodWeights = new int[foodTypes.length];

	for (int i = 0 ; i < foodWeights.length ; i++)
	{
		//EditText edit = (EditText) findViewById(editIds[i]);
		foodWeights[i] = currentWeight; // Integer.parseInt(edit.getText().toString());
		
		stop.setFoodTypeWeight(route, foodTypes[i], foodWeights[i]);
	}
}
else{
	// get the current stop

			stop.setWeight(route, currentWeight);
}
		
		
		finish();
		

		/*
		// get the current stop
		stop = (route.getPickup(stopName) != null) ?
			route.getPickup(stopName) : route.getDropoff(stopName);

		stop.setWeight(route, currentWeight);

		finish();
		*/
	}


	public void recordWeightBreakdown(View view)
	{
		// order : Meat, Frozen, Bakery, Grocery, Dairy, Produce
		int[] foodWeights = new int[foodTypes.length];

		for (int i = 0 ; i < foodWeights.length ; i++)
		{
			EditText edit = (EditText) findViewById(editIds[i]);
			foodWeights[i] = Integer.parseInt(edit.getText().toString());
			
			stop.setFoodTypeWeight(route, foodTypes[i], foodWeights[i]);
		}
		
		finish();
	}
	
	private void oink (int animal)
	{
		if (mp != null)
		{
			mp.reset();
			mp.release();
		}
		switch (animal) {
		case 1: mp = MediaPlayer.create(this, R.raw.squeal); break;
		case 2: mp = MediaPlayer.create(this, R.raw.lion); break;
		default: mp = MediaPlayer.create(this, R.raw.rooster);
		}
	//	mp = MediaPlayer.create(this, R.raw.squeal);
		mp.start();
	}

	public void switchEntryMode(View view){



		if (breakdownMode)
		{
			breakdownMode = false;
			setContentView(R.layout.weight);
			weightTotalSetup();

			/*
			Intent intentMain = new Intent(CurrentActivity.this ,
					Weight.class);
					intentMain.putExtra("breakdownMode", breakdownMode);
			CurrentActivity.this.startActivity(intentMain);
			Log.i("Content ", " Main layout ");
			*/

		}
		else {
			breakdownMode = true;
			setContentView(R.layout.weight_breakdown);
			weightBreakdownSetup();

		}
		((TextView)findViewById(R.id.weight_name_tag)).setText(stopName);

	}

}
