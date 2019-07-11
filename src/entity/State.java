package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class State {
	private String name;
	private String stateCode;

	public String getName() {
		return name;
	}
	public String getStateCode() {
		return stateCode;
	}
}
