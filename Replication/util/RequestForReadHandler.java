package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import protocols.ControllerRespondingToReadRequest;
import node.Controller;

public class RequestForReadHandler implements Runnable {

	int cID;
	String fileToRead;
	Controller c;

	public RequestForReadHandler(int clientID, String reqFileName,
			Controller controller) {
		// TODO Auto-generated constructor stub
		this.cID = clientID;
		this.fileToRead = reqFileName;
		this.c = controller;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// System.out.println("File Name " + fileToRead);
		int numberOfChunks = c.FileToChunks.get(fileToRead).get(0);
		ControllerRespondingToReadRequest controllerRespondingToReadRequest = new ControllerRespondingToReadRequest();

		Set<Integer> knowID = new HashSet<Integer>();
		// System.out.println("Chunks" + numberOfChunks);
		if (c.clientKnowsCS.get(cID) == null) {
			ArrayList<Integer> IDs = new ArrayList<Integer>();
			c.clientKnowsCS.put(cID, IDs);
		}
		ArrayList<Integer> chunksList = new ArrayList<Integer>();
		for (int i = 1; i <= numberOfChunks; i++) {
			// System.out.println("For " + i +
			// c.OffsetToChunkServersList.get(c.FileToChunks.get(fileToRead).get(i)).get(0));

			//
			// Want To Shuffle?

			// Collections.shuffle(c.OffsetToChunkServersList.get(c.FileToChunks.get(fileToRead).get(i)));
			// System.out.println("For " + i +
			// c.OffsetToChunkServersList.get(c.FileToChunks.get(fileToRead).get(i)).get(0));
			int IDOfRelNode = c.OffsetToChunkServersList.get(
					c.FileToChunks.get(fileToRead).get(i)).get(0);
			chunksList.add(IDOfRelNode);
			if (!c.clientKnowsCS.get(cID).contains(IDOfRelNode)) {
				knowID.add(IDOfRelNode);
				c.clientKnowsCS.get(cID).add(IDOfRelNode);
			}

		}
		controllerRespondingToReadRequest.status = 1;
		controllerRespondingToReadRequest.controller = c;
		controllerRespondingToReadRequest.file = fileToRead;
		controllerRespondingToReadRequest.totalChunk = numberOfChunks;
		controllerRespondingToReadRequest.chunkList = chunksList;
		controllerRespondingToReadRequest.IPandPortToSend = knowID;

		try {
			// System.out.println("Sending Exe");
			c.IDToConnnectionClient.get(cID).sender
					.sendData(controllerRespondingToReadRequest.getByte());
			// System.out.println("Sending Done");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println("Known" + knowID);
	}

}
