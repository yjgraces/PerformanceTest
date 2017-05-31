package letv.jmeter.pay.functions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.json.JSONException;
import org.json.JSONObject;

public class UspayJson extends AbstractFunction {
	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__LetvUspayJson";
	private CompoundVariable json;

	static {
		desc.add("origin json");
	}

	@Override
	public List<String> getArgumentDesc() {
		return desc;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		try {
			String orginJson = json.execute().trim();
			JSONObject jobj = new JSONObject(orginJson);
			jobj.put("card_type", "001");
			jobj.put("card_number", "4392260805971569");
			jobj.put("card_expiry_date", "06-2020");
			jobj.put("card_cvn", "122");
			return jobj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
		checkParameterCount(parameters, 1);
		Object[] values = parameters.toArray();
		json = (CompoundVariable) values[0];
	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

}
