package util;

import java.util.ArrayList;
import java.util.List;

import protocols.ChunkServerRequestsControllerForHealing;
import node.ChunkServer;

public class StartHealing {
	ChunkServer chunkServer;
	String fileToHeal;
	int chunkNumber;
	ArrayList<Integer> badSlices;
	int client;
	int inCons;
	public StartHealing(ChunkServer c, String fileNameActual, int chunk,
			ArrayList<Integer> badSlice, int requestedClient, int inCon) {
		// TODO Auto-generated constructor stub

		this.chunkServer = c;
		this.fileToHeal = fileNameActual;
		this.chunkNumber = chunk;
		this.badSlices = badSlice;
		this.client = requestedClient;
		this.inCons = inCon;
	}

	// @Override
	public void start() {
		// TODO Auto-generated method stub
		System.out.println("Healing Is Going On...");

		ChunkServerRequestsControllerForHealing chunkServerRequestsControllerForHealing = new ChunkServerRequestsControllerForHealing(
				chunkServer, fileToHeal, chunkNumber, badSlices, client, inCons);
		try {
//			System.out.println("Sending To Controller?");
			chunkServer.connectionToController.sender
					.sendData(chunkServerRequestsControllerForHealing.getByte());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
