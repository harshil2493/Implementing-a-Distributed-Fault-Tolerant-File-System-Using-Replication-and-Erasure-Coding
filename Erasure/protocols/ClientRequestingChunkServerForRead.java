package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;

public class ClientRequestingChunkServerForRead {
	int requestingShard;
	int requestingChunk;
	String reqFile;
	int ID;
	InetAddress IP;
	int port;

	public ClientRequestingChunkServerForRead(int myIDAsClient,
			InetAddress myIP, int myPortNumber, String fileNameToReadFinal,
			int chunkNumber, int shard) {
		// TODO Auto-generated constructor stub
		this.requestingChunk = chunkNumber;
		this.reqFile = fileNameToReadFinal;
		this.ID = myIDAsClient;
		this.IP = myIP;
		this.port = myPortNumber;
		this.requestingShard = shard;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CLIENT_REQUESTING_CHUNKSERVER_FOR_READ);

		int localPortNumber = port;
		byte[] byteLocalIP = IP.getHostAddress().getBytes();
		// System.out.println("IP" + IP.getHostAddress());

		int addressLength = byteLocalIP.length;
		// System.out.println(byteLocalIP[0]);
		dout.writeInt(ID);
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);
		dout.writeInt(localPortNumber);

		byte[] fileNameB = reqFile.getBytes();
		dout.writeInt(fileNameB.length);
		dout.write(fileNameB);
		dout.writeInt(requestingChunk);
		dout.writeInt(requestingShard);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
