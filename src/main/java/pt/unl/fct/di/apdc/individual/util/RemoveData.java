package pt.unl.fct.di.apdc.individual.util;

public class RemoveData {
	
	private String username;
	private String token;
	private String userToRemove;
	
	public RemoveData() {}
	
	public RemoveData(String username, String token, String userToRemove) {
		this.username = username;
		this.token = token;
		this.userToRemove = userToRemove;
	}

	public String getUsername() {
		return username;
	}

	public String getToken() {
		return token;
	}

	public String getUserToRemove() {
		return userToRemove;
	}

}
