package transport;

import java.io.DataOutputStream;
import java.net.Socket;

public class TCPSender {

	public Socket socket;
	private DataOutputStream dataOutputStream;

	public TCPSender(Socket socket) throws Exception {
		this.socket = socket;
		this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
	}

	public void sendData(byte[] data) {
		// System.out.println("Called> sendData");
		int dataLength = data.length;
		try {
			dataOutputStream.writeInt(dataLength);
			dataOutputStream.write(data, 0, dataLength);
			dataOutputStream.flush();

		} catch (Exception e) {
		}
	}
}
