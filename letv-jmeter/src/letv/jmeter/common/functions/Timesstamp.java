package letv.jmeter.common.functions;

import java.util.Collection;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

public class Timesstamp extends AbstractFunction {
	private static final String KEY = "__Timesstamp";

	@Override
	public List<String> getArgumentDesc() {
		return null;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		//返回1分钟前的时间戳
		String timeStamp = String.valueOf(System.currentTimeMillis()/1000-60);
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