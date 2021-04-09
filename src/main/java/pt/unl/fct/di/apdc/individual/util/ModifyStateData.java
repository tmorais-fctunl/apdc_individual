package pt.unl.fct.di.apdc.individual.util;

public class ModifyStateData {
	
	private String state;
	private String username;
	private String userToChange;
	private String token;
	
	public ModifyStateData() {}
	
	public ModifyStateData(String username, String token, String userToChange, String state) {
		
		this.state = state;
		this.username = username;
		this.userToChange = userToChange;
		this.token = token;
		
	}

	public String getState() {
		return state;
	}

	public String getUsername() {
		return username;
	}

	public String getUserToChange() {
		return userToChange;
	}

	public String getToken() {
		return token;
	}

}
