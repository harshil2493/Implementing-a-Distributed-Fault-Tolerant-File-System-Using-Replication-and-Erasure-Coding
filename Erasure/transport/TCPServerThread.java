package transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import protocols.ChunkServerSendsRegistration;
import node.ChunkServer;
import node.Client;
import node.Controller;
import node.Node;

public class TCPServerThread implements Runnable {
	int serverPortNumber;
	Node server;

	public TCPServerThread(String portNumber, Node requestingServer) {
		// TODO Auto-generated constructor stub
		serverPortNumber = Integer.parseInt(portNumber);
		server = requestingServer;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			ServerSocket serverSocket = new ServerSocket(serverPortNumber);
			if (server instanceof Controller) {
				System.out
						.println("[INFO] Controller Is Online Now..! ChunkServers Can Now Send Requests!");
			} else if (server instanceof ChunkServer) {
				System.out.println("[INFO] ChunkServer Is Online Now..! "
						+ " Host: "
						+ serverSocket.getInetAddress().getLocalHost()
						+ " Port: " + serverSocket.getLocalPort());
				// ((ChunkServer) server).hostAddressOfController =
				// serverSocket.getInetAddress().getLocalHost().toString();
				// ((ChunkServer) server).portOfController =
				// serverSocket.getLocalPort();
				// System.out.println("-ip-" + ((ChunkServer)
				// server).hostAddressOfController + "-port-" + ((ChunkServer)
				// server).portOfController);
				Socket socket = new Socket(
						((ChunkServer) server).hostAddressOfController,
						((ChunkServer) server).portOfController);
				// TCPConnection connection = new TCPConnection(server, socket);
				// connection.sender.sendData(new
				// ChunkServerSendsRegistration(serverSocket.getInetAddress().getLocalHost(),
				// serverSocket.getLocalPort()).getByte());
				// DataOutputStream dataOutputStream = new
				// DataOutputStream(socket.getOutputStream());
				//
				byte[] raw = new ChunkServerSendsRegistration(InetAddress
						.getLocalHost().getHostAddress(),
						serverSocket.getLocalPort()).getByte();
				TCPSender sender = new TCPSender(socket);
				sender.sendData(raw);
				DataInputStream dataInputStream = new DataInputStream(
						socket.getInputStream());
				int dataLength;
				dataLength = dataInputStream.readInt();
				byte[] data = new byte[dataLength];
				dataInputStream.readFully(data, 0, dataLength);
				server.onEvent(data, socket);

			}
			while (true) {
				Socket socket = serverSocket.accept();
				// System.out.println("-socket" +
				// socket.getInetAddress().getHostName());
				if (server instanceof Controller) {
					// System.out.println("[INFO] Chunk Server Sent Request To Get Registration");
					// System.out.println(socket.getPort() + ""
					// +socket.getInetAddress().getCanonicalHostName());
				}
				// System.out.println(socket.getPort());
				TCPConnection tcpConnection = new TCPConnection(server, socket);

				// Controller convertedController = (Controller) server;
				// byte[] data = {(byte) 1};
				// tcpConnection.sender.sendData(data);

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out
					.println("[ERROR] Registry Might Be Already Live On Same Port");

		}
	}

}
