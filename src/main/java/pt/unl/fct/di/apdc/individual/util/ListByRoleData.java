package pt.unl.fct.di.apdc.individual.util;

public class ListByRoleData {
	
	private String username;
	private String token;
	private String role;
	
	public ListByRoleData() {}
	
	public ListByRoleData(String username, String token, String role) {
		this.username = username;
		this.token = token;
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public String getToken() {
		return token;
	}

	public String getRole() {
		return role;
	}

}
