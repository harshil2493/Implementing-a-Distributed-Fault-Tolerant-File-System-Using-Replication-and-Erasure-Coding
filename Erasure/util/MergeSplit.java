package util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocols.ClientRequestingChunkServer;
import erasure.ReedSolomon;
import node.Client;

public class MergeSplit {
	// public File file;
	public Client requestingClient;
	public static final int DATA_SHARDS = 6;
	public static final int PARITY_SHARDS = 3;
	public static final int TOTAL_SHARDS = 9;

	public static final int BYTES_IN_INT = 4;

	public MergeSplit(Client client) {
		// TODO Auto-generated constructor stub
		// this.file = neededFile;
		requestingClient = client;
	}

	public boolean splitFile(File f) throws Exception {

		int chunkCounter = 1;
		requestingClient.fileName = f.getAbsolutePath();
		// ArrayList<byte[]> byteData = new ArrayList<byte[]>();
		int sizeOfChunk = 1024 * 64;// 64KB
		System.out.println("[INFO] File Size " + f.length());
		System.out.println("[INFO] Chunk Should Be "
				+ Math.ceil(f.length() * 1.0 / sizeOfChunk));
		byte[] bufferChunk = new byte[sizeOfChunk];

		// System.out.println("file read-" + f.canRead());
		try (BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(f))) {

			// System.out.println(f.getAbsolutePath());
			int tmp = 0;
			while ((tmp = bis.read(bufferChunk)) > 0) {

				// Reed Solomon Code..
				int storedSize = tmp + BYTES_IN_INT;
				int shardSize = (storedSize + DATA_SHARDS - 1) / DATA_SHARDS;
				int bufferSize = shardSize * DATA_SHARDS;
				byte[] allBytes = new byte[bufferSize];
				// System.out.println("TMP: " + tmp);
				ByteBuffer.wrap(allBytes).putInt(tmp);
				// raf.seek(i * filesize);
				// int bytesRead = raf.read(allBytes,
				// Constants.SHARDS_SIZES.BYTES_IN_INT, filesize);
				System.arraycopy(bufferChunk, 0, allBytes, BYTES_IN_INT, tmp);

				byte[][] shardsConverted = new byte[TOTAL_SHARDS][shardSize];
				// pad with zeros
				int paddedZeros = bufferSize - storedSize;
				// System.out.println("paadded" + paddedZeros);
				byte[] paddingZeros = new byte[paddedZeros];
				for (int pad = 0; pad < paddedZeros; pad++) {
					paddingZeros[pad] = 0;
				}

				System.arraycopy(paddingZeros, 0, allBytes, storedSize,
						paddedZeros);
				// System.out.println("-padded zeros-" + paddedZeros);
				for (int converting = 0; converting < DATA_SHARDS; converting++) {
					System.arraycopy(allBytes, converting * shardSize,
							shardsConverted[converting], 0, shardSize);
				}
				// for(int i = 0; i < 4; i++){
				// System.out.println("-len-" + allBytes[i]);
				// }

				int size = ByteBuffer.wrap(allBytes).getInt();
				// System.out.println("TMP////////" + size);
				// Put padded zeros
				// System.arraycopy(zeros, 0, allBytes, storedSize,
				// paddedZeros);
				ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS,
						PARITY_SHARDS);
				reedSolomon.encodeParity(shardsConverted, 0, shardSize);

				for (int i = 0; i < TOTAL_SHARDS; i++) {
					// System.out.println("shard-" + i + "-"
					// + new String(shardsConverted[i]));
				}
				requestingClient.dataOfChunks
						.put(chunkCounter, shardsConverted);
				// this.requestingClient.fileIntoChunk.add(byteArrayCopy);
				bufferChunk = new byte[sizeOfChunk];
				ClientRequestingChunkServer clientRequestingChunkServer = new ClientRequestingChunkServer(
						requestingClient, f.getAbsolutePath(), chunkCounter);
				requestingClient.senderOfControllerSender
						.sendData(clientRequestingChunkServer.getByte());

				DataInputStream dataInputStream = new DataInputStream(
						requestingClient.senderOfControllerSender.socket
								.getInputStream());
				// senderOfControllerSender = senderController;
				int dataLength;
				dataLength = dataInputStream.readInt();
				byte[] data = new byte[dataLength];
				dataInputStream.readFully(data, 0, dataLength);
				requestingClient.onEvent(data,
						requestingClient.senderOfControllerSender.socket);

				chunkCounter++;

			}

			// System.out.println("Chunk" + chunkCounter);
			// this.requestingClient.totalNumberOfChunk = chunkCounter - 1;
			// System.out.println("Size: " +
			// requestingClient.fileIntoChunk.size());
			return true;
		}
	}

	public void store(byte[] newByte) {
		// TODO Auto-generated method stub
		// System.out.println(new String(newByte));
		byte byteArrayCopy[];
		byteArrayCopy = Arrays.copyOf(newByte, newByte.length);
		// this.requestingClient.fileIntoChunk.add(byteArrayCopy);

	}

	public static void mergeFile(File f) throws FileNotFoundException {
		File ofile = new File(f.getName() + "_merged");
		FileOutputStream fos;
		FileInputStream fis;
		byte[] fileBytes;
		int bytesRead = 0;
		List<File> files = new ArrayList<>();
		long noOfChunks = (long) Math.ceil(f.length() / 1024 / 64f);
		System.out.println("-length-" + f.length() + "-no of chunks-"
				+ noOfChunks);
		for (long i = 0; i < noOfChunks; i++) {
			files.add(new File(f.getName() + "_"
					+ String.format("%03d", (i + 1))));
		}
		try {
			fos = new FileOutputStream(ofile, false);
			for (File file : files) {
				fis = new FileInputStream(file);
				fileBytes = new byte[(int) file.length()];
				bytesRead = fis.read(fileBytes, 0, (int) file.length());
				assert (bytesRead == fileBytes.length);
				assert (bytesRead == (int) file.length());
				fos.write(fileBytes);
				fos.flush();
				fileBytes = null;
				fis.close();
				fis = null;
			}
			fos.close();
			fos = null;

		} catch (Exception e) {

		}
	}

}