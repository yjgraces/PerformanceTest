package letv.jmeter.common.functions;

import java.util.Collection;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

/**
 * 返回1分钟之前的时间戳 
 * @author luoyanying
 * @date 2016年10月31日
 */
public class TimestampLater extends AbstractFunction {
	private static final String KEY = "__TimestampLater";

	@Override
	public List<String> getArgumentDesc() {
		return null;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		//返回1分钟前的时间戳
	//	String timeStamp = String.valueOf(System.currentTimeMillis()/1000-60);
		//返回当前时间戳
		String timeStamp = String.valueOf(System.currentTimeMillis()/1000+1000);
		return timeStamp;
	}

	@Override
	public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {

	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

}
