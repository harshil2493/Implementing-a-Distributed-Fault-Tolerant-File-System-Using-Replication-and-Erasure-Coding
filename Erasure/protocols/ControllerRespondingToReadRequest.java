package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import node.Controller;

public class ControllerRespondingToReadRequest {
	public int status;
	public Controller controller;
	public int totalChunk;

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
			System.out.println("File Does Exists");
			System.out.println(controller.FileToChunksToShardToNode.get(file));
			byte[] fileNameByte = file.getBytes();
			int size = fileNameByte.length;
			dout.writeInt(size);
			dout.write(fileNameByte);
			// System.out.println("File" + file);

			// totalChunk
			// Map<String, HashMap<Integer, HashMap<Integer, Integer>>>
			// FileToChunksToShardToNode
			this.totalChunk = controller.FileToChunksToShardToNode.get(file)
					.size();
			System.out.println("Total Chunks" + totalChunk);
			dout.writeInt(totalChunk);
			// System.out.println("Chunk: " + totalChunk);

			for (int i = 1; i <= totalChunk; i++) {
				// dout.writeInt(i);
				Map<Integer, Integer> shardList = controller.FileToChunksToShardToNode
						.get(file).get(i);

				System.out
						.println("For Chunk.. List: "
								+ controller.FileToChunksToShardToNode
										.get(file).get(i));
				for (int j = 1; j <= 9; j++) {
					// dout.writeInt(j);
					dout.writeInt(shardList.get(j));

					ArrayList<String> IPPort = controller.hostAddressAndPortOfChunkServer
							.get(shardList.get(j));
					System.out.println(IPPort);
					String IPS = IPPort.get(0);
					byte[] IPB = IPS.getBytes();
					dout.writeInt(IPB.length);
					dout.write(IPB);
					dout.writeInt(Integer.parseInt(IPPort.get(1)));
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
