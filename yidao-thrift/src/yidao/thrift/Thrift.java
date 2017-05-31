package yidao.thrift;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.yongche.LocationServiceThriftClient;

public class Thrift {
	static List<DriverInfo> driverList1 = new LinkedList<DriverInfo>();
	static List<DriverInfo> driverList2 = new LinkedList<DriverInfo>();
	static int SWITCH;
	static ExecutorService fixedThreadPool;
	static Map<String, String> properties;

	static {
		try {
			initProperties();
			init();
			fixedThreadPool = Executors.newFixedThreadPool(Integer.parseInt(getProperty("threadcount")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SWITCH = Integer.parseInt(getProperty("uploadmode"));
		if ((SWITCH & 1) == 1) {
			// for (final DriverInfo driverInfo1 : driverList1) {
			// final String[] IpPort = setIPAndPort(driverInfo1.getUserid());

			// 上传位置
			// MODE 1 改为1个线程单发所有司机坐标，间隔3秒。
			fixedThreadPool.execute(new Runnable() {
				public void run() {
					while (true) {
						for (final DriverInfo driverInfo1 : driverList1) {
							final String[] IpPort = setIPAndPort(driverInfo1.getUserid());
							LocationServiceThriftClient client = new LocationServiceThriftClient(IpPort[0], Integer.parseInt(IpPort[1]));
							try {
								client.open();
//								 double lat = Double.valueOf("38.772685");
//								 double lon = Double.valueOf("82.681054");
								 double lat = Double.valueOf("39.990"+RandomNum(3));
								 double lon = Double.valueOf("116.314"+RandomNum(3));
//								double lat = Double.valueOf("38.77" + RandomNum12(1) + RandomNum(3));
//								double lon = Double.valueOf("82.683" + RandomNum(3));
								client.add(String.valueOf(driverInfo1.getUserid()), lat, lon);
								System.out.println(lat + "---" + lon + "drivers thrift updated success. IP:" + IpPort[0] + ":" + IpPort[1] + "  driverID:" + String.valueOf(driverInfo1.getUserid()));

							} catch (TTransportException e) {
								e.printStackTrace();
							} catch (TException e) {
								e.printStackTrace();
							} finally {
								client.close();
							}
						}
						try {
							System.out.println("=========all " + driverList1.size() + " drivers updeted==========");
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}

		if ((SWITCH & 2) == 2) {
			for (final DriverInfo driverInfo : driverList2) {
				final String[] IpPort = setIPAndPort(driverInfo.getUserid());

				fixedThreadPool.execute(new Runnable() {

					@Override
					public void run() {
						try {
							LocationServiceThriftClient client = new LocationServiceThriftClient(IpPort[0], Integer.parseInt(IpPort[1]));
							client.open();
							while (true) {
								long start = System.currentTimeMillis();
								// for (DriverInfo driverInfo : driverList2) {
								client.add(String.valueOf(driverInfo.getUserid()), Double.valueOf("38.297" + RandomNum(3)), Double.valueOf("97.584" + RandomNum(3)));
								// }
								long end = System.currentTimeMillis();
								System.out.println("drivers 2 updated in " + (end - start) + " ms");
								Thread.sleep(1000);
							}
						} catch (TTransportException e) {
							e.printStackTrace();
						} catch (TException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}

	}

	public static void init() {
		try {
			SWITCH = Integer.parseInt(getProperty("uploadmode"));

			if ((SWITCH & 1) == 1) {
				File file = new File("driverinfo1.dat");
				FileReader reader = new FileReader(file);
				BufferedReader buffer = new BufferedReader(reader);
				String line;
				while ((line = buffer.readLine()) != null) {
					DriverInfo driverInfo = new DriverInfo();
					String[] values = line.split(",");
					driverInfo.setUserid(Long.parseLong(values[0]));
					driverList1.add(driverInfo);
				}
				reader.close();
			}

			if ((SWITCH & 2) == 2) {
				File file2 = new File("driverinfo2.dat");
				FileReader reader2 = new FileReader(file2);
				BufferedReader buffer2 = new BufferedReader(reader2);
				String line2;
				while ((line2 = buffer2.readLine()) != null) {
					DriverInfo driverInfo = new DriverInfo();
					String[] values = line2.split(",");
					driverInfo.setUserid(Long.parseLong(values[0]));
					driverList2.add(driverInfo);
				}
				reader2.close();
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void initProperties() throws IOException {
		properties = new HashMap<String, String>();
		Properties p = new Properties();
		FileReader reader = new FileReader(new File("thrift.properties"));
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

	public static String RandomNum(int length) {
		String base = "0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public static String RandomNum12(int length) {
		String base = "12";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	private static String[] setIPAndPort(long userId) {
		String[] ipAndPort = new String[2];

		if (Integer.parseInt(getProperty("testmode")) == 1) {
			ipAndPort[0] = getProperty("testIP");
			if (userId % 2 == 0) 
			{
				ipAndPort[1] = getProperty("port2");
			} else {
				ipAndPort[1] = getProperty("port1");
			}
		} else {
			if (userId % 3 == 0) {
				ipAndPort[0] = getProperty("ip1");
			} else if (userId % 3 == 1) {
				ipAndPort[0] = getProperty("ip2");
			} else if (userId % 3 == 2) {
				ipAndPort[0] = getProperty("ip3");
			}
			ipAndPort[1] = getProperty("port2");
		}

		return ipAndPort;
	}

}
