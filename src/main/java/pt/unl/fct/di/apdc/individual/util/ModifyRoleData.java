package pt.unl.fct.di.apdc.individual.util;

public class ModifyRoleData {
	
	private String role;
	private String username;
	private String userToChange;
	private String token;
	
	public ModifyRoleData() {}
	
	public ModifyRoleData(String username, String token, String userToChange, String role) {
		
		this.role = role;
		this.username = username;
		this.userToChange = userToChange;
		this.token = token;
		
	}

	public String getRole() {
		return role;
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
