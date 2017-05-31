package com.yongche.component.groundhog.client;

public class TokenInfo {
	//设备IMEI
	private String imei;
	// Access_token
	private String Access_token;
	//oauth_token
	private String oauth_token;
	//oauth_token_secret
	private String oauth_token_secret;	
	//oauth_consumer_key
	private String oauth_consumer_key;	
	//oauth_signature
	private String 	oauth_signature;
	

	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getAccess_token() {
		return Access_token;
	}
	public void setAccess_token(String access_token) {
		Access_token = access_token;
	}
	public String getOauth_token() {
		return oauth_token;
	}
	public void setOauth_token(String oauth_token) {
		this.oauth_token = oauth_token;
	}
	public String getOauth_token_secret() {
		return oauth_token_secret;
	}
	public void setOauth_token_secret(String oauth_token_secret) {
		this.oauth_token_secret = oauth_token_secret;
	}
	public String getOauth_consumer_key() {
		return oauth_consumer_key;
	}
	public void setOauth_consumer_key(String oauth_consumer_key) {
		this.oauth_consumer_key = oauth_consumer_key;
	}
	public String getOauth_signature() {
		return oauth_signature;
	}
	public void setOauth_signature(String oauth_signature) {
		this.oauth_signature = oauth_signature;
	}

}