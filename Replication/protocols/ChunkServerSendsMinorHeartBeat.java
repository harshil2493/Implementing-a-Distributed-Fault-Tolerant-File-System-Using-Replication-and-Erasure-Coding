package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import node.ChunkServer;

public class ChunkServerSendsMinorHeartBeat {
	ChunkServer chunkServer;

	public ChunkServerSendsMinorHeartBeat(ChunkServer chunkServerAssociated) {
		// TODO Auto-generated constructor stub
		this.chunkServer = chunkServerAssociated;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CHUNK_SERVER_SENDS_MINOR_HEARTBEAT);

		int ID = chunkServer.myID;
		dout.writeInt(ID);
		chunkServer.freeSpaceAvailable = chunkServer.rootToStore.getFreeSpace();
		dout.writeLong(chunkServer.freeSpaceAvailable);
		// System.out.println(chunkServer.freeSpaceAvailable);
		dout.writeLong(chunkServer.numberOfChunks);
		synchronized (chunkServer.newlyAddedChunks) {
			dout.writeInt(chunkServer.newlyAddedChunks.size());
			for(String data : chunkServer.newlyAddedChunks)
			{
				byte[] dataInByte = data.getBytes();
				dout.writeInt(dataInByte.length);
				dout.write(dataInByte);
			}
		}
		
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
