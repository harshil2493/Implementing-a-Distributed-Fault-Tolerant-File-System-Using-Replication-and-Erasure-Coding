package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import node.Controller;

public class ControllerSendsChunkServerList {

	HashMap<Integer, Integer> chunkList;
	int chunkNumber;
	Controller c;

	public ControllerSendsChunkServerList(Controller controller, int chunk,
			HashMap<Integer, Integer> nodeLoc) {
		// TODO Auto-generated constructor stub
		this.chunkNumber = chunk;
		this.chunkList = nodeLoc;
		this.c = controller;

	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.CONTROLLER_SENDS_CHUNKSERVERLIST);

		dout.writeInt(chunkNumber);

		// dout.writeInt(chunkList.size());
		for (int i : chunkList.keySet()) {
			dout.writeInt(chunkList.get(i));
			ArrayList<String> IPPort = c.hostAddressAndPortOfChunkServer
					.get(chunkList.get(i));
			System.out.println(IPPort);
			String IPS = IPPort.get(0);
			byte[] IPB = IPS.getBytes();
			dout.writeInt(IPB.length);
			dout.write(IPB);
			dout.writeInt(Integer.parseInt(IPPort.get(1)));
		}

		// dout.writeInt(needed.size());
		// // System.out.println("needed Size" + needed.size());
		// //
		// System.out.println(controllerNode.hostAddressAndPortOfChunkServer);
		// for (int i : needed) {
		// dout.writeInt(i);
		// String key = ((controllerNode.hostAddressAndPortOfChunkServer
		// .get(i)).keySet().toString());
		// key = key.substring(1, key.length() - 1);
		// int port = (controllerNode.hostAddressAndPortOfChunkServer.get(i)
		// .get((Object) key));
		// // System.out.println(key + port);
		// byte[] byteLocalIP = key.getBytes();
		// int addressLength = byteLocalIP.length;
		// dout.writeInt(addressLength);
		// dout.write(byteLocalIP);
		// dout.writeInt(port);
		//
		// //
		// System.out.println(controllerNode.hostAddressAndPortOfChunkServer.get(i).get((controllerNode.hostAddressAndPortOfChunkServer.get(i)).keySet().toString()));
		// }
		// dout.writeInt(chunk);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		// System.out.println("Bytes Generated");
		return marshalledBytes;

	}
}
