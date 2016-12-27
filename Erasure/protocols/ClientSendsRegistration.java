package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientSendsRegistration {
	InetAddress clientIP;
	int clientPort;

	public ClientSendsRegistration(InetAddress myIPInString, int myPortNumber)
			throws UnknownHostException {
		// TODO Auto-generated constructor stub
		clientIP = (myIPInString);
		clientPort = myPortNumber;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CLIENT_SENDS_REGISTRATION);

		int localPortNumber = clientPort;
		byte[] byteLocalIP = clientIP.getAddress();
		int addressLength = byteLocalIP.length;
		// System.out.println(byteLocalIP[0]);
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);
		dout.writeInt(localPortNumber);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
