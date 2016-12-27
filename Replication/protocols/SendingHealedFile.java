package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class SendingHealedFile {
	byte[] fileName;
	int chunkBad;
	ArrayList<Integer> badSlice;
	ArrayList<byte[]> data;
	int totalSlice;

	public SendingHealedFile(int sliceAva, byte[] fileBadInBytes,
			int effectedChunk, ArrayList<Integer> listOfBadSlices,
			ArrayList<byte[]> lostData) {
		// TODO Auto-generated constructor stub
		this.fileName = fileBadInBytes;
		this.chunkBad = effectedChunk;
		this.badSlice = listOfBadSlices;
		this.data = lostData;
		this.totalSlice = sliceAva;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.SENDING_HEALED_FILE);

		dout.writeInt(fileName.length);
		dout.write(fileName);
		dout.writeInt(chunkBad);
		dout.writeInt(badSlice.size());
		for (int i = 0; i < badSlice.size(); i++) {
			// int j = 0;
			dout.writeInt(badSlice.get(i));
			// System.out.println("Writing To Socket: " + data.get(i).length);
			dout.writeInt(data.get(i).length);
			dout.write(data.get(i));
			// j++;
		}
		dout.writeInt(totalSlice);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
