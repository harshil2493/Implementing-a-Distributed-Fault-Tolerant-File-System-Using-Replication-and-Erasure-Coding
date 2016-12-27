package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Set;

import node.Controller;

public class ControllerRespondingToReadRequest {
	public int status;
	public Controller controller;
	public int totalChunk;
	public ArrayList<Integer> chunkList;
	public Set<Integer> IPandPortToSend;
	public String file;

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CONTROLLER_RESPODING_TO_READ_REQUEST);

		dout.writeInt(this.status);
		if (this.status == -1) {
			byte[] errorB = "[ERROR FROM CONTROLLER] No Such File Exists"
					.getBytes();
			int errorSize = errorB.length;

			// dout.write(byteLocalIP);
			dout.writeInt(errorSize);
			dout.write(errorB);
		} else {
			byte[] fileNameByte = file.getBytes();
			int size = fileNameByte.length;
			dout.writeInt(size);
			dout.write(fileNameByte);
			// System.out.println("File" + file);
			dout.writeInt(totalChunk);
			// System.out.println("Chunk: " + totalChunk);
			dout.writeInt(IPandPortToSend.size());
			// System.out.println("IP PORT SIZE: " + IPandPortToSend.size());
			for (int i : IPandPortToSend) {
				dout.writeInt(i);
				String key = ((controller.hostAddressAndPortOfChunkServer
						.get(i)).keySet().toString());
				key = key.substring(1, key.length() - 1);
				int port = (controller.hostAddressAndPortOfChunkServer.get(i)
						.get((Object) key));
				// System.out.println(key + port);
				byte[] byteLocalIP = key.getBytes();
				int addressLength = byteLocalIP.length;
				dout.writeInt(addressLength);
				dout.write(byteLocalIP);
				dout.writeInt(port);

				// System.out.println(controllerNode.hostAddressAndPortOfChunkServer.get(i).get((controllerNode.hostAddressAndPortOfChunkServer.get(i)).keySet().toString()));
			}
			// System.out.println("Chunk List: " + chunkList);
			for (int j : chunkList) {
				dout.writeInt(j);
			}

		}
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
