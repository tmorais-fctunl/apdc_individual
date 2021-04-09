package pt.unl.fct.di.apdc.individual.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordData {
	
	private static final String regexPwdTramada = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}";
	
	private String username;
	private String password;
	private String newPassword;
	private String confirmNewPassword;
	
	public ChangePasswordData() {}
	
	public ChangePasswordData(String username, String password, String newPassword, String confirmNewPassword) {
		this.username = username;
		this.password = password;
		this.confirmNewPassword = confirmNewPassword;
		this.newPassword = newPassword;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}
	
	public Boolean validPassword() {
		if (password==null | newPassword==null| confirmNewPassword==null| !newPassword.equals(confirmNewPassword))
			return false;
		else {
			Pattern patternPwd = Pattern.compile(regexPwdTramada);
			Matcher matcherPwd = patternPwd.matcher(newPassword);
			return matcherPwd.matches();
		}
	}
	
	

}
