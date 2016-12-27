package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import node.Client;

public class SendingDataToChunkServer {
	ArrayList<Integer> newSenderList;
	byte[] sendingData;
	String file;
	int chunk;
	Map<Integer, List<String>> IPAndPort = new HashMap<Integer, List<String>>();

	public SendingDataToChunkServer(Map<Integer, List<String>> knownIPPort,
			String fileName, int chunkGot, byte[] dataToSend,
			ArrayList<Integer> listToSend) {
		// TODO Auto-generated constructor stub
		this.sendingData = dataToSend;
		this.newSenderList = listToSend;
		this.file = fileName;
		IPAndPort = knownIPPort;
		this.chunk = chunkGot;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.SENDING_DATA_TO_CHUNKSERVER);

		byte[] fileNameInBytes = file.getBytes();
		int sizeOfFName = fileNameInBytes.length;
		dout.writeInt(sizeOfFName);
		dout.write(fileNameInBytes);
		// System.out.println("chunk" + chunk);
		dout.writeInt(chunk);
		int lengthOfData = sendingData.length;
		// System.out.println(byteLocalIP[0]);
		dout.writeInt(lengthOfData);
		dout.write(sendingData);
		// dout.writeInt(localPortNumber);
		dout.writeInt(newSenderList.size());
		for (int i : newSenderList) {
			dout.writeInt(i);

			String IPInS = IPAndPort.get(i).get(0);
			// System.out.println("Grab IP?");
			byte[] IPInB = IPInS.getBytes();
			int sizeOfIPName = IPInB.length;
			dout.writeInt(sizeOfIPName);
			dout.write(IPInB);
			int portS = Integer.parseInt(IPAndPort.get(i).get(1));
			dout.writeInt(portS);
			// System.out.println("Grab Port?");

		}
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
