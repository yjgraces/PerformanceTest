package letv.jmeter.props.functions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import letv.jmeter.common.functions.MD5;

/**
 * 奇葩的签名算法
 * 
 * @author luoyanying
 * @date 2016年5月5日
 */
public class PropsSign extends AbstractFunction {
	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__LetvPropsSign";
	private CompoundVariable body;
	private CompoundVariable key;

	static {
		desc.add("body:GET|POST+k=v...");
		desc.add("private key");
	}

	@Override
	public List<String> getArgumentDesc() {
		return desc;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		String bodyStr = body.execute().trim();
		String keyStr = key.execute().trim();
		MD5 md5 = new MD5();

		try {
			return md5.getMD5String(URLEncoder.encode(bodyStr, "utf-8") + keyStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
		checkParameterCount(parameters, 2);
		Object[] values = parameters.toArray();
		body = (CompoundVariable) values[0];
		key = (CompoundVariable) values[1];
	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

}
