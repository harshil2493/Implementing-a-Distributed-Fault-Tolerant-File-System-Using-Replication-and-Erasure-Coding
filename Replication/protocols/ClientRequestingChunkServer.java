package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import node.Client;

public class ClientRequestingChunkServer {
	String sendingFileName;
	int chunk;
	Client requestingClient;
	int total;

	public ClientRequestingChunkServer(Client reqClient, String fileName,
			int chunkNumber, int totalNumberOfChunk) {
		// TODO Auto-generated constructor stub
		sendingFileName = fileName;
		chunk = chunkNumber;
		requestingClient = reqClient;
		total = totalNumberOfChunk;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CLIENT_REQUESTING_CHUNKSERVER);

		int ID = requestingClient.myIDAsClient;
		dout.writeInt(ID);
		dout.writeInt(total);
		byte[] fName = sendingFileName.getBytes();
		int fileLength = fName.length;
		dout.writeInt(fileLength);
		dout.write(fName);
		dout.writeInt(chunk);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
