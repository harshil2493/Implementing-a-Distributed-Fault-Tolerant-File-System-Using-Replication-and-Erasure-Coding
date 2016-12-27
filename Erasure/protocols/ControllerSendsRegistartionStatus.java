package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class ControllerSendsRegistartionStatus {
	int ID;

	public ControllerSendsRegistartionStatus(int IDAllocated) {
		// TODO Auto-generated constructor stub
		this.ID = IDAllocated;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CONTROLLER_SENDS_REGISTRATION_STATUS);

		dout.writeInt(ID);

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
