package org.sh.homeplate.data;

public class Stop
{

	private String area;
	private String date;
	private String id;    // name of the client
	private String type;  // 'pickup' or 'dropoff'
	private int totalWeight;
	private String weightType; //legacy was  'pounds' or 'foodtype' now only foodtype used
	private int[] foodTypeWeights; // array of 6 weights Meat, Frozen, Bakery, Grocery, Dairy, Produce
	private static final String[] foodTypes =
	{ "Meat", "Frozen", "Bakery", "Grocery", "Dairy", "Produce" };


	// constructor returns a Stop for a particular client
	public Stop(String area, String date,
			String id, String type, int totalWeight,
			String weightType, int[] foodTypeWeights)
	{
		this.area = area;
		this.date = date;
		this.id = id;
		this.type = type;
		this.totalWeight = totalWeight;
		
		//pick ups (aka donors) are always by foodtype
		if (this.type == "pickup"){
			this.weightType = "foodtype"; // weightType;
		}
		else{
			this.weightType = weightType;
		}
		
		this.foodTypeWeights = foodTypeWeights;
	}


	// getters for a Stop object

	public String getArea()
	{
		return this.area;
	}


	public String getDate()
	{
		return this.date;
	}


	public String getId()
	{
		return this.id;
	}


	public String getType()
	{
		return this.type;
	}


	public int getWeight()
	{
		return this.totalWeight;
	}


	public String getWeightType()
	{
		return this.weightType;
	}


	public int[] getFoodTypeWeights()
	{
		return this.foodTypeWeights;
	}


	public static String[] getFoodTypes()
	{
		return foodTypes;
	}


	// setters for a Stop object, with side-effects on ftpin
	// sets the total weight
	public void setWeight(Route r, int weight)
	{
		this.totalWeight = weight;
		if (this.type == "pickup")
			r.setPickup(this);
		else
			r.setDropoff(this);
	}


	// sets the individual food type weights and recalcuates the total weight
	public void setFoodTypeWeight(Route r, String foodType, int weight)
	{
		this.totalWeight = 0;
		for (int i = 0; i < 6; i++)
		{
			if (foodType == foodTypes[i])
				this.foodTypeWeights[i] = weight;
			this.totalWeight += this.foodTypeWeights[i];
		}
		r.setPickup(this);
	}

}
