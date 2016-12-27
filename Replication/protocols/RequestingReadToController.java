package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class RequestingReadToController {

	int ID;
	String fileNeed;

	public RequestingReadToController(int myIDAsClient, String needToRead) {
		// TODO Auto-generated constructor stub
		this.ID = myIDAsClient;
		this.fileNeed = needToRead;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.REQUESTING_READ_TO_CONTROLLER);

		dout.writeInt(this.ID);

		byte[] fileNeedByte = fileNeed.getBytes();
		int fileNeededSize = fileNeedByte.length;

		// dout.write(byteLocalIP);
		dout.writeInt(fileNeededSize);
		dout.write(fileNeedByte);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
