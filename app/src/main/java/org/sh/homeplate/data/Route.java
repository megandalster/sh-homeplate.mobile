package org.sh.homeplate.data;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter;

public class Route
{
	
	private String						date;								// yy-mm-dd, eg "12-07-06"
	private String						fullDate;							// eg "Friday July 6, 2012"
	private String						area;								// HHI, SUN, or BFT
	private String						areaName;							// "Hilton Head", "Bluffton", or "Beaufort"
	private String						deviceId;							// unique identifier for this tablet
	private String						startTime;							// hh:mm time when route began
	private String						endTime;							// hh:mm time when route ended
	private String						teamCaptain;						// eg "Jon Peluso"
	private String						teamCaptainPhone;				// eg 843-342-3118
																					
	private List<String>				drivers;
	private List<Stop>				pickups;
	private List<Stop>				dropoffs;
	
	private List<Client> donors;
	private List<Client> recipients;
	private List<Volunteer> volunteers;
	
	
	private int							totalPickupWeight;
	private int							totalDropoffWeight;
	
	// these initialized by constructor, updated by setters
	private String[]					outBuffer	= new String[7];
	private String[]					inBuffer		= new String[7];
	
	private static final String[]	foodTypes	= { "Meat", "Frozen", "Bakery",
			"Grocery", "Dairy", "Produce"		};
	
	
	// constructor returns a Route for a particular area and date and device
	// initialized with data from ftpout
	// and updated with data from ftpin whenever a setter is called on the route
	public Route(String date, String time, String area, String deviceId) throws FileNotFoundException
	{
		this.date = date;
		this.area = area;
		this.deviceId = deviceId;
		
		Log.d("Homeplate", "new route being created for " +
				Environment.getExternalStorageDirectory() + "/homeplateftp/ftpin/" + date + "-" + area + ".csv");
		String ftpInPath = Environment.getExternalStorageDirectory() + "/homeplateftp/ftpin/";

		File folder = new File(ftpInPath);
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdirs();
		}


		File extStore =  new File(ftpInPath);

		//File extStore =  new File("/mnt/sdcard/homeplateftp/ftpin/");
		//File extStore =  new File("/mnt/sdcard/homeplateftp/ftpin/");
		//File extStore = new File(Environment.getExternalStorageDirectory().getPath() + "/homeplateftp/ftpin/");
		/*
		File myFile = new File("/mnt/sdcard/homeplateftp/ftpin/" + date + "-" +
				area + "-" + deviceId + ".csv");
				*/
		if(extStore.exists()){
			Log.d("Homeplate", "Env path: " + extStore);
		}
		else{

			try{

			}
			catch(Exception ex){
				Log.d("Homeplate", "NOT FOUND!! Env path: " + extStore);

				throw new FileNotFoundException("NOT FOUND!! Env path: " + extStore);
			}

		
		}
			
			
		
/*
		loadTestCSVfrom(time);
		return;
*/
		

		// try to get this route from ftpin (to resume where we left off)
		if (getFtpin(date, area, deviceId))
		{
			parseCSVfrom(inBuffer);
			this.endTime = time; // reset the end time
		}
		
		// if not successful, try to get this route from ftpout and initialize
		// it in ftpin
		else if (getFtpout(date, area))
		{
			parseCSVfrom(outBuffer);
			
			this.startTime = time; // initialize the start and end times
			this.endTime = time;
			rebuildFtpinBuffer(0); // initialize the Ftpin buffer and the ftpin
			// file itself
		}
		
