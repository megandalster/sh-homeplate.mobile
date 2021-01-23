package org.sh.homeplate.data;

public class Volunteer {

	private String full_name;
	/*
	private String id;
	private String last_name;
	private String first_name;
	*/
	
	public Volunteer(String fullName){
		this.full_name = fullName;
	}
	
	public String getFullName(){
		return this.full_name;
	}
	
	/*
	public String getId()
	{
		return this.id;
	}
	
	public String getLastName()
	{
		return this.last_name;
	}
	
	public String getFirstName()
	{
		return this.first_name;
	}
	*/
}
