package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import node.ChunkServer;

public class ChunkServerDataToClient {
	int shard;
	int chunkID;
	String file;
	byte[] dataRead;
	boolean SHAResult;
	ChunkServer chunkServer;
	public int flagForNotFound=0;
	public ChunkServerDataToClient(ChunkServer c, byte[] fileContent,
			String fileNameActual, int chunk, int shardID, boolean doSHACheck) {
		// TODO Auto-generated constructor stub
		this.chunkID = chunk;
		this.file = fileNameActual;
		this.dataRead = fileContent;
		this.SHAResult = doSHACheck;
		this.chunkServer = c;
		this.shard = shardID;
	}

	public ChunkServerDataToClient() {
		// TODO Auto-generated constructor stub
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CHUNKSERVER_DATA_TO_CLIENT);

		byte[] fileNameByte = file.getBytes();
		int sizeByteFile = fileNameByte.length;

		dout.writeInt(sizeByteFile);
		dout.write(fileNameByte);

		dout.writeInt(chunkID);
		dout.writeInt(shard);
		if(flagForNotFound == 0)
		{
		if (SHAResult) {
			int flag = 1;

			dout.writeInt(flag);
			int dataReadSize = dataRead.length;
			dout.writeInt(dataReadSize);
			// System.out.println("Puting" + dataReadSize);
			dout.write(dataRead);

		} else {
			dout.writeInt(-1);

			byte[] errorMessage = ("[ERROR Bad Copy " + file + " Chunk: "
					+ chunkID + " On ChunkServer: " + chunkServer.myID)
					.getBytes();
			int errorSize = errorMessage.length;
			dout.writeInt(errorSize);
			// System.out.println("Puting" + dataReadSize);
			dout.write(errorMessage);

			// dout.writeInt(flag);
			int dataReadSize = dataRead.length;
			dout.writeInt(dataReadSize);
			// System.out.println("Puting" + dataReadSize);
			dout.write(dataRead);

			// String badFileNameInString = file;
			// int badChunkNumber = chunkID;
			//
			// byte[] badFileNameByte = badFileNameInString.getBytes();
			//
			// dout.writeInt(badFileNameByte.length);
			// dout.write(badFileNameByte);
			// dout.writeInt(badChunkNumber);

			// byte[] corruptedFile = file
		}
		
		}
		else
		{
			dout.writeInt(flagForNotFound);
		}
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
