package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class RequestingChunkForReSend {
	
	int nodeToSend;
	String IPAddress;
	int portNumber;
	String fileName;
	
	public RequestingChunkForReSend(int newID, String iP, int port, String fileNameR) {
		// TODO Auto-generated constructor stub
		
		this.nodeToSend = newID;
		this.IPAddress = iP;
		this.portNumber = port;
		this.fileName = fileNameR;
	}
	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.REQUESTING_CHUNKSERVER_TO_RESEND_DATA);

//		int localPortNumber = clientPort;
		
		dout.writeInt(nodeToSend);
		byte[] byteLocalIP = IPAddress.getBytes();
		int addressLength = byteLocalIP.length;
		// System.out.println(byteLocalIP[0]);
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);
		dout.writeInt(portNumber);
		
		byte[] fInB = fileName.getBytes();
		dout.writeInt(fInB.length);
		dout.write(fInB);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
