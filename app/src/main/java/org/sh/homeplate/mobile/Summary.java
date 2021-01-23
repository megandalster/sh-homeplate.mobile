package org.sh.homeplate.mobile;

import org.sh.homeplate.data.Route;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


public class Summary extends Activity
{
	private static final String	TAG	= "HomeplateFTP";
	private RouteControl	rc;
	private Route			route;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.summary);
		
		rc = (RouteControl) getApplicationContext();
		route = rc.getRoute();
		route.resetEndTime();   // save the time when this page was reached
		
		TextView dateText = (TextView) findViewById(R.id.report_date);
		dateText.setText("Today is " + rc.getDateString() + ".");
		
		ListView pickups = (ListView) findViewById(R.id.report_pickups);
		ListView dropoffs = (ListView) findViewById(R.id.report_dropoffs);
		
		pickups.setAdapter(rc.getRouteAdapter(route.getPickups()));
		dropoffs.setAdapter(rc.getRouteAdapter(route.getDropoffs()));
		
		TextView weightPickedup = (TextView) findViewById(R.id.report_total_pickup_weight_tag);	
		weightPickedup.append(" " + route.getTotalPickupWeight() + " lbs.");
		TextView weightDroppedoff = (TextView) findViewById(R.id.report_total_dropoff_weight_tag);	
		weightDroppedoff.append(" " + route.getTotalDropoffWeight() + " lbs." + 
		 "\n(To upload recent data, tap WI-FI SYNC in a Wi-fi zone.)");
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ftp_sync, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.andftp_sync:
				Intent i = RouteControl.formFtpIntent(RouteControl.FTPIN);
				startActivityForResult(i, 0);
				
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
		Log.i(TAG, "Result: "+resultCode+ " from request: "+requestCode);
		if (intent != null)
		{
			String transferredBytesStr = intent.getStringExtra("TRANSFERSIZE");
			String transferTimeStr = intent.getStringExtra("TRANSFERTIME");
			Log.i(TAG, "Transfer status: " + intent.getStringExtra("TRANSFERSTATUS"));
			Log.i(TAG, "Transfer amount: " + intent.getStringExtra("TRANSFERAMOUNT") + " file(s)");
			Log.i(TAG, "Transfer size: " + transferredBytesStr + " bytes");
			Log.i(TAG, "Transfer time: " + transferTimeStr + " milliseconds");
		}
		if (requestCode==0) {
			Intent j = RouteControl.formFtpIntent(RouteControl.FTPOUT);


			startActivityForResult(j, 3);	
		}
	}
	
	public void summaryFinish(View view)
	{
		Intent i = new Intent(this, SignOff.class);
		finish();
		
		startActivity(i);
	}
}
