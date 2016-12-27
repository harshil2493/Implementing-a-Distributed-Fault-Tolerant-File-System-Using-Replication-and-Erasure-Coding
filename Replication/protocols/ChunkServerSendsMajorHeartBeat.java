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
		chunkServer.freeSpaceAvailable = chunkServer.rootToStore.getFreeSpace();
		dout.writeLong(chunkServer.freeSpaceAvailable);
		// System.out.println(chunkServer.freeSpaceAvailable);
		dout.writeLong(chunkServer.numberOfChunks);
		
		
		synchronized (chunkServer.listOfChunks) {
			dout.writeInt(chunkServer.listOfChunks.size());
			for(String f : chunkServer.listOfChunks.keySet())
			{
				byte[] fB = f.getBytes();
				dout.writeInt(fB.length);
				dout.write(fB);
				dout.writeInt(chunkServer.listOfChunks.get(f).size());
				for(int chunk : chunkServer.listOfChunks.get(f))
				{
					dout.writeInt(chunk);
				}
			}
		}
		
		
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
