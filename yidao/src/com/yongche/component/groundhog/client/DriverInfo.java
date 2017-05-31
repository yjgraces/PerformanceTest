package com.yongche.component.groundhog.client;

public class DriverInfo {
	//司机ID
	private long userid;
	//设备号
	private long deviceId;
	//用户类型 默认都是DR
	private String userType;
	//TOKEN
	private String token;
	//车号
	private String carId;	
	//imei号
	private String Imei;
	//Access_token
	private String Access_token;
	//Oauth_token
	private String Oauth_token;
	//Oauth_token_secret
	private String Oauth_token_secret;
	
	
//	tokenInfo.setImei(values[0]);
//	tokenInfo.setAccess_token(values[1]);
//	tokenInfo.setOauth_token(values[2]);	
//	tokenInfo.setOauth_token_secret(values[3]);
	
	public String getImei() {
		return Imei;
	}

	public void setImei(String imei) {
		Imei = imei;
	}

	public String getAccess_token() {
		return Access_token;
	}

	public void setAccess_token(String access_token) {
		Access_token = access_token;
	}

	public String getOauth_token() {
		return Oauth_token;
	}

	public void setOauth_token(String oauth_token) {
		Oauth_token = oauth_token;
	}

	public String getOauth_token_secret() {
		return Oauth_token_secret;
	}

	public void setOauth_token_secret(String oauth_token_secret) {
		Oauth_token_secret = oauth_token_secret;
	}


	public static String test;
	

	public String getCarId() {
		return carId;
	}

	public void setCarId(String carId) {
		this.carId = carId;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