		// if not successful, return an empty Route
		else
		{
			Log.d("Homeplate",
				"cannot get route from either ftpin or ftpout file " +
					"/mnt/sdcard/homeplateftp/ftpin/" + date + "-" + area + ".csv");
		}
		
		

		
/*
		//all volunteers
		StringTokenizer st = new StringTokenizer(stripQuotes("Bob Augustine;Bob Hynes;David Elow;David OConnor;Dick Daily;Kevin Daily*;Kevin McMahon*;Leigh Bullen*;Other*;Paul Crunkleton*;Paul Stillman*;Richard Whitmore*;Tom Sabatelle*"), ";");
		this.volunteers = new ArrayList<Volunteer>();
		
		while (st.hasMoreTokens()){
			Volunteer thisVol = new	 Volunteer(st.nextToken());
			
			this.volunteers.add(thisVol);
		}
		
		//all donors
				st = new StringTokenizer(stripQuotes("BiLo  Boundary Street (158);BiLo  Shell Point (525);Bimbo Bakery;Food Lion  Ladies Island (945);Food Lion  Laurel Bay (1698)"), ";");
				this.donors = new ArrayList<Client>();
				
				while (st.hasMoreTokens()){
					Client thisClient = new	 Client(st.nextToken(), "donor");
					
					this.donors.add(thisClient);
				}
		
				//all recipients
				st = new StringTokenizer(stripQuotes("AME Church;Bethel Deliverance Temple;Burton Wells  Senior Center;Canal Appartments;Cannan Baptist;CAPA;Church of God and Unity;CODA"), ";");
				this.recipients = new ArrayList<Client>();
				
				while (st.hasMoreTokens()){
					Client thisClient = new	 Client(st.nextToken(), "recipient");
					
					this.recipients.add(thisClient);
				}
				
			*/
	}
	
	
	private void loadTestCSVfrom(String time)
	{
		
		String[] dataFile = {"14-01-29-HHI;\"Hilton Head\";\"Wednessday January 29, 2014\";\"Ed Dishart\";843-342-5530",
"\"Bob Engle\";\"Ed Farren\";\"Don Fearman\";\"Rick Hayward\";\"Jim Levesque\";\"Bill Putnam\";\"Bob Smith\";\"Jan Steffe\";\"Henri Eltzer\";\"Carl Fink\";\"Tom Foggo\";\"Ed Dishart\";\"Mike Kapitula\";\"David OConnor\";Other",
"\"Atlanta Bread  Hilton Head,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"BI LO  North   HHI  (164),0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"BiLo  Hilton Head South (275),0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"First Baptist Church,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Found on HH truck,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Fresh Market,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Harris Teeter  HHI North (152),0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Harris Teeter South Store 423,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Other Donors,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Piggly Wiggly  Shelter Cove,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Publix  Hilton Head North (473),0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Publix  Hilton Head South (700),0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Sams Club,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Second Helpings Office Pick Up,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"USDA Yemesee  HH Truck,0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\";\"Walmart Supercenter  Rt  278  (278),0,Meat:0,Frozen:0,Bakery:0,Grocery:0,Dairy:0,Produce:0\"",
"\"Bluffton Self Help  HHI,0\";\"Childrens Center  Bluffton,0\";\"Childrens Center  HHI,0\";\"Delivered to SH HHI Office,0\";\"Holy Family Catholic Church,0\";\"Housing Auth.  Sandalwood,0\";\"Left on HHI Truck,0\";\"Second Helpings Office drop off,0\";\"Senior Citizens,0\";\"St.Francis By The Sea,0\"",
"\"Bruce Algar\";\"Bruce Anderson\";\"Swinton Anderson\";\"Bob Anderson\";\"Dave Arnold\";\"Jon Ash\";\"Bob Augustine\";\"Bill Ballard\";\"Tricia Barrett\";\"Fred Bartlett\";\"Larry Beidelman\";\"Jim Benford\";\"Ed Berg\";\"Jim Bergenthal\";\"Peter Binazeski\";\"Jim Blackstone\";\"Don Bowers\";\"Steve Brandon\";\"Pat Brennan\";\"Nelson Brown\";\"Joseph Brown, Jr.\";\"Leigh Bullen\";\"Phil Burden\";\"Denny Burnette\";\"Bob Buterbaugh\";\"Laura Campbell\";\"brian carmines\";\"John Carter\";\"Mike Clark\";\"Ardene Clark\";\"Roy Clelland\";\"Guy Collier\";\"Michael Coyne\";\"John Cripps\";\"George Crook\";\"Paul Crunkleton\";\"Bill Culp\";\"Kevin Daily\";\"Dick Daily\";\"Robert Dalziel\";\"Ron DAmbrosi\";\"Frank DeAgro\";\"Vinny DiCanio\";\"Jack Dick\";\"Russ Dinke\";\"Ed Dishart\";\"Paul Dolan\";\"Bonita Dorsman\";\"Don Douglas\";\"Bruce Drake\";\"Roseann Dunn\";\"Peter Durand\";\"Lawrence Dziomba\";\"Lane Ehmke\";\"Ed Ehrlich\";\"Ken Eickhoff\";\"Bob Eilerman\";\"David Elow\";\"Henri Eltzer\";\"Bob Engle\";\"Johnny Evans\";\"Steve Farkasovsky\";\"Ed Farren\";\"Don Fearman\";\"Carl Fink\";\"Bill Fitzpatrick\";\"Tom Foggo\";\"Jim Foley\";\"Renee Ford\";\"Joe Fortin\";\"Don Fortney\";\"Clutch Fullerton\";\"Nicholson Gail\";\"Cesar Garcia\";\"John Geisler\";\"Norm George\";\"Perry Gesell\";\"Bob Ghirardelli\";\"John Gibbs\";\"fred goddin\";\"Jeronne Good\";\"Luther Good\";\"Charlene Gorrell\";\"David Grim\";\"Paul Griswold\";\"Joe Groncki\";\"Jim Grove\";\"Edward Gruel\";\"Frank Hager\";\"Charlie Hammel\";\"Trace Hartman\";\"Rick Hayward\";\"Marianne Hedemark\";\"Michele Henson\";\"Sharon Hester\";\"Bill Hilborn\";\"Jim Hirsch\";\"Mickey Hoehl\";\"Art Holland\";\"Luther Homes\";\"Floyd Honts\";\"Bill Hoppenrath\";\"Steve Hutten\";\"Bob Hynes\";\"James Irby\";\"Samuel Irby, Jr\";\"Linda Jackson\";\"Valery Jackson\";\"Johnny Jackson\";\"Garry James\";\"Norman Jenkins\";\"Cranston Jervey\";\"Glyn John\";\"Bill John\";\"Jim Johnson\";\"James Jones\";\"Joe Jones\";\"Matt Jordan\";\"Tim Joy\";\"Mike Kapitula\";\"Kevin Karg\";\"Michele Karg\";\"Rich Keefner\";\"Norm Kellogg\";\"Jim Kelly\";\"Jim Kemp\";\"Bill Kennedy\";\"Dennis Kinney\";\"Jim Kistler\";\"Bill Klein\";\"Stephen Korz\";\"Maureen Korzik\";\"Maureen Korzik\";\"David Koster\";\"Steve Krasinski\";\"Ed Krause\";\"Kathy Krause\";\"Kevin LaPierre\";\"Larry Laughlin\";\"Walter Lee\";\"Dennis Leo\";\"Jim Levesque\";\"Art Looney\";\"Loran Lopp\";\"Terry M. Lurtz\";\"Terry Lurtz\";\"Dave Luzzi\";\"Andy Lyons\";\"Scott Macdonald\";\"Michael Mackewich\";\"Tom Maier\";\"pat mangum\";\"Fred Matheson\";\"Michael McKeown\";\"Kevin McMahon\";\"Alan Meahen\";\"Philip Meeker\";\"Mike Melnick\";\"Roger Metzger\";\"Larry Millner\";\"Bernadette Montali\";\"Sal Montali\";\"David Morris\";\"Jim Morrissey\";\"Michael Morton\";\"Gerry Mueller\";\"Jim Nagy\";\"Patty Obrien\";\"Ken OBrien\";\"Paddy Obrien\";\"Pat Obrien\";\"David OConnor\";\"Richard Oliver\";\"Niels Olsen\";\"Ron Orlosky\";\"Joe Oros\";\"Julio Ortiz\";\"Bill Palcic\";\"Tim Parsons\";\"Judy Patchen\";\"Jon Peluso\";\"Bob Petersen\";\"Dan Pierce\";\"Antonni Pierce\";\"Bob Pogachnick\";\"Pearl Polite\";\"Bob Poveromo\";\"Mitch Progressive\";\"Bill Putnam\";\"Jean Radomski\";\"Judy Raney\";\"Ed Raney\";\"George Reilly\";\"Dan Resetarits\";\"thornton rice\";\"Chuck Riggs\";\"Joe Roeser\";\"Bob Roll\";\"Joseph Roney\";\"Michelle Roopcharan\";\"George Rosehart\";\"Donna Rosehart\";\"Tom Ryan\";\"Jeff Sanders\";\"Mark Sansone\";\"Mark Sansone\";\"John Schafer\";\"Rich Schiebel\";\"Maria Schiebel\";\"Steve Schmitt\";\"Drew Schrader\";\"Wes Schuster\";\"Michael Schwartzkopf\";\"Wes Seibert\";\"David Shaleuly\";\"Bill Shaw\";\"Bill Sherwood\";\"Graham Silcox\";\"Linda Silver\";\"Abrahan Simon\";\"Warren Slesinger\";\"Bob Smith\";\"Bill_ Standen\";\"Jan Steffe\";\"Gary Steger\";\"Ted Stevenson\";\"Paul Stillman\";\"James Stokes\";\"Milton Stombler\";\"Frank Stroncheck\";\"Michael Sullivan\";\"Bill Sutherland\";\"Cal Swan\";\"Ernie Thomas\";\"Noel Tillman\";\"Dennie Tomlin\";\"Bill Tomsik\";\"Allen Tucker\";\"Dick Tullie\";\"Bill Usher\";\"Rich Vallero\";\"Eric Vanwegen\";\"Karl Vogt\";\"adam wallace\";\"Jim Walsh\";\"Steve Weeks\";\"Robert Welborn\";\"Richard White\";\"Conard White\";\"Darrell White\";\"Richard Whitmore\";\"Ed Wilcox\";\"Helm Wilden\";\"Les Wilner\";\"Jannese Wrigth\";\"Sue Yearwood\";\"Sal Zuccala\"",
"\"Atlanta Bread  Hilton Head\";\"Beaufort Warehouse Pick UP\";\"BI LO  North   HHI  (164)\";\"Bi Lo Bluffton Parkway (5760)\";\"Big Lots\";\"BiLo  Boundary Street (158)\";\"BiLo  Hilton Head South (275)\";\"BiLo  Shell Point (525)\";\"Bimbo Bakery\";\"Black Marlin\";\"Bluffton Truck. SH.\";\"Demonstration Market\";\"Donor Programs\";\"First Baptist Church\";\"First Presbytarian Church\";\"Food Lion  Kitties Xing  (2691)\";\"Food Lion  Ladies Island (945)\";\"Food Lion  Laurel Bay (1698)\";\"Food Lion Sun City (1330)\";\"Found on HH truck\";\"Fresh Market\";\"Harris Teeter  HHI North (152)\";\"Harris Teeter South Store 423\";\"Healing Waters Mission and Wellness Ctr\";\"Henry Farms\";\"Henrys Farms\";\"Heritage Farms\";\"Hilton Head Island Truck\";\"Honey Baked Ham\";\"Island  Bagels\";\"Krogers Market\";\"Longs Deer Processing\";\"Longs Processing Facility\";\"Miscellaneous Pick Up\";\"NHC Healthcare\";\"On The Truck\";\"Other Donors\";\"Pepperidge Farm\";Pepsi;\"Pepsi Bottling Company\";\"Piggly Wiggly  Bluffton Pkwy\";\"Piggly Wiggly  Hardeeville\";\"Piggly Wiggly  Shelter Cove\";\"Providence Presbyterian Church\";\"Publix  Buckwalter (1205)\";\"Publix  Hardeeville (1354)\";\"Publix  Hilton Head North (473)\";\"Publix  Hilton Head South (700)\";\"Publix  RT 278   (845)\";\"Publix Ladys Island (623)\";\"Publix New River (1354)\";\"Red Lobster and Olive Garden\";\"RTs Market\";\"Sams Club\";\"Seabrook of HH\";\"Second Helpings Office Pick Up\";\"SH HHI Office\";\"Sun City Food Donation Programs\";\"Target Kitties Crossing\";\"Target Kitties Xing\";\"Truffles Coligny Plaza\";\"US Postal Service\";\"USDA Yemesee\";\"USDA Yemesee  HH Truck\";USPS;Walgreens;\"Walgreens  Beaufort\";\"Walgreens  Sun City\";\"WalMart Hardeeville (2832)\";\"Walmart Supercenter  Rt  278  (278)\";\"Walmart Supercenter Store (1383)  1st Pick Up\";\"Walmart Supercenter Store (1383)  2nd  Pick Up\";\"Westin Hotel\";\"Wild Boys Deer Processing\";\"Wild Wing Cafe\";\"Wild Wings Rt 278 Bluffton\"",
"\"A Grateful Recipient (demo)\";\"Access Network\";\"Agape Family Life Center\";\"Alzheimers Family Services\";\"AME Church\";\"Beaufort Marine Institute\";\"Beaufort Truck\";\"Beaufort Truck HHI\";\"Beaufort Warehouse Drop Off\";\"Bethel Deliverance Temple\";\"Bluffton Recreation Center\";\"Bluffton Self Help  1st delivery\";\"Bluffton Self Help  2nd  delivery\";\"Bluffton Self Help  HHI\";\"Booker T. Washington Center\";\"Boys and Girls\";\"Boys and Girls Club  Bluffton\";\"Boys and Girls Club  HHI\";\"Boys and Girls Club Ridgeland\";\"Burton Wells  Senior Center\";\"Burton Wells Center\";\"Campbell  AME Church\";\"Canal Appartments\";\"Cannan Baptist\";CAPA;\"Charles Lind Brown Center\";\"Childrens Center\";\"Childrens Center  Bluffton\";\"Childrens Center  HHI\";\"Church of God and Unity\";\"Church of the Cross  HHI\";Church_of_the_Cross;CODA;\"Community Bible Church\";\"Deep Well\";\"Delivered to SH HHI Office\";\"Disability/Special Needs DSN\";\"Ebenezer Baptist Church\";\"First African Baptist  Beaufort\";\"First Baptist Bluffton\";\"Franciscan Center\";\"Freeborn Deliverance Temple\";\"Grays Hill  Delivery CH.\";\"Hardeeville Thrift\";\"Harvest Church\";\"Healing Waters Wesleyan Church\";\"HELP OF BEAUFORT\";\"Holy Family Catholic Church\";\"Housing Auth.  Sandalwood\";\"Huspah Baptist Church\";\"Island House\";\"Kids Cafe\";\"Krogers   Belfair\";\"Left on HHI Truck\";\"Left on Truck\";\"Leroy Brown Senior Center\";\"Love House Ministries.\";\"Memory Matters\";\"Mercy Ministries (Hardeeville) 1st delivery\";\"Mercy Ministries (Hardeeville) 2nd delivery\";\"Mercy Ministries (Rear of Hardeeville Thrift)  HHI  Delivery\";\"Miscellaneous Delivery\";\"Mossy Oaks\";\"Mt.Calvary Baptist\";\"New Abundant Life   Hampton Cty via Ridgeland\";\"New Hope\";\"Other (adjustment)\";\"Our Ladys Pantry\";\"Parks and Leisure Seniors Bft\";\"Parks and Rec.  Bluffton\";\"Port Royal  United Methodist Church\";\"Praise Assembly\";\"Revival Team Outreadh Ministry\";\"Room At The Inn\";\"Saint  Jude Ch.\";\"Scotts Hill Church of Christ\";\"Second Goodwill Church\";\"Second Helpings Office\";\"Second Helpings Office drop off\";\"Second Jordan Church\";\"Second Mt. Carmel Baptist\";\"Senior Citizens\";\"Senior Citizens  Bluffton\";\"Sheldon Community Enrichment\";\"Sinai Baptist Church\";\"SprinG Hill AME. Church.\";\"St. Andrew By The Sea\";\"St. Gregory\";\"St. Jude via St Stephens Ridgeland\";\"St. Stephens Ridgeland\";\"St. Stevens Ridgeland  First Delivery\";\"St. Stevens Ridgeland  Second  Delivery\";\"St.Francis By The Sea\";\"St.Paul Baptist Church.\";\"Tabernacle Baptist Church\";\"United Methodist CH. Port Royal.\""};

		parseCSVfrom(dataFile);
		this.endTime = time; // reset the end time
		
		
		/*
		this.areaName = "Hilton Head"; 
		this.fullDate = "Saturday August 31, 2013";
		this.teamCaptain = "Richard Whitmore";
		this.teamCaptainPhone = "843-342-5197";
		this.startTime = "07:27;16:03";
		
		
		//if (line1.hasMoreTokens())
	//		this.startTime = line1.nextToken();
		
	//	StringTokenizer line2 = new StringTokenizer(stripQuotes(Bob Augustine*;Bob Hynes*;David Elow*;David OConnor#;Dick Daily*;Kevin Daily*;Kevin McMahon*;Leigh Bullen*;Other*;Paul Crunkleton*;Paul Stillman*;Richard Whitmore*;Tom Sabatelle*), ";");
		
		StringTokenizer line2 = new StringTokenizer(stripQuotes("Bob Augustine*;Bob Hynes*;David Elow*;David OConnor#;Dick Daily*;Kevin Daily*;Kevin McMahon*;Leigh Bullen*;Other*;Paul Crunkleton*;Paul Stillman*;Richard Whitmore*;Tom Sabatelle*"), ";");
		
		this.drivers = new ArrayList<String>();
		
		while (line2.hasMoreTokens())
			this.drivers.add(line2.nextToken());
		
		//this.drivers.add("Test Tester");
		
		Collections.sort(drivers);
		
		this.pickups = new ArrayList<Stop>();
		this.totalPickupWeight = 0;
		
		StringTokenizer st = new StringTokenizer(stripQuotes("Atlanta Bread  Hilton Head,50;BI LO  North   HHI  (164),170,Meat:0,Frozen:0,Bakery:30,Grocery:0,Dairy:140,Produce:0;BiLo  Hilton Head South (275),260,Meat:110,Frozen:0,Bakery:60,Grocery:0,Dairy:90,Produce:0;Found on HH truck,0;Fresh Market,150;Harris Teeter  HHI North (152),160,Meat:0,Frozen:0,Bakery:40,Grocery:0,Dairy:0,Produce:120;Harris Teeter South Store 423,35,Meat:0,Frozen:0,Bakery:25,Grocery:0,Dairy:0,Produce:10;Other Donors,0;Pepperidge Farm,30;Piggly Wiggly  Shelter Cove,150;Publix  Hilton Head North (473),80,Meat:0,Frozen:0,Bakery:60,Grocery:0,Dairy:0,Produce:20;Publix  Hilton Head South (700),135,Meat:60,Frozen:0,Bakery:35,Grocery:0,Dairy:0,Produce:40;SH HHI Office,0;Walmart Supercenter  Rt  278  (278),720,Meat:180,Frozen:0,Bakery:420,Grocery:120,Dairy:0,Produce:0"), ";");
		
		while (st.hasMoreTokens())
		{
			StringTokenizer std = new StringTokenizer(st.nextToken(), ",");
			String id = std.nextToken();
			int totalWeight = Integer.parseInt(std.nextToken());
			String weightType = "pounds";
			
			int[] foodTypeWeights = { 0, 0, 0, 0, 0, 0 };
			
			Log.d("Homeplate", "stop id = " + id);
			
			if (std.hasMoreTokens())
			{
				weightType = "foodtype";
				for (int i = 0; i < 6; i++)
				{
					String nt = std.nextToken();
				//	Log.d("Homeplate", "stopDetails for foodtype " + nt);
					int j = nt.indexOf(":") + 1;
					foodTypeWeights[i] = Integer.parseInt(nt.substring(j));
				}
			}
			
			this.pickups.add(new Stop(this.area, this.date, id, "pickup",
				totalWeight, weightType, foodTypeWeights));
			this.totalPickupWeight += totalWeight;
		}
		
		this.dropoffs = new ArrayList<Stop>();
		this.totalDropoffWeight = 0;
		st = new StringTokenizer(stripQuotes("Beaufort Truck HHI,0;Delivered to SH HHI Office,0;Left on HHI Truck,0;Mercy Ministries (Rear of Hardeeville Thrift)  HHI  Delivery,1940"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer std = new StringTokenizer(st.nextToken(), ",");
			String id = std.nextToken();
			int totalWeight = Integer.parseInt(std.nextToken());
			String weightType = "pounds";
			int[] foodTypeWeights = { 0, 0, 0, 0, 0, 0 };
			this.dropoffs.add(new Stop(this.area, this.date, id, "dropoff",
				totalWeight, weightType, foodTypeWeights));
			this.totalDropoffWeight += totalWeight;
		}
		
		
		
		//all volunteers
		st = new StringTokenizer(stripQuotes("Bob Augustine;Bob Hynes;David Elow;David OConnor;Dick Daily;Kevin Daily*;Kevin McMahon*;Leigh Bullen*;Other*;Paul Crunkleton*;Paul Stillman*;Richard Whitmore*;Tom Sabatelle*"), ";");
		this.volunteers = new ArrayList<Volunteer>();
		
		while (st.hasMoreTokens()){
			Volunteer thisVol = new	 Volunteer(st.nextToken());
			
			this.volunteers.add(thisVol);
		}
		
		//all donors
				st = new StringTokenizer(stripQuotes("BiLo  Boundary Street (158);BiLo  Shell Point (525);Bimbo Bakery;Food Lion  Ladies Island (945);Food Lion  Laurel Bay (1698)"), ";");
				this.donors = new ArrayList<Client>();
				
				while (st.hasMoreTokens()){
					Client thisClient = new	 Client(st.nextToken(), "donor");
					
					this.donors.add(thisClient);
				}
		
				//all recipients
				st = new StringTokenizer(stripQuotes("AME Church;Bethel Deliverance Temple;Burton Wells  Senior Center;Canal Appartments;Cannan Baptist;CAPA;Church of God and Unity;CODA"), ";");
				this.recipients = new ArrayList<Client>();
				
				while (st.hasMoreTokens()){
					Client thisClient = new	 Client(st.nextToken(), "recipient");
					
					this.recipients.add(thisClient);
				}
				*/
		
	}
	
	
	// initialize class variables from a 4-line buffer (inBuffer or outBuffer)
	private void parseCSVfrom(String[] buffer)
	{
		StringTokenizer line1 = new StringTokenizer(stripQuotes(buffer[0]), ";");
		
		line1.nextToken(); // skip the file name
		this.areaName = line1.nextToken();
		this.fullDate = line1.nextToken();
		this.teamCaptain = line1.nextToken();
		this.teamCaptainPhone = line1.nextToken();
		
		if (line1.hasMoreTokens())
			this.startTime = line1.nextToken();
		
		StringTokenizer line2 = new StringTokenizer(stripQuotes(buffer[1]), ";");
		
		this.drivers = new ArrayList<String>();
		
		while (line2.hasMoreTokens())
			this.drivers.add(line2.nextToken());
		
		Collections.sort(drivers);
		
		this.pickups = new ArrayList<Stop>();
		this.totalPickupWeight = 0;
		
		StringTokenizer st = new StringTokenizer(stripQuotes(buffer[2]), ";");
		
		while (st.hasMoreTokens())
		{
			StringTokenizer std = new StringTokenizer(st.nextToken(), ",");
			String id = std.nextToken();
			int totalWeight = Integer.parseInt(std.nextToken());
			String weightType = "pounds";
			
			int[] foodTypeWeights = { 0, 0, 0, 0, 0, 0 };
			
			//Log.d("Homeplate", "stop id = " + id);
			
			if (std.hasMoreTokens())
			{
				weightType = "foodtype";
				for (int i = 0; i < 6; i++)
				{
					String nt = std.nextToken();
				//	Log.d("Homeplate", "stopDetails for foodtype " + nt);
					int j = nt.indexOf(":") + 1;
					foodTypeWeights[i] = Integer.parseInt(nt.substring(j));
				}
			}
			
			this.pickups.add(new Stop(this.area, this.date, id, "pickup",
				totalWeight, weightType, foodTypeWeights));
			this.totalPickupWeight += totalWeight;
		}
		
		
		this.dropoffs = new ArrayList<Stop>();
		this.totalDropoffWeight = 0;
		st = new StringTokenizer(stripQuotes(buffer[3]), ";");
		Log.d("Homeplate", "Reading drop off: " + stripQuotes(buffer[3]));
		while (st.hasMoreTokens())
		{
			
			//Log.d("Homeplate", "Reading drop off: " + stripQuotes(buffer[3]));
			
			StringTokenizer std = new StringTokenizer(st.nextToken(), ",");
			String id = std.nextToken();
			
			
			int totalWeight = Integer.parseInt(std.nextToken());
			String weightType = "pounds";
			int[] foodTypeWeights = { 0, 0, 0, 0, 0, 0 };
			this.dropoffs.add(new Stop(this.area, this.date, id, "dropoff",
				totalWeight, weightType, foodTypeWeights));
			this.totalDropoffWeight += totalWeight;
		}

		/*
		this.dropoffs = new ArrayList<Stop>();
		this.totalDropoffWeight = 0;
		st = new StringTokenizer(stripQuotes(buffer[3]), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer std = new StringTokenizer(st.nextToken(), ",");
			String id = std.nextToken();
			int totalWeight = Integer.parseInt(std.nextToken());
			String weightType = "pounds";
			int[] foodTypeWeights = { 0, 0, 0, 0, 0, 0 };
			this.dropoffs.add(new Stop(this.area, this.date, id, "dropoff",
				totalWeight, weightType, foodTypeWeights));
			this.totalDropoffWeight += totalWeight;
		}
		*/
		
		String volList = buffer[4];
		if(volList != null){
		 st = new StringTokenizer(stripQuotes(buffer[4]), ";");
		this.volunteers = new ArrayList<Volunteer>();
		
		while (st.hasMoreTokens()){
			Volunteer thisVol = new	 Volunteer(st.nextToken());
			
			this.volunteers.add(thisVol);
		}
		
		//all donors
				st = new StringTokenizer(stripQuotes(buffer[5]), ";");
				this.donors = new ArrayList<Client>();
				
				while (st.hasMoreTokens()){
					Client thisClient = new	 Client(st.nextToken(), "donor");
					
					this.donors.add(thisClient);
				}
		
				//all recipients
				st = new StringTokenizer(stripQuotes(buffer[6]), ";");
				this.recipients = new ArrayList<Client>();
				
				while (st.hasMoreTokens()){
					Client thisClient = new	 Client(st.nextToken(), "recipient");
					
					this.recipients.add(thisClient);
				}
		}
				
	}
	
	
	private String stripQuotes(String buffer)
	{
		String result = "";
		for (int i = 0; i < buffer.length(); i++)
			if (buffer.charAt(i) != '"')
				result += buffer.charAt(i);
		return result;
	}
	
	
	// getters for a Route object
	public String getDate()
	{
		return this.date;
	}
	
	
	public String getFullDate()
	{
		return this.fullDate;
	}
	
	
	public String getArea()
	{
		return this.area;
	}
	
	
	public String getAreaName()
	{
		return this.areaName;
	}
	
	
	public String getTeamCaptain()
	{
		return this.teamCaptain;
	}
	
	
	public String getTeamCaptainPhone()
	{
		return this.teamCaptainPhone;
	}
	
	
	public List<String> getDrivers()
	{
		return this.drivers;
	}
	
	
	public List<Stop> getPickups()
	{
		return this.pickups;
	}
	
	
	public Stop getPickup(String id)
	{
		for (int i = 0; i < this.pickups.size(); i++)
			if (this.pickups.get(i).getId().equals(id))
			{
				Log.d("Homeplate", "found a stop with id: " + id);
				return this.pickups.get(i);
			}
		return null;
	}
	
	
	public List<Stop> getDropoffs()
	{
		return this.dropoffs;
	}
	
	
	public Stop getDropoff(String id)
	{
		for (int i = 0; i < this.dropoffs.size(); i++)
			if (this.dropoffs.get(i).getId().equals(id))
				return this.dropoffs.get(i);
		return null;
	}
	
	
	public int getTotalPickupWeight()
	{
		totalPickupWeight = 0;
		
		for (Stop stop : pickups){
			int thisWeight = stop.getWeight();
			if(thisWeight > 0){
			totalPickupWeight += thisWeight;
			}
		}
		return this.totalPickupWeight;
	}
	
	
	public int getTotalDropoffWeight()
	{
		totalDropoffWeight = 0;
		
		for (Stop stop : dropoffs){
			int thisStopWeight =stop.getWeight();
			if(thisStopWeight > 0){
				totalDropoffWeight += thisStopWeight;
			}
		}
		return this.totalDropoffWeight;
	}
	
	
	public int getTotalWeightOnTruck()
	{
		return getTotalPickupWeight() - getTotalDropoffWeight();
	}
	
	public List<Client> getDonors(){
		return this.donors;
	}
	
	public List<Client> getRecipients(){
		return this.recipients;
	}
	
	public List<Volunteer> getVolunteers(){
		return this.volunteers;
	}
	
	// setters for a Route object, with side-effects on ftpin
	public void setDrivers(List<String> d)
	{
		this.drivers = d;
		rebuildFtpinBuffer(2);
		resetEndTime();
	}
	
	
	public void addDriver(String driverName){
		this.drivers.add(driverName);
		rebuildFtpinBuffer(2);
	}
	
	public void addStop(Stop stop)
	{
		if (stop == null)
			return;
		
		if (stop.getType().equals("pickup"))
		{
			pickups.add(stop);
			rebuildFtpinBuffer(3);
		}
		else
		{
			dropoffs.add(stop);
			rebuildFtpinBuffer(4);
		}
	}
	
	
	public void setPickup(Stop pickup)
	{
		for (int i = 0; i < this.pickups.size(); i++)
			if (this.pickups.get(i).getId() == pickup.getId())
			{
				this.pickups.set(i, pickup);
				rebuildFtpinBuffer(3);
				resetEndTime();
				break;
			}
	}
	
	
	public void setDropoff(Stop dropoff)
	{
		for (int i = 0; i < this.dropoffs.size(); i++)
			if (this.dropoffs.get(i).getId() == dropoff.getId())
			{
				this.dropoffs.set(i, dropoff);
				rebuildFtpinBuffer(4);
				resetEndTime();
				break;
			}
	}
	
	public void resetEndTime() {
		SimpleDateFormat dfDateTime = new SimpleDateFormat("HH:mm");
		this.endTime = dfDateTime.format(new Date());
		rebuildFtpinBuffer(1);
	}
	
	
	// build line 2, 3, or 4 of inBuffer and update ftpin
	// if line == 0 build all four lines of the buffer
	public void rebuildFtpinBuffer(int line)
	{
		if (line == 0 || line == 1)
		{
			// rebuild line 1
			inBuffer[0] = this.date + "-" + this.area + "-" + this.deviceId + ";" +
				this.areaName + ";" + this.fullDate + ";" + this.teamCaptain + ";" +
				this.teamCaptainPhone + ";" + this.startTime + ";" + this.endTime;
		}
		if (line == 0 || line == 2)
		{
			// rebuild line 2
			inBuffer[1] = "";
			
			if (this.drivers.size() > 0)
			{
				for (int i = 0; i < this.drivers.size(); i++)
				{
					String name = drivers.get(i);
					if (!(name.endsWith("*") | name.endsWith("#")))
						drivers.set(i, name + "*");
					inBuffer[1] += drivers.get(i) ;
					if (i < this.drivers.size() - 1)
						inBuffer[1] += ";";
				}
			}
		}
		if (line == 0 || line == 3)
		{
			// rebuild line 3
			inBuffer[2] = "";
			for (int i = 0; i < this.pickups.size(); i++)
			{
				inBuffer[2] += this.pickups.get(i).getId() + "," +
					this.pickups.get(i).getWeight();
				if (this.pickups.get(i).getWeightType() == "foodtype")
					for (int j = 0; j < 6; j++)
					{
						inBuffer[2] += "," + foodTypes[j] + ":" +
							this.pickups.get(i).getFoodTypeWeights()[j];
					}
				if (i < this.pickups.size() - 1)
					inBuffer[2] += ";";
			}
		}
		if (line == 0 || line == 4)
		{
			// rebuild line 4
			inBuffer[3] = "";
			for (int i = 0; i < this.dropoffs.size(); i++)
			{
				inBuffer[3] += this.dropoffs.get(i).getId() + "," +
					this.dropoffs.get(i).getWeight();
				if (i < this.dropoffs.size() - 1)
					inBuffer[3] += ";";
			}
		}
		
		if(line == 0 || line == 5){
			//rebuild line 5 volunteers
			inBuffer[4] = "";
			for(int i=0; i < this.volunteers.size(); i++){
				inBuffer[4] += this.volunteers.get(i).getFullName();
				if (i < this.volunteers.size() - 1)
					inBuffer[4] += ";";
			}
		}
		
		if(line == 0 || line == 6){
			//rebuild line 6 donors
			inBuffer[5] = "";
			for(int i=0; i < this.donors.size(); i++){
				inBuffer[5] += this.donors.get(i).getId();
				if (i < this.donors.size() - 1)
					inBuffer[5] += ";";
			}
		}
		
		if(line == 0 || line == 7){
			//rebuild line 7 recipients
			inBuffer[6] = "";
			for(int i=0; i < this.recipients.size(); i++){
				inBuffer[6] += this.recipients.get(i).getId();
				if (i < this.recipients.size() - 1)
					inBuffer[6] += ";";
			}
		}
		
		putFtpin(this.date, this.area, this.deviceId);
		Log.d("Homeplate", "ftpin file rebuilt " +
			 date + "-" + area + "-" +
			deviceId + ".csv");
	}
	
	
	// file retrieval and updating
	
	// get an array of 4 CSV lines from ftpin whose filename = this.area +
	// this.date + this.deviceId
	private boolean getFtpin(String date, String area, String deviceId)
	{
		boolean result = true;
		String storagePath = Environment.getExternalStorageDirectory() + "/homeplateftp/ftpin";
		String csvPath = storagePath + "/" + date + "-" + area + "-" + deviceId + ".csv";


		try
		{
			File myFile = new File(csvPath);

			/*
			File myFile = new File("/mnt/sdcard/homeplateftp/ftpin/" + date + "-" +
				area + "-" + deviceId + ".csv");
				*/
			/*
			File myFile = new File("/mnt/sdcard/homeplateftp/ftpin/" + date + "-" +
				area + "-" + deviceId + ".csv");

				*/
			Log.d("Homeplate", "reading data from file " + myFile);
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(
				new InputStreamReader(fIn));
			int i = 0;
			while (i < 7 && (inBuffer[i] = myReader.readLine()) != null)
			{
				i++;
			}
			if (i < 7) // didn't get all 4 lines
				result = false;
			myReader.close();
		}
		catch (Exception e)
		{
			result = false;
			Log.d("Homeplate", "cannot open input file " + csvPath);
			Log.d("Homeplate", e.toString());
		}
		return result;
	}
	
	
	// get an array of 4 CSV lines from ftpout whose filename = this.area +
	// this.date
	private boolean getFtpout(String date, String area)
	{
		boolean result = true;
		String storagePath = Environment.getExternalStorageDirectory() + "/homeplateftp/ftpout";
		String csvPath = storagePath + "/" +  date + "-" + area + ".csv";
		try
		{
			
			//String storagePath = "/mnt/sdcard/homeplateftp/ftpout/";



			File dir = new File(storagePath);
			if(!dir.exists()) {
				dir.mkdirs();
				Log.d("Homeplate", "Data path not found:" + csvPath);
			  
			}
			
			
			File myFile = new File(csvPath);
			/*
			File myFile = new File("/mnt/sdcard/homeplateftp/ftpout/" + date +
				"-" + area + ".csv");
			*/
			Log.d("Homeplate", "reading data from file " + myFile);
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(
				new InputStreamReader(fIn));
			int i = 0;
			while (i < 7 && (outBuffer[i] = myReader.readLine()) != null)
			{
				inBuffer[i] = outBuffer[i];
				i++;
			}
			if (i < 7) // didn't get all 4 lines
				result = false;
			myReader.close();
		}
		catch (Exception e)
		{
			result = false;
			Log.d("Homeplate", "cannot open input file " + csvPath);
			Log.d("Homplate", e.toString());
			

			
		}
		return result;
	}
	
	
	// write or update a record in ftpin whose filename is this.area + this.date
	// + this.device
	private boolean putFtpin(String date, String area, String deviceId)
	{
		boolean result = true;
		String storagePath = Environment.getExternalStorageDirectory() + "/homeplateftp/ftpin";
		String csvPath = storagePath + "/" +  date + "-" + area + "-" + deviceId + ".csv";
		try
		{
			
//TODO: consider dependency injection instead of hard coding paths
//String storagePath = "/mnt/sdcard/homeplateftp/ftpin/";

			/*
		File myFile = new File("data/homeplateftp/ftpin/" + date + "-" +
				area + "-" + deviceId + ".csv");
	*/


File dir = new File(storagePath);
if(!dir.exists()) {
	Log.d("Homeplate", "Data path not found. Creating:" + storagePath);
   dir.mkdirs();
}



			File myFile = new File(csvPath);
				/*
			File myFile = new File("/mnt/sdcard/homeplateftp/ftpin/" + date + "-" +
				area + "-" + deviceId + ".csv");

				*/
				
			Log.d("Homeplate", "writing data to file " + myFile);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myWriter = new OutputStreamWriter(fOut);
			int i = 0;
			while (i < 7)
			{
				myWriter.append(inBuffer[i] + "\n");
				i++;
			}
			myWriter.close();
		}
		catch (Exception e)
		{
			result = false;
			Log.d("Homeplate", "cannot open output file " + csvPath);
		}
		return result;
	}
	
}
