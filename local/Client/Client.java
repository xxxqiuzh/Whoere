
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread {
	private static Socket socket;
	private static BufferedReader is;
	private static PrintWriter os;
	public void run() {
		try {
			os = new PrintWriter(socket.getOutputStream());
			os.println(defaultSend());
			os.flush();
			BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				String str = sin.readLine();
				String meassge = "username:qz" + "longitude:117.130345" + "latitude:36.668129" + "bounds:2km"
						+ "tags:学习" + "message:" + str;
				os.println(meassge);
				os.flush();
				if (str.equals("end")) {
					break;
				}
			}
			is.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String defaultSend(){
		String meassge = "$username:qz" + "longitude:117.130345" + "latitude:36.668129" + "bounds:2km"
				+ "tags:学习" + "message:";
		return meassge;
	}
	
	public static void main(String[] args) throws Exception {
		socket = new Socket(InetAddress.getLocalHost(), 5469);

		Client client = new Client();
		client.start();
		is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		while (true) {
			try {
				String Msg = is.readLine();
				if (Msg != null) {
					int[] index = new int[8];
					int defaultIndex = Msg.indexOf("ipAddress:");
					if(defaultIndex == 1){
						index[0] = Msg.indexOf("username:");
						index[1] = Msg.indexOf("longitude:");
						String username = Msg.substring(index[0]+"username:".length(), index[1]);
						if(username.equals("1")){
							index[2] = Msg.indexOf("counts:");
							index[3] = Msg.indexOf("message:");
							String counts = Msg.substring(index[2]+"counts:".length(), index[3]);
							System.out.println("neighbor = "+counts);
						}
					}else{
						index[0] = Msg.indexOf("ipAddress:");
						index[1] = Msg.indexOf("username:");
						index[2] = Msg.indexOf("longitude:");
						index[3] = Msg.indexOf("latitude:");
						index[4] = Msg.indexOf("bounds:");
						index[5] = Msg.indexOf("tags:");
						index[6] = Msg.indexOf("counts:");
						index[7] = Msg.indexOf("message:");
						String ipAddress = Msg.substring(index[0], index[1]);
						String username = Msg.substring(index[1], index[2]);
						String longitude = Msg.substring(index[2], index[3]);
						String latitude = Msg.substring(index[3], index[4]);
						String bounds = Msg.substring(index[4], index[5]);
						String tags = Msg.substring(index[5], index[6]);
						String counts = Msg.substring(index[6], index[7]);
						String message = Msg.substring(index[7]);
						String[] str = { ipAddress, username, longitude, latitude, bounds, tags, counts, message };
						System.out.println("Server : " + Msg);
						for (int i = 0; i < str.length; i++) {
							System.out.println(str[i] + "\t");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

		}
//		os.close();
//		is.close();
		System.out.println("end");
		socket.close();
	}

}
