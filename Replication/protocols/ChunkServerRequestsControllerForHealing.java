package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import node.ChunkServer;

public class ChunkServerRequestsControllerForHealing {
	ChunkServer server;
	String file;
	int chunk;
	ArrayList<Integer> badList;
	int clientReq;
	int inConsFinal;
	public ChunkServerRequestsControllerForHealing(ChunkServer chunkServer,
			String fileToHeal, int chunkNumber, ArrayList<Integer> badSlices, int client, int inCons) {
		// TODO Auto-generated constructor stub
		this.server = chunkServer;
		this.file = fileToHeal;
		this.chunk = chunkNumber;
		this.badList = badSlices;
		this.clientReq = client;
		this.inConsFinal = inCons;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CHUNKSERVER_REQUESTS_CONTROLLER_FOR_HEALING);

		int ID = server.myID;

		// System.out.println(byteLocalIP[0]);
		dout.writeInt(ID);

		byte[] fileBadByte = file.getBytes();
		int sizeOfFile = fileBadByte.length;
		dout.writeInt(sizeOfFile);
		dout.write(fileBadByte);

		dout.writeInt(chunk);

		dout.writeInt(badList.size());

		for (int i : badList) {
			dout.writeInt(i);
		}
		
		dout.writeInt(clientReq);
		
		dout.writeInt(inConsFinal);
		// dout.writeLong(chunkServer.rootToStore.getFreeSpace());
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
