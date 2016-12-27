package transport;

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

				ChunkServerSendsRegistration chunkServerSendsRegistration = new ChunkServerSendsRegistration(
						serverSocket.getInetAddress().getLocalHost(),
						serverSocket.getLocalPort());

				((ChunkServer) server).connectionToController.sender
						.sendData(chunkServerSendsRegistration.getByte());
			} else if (server instanceof Client) {
				System.out.println("[INFO] Client Is Online Now..! "
						+ " Host: "
						+ serverSocket.getInetAddress().getLocalHost()
						+ " Port: " + serverSocket.getLocalPort());
				((Client) server).myIP = serverSocket.getInetAddress()
						.getLocalHost();
				((Client) server).myPortNumber = serverSocket.getLocalPort();
				// System.out.println(((Client) server).myIP);
			}
			while (true) {
				Socket socket = serverSocket.accept();
				// System.out.println("Accepted??s?");
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
