package de.shellfire.vpn.webservice.model;

public class RegisterRequest {

	private int newsletter;
	private String password;
	private String email;
	private String language;
	private int resend;

	public RegisterRequest(String lang, String email, String password, int subscribeToNewsletter, int resend) {
		this.language = lang;
		this.email = email;
		this.password = password;
		this.newsletter = subscribeToNewsletter;
		this.resend = resend;
	}

	public int getSubscribeToNewsletter() {
		return newsletter;
	}

	public void setSubscribeToNewsletter(int subscribeToNewsletter) {
		this.newsletter = subscribeToNewsletter;
	}

	public int getResend() {
		return resend;
	}

	public void setResend(int resend) {
		this.resend = resend;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLang() {
		return language;
	}

	public void setLang(String lang) {
		this.language = lang;
	}

}
