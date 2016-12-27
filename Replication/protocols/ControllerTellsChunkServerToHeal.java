package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;

public class ControllerTellsChunkServerToHeal {

	int nodeID;
	String iPAddressString;
	int portNode;
	String badFile;
	int chunkEffected;
	ArrayList<Integer> listBad;
	int neededClient;
	InetAddress clientIP;
	int portOfClientInNeed;
	int inL;
	public ControllerTellsChunkServerToHeal(int nodeID, String iPToString,
			int portOfNode, String fileBad, int effectedChunk,
			ArrayList<Integer> listOfBadSlices, int clientNeededFile, InetAddress iPClientInet, int portOfClientNeed, int inLevel) {
		// TODO Auto-generated constructor stub
		this.nodeID = nodeID;
		this.iPAddressString = iPToString;
		this.portNode = portOfNode;
		this.badFile = fileBad;
		this.chunkEffected = effectedChunk;
		this.listBad = listOfBadSlices;
		this.clientIP = iPClientInet;
		this.neededClient = clientNeededFile;
		this.portOfClientInNeed = portOfClientNeed;
		this.inL = inLevel;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CONTROLLER_TELLS_CHUNKSERVER_TO_HEAL);

		int ID = nodeID;

		// System.out.println(byteLocalIP[0]);
		dout.writeInt(ID);
		byte[] IPOfNode = iPAddressString.getBytes();
		int IPSize = IPOfNode.length;
		dout.writeInt(IPSize);
		dout.write(IPOfNode);
		dout.writeInt(portNode);
		byte[] fileBadByte = badFile.getBytes();
		int sizeOfFile = fileBadByte.length;
		dout.writeInt(sizeOfFile);
		dout.write(fileBadByte);

		dout.writeInt(chunkEffected);

		dout.writeInt(listBad.size());

		for (int i : listBad) {
			dout.writeInt(i);
		}
		// dout.writeLong(chunkServer.rootToStore.getFreeSpace());
		
		dout.writeInt(neededClient);
		byte[] IPInB = clientIP.getAddress();
		dout.writeInt(IPInB.length);
		dout.write(IPInB);
		dout.writeInt(portOfClientInNeed);
		dout.writeInt(inL);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
