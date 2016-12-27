package util;

import protocols.ClientRequestingChunkServer;
import node.Client;

public class StartRequestingController {

	Client reqClient;
	String FileName;
	int chunkNumber;

	public StartRequestingController(Client c, int i, String needed) {
		// TODO Auto-generated constructor stub
		reqClient = c;
		chunkNumber = i;
		FileName = needed;
	}

	public void startSending() {
		System.out.println(reqClient.totalNumberOfChunk);
		// TODO Auto-generated method stub
		for (int i = 0; i < reqClient.totalNumberOfChunk; i++) {
			try {
				ClientRequestingChunkServer clientRequestingChunkServer = new ClientRequestingChunkServer(
						reqClient, FileName, (i + 1),
						reqClient.totalNumberOfChunk);
				// System.out.println("Created - " + (i+1));
				reqClient.connectionToController.sender
						.sendData(clientRequestingChunkServer.getByte());
				// System.out.println("Sending - " + (i+1));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// System.out.println("Out Of For Loop");

	}

}
