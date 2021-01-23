package org.sh.homeplate.data;

public class Client {
	private String id;
	private String type;
	
	public Client(String clientId, String type){
		this.id = clientId;
		this.type = type;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public String getType()
	{
		return this.type;
	}
}
