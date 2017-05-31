package letv.jmeter.pay.functions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import kraken.utils.StringTool;
import letv.jmeter.common.functions.MD5;

/**
 * 获取token
 * 
 * @author luoyanying
 *
 */
public class UspayGettoken extends AbstractFunction {
	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__LetvUspayGettoken";
	private Object[] values;

	static {
		desc.add("merchant key");
		desc.add("service");
	}

	@Override
	public List<String> getArgumentDesc() {
		return desc;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		String tradeno = "1" + StringTool.getRandNumber(12);
		String userid = "18" + StringTool.getRandNumber(8);
		String service=((CompoundVariable)values[1]).execute().trim();
		// String timestamp=StringTool.getDateStr1();
		String url = String.format(
				"address={\"email\":\"lilixin@letv.com\",\"gift_wrap\":\"false\",\"shiptocity\":\"CULVER CITY\",\"shiptocountrycode\":\"CN\",\"shiptomethod\":\"twoday\",\"shiptoname\":\"kakaka\",\"shiptophonenum\":\"18614021716\",\"shiptoprovince\":\"CA\",\"shiptostreet\":\"4114 Sepulveda Blvd.\",\"shiptozip\":\"90230\"}&call_back_url=http://www.baidu.com&companyurl=http://www.lemall.com/us&currency=USD&extend_info={\"is_auth\":\"true\",\"is_create_token\":\"true\",\"pre_step_link\":\"http://www.google.cn\"}&insuranceamt=0.0&ip=127.0.0.1&itemamt=0.01&localecode=US&logourl=http://us.img1.lemall.com/file/20160112/default/14077201403149139&merchant_business_id=809&merchant_no=%s&notify_url=http://www.letv.com&out_trade_no=%s&pay_expire=10&price=0.03&product=[{\"category\":\"mobilephone\",\"desc\":\"X60S\",\"id\":\"1\",\"name\":\"X60S\",\"price\":\"0.01\",\"quantity\":\"1\",\"sku\":\"12\",\"url\":\"http://www.lemall.com/product/products-pid-1000264.html\"}]&service=%s&shippingamt=0.01&sign_type=MD5&taxamt=0.01&user_id=%s&user_name=66433&version=1.0",
				tradeno, tradeno, service,userid);

		MD5 md5 = new MD5();
		return url + "&sign=" + md5.getMD5String(url + "&key=" + ((CompoundVariable)values[0]).execute().trim());

	}

	@Override
	public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
		checkParameterCount(parameters, 2);
		values = parameters.toArray();
	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

}
