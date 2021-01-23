package org.sh.homeplate.mobile;

import java.util.List;
import org.sh.homeplate.data.Route;
import org.sh.homeplate.data.Stop;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class RouteView extends Activity
{
	private static final int		DIALOG_FINISH_ID	= 0;
	
	private static final int		PICKUP_MODE			= 0;
	private static final int		DROPOFF_MODE		= 1;
	
	private int							mode					= DROPOFF_MODE;
	private int							offset;
	private ListView					routeInfo;
	private Route						route;
	private TextView					totalWeight;
	
	private List<Stop>				stops;
	
	private static final String	pickupHint			= "Tap a pickup to enter weight";
	private static final String	dropoffHint			= "Tap a drop off to enter weight";
	
	private static final String	pickupWeight		= "Weight picked up : ";
	private static final String	dropoffWeight		= "Weight dropped off : ";
	
	private static final String	pickupButton		= "Display Pickups";
	private static final String	dropoffButton		= "Display Drop offs";
	
	private static final String	totalWeightString	= "Total weight on truck : ";
	private static final String	weightError			= "Weight dropped off exceeds pickups!";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route);
		
		RouteControl routeControl = (RouteControl) getApplicationContext();
		route = routeControl.getRoute();
		
		updateText();
	}
	
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		// remember list position
		offset = routeInfo.getFirstVisiblePosition();
	}
	
	
	@Override
	public void onResume()
	{
		super.onRestart();
		populateLists();
		updateTotal();
		
		// recall list position
		routeInfo.setSelection(offset);
	}
	
	
	// the button in the layout calls this
	public void swapMode(View view)
	{
		updateText();
	}
	
	
	// updates the strings / textviews of the layout when called
	private void updateText()
	{
		TextView routeHint = (TextView) findViewById(R.id.route_hint_tag);
		Button modeButton = (Button) findViewById(R.id.mode_swap_button);
		
		totalWeight = (TextView) findViewById(R.id.total_weight_tag);
		
		if (mode == PICKUP_MODE)
		{
			// display dropoff view
			routeHint.setText(dropoffHint);
			
			
			totalWeight.setText(dropoffWeight);
			modeButton.setText(pickupButton);
			
			mode = DROPOFF_MODE;
		}
		
		else if (mode == DROPOFF_MODE)
		{
			// go to pickup view
			routeHint.setText(pickupHint);
			totalWeight.setText(pickupWeight);
			modeButton.setText(dropoffButton);
			
			mode = PICKUP_MODE;
		}
		
		populateLists();
		updateTotal();
	}
	
	
	private void updateTotal()
	{
		TextView helperText = (TextView) findViewById(R.id.helper_weight_tag);
		Button finishRoute = (Button) findViewById(R.id.route_finish_button);
		
		if (route.getTotalWeightOnTruck() < 0)
		{
			helperText.setText(weightError);
			finishRoute.setVisibility(View.GONE);
		}
		else
		{
			helperText.setText(totalWeightString);
			helperText.append(Integer.toString(route.getTotalWeightOnTruck()) +
				" lbs");
			finishRoute.setVisibility(View.VISIBLE);
		}
		
		helperText.setTypeface(null, Typeface.BOLD);
		
		int weight = (mode == PICKUP_MODE) ? route.getTotalPickupWeight() : route
				.getTotalDropoffWeight();
		
		Log.d("Weight", "weight= " + weight);
		totalWeight.setText((mode == PICKUP_MODE) ? pickupWeight : dropoffWeight);
		totalWeight.append(Integer.toString(weight) + " lbs");
	}
	
	
	// helper method to add entries to the listviews 
	private void populateLists()
	{
		routeInfo = (ListView) findViewById(R.id.route_list);
		
		if (mode == PICKUP_MODE)
			stops = route.getPickups();
		else
			stops = route.getDropoffs();
		
		// finally, set the listview's adapter
		routeInfo.setAdapter(((RouteControl) getApplicationContext())
				.getRouteAdapter(stops, RouteControl.ICONS));
		routeInfo.setOnItemClickListener(new OnItemClickListener()
		{
			// this method packs the name and weight of the stop into the intent
			public void onItemClick(AdapterView<?> parent, View view,
				int position, long id)
			{
				TextView stop = (TextView) view.findViewById(android.R.id.text1);
				String stopName = (String) stop.getText();
				
				Intent i = new Intent(RouteView.this, Weight.class);
				
				// add the stop name and weight to the intent
				i.putExtra("STOP_NAME", stopName);
				
				startActivity(i);
			}
		});
	}
	
	
	public void scrollUp(View view)
	{
		routeInfo.smoothScrollByOffset(-3);
	}
	
	
	public void scrollDown(View view)
	{
		routeInfo.smoothScrollByOffset(3);
	}
	
	
	public void weightConfirm(View view)
	{
		Intent i = new Intent(RouteView.this, CreateStop.class);
		startActivity(i);
	}
	
	
	@SuppressWarnings("deprecation")
	public void finishRoute(View view)
	{
		showDialog(DIALOG_FINISH_ID);
	}
	
	
	// creates dialogs
	protected Dialog onCreateDialog(int id)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		
		switch (id)
		{
			case DIALOG_FINISH_ID:
				
				// create yes/no dialog
				builder.setMessage("Finished with the route?");
				
				builder.setPositiveButton("Yup!",
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							Intent i = new Intent(RouteView.this, Summary.class);
							RouteView.this.finish();
							startActivity(i);
						}
					});
				
				builder.setNegativeButton("Not yet.",
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							dialog.cancel();
						}
					});
				
				break;
		}
		AlertDialog alert = builder.create();
		
		return alert;
	}
}
