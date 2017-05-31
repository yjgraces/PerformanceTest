package com.yongche.component.groundhog.client;

public class HttpClient {

	public static void main(String[] args) {
		String res=HttpUtil.getIntance().get("http://www.baidu.com/");
		System.out.println(res);
	}

}
