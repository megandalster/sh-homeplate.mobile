package org.sh.homeplate.mobile;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.sh.homeplate.data.Route;
import org.sh.homeplate.data.Stop;

import android.app.Application;
import android.content.Intent;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class RouteControl extends Application
{
	private Route							route;
	private static String					dateTime, date, dateTwoWeeksAgo, time;
	private static Date 					today, twoWeeksAgo;
	private HashMap<String, String>			areaMap;
	private String							area, androidID;
	
	public static final int				PICKUP		= 0;
	public static final int				DROPOFF		= 1;
	
	public static final int				TOTAL			= 0;
	public static final int				BREAKDOWN	= 1;
	
	public static final int				ICONS			= 0;
	public static final int				NO_ICONS		= 1;
	
	public static final int				FTPOUT		= 0;
	public static final int				FTPIN			= 1;
	
	private boolean CompleteRoute = false;
	
	
	public void onCreate()
	{
		super.onCreate();
		
		areaMap = new HashMap<String, String>();		
		areaMap.put("Hilton Head", "HHI");
		areaMap.put("Bluffton", "SUN");
		areaMap.put("Beaufort", "BFT");
		
		SimpleDateFormat dfDateTime = new SimpleDateFormat("yy-MM-dd-HH:mm");
		dateTime = dfDateTime.format(new Date());		
		date = dateTime.substring(0, 8);
		time = dateTime.substring(9);
	}
	
	
	public Route getRoute()
	{
		return route;
	}
	
	public boolean getCompleteRoute(){
		return this.CompleteRoute;
	}
	
	public void setCompleteRoute(boolean value){
		this.CompleteRoute = value;
	}
		
	public void setRoute(String area)
	{

		try{


		androidID = Secure
				.getString(this.getContentResolver(), Secure.ANDROID_ID);
		
		SimpleDateFormat dfDateTime = new SimpleDateFormat("yy-MM-dd-HH:mm");
		SimpleDateFormat dfDate = new SimpleDateFormat("yy-MM-dd");
		today = new Date();
		dateTime = dfDateTime.format(today);
		date = dateTime.substring(0, 8);
		time = dateTime.substring(9);
		twoWeeksAgo = new Date(today.getTime() - 14*86400000L);
		dateTwoWeeksAgo = dfDate.format(twoWeeksAgo);
		this.area = areaMap.get(area);

		}
		catch(Exception ex){
			CharSequence text1 = ex.toString();
			int duration1 = Toast.LENGTH_LONG;

			Toast toast1 = Toast.makeText(this, text1, duration1);
			toast1.show();
			return;
		}
		
		try{
			route = new Route(date, time, this.area, androidID);
		}
		catch(FileNotFoundException fex){
			CharSequence text = fex.toString();
			int duration = Toast.LENGTH_LONG;
			
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
			return;
		}
		catch(Exception ex){
			
			CharSequence text = ex.toString();
			int duration = Toast.LENGTH_LONG;
			
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
			return;
		}
		
		
		CharSequence text = "Route successfully generated";
		int duration = Toast.LENGTH_LONG;
		
		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}
		
	public static Intent formFtpIntent(int type)
	{
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		
		
		Uri ftpUri = Uri.parse("ftp://homeplateftp1@184.168.164.1/");
		intent.setDataAndType(ftpUri,
			"vnd.android.cursor.dir/lysesoft.andftp.uri");
		intent.putExtra("ftp_username", "homeplateftp1");
		intent.putExtra("ftp_password", "Jon25T@aol.com");
		
		
		/* Prod
		//Log.d("Homeplate", "entered RouteControl.formFtpIntent()");
		
		Uri ftpUri = Uri.parse("ftp://shadmin9@ftp3.ftptoyoursite.com/");
		intent.setDataAndType(ftpUri,
			"vnd.android.cursor.dir/lysesoft.andftp.uri");
		intent.putExtra("ftp_username", "shadmin9");
		intent.putExtra("ftp_password", "SH3689hp");
			*/
		
		intent.putExtra("ftp_overwrite", "skip");
		String command = (type == FTPOUT) ?
			"download" : "upload";
		
		String remote  = (type == FTPOUT) ?  
			"ftpout" : "/";

		String dataPath = Environment.getExternalStorageDirectory() + "/homeplateftp";
		String local   = (type == FTPOUT) ? dataPath : dataPath + "/ftpin";
		/*
		String local   = (type == FTPOUT) ? 
		"/sdcard/homeplateftp" : "/sdcard/homeplateftp/ftpin";
		*/
		intent.putExtra("ftp_pasv", "true");
		intent.putExtra("command_type", command);
		
		//intent.putExtra("ftp_overwrite", "skip");
		if(type == FTPOUT)
		{
			purgeFTPOUT();
			local = dataPath;
			intent.putExtra("remote_file1", remote);
			intent.putExtra("local_folder", local);
			//intent.putExtra("ftp_overwrite", "skip");
			Log.d("Homeplate",
					"Downloading files from " + remote + " to " + local);
		}
		
		else
		{
			purgeFTPIN();
			
			intent.putExtra("local_file1", local);
			intent.putExtra("remote_folder", remote);
			//intent.putExtra("ftp_overwrite", "skip");
			Log.d("Homeplate",
					"Uploading files from " + local + " to " + remote);
		}
		
		String title = (type == FTPOUT) ?
			"Downloading routes from Homeplate" : "Uploading weights to Homeplate";
		intent.putExtra("progress_title", title);
		
		return intent;
	}
	
	// get rid of routes that are older than today -- keep the directory lean
	private static void purgeFTPOUT () {
		
		Log.d("Homeplate", "entered RouteControl.purgeFTPOUT()");

		String path = Environment.getExternalStorageDirectory() + "/homeplateftp/ftpout";
		File folder = new File(path);
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdirs();
		}
		if (success) {

		} else {
			// Do something else on failure
			Log.d("Homeplate", "Failed to create data directory: " + path);


		}

		File theFiles = new File(path);
		String[] fileNameList = theFiles.list();
		File[] fileList = theFiles.listFiles();
		int count = 0;
		
		if(fileNameList == null){
			return;
		}
		for (int i=0; i<fileNameList.length; i++) {
			if ((fileNameList[i].substring(0,8)).compareTo(date)<0) {
				fileList[i].delete();
				count++;
			}
		}
		Log.d("Homeplate",
				"Purged " + count + " files from " + fileNameList.length + " files in FTPOUT preceding " + date + ".");
	}
	// get rid of weights that are more than 2 weeks old -- keep the directory lean
	private static void purgeFTPIN () {
		String path = Environment.getExternalStorageDirectory() + "/homeplateftp/ftpin"; // "/sdcard/homeplateftp/ftpin";
		File theFiles = new File(path);
		String[] fileNameList = theFiles.list();
		
		if(fileNameList == null){
			return;
		}
		
		File[] fileList = theFiles.listFiles();
		int count = 0;
		for (int i=0; i<fileNameList.length; i++) {
			if ((fileNameList[i].substring(0,8)).compareTo(dateTwoWeeksAgo)<0) {
				fileList[i].delete();
				count++;
			}
		}
		Log.d("Homeplate",
				"Purged " + count + " files from " + fileNameList.length + " files in FTPIN preceding " + dateTwoWeeksAgo + ".");
	}
	
	public void addNewStop(String name, int type, int weightType)
	{
		// make a new stop
		String stopType = (type == PICKUP) ? "pickup" : "dropoff";
		String stopWeightType = (weightType == TOTAL) ? "pounds" : "foodtype";
		int[] weights = { 0, 0, 0, 0, 0, 0 };
		
		Stop stop = new Stop(area, date, name, stopType, 0, stopWeightType,
			weights);
		
		route.addStop(stop);
	}
	
	
	public String getDateString()
	{
		// get today's date
		SimpleDateFormat df = new SimpleDateFormat("EEEE MMMM d, yyyy");
		return df.format(new Date());
	}
	
	
	public SimpleCursorAdapter getRouteAdapter(List<Stop> stops, int iconFlag)
	{
		final String[] matrix = { "_id", "name", "weight" };
		final String[] columns = { "name", "weight" };
		final int[] layouts = { android.R.id.text1, android.R.id.text2 };
		
		int key = 0;
		
		MatrixCursor matrixCursor = new MatrixCursor(matrix);
		
		for (Stop stop : stops)
		{
			if(iconFlag == NO_ICONS && stop.getWeight() == 0)
			{
				continue;
			}
			
			String weight;
			
			int thisStopWeight = stop.getWeight() ;
			if(thisStopWeight < 0){
				weight = "No Items";
			}
			else{
			weight = stop.getWeight() + " lbs";
			}
			
			matrixCursor.addRow(new Object[] { key++, stop.getId(), weight });
		}
		
		int layout = (iconFlag == ICONS) ? R.layout.row_icons : R.layout.row;
		
		SimpleCursorAdapter routeAdapter = new SimpleCursorAdapter(this, layout,
			matrixCursor, columns, layouts, 0);
		
		return routeAdapter;
	}
	
	
	public SimpleCursorAdapter getRouteAdapter(List<Stop> stops)
	{
		return getRouteAdapter(stops, NO_ICONS);
	}
}
