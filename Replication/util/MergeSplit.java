package util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.Client;

public class MergeSplit {
	// public File file;
	public Client requestingClient;

	public MergeSplit(Client client) {
		// TODO Auto-generated constructor stub
		// this.file = neededFile;
		requestingClient = client;
	}

	public boolean splitFile(File f) throws IOException {

		int chunkCounter = 1;
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
				// byte[] newByte = bufferChunk;

				// store(newByte);
				byte byteArrayCopy[];
				byteArrayCopy = Arrays.copyOf(bufferChunk, tmp);
				this.requestingClient.fileIntoChunk.add(byteArrayCopy);
				bufferChunk = new byte[sizeOfChunk];

				chunkCounter++;

			}
			// System.out.println("Chunk" + chunkCounter);
			this.requestingClient.totalNumberOfChunk = chunkCounter - 1;
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
		this.requestingClient.fileIntoChunk.add(byteArrayCopy);

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