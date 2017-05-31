package letv.jmeter.yidao.functions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * LBS距离预估接口拼参数 文件名固定为estimate.csv,放bin目录下
 * 
 * @author luoyanying
 * @date 2016年10月8日
 */
public class LBSEstimate extends AbstractFunction {
	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__YidaoLbsEstimate";
	static List<String[]> location = new LinkedList<String[]>();
	static Object lock = new Object();

	static {
		synchronized (lock) {
			if (location.size() == 0) {
				try {
					readF1();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public List<String> getArgumentDesc() {
		return desc;
	}

	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		String content1 = "{\"service_order_id\":1,\"in_coord_type\": \"world\",\"origins\":[";
		String content2 = "\"destination\":{ \"lat\": 39.9928852933,\"lng\": 116.3377745980 },\"provider\":\"amap\"}";
		Random rdm = new Random();
		int n = 100;
		for (int i = 0; i < n; i++) {
			String[] loc = location.get(rdm.nextInt(location.size()));
			if (i == n - 1) {
				content1 += "{\"driver_id\":" + (i + 1) + ",\"lat\":" + loc[1] + ",\"lng\":" + loc[2] + "}],";
			} else {
				content1 += "{\"driver_id\":" + (i + 1) + ",\"lat\":" + loc[1] + ",\"lng\":" + loc[2] + "},";
			}
		}
		// System.out.println(origins);
		content1 += content2;
		// System.out.println(content1);
		return content1;
	}

	@Override
	public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
		checkParameterCount(parameters, 0, 0);
	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

	public static final void readF1() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("estimate.csv")));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] lineArray = line.split(",");
			location.add(lineArray);
		}
		br.close();
	}

}
