package pt.unl.fct.di.apdc.individual.util;

import java.util.UUID;

public class AuthToken {
	
	private static final long EXPIRATION_TIME = 1000*60*60*2;
	
	private String username;
	private String role;
	private String tokenID;
	private long creationData;
	private long expirationData;
	
	
	public AuthToken(String username, String role) {
		this.username = username;
		this.role = role;
		this.tokenID = UUID.randomUUID().toString();
		this.creationData = System.currentTimeMillis();
		this.expirationData = System.currentTimeMillis()+AuthToken.EXPIRATION_TIME;
	}
	
	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public String getTokenID() {
		return tokenID;
	}

	public long getCreationData() {
		return creationData;
	}

	public long getExpirationData() {
		return expirationData;
	}

}
