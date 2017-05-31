package letv.jmeter.demo;

import letv.jmeter.common.functions.MD5;

public class MD5Demo {
	public static void main(String[] args) {
		MD5 md5 = new MD5();
		String str = md5.getMD5String("bizId=27&bizNo=201626980351&itemNo=106&vrCurrency=1001&userId=1644590437&terminal=1200&KEY=89889d7db08c1a5552f220ff93be3887");
		System.out.println(str);
	}
}
