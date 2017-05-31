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

public class RupayGettoken extends AbstractFunction {
	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__LetvRupayGettoken";
	private Object[] values;

	static {
		desc.add("merchant key");
		desc.add("service");
		desc.add("bussiness id");
		desc.add("currency");//货币种类，paypal是USD，qiwi是RUB
	}

	@Override
	public List<String> getArgumentDesc() {
		return desc;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		String tradeno = "1" + StringTool.getRandNumber(12);
		String userid = "18" + StringTool.getRandNumber(8);
		String service = ((CompoundVariable) values[1]).execute().trim();
		String bussinessId = ((CompoundVariable) values[2]).execute().trim();
		String currency = ((CompoundVariable) values[3]).execute().trim();
		String url = String.format(
				"address={\"email\":\"lilixin@letv.com\",\"gift_wrap\":\"false\",\"shiptocity\":\"shengbidebao\",\"shiptocountrycode\":\"RU\",\"shiptomethod\":\"twoday\",\"shiptoname\":\"kakaka\",\"shiptophonenum\":\"692941\",\"shiptoprovince\":\"shengbidebao\",\"shiptostreet\":\"street\",\"shiptozip\":\"90230\"}&call_back_url=http://www.baidu.com&companyurl=http://www.lemall.com/us&currency=%s&extend_info={\"is_auth\":\"true\",\"is_create_token\":\"true\",\"pre_step_link\":\"http://www.google.cn\"}&insuranceamt=2&ip=127.0.0.1&itemamt=4&language=3&localecode=US&logourl=http://us.img1.lemall.com/file/20160112/default/14077201403149139&merchant_business_id=%s&merchant_no=%s&notify_url=http://10.183.225.131/&out_trade_no=%s&pay_expire=10&price=10&product=[{\"category\":\"mobilephone\",\"desc\":\"X60S\",\"id\":\"1\",\"img1X1\":\"http://a4.att.hudong.com/20/31/20300000929429130502315466044.jpg\",\"name\":\"X60S\",\"price\":\"4\",\"quantity\":\"1\",\"sku\":\"12\",\"url\":\"http://www.lemall.com/product/products-pid-1000264.html\"}]&service=%s&shippingamt=3&sign_type=MD5&taxamt=1&user_id=%s&user_name=139923536&version=1.0",
				currency, bussinessId, tradeno, tradeno, service, userid);

		MD5 md5 = new MD5();
		return url + "&sign=" + md5.getMD5String(url + "&key=" + ((CompoundVariable) values[0]).execute().trim());
	}

	@Override
	public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
		checkParameterCount(parameters, 4);
		values = parameters.toArray();
	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

}
