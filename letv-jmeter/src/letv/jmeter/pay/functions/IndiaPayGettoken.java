package letv.jmeter.pay.functions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;

import kraken.utils.StringTool;
import letv.jmeter.common.functions.MD5;

public class IndiaPayGettoken extends AbstractFunction {
	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__LetvIndiapayGettoken";
	private CompoundVariable mkey;

	static {
		desc.add(JMeterUtils.getResString("merchant key"));
	}

	@Override
	public List<String> getArgumentDesc() {
		return desc;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		String tradeno = "1" + StringTool.getRandNumber(12);
		String userid = "18" + StringTool.getRandNumber(8);
		//String timestamp = String.valueOf(System.currentTimeMillis());
		String url = String.format("address={\"email\":\"letvboss@letv.com\",\"shiptophonenum\":\"18614021716\"}&call_back_url=http://www.baidu.com&currency=INR&extend_info={}&ip=127.0.0.1&localecode=IN&merchant_business_id=801&merchant_no=%s&notify_url=http://www.letv.com&out_trade_no=%s&pay_expire=10&price=20&product=[{\"category\":\"mobilephone\",\"desc\":\"X60S\",\"id\":\"1\",\"name\":\"X60S\",\"price\":\"10\",\"quantity\":\"1\",\"sku\":\"12\",\"url\":\"http://www.lemall.com/product/products-pid-1000264.html\"}]&service=lepay.pc.api.show.cashier.india&sign_type=MD5&use_paytype=[{\"channel_id\":101,\"paytypes\":[\"debit_card\",\"credit_card\",\"netbanking\",\"emi\",\"cash_card\"]},{\"channel_id\":105,\"paytypes\":[\"cod\"]}]&user_id=%s&user_name=letvboss&version=1.0", tradeno, tradeno, userid);

		MD5 md5 = new MD5();
		return url + "&sign=" + md5.getMD5String(url + "&key=" + mkey.execute().trim());

	}

	@Override
	public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
		checkParameterCount(parameters, 1);
		Object[] values = parameters.toArray();
		mkey = (CompoundVariable) values[0];
	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

}
