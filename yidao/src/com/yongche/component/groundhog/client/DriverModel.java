package com.yongche.component.groundhog.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DriverModel {
	// 司机信息列表
	static ArrayList<DriverInfo> driverList = new ArrayList<DriverInfo>();
	// 事务数量
	static AtomicInteger transactions;
	// 响应时间列表
	private static ArrayList<Double> responseTime;
	static Object syncObj;
	static Map<String, String> properties;
	static ExecutorService fixedThreadPool;

	public static void main(String[] args) {
		init();

		// 统计TPS，3秒一次
		// new Thread(new Runnable() {
		// public void run() {
		// try {
		// statistics();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// }).run();
		
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				try {
					statistics();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		final String[] hosts = { getProperty("driverhost") };
		// DriverLogin login = new DriverLogin("DR", 50057934, 50005145,
		// "df6d2338b2b8fce1ec2f6dda0a630eb0", hosts1);
		for (final DriverInfo driverInfo : driverList) {
			// new Thread(new DriverLogin(driverInfo, hosts)).run();
			fixedThreadPool.execute(new DriverLogin(driverInfo, hosts));
			// DriverLogin login = new DriverLogin(driverInfo, hosts);
			// login.run();
		}
	}

	private static void init() {
		try {
			initProperties();
			fixedThreadPool = Executors.newFixedThreadPool(Integer.valueOf(getProperty("threadcount")));
			transactions = new AtomicInteger(0);
			responseTime = new ArrayList<Double>(3000);
			syncObj = new Object();
			File file = new File("driverinfo.dat");
			FileReader reader = new FileReader(file);
			@SuppressWarnings("resource")
			BufferedReader buffer = new BufferedReader(reader);
			String line;
			while ((line = buffer.readLine()) != null) {
				DriverInfo driverInfo = new DriverInfo();
				String[] values = line.split(",");
				driverInfo.setUserid(Long.parseLong(values[0]));
				driverInfo.setDeviceId(Long.parseLong(values[1]));
				driverInfo.setUserType(values[2]);
				driverInfo.setToken(values[3]);
				driverInfo.setCarId(values[4]);
				driverInfo.setImei(values[5]);
				driverInfo.setAccess_token(values[6]);
				driverInfo.setOauth_token(values[7]);
				driverInfo.setOauth_token_secret(values[8]);
				driverList.add(driverInfo);
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("driverlist长度："+driverList.size());
	}

	private static void initProperties() throws IOException {
		properties = new HashMap<String, String>();
		Properties p = new Properties();
		FileReader reader = new FileReader(new File("env.properties"));
		p.load(reader);
		Set<Object> keySet = p.keySet();
		for (Object object : keySet) {
			String key = (String) object;
			properties.put(key, p.getProperty(key));
		}
	}

	public static String getProperty(String key) {
		return properties.get(key);
	}

	public static void addResTime(double time) {
		synchronized (syncObj) {
			responseTime.add(time);
		}
	}

	public static double getResTime() {
		double resTime = 0;
		if (responseTime.size() == 0) {
			return 0.00;
		}
		synchronized (syncObj) {
			int size = responseTime.size();
			if (size > 0) {
				for (int i = 0; i < size; i++) {
					resTime += responseTime.get(i);
				}
			}
			resTime = resTime / size;
			responseTime.clear();
		}
		return resTime;
	}

	/**
	 * 统计TPS和平均响应时间
	 * 
	 * @throws InterruptedException
	 */
	private static void statistics() throws InterruptedException {
		// 延迟5秒再统计
		Thread.sleep(5000);
		//int interval = 60;
		int interval = 5;
		while (true) {
			StringBuilder sb = new StringBuilder();
			double recCount = Double.valueOf(transactions.get());
			transactions.set(0);
			double resTime = DriverModel.getResTime();
			DecimalFormat df = new DecimalFormat("#0.00");
			sb.append("Request in last " + interval + "s:" + recCount);
			sb.append("  ");
			sb.append("Current tps:" + df.format(recCount / interval));
			sb.append("  ");
			sb.append("Average response time(ms):" + df.format(resTime));
			System.out.println(sb.toString());
			try {
				Thread.sleep(1000 * interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
