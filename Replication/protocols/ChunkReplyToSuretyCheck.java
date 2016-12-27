package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class ChunkReplyToSuretyCheck {
	int IDToAttach;

	public ChunkReplyToSuretyCheck(int myID) {
		// TODO Auto-generated constructor stub
		this.IDToAttach = myID;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CHUNK_REPLY_TO_SURETY_CHECK);

		dout.writeInt(IDToAttach);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
