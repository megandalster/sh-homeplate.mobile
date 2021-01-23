package org.sh.homeplate.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class Welcome extends Activity
{
	private static final String	TAG	= "HomeplateFTP";
	private String			area;
	private RouteControl	routeControl;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		// make the global route
		routeControl = (RouteControl) getApplicationContext();
		
		TextView welcomeDate = (TextView) findViewById(R.id.date_text);
		welcomeDate.append(" " + routeControl.getDateString());
	//	welcomeDate.append("\n(If Homeplate stops, tap REFRESH in a Wi-fi zone.)");
		
		TextView versionName = (TextView) findViewById(R.id.ver_name);
		versionName.append(getVersionInfo());
		
		String androidID = Secure
				.getString(this.getContentResolver(), Secure.ANDROID_ID);
		versionName.append("\nAndroid ID: " + androidID);

		doPreflightCheck();
	}

	public void doPreflightCheck(){
		String path = Environment.getExternalStorageDirectory() + "/homeplateftp/ftpout";
		File folder = new File(path);
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdirs();
		}
		else{
			CharSequence text1 = "Failed to create missing data directory: " + path;
			int duration1 = Toast.LENGTH_LONG;

			Toast toast1 = Toast.makeText(this, text1, duration1);
			toast1.show();
		}


	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.refresh, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.andftp_sync:
				Intent i;
				
				i = RouteControl.formFtpIntent(RouteControl.FTPOUT);
				startActivityForResult(i, 3);
				
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
		
	}
	
	public void driverScreenLaunch(View view)
	{
		
		
		
		// pull the selected area from the radio group
		RadioGroup radioAreaGroup = (RadioGroup) this
				.findViewById(R.id.radioArea);
		RadioButton radioArea = (RadioButton) findViewById(radioAreaGroup
				.getCheckedRadioButtonId());
		area = radioArea.getText().toString();
		int duration = Toast.LENGTH_LONG;


		CharSequence text1 = "Setting Route";

		Toast toast1 = Toast.makeText(this, text1, duration);
		toast1.show();

		try{
			routeControl.setRoute(area);
			// make the new activity
			Intent i = new Intent(this, DriverSelect.class);
			i.putExtra("AREA", area);
			startActivity(i);
		}
		catch(Exception ex){

			CharSequence exText = "Error setting route" + ex.getMessage();
			Toast toast2 = Toast.makeText(this, exText, duration);
			toast2.show();
			return;
		}
		      /*
			CharSequence text = "Launching driver screen";
		
			
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
			    */

	}
	
	public String getVersionInfo() {
        String strVersion = "Version:";

        PackageInfo packageInfo;
        try {
            packageInfo = getApplicationContext()
                .getPackageManager()
                .getPackageInfo(
                    getApplicationContext().getPackageName(), 
                    0
                );
            strVersion += packageInfo.versionName;
        } catch (NameNotFoundException e) {
            strVersion += "Unknown";
        }
        
        /*
         * PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
version = pInfo.versionName;
         */

        return strVersion;
    }
	
}
