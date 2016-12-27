package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import node.ChunkServer;

public class ChunkServerSendsMajorHeartBeat {
	ChunkServer chunkServer;

	public ChunkServerSendsMajorHeartBeat(ChunkServer chunkServerAssociated) {
		// TODO Auto-generated constructor stub
		this.chunkServer = chunkServerAssociated;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT);

		int ID = chunkServer.myID;
		dout.writeInt(ID);

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
