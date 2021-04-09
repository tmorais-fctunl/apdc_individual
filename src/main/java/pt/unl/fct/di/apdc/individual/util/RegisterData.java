package pt.unl.fct.di.apdc.individual.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;

public class RegisterData {
	
	private String username;
	private String password;
	private String email;
	private String confirmPassword;
	private static final String regexPwdTramada = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}";
	private static final String regexEmail = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

	
	public RegisterData() {}
	
	public RegisterData(String username, String password, String email, String confirmPassword) {
		this.username = username;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.email = email;
	}
	
	public boolean validRegistration() {
		if (username==null | password==null | confirmPassword==null | !password.equals(confirmPassword) | email==null)
			return false;
		
		Pattern patternEmail = Pattern.compile(regexEmail);
		Pattern patternPwd = Pattern.compile(regexPwdTramada);
		Matcher matcherEmail = patternEmail.matcher(email);
		Matcher matcherPwd = patternPwd.matcher(password);
		if (!matcherEmail.matches() | !matcherPwd.matches())
			return false;
		return true;
		
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getConfirmPassword() {
		return confirmPassword;	
	}
	
	
	
	


}
