package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Venue {
	private Lines address;
	private City city;
	private State state;
	private String postalCode;
	
	public Lines getAddress() {
		return address;
	}
	public City getCity() {
		return city;
	}
	public State getState() {
		return state;
	}
	public String getPostalCode() {
		return postalCode;
	}
}
