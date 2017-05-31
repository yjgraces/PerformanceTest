package letv.jmeter.common.functions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * 返回指定的字符串值，并保存到变量里
 * 
 * @author luoyanying
 * @date 2016年5月6日
 */
public class GetAndSaveString extends AbstractFunction {
	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__GetAndSaveString";

	static {
		desc.add("value of String");
		desc.add("name of variable in which store the value of string");
	}

	private CompoundVariable[] values;

	@Override
	public List<String> getArgumentDesc() {
		return desc;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		String myValue = values[0].execute().trim();
		String myName = values[1].execute().trim();

		if (myValue.equals(null) || myName.equals(null)) {
			try {
				throw new Exception("Name or Value is null!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		JMeterVariables vars = getVariables();
		if (vars != null) {
			vars.put(myName, myValue);
		}

		return myValue;
	}

	@Override
	public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
		checkParameterCount(parameters, 2);
		values = parameters.toArray(new CompoundVariable[2]);
	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

}
