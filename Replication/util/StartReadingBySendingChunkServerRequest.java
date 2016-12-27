package util;

import java.util.ArrayList;

import node.Client;
import protocols.ClientRequestingChunkServerForRead;

public class StartReadingBySendingChunkServerRequest  {
	Client c;
	String fName;
	int tChunk;
	ArrayList<Integer> chunkList;

	public StartReadingBySendingChunkServerRequest(Client client,
			String fileNameToRead, int totalChunksOfFile,
			ArrayList<Integer> chunksListToRead) {
		// TODO Auto-generated constructor stub
		this.c = client;
		this.fName = fileNameToRead;
		this.tChunk = totalChunksOfFile;
		this.chunkList = chunksListToRead;
	}

//	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (int i = 1; i <= tChunk; i++) {
			ClientRequestingChunkServerForRead clientRequestingChunkServerForRead = new ClientRequestingChunkServerForRead(
					c.myIDAsClient, c.myIP, c.myPortNumber, fName, i);
			;
			try {
				c.knownID.get(chunkList.get(i - 1)).sender
						.sendData(clientRequestingChunkServerForRead.getByte());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
