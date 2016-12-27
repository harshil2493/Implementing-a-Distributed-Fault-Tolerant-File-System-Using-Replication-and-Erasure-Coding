package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;

import node.ChunkServer;
import node.Node;

public class ChunkServerSendsRegistration {
	String hostName;
	int portNumber;
	ChunkServer chunkServer;

	public ChunkServerSendsRegistration(String string, int localPort) {
		// TODO Auto-generated constructor stub
		// this.node = chunkServer;
		this.hostName = string;
		this.portNumber = localPort;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CHUNK_SERVER_SENDS_REGISTRATION);

		int localPortNumber = portNumber;
		byte[] byteLocalIP = hostName.getBytes();
		int addressLength = byteLocalIP.length;
		// System.out.println(byteLocalIP[0]);
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);
		dout.writeInt(localPortNumber);
		// dout.writeLong(chunkServer.rootToStore.getFreeSpace());
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
