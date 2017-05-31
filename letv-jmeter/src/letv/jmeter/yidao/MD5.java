package letv.jmeter.yidao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

/**
 * return MD5 value of string
 * 
 * @author luoyanying
 *
 */
public class MD5 extends AbstractFunction {

	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__MD5";

	static {
		desc.add("source string");
	}

	private CompoundVariable src;

	@Override
	public List<String> getArgumentDesc() {
		return desc;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		String srcValue = src.execute().trim();
		return getMD5String(srcValue);
	}

	/**
	 * 生成字符串的md5校验值
	 * 
	 * @param s
	 * @return
	 */
	public String getMD5String(String s) {
		return getMD5String(s.getBytes());
	}

	public String getMD5String(byte[] bytes) {
		try {
			MessageDigest messagedigest;
			messagedigest = MessageDigest.getInstance("MD5");
			messagedigest.update(bytes);
			return binaryToHex(messagedigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String binaryToHex(byte[] binary) {
		String HEXES = "0123456789abcdef";

		if (binary == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * binary.length);
		for (byte b : binary) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	@Override
	public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
		checkParameterCount(parameters, 1);
		Object[] values = parameters.toArray();
		src = (CompoundVariable) values[0];
	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

}
