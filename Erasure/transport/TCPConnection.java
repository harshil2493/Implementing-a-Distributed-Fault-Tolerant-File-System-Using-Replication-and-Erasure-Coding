package transport;

import java.net.Socket;

import node.Node;

public class TCPConnection {
	public TCPReceiverThread receiver;
	public TCPSender sender;
	public Socket s;

	public TCPConnection(Node node, Socket socket) throws Exception {
		// TODO Auto-generated constructor stub
		// System.out.println("-receiver started-");
		receiver = new TCPReceiverThread(node, socket);
		sender = new TCPSender(socket);
		s = socket;
		Thread threadReceiver = new Thread(receiver);

		threadReceiver.start();

	}
}
