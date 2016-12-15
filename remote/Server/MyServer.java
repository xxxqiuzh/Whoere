
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyServer extends Thread {

	private static final double EARTH_RADIUS = 6378.137;
	private static Lock linklistlock = new ReentrantLock();
	private static Lock messagelock = new ReentrantLock();
	private static LinkedList<String> messages = new LinkedList<String>();
	private static LinkedList<ClientCopy> linklist = new LinkedList<ClientCopy>();

	private static void addClient(ClientCopy cc) {
		linklistlock.lock();
		linklist.add(cc);
		linklistlock.unlock();
	}

	private synchronized void deleteClient(ClientCopy cc) {
		try {
			if(cc.getPrintWriter() != null){
				cc.getPrintWriter().close();
			}
			cc.getSocket().close();
			linklistlock.lock();
			System.out.println("clientCounts"+linklist.size());
			linklist.remove(cc);
			linklistlock.unlock();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void addMessage(String m) {// 将待发送的消息加入messages最后
		messagelock.lock();
		messages.addLast(m);
		messagelock.unlock();
	}

	private synchronized void deleteMessages() {// 删除messages中最后一个元素
		messagelock.lock();
		messages.removeLast();
		messagelock.unlock();
	}

	public static void main(String[] args) throws Exception {
		ServerSocket server = new ServerSocket(5469);// 创建一个ServerSocket在端口5469监听客户请求
		MyServer ms = new MyServer();
		Send send = ms.new Send();
		send.start();
		while (true) {
			Socket ck = server.accept();// 使用accept()阻塞等待客户请求
			ClientCopy ccy = new ClientCopy(ck);
			Accepet ac = ms.new Accepet(ccy);
			// linklistlock.lock();
			// addClient(ccy);
			// linklistlock.unlock();
			ac.start();
		}
	}

	class Accepet extends Thread {
		private ClientCopy clientcopy;
		private boolean tag = true;
		private LinkedList<Socket> neighbor;

		public Accepet(ClientCopy ccy) {
			clientcopy = ccy;
			neighbor = new LinkedList<Socket>();
		}

		public void run() {
			try {
				Socket client = clientcopy.getSocket();
				BufferedReader is = new BufferedReader(new InputStreamReader(client.getInputStream()));
				while (tag) {
					String messa = is.readLine();
					// if(messa==null)
					// break;
					if (messa == null)
						throw new NullPointerException();
					System.out.println(messa);
					int defaultIndex = messa.indexOf("username:");
					if (defaultIndex == 1) { // 根据消息头部判断是否为用户连接后自动发送的默认信息
						linklistlock.lock();
						getMesaages(clientcopy, messa);
						addClient(clientcopy);
						linklistlock.unlock();
						
						String ipAddress = client.getInetAddress().toString().substring(1);
						clientcopy.setIpAddress(ipAddress);
						
						messa = messa.substring(defaultIndex);
						final String str = "$ipAddress:" + ipAddress + messa;
						System.out.println(str);
						messagelock.lock();
						addMessage(str);
						messagelock.unlock();
					} else {
						int messageIndex = messa.indexOf("message:");
						final String message = messa.substring(messageIndex + "message:".length());
						if (!message.equals("end")) {
							getMesaages(clientcopy, messa);
							String ipAddress = client.getInetAddress().toString().substring(1);
							clientcopy.setIpAddress(ipAddress);

							final String str = "ipAddress:" + ipAddress + messa;
							System.out.println(str);
							messagelock.lock();
							addMessage(str);
							messagelock.unlock();
						} else {// 结束
							tag = false;
							is.close();// 关闭Socket输入流
							linklistlock.lock();
							deleteClient(clientcopy);
							linklistlock.unlock();
							break;
						}
					}
				}
			} catch (Exception e) {
				linklistlock.lock();
				deleteClient(clientcopy);
				linklistlock.unlock();
				e.printStackTrace();
			}
		}

	}

	private void getMesaages(ClientCopy clientcopy, String messa) {
		int[] index = new int[5];
		index[0] = messa.indexOf("longitude:");
		index[1] = messa.indexOf("latitude:");
		index[2] = messa.indexOf("bounds:");
		index[3] = messa.indexOf("tags:");
		index[4] = messa.indexOf("message:");
		String longitude = messa.substring((index[0] + "longitude:".length()), index[1]);
		if(longitude.equals("null") || longitude == null){
			longitude = "117.130345";
		}
		clientcopy.setLongitude(longitude);
		
		String latitude = messa.substring((index[1] + "latitude:".length()), index[2]);
		if(latitude.equals("null") || latitude == null){
			latitude = "36.668129";
		}
		clientcopy.setLatitude(latitude);
		
		String bounds = messa.substring((index[2] + "bounds:".length()), index[3]);
		clientcopy.setBounds(bounds);
		String tags = messa.substring((index[3] + "tags:".length()), index[4]);
		clientcopy.setTags(tags);
	}

	class Send extends Thread {
		public Send() {
		}

		public void run() {
			while (true) {
				// 取信息之前加锁
				messagelock.lock();
				int messagesSize = messages.size();
				if (messagesSize <= 0) {
					messagelock.unlock();
					Thread.yield();
				} else {
					System.out.println("messagesSize:" + messagesSize);
					try {
						ClientCopy ccy = null;
						PrintWriter os = null;
						String Msg = null;
						for (int j = messagesSize - 1; j >= 0; j--) {
							Msg = messages.get(j);
							linklistlock.lock();
							int linklistSize = linklist.size();
							LinkedList<ClientCopy> ll = new LinkedList<ClientCopy>();
							int counts = 0	;
							for (int i = 0; i < linklistSize; i++) {
								ccy = linklist.get(i);
								if (getNeighbor(Msg, ccy)) {
									ll.add(ccy);
									counts++;
								}
							}
							Msg = ensureNeighborCounts(Msg, counts);
							System.out.println("counts = "+counts);
							for(int i=0; i<ll.size();i++){
								ccy = ll.get(i);
								if (ccy.getPrintWriter() == null) {
									ccy.setPrintWriter(new PrintWriter(ccy.getSocket().getOutputStream()));
								}
								os = ccy.getPrintWriter();
								os.println(Msg);
								os.flush();// 刷新输出流，使Client马上收到该字符串
							}
							ll = null ;
							linklistlock.unlock();
							deleteMessages();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					messagelock.unlock();
				}
			}
		}
	}
	
	private String ensureNeighborCounts(String Msg, int counts){
		//将待发送的消息添加neighbor字段
		int index = Msg.indexOf("message:");
		Msg = Msg.substring(0, index)+"counts:"+counts+Msg.substring(index);
		return Msg;
	}

	private boolean getNeighbor(String message, ClientCopy ccy) {
		boolean isNeighbor = false;
		// 得到信息源的GPS定位信息、和消息发送范围
		int[] index = new int[5];
		index[0] = message.indexOf("longitude:");
		index[1] = message.indexOf("latitude:");
		index[2] = message.indexOf("bounds:");
		index[3] = message.indexOf("tags:");
		index[4] = message.indexOf("message:");
		String tags0 = message.substring((index[3] + "tags:".length()), index[4]);
		String tags1 = ccy.getTags();
		if(tags0.equals(tags1)){
			double longitude0 = Double.valueOf(message.substring((index[0] + "longitude:".length()), index[1]));
			double latitude0 = Double.valueOf(message.substring((index[1] + "latitude:".length()), index[2]));
			String bounds = message.substring((index[2] + "bounds:".length()), index[3]);
			double bounds0 = getFormatBounds(bounds);
			// 判断ccy是否为他的邻居
			double longitude1 = Double.valueOf(ccy.getLongitude());
			double latitude1 = Double.valueOf(ccy.getLatitude());
			double bounds1 = ccy.getBounds();
			System.out.println(longitude1 + " " + latitude1 + " " + bounds1);
			double distance = GetDistance(longitude0, latitude0, longitude1, latitude1);
			System.out.println("distance: " + distance);
			if (distance <= bounds0 && distance <= bounds1) {
				isNeighbor = true;
			}
		}
		return isNeighbor;
	}

	private static double getFormatBounds(String bounds) {// 得到km为单位的bounds
		String unit;
		double value = 0.1; // 初始化为0.1km
		Pattern pa1 = Pattern.compile("[0-9]+");
		Pattern pa2 = Pattern.compile("[a-z]+");
		Matcher ma1 = pa1.matcher(bounds);
		Matcher ma2 = pa2.matcher(bounds);
		if (ma1.find()) {
			value = Double.valueOf(ma1.group());
		}
		if (ma2.find()) {
			unit = ma2.group();
			if (unit.equals("m")) {
				value = value / 1000;
			}
		}
		return value;
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	private static double GetDistance(double long1, double lat1, double long2, double lat2) {
		// 根据两点间的经纬度计算距离，单位：km
		double a, b, d, sa2, sb2;
		lat1 = rad(lat1);
		lat2 = rad(lat2);
		a = lat1 - lat2;
		b = rad(long1 - long2);

		sa2 = Math.sin(a / 2.0);
		sb2 = Math.sin(b / 2.0);
		d = 2 * EARTH_RADIUS * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
		return d;
	}

}
