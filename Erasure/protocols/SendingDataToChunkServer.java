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
	// ArrayList<Integer> newSenderList;
	byte[] sendingData;
	String file;
	int chunk;
	int shardNum;

	// Map<Integer, List<String>> IPAndPort = new HashMap<Integer,
	// List<String>>();

	public SendingDataToChunkServer(String fileName, int chunkGot, int shard,
			byte[] dataToSend) {
		// TODO Auto-generated constructor stub
		this.sendingData = dataToSend;

		this.file = fileName;
		this.chunk = chunkGot;
		this.shardNum = shard;
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
		dout.writeInt(shardNum);
		int lengthOfData = sendingData.length;
		// System.out.println(byteLocalIP[0]);
		dout.writeInt(lengthOfData);
		dout.write(sendingData);
		// dout.writeInt(localPortNumber);

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
