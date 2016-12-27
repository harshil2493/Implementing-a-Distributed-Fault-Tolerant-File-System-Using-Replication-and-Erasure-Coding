package transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import node.ChunkServer;
import node.Client;
import node.Node;

public class TCPClientThread implements Runnable {
	int serverPortNumber;
	Node client;
	String serverHost;

	public TCPClientThread(String host, String portNumber, Node requestingClient) {
		// TODO Auto-generated constructor stub
		serverPortNumber = Integer.parseInt(portNumber);
		serverHost = host;
		client = requestingClient;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			Socket socket = new Socket(InetAddress.getByName(serverHost),
					(serverPortNumber));
			TCPConnection connectionToController = new TCPConnection(client,
					socket);

			if (client instanceof ChunkServer) {
				((ChunkServer) client).connectionToController = connectionToController;
				System.out
						.println("[INFO] Chunk Server Is Connected To Controller");

				// System.out.println(((ChunkServer)
				// client).connectionToController);
				((ChunkServer) client).startReceiver();
			}
			if (client instanceof Client) {
				((Client) client).connectionToController = connectionToController;
				((Client) client).startConnectionionWithController();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println();
			System.out
					.println("[ERROR] Error In I/O.. Shutting Down The System");
			System.out.println("[CONT] May Be Receiver Is Not Alive");
			System.out.println();
			System.out.println("ERROR REPORT");
			e.printStackTrace();
			System.exit(0);
			// e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println();
			System.out.println("[ERROR] Some Exception Occured..");
			System.out.println("[CONT] Shutting Down The System");
			System.out.println();
			System.out.println("ERROR REPORT");
			e.printStackTrace();
			System.exit(0);
		}

	}

}
