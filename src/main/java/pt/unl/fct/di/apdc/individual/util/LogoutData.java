package pt.unl.fct.di.apdc.individual.util;

public class LogoutData {
	
	private String username;
	private String token;
	
	public LogoutData() {}
	
	public LogoutData(String username, String token) {
		
		this.username = username;
		this.token = token;
		
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getToken() {
		return token;
	}

}
