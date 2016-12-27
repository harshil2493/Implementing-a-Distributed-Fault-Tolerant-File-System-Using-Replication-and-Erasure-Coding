package util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import protocols.ChunkServerDataToClient;
import transport.TCPConnection;
import node.ChunkServer;

public class ChunkServerSendsDataToClient {
	String fileLoc;
	TCPConnection connectToClient;
	ChunkServer c;
	String fileNameActual;
	int chunk;
	int requestedClient;
	int inCon;
	ArrayList<Integer> badList = new ArrayList<Integer>();
	public ChunkServerSendsDataToClient(int clientID, String actualFileNameInString,
			int chunkAssociated, String actualLocation,
			TCPConnection connection, ChunkServer chunkServer, int n) {
		// TODO Auto-generated constructor stub
		this.fileNameActual = actualFileNameInString;
		this.chunk = chunkAssociated;
		this.fileLoc = actualLocation;
		this.connectToClient = connection;
		this.c = chunkServer;
		this.requestedClient = clientID;
		this.inCon = n;
	}

	public ChunkServerSendsDataToClient(int clientNeedFile,
			String fileReadingRequest, int chunkNo, ChunkServer chunkServer, String fileLocation, int in) {
		// TODO Auto-generated constructor stub
		this.fileNameActual = fileReadingRequest;
		this.chunk = chunkNo;
		this.c = chunkServer;
		this.requestedClient = clientNeedFile;
		this.fileLoc = fileLocation;
		this.inCon = in;
	}

	// @Override
	public void start() {
		// TODO Auto-generated method stub
		try {

			File file = new File(fileLoc);
			FileInputStream fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int) file.length()];
			fin.read(fileContent);
			// try
			// {
			boolean doSHACheck = doSHA(fileContent);

			// if(!doSHACheck)
			// {
			// //StartHealing
			// }
			// System.out.println("File: " + chunk + "\n");
			// System.out.println(new String(fileContent));
			fin.close();
			ChunkServerDataToClient chunkServerDataToClient = new ChunkServerDataToClient(
					c, fileContent, fileNameActual, chunk, doSHACheck);

			connectToClient.sender.sendData(chunkServerDataToClient.getByte());
			
			if(!doSHACheck)
			{
				
					StartHealing startHealing = new StartHealing(c, fileNameActual,
							chunk, this.badList, requestedClient, inCon);

					// Thread healingThread = new Thread(startHealing);
					startHealing.start();

				
			}
			// System.out.println(fileNameActual + chunk+"--------");
			// dataOutputStream.write(b);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			ChunkServerDataToClient chunkServerDataToClientE = new ChunkServerDataToClient(fileNameActual, chunk, 2);
			try {
				connectToClient.sender.sendData(chunkServerDataToClientE.getByte());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			ArrayList<Integer> badSlice = new ArrayList<Integer>();
			for(int i = 1 ; i<=8;i++)
			{
				badSlice.add(i);
			}
			StartHealing startHealing = new StartHealing(c, fileNameActual,
					chunk, badSlice, requestedClient, inCon);

			// Thread healingThread = new Thread(startHealing);
			startHealing.start();
			
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean doSHA(byte[] data) throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		boolean NoCorruption = true;
		String chunkLocation = c.defaultLocation + "/"
				+ fileNameActual.replace("/", "-").substring(1) + "_chunk"
				+ chunk;
		// System.out.println("MetaDeta" + c.metaDetaOfChunks);
//		System.out.println("Loc: " + chunkLocation + "Meta: " + c.metaDetaOfChunks);
//		System.out.println("Fetching  ...  Loc: " + chunkLocation + "Meta: " + " Info: " + c.metaDetaOfChunks.get(chunkLocation));
		String metadataInfo;
		synchronized (c.metaDetaOfChunks) {
			metadataInfo = c.metaDetaOfChunks.get(chunkLocation);
		}
//		String metadataInfo = c.metaDetaOfChunks.get(chunkLocation);
//		System.out.println(metadataInfo);
//		System.out.println("Loc: " + chunkLocation + "Meta: " + " Info: " + metadataInfo);
		metadataInfo = metadataInfo.substring(metadataInfo.indexOf("SHA:") + 5);
		String[] shaValue = metadataInfo.split("\n");
		int slices = (int) Math.ceil(data.length * 1.0 / (8 * 1024));
//		System.out.println("slices: " + slices);
		MessageDigest md = MessageDigest.getInstance("SHA1");
		// FileInputStream fis = new FileInputStream(datafile);
		byte[] dataBytes = data;
		int totalSlice = 0;
		// int slices = (int) Math.ceil((dataInBytes.length * 1.0) / (1024 *
		// 8));
		if (slices > 8) {
			slices = 8;
			totalSlice = 8;
		}
//		ArrayList<Integer> badSlice = new ArrayList<Integer>();
		for (int j = 0; j < slices; j++) {
			StringBuffer sb = new StringBuffer("");
			if (j == slices - 1)
				md.update(dataBytes, j * 1024 * 8, dataBytes.length
						- ((j) * 1024 * 8));
			else
				md.update(dataBytes, j * 1024 * 8, 1024 * 8);

			byte[] mdbytes = md.digest();

			// convert the byte to hex format
			// StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
			// sb.append("\n");
			// System.out.println("Computed: " + sb.toString());
			// System.out.println("Given: " + shaValue[j]);
			if (!shaValue[j].equals(sb.toString())) {
				NoCorruption = false;
				badList.add((j + 1));
				System.out.println("File: " + fileNameActual + " Chunk: "
						+ chunk + " False Slice: " + (j + 1));
			}
			// return null;
			// System.out.println("Digest(in hex format):: " + sb.toString());
		}
		totalSlice = slices;
		if (slices < shaValue.length) {
			for (int i = (slices + 1); i <= shaValue.length; i++) {
				badList.add((i));
			}
			totalSlice = shaValue.length;
		}
		// for(int i = 0; i < slices ; i++)
		// {
		// byte[] byteToCheck;
		// if(i == slices - 1)
		// {
		// byteToCheck = new byte[data.length % (1024*8)];
		// // byteToCheck = Arrays.copyOfRange(data, i * (1024*8), data.length -
		// 1);
		// System.out.println("byteToCheck Size" + byteToCheck.length);
		//
		//
		//
		// }
		// else
		// {
		// byteToCheck = new byte[1024*8];
		//
		// // byteToCheck = Arrays.copyOfRange(data, i * (1024*8), (i+1) * 1024
		// * 8 - 1);
		// System.out.println("byteToCheck Size" + byteToCheck.length);
		//
		// }
		// for(int q = 0 ; q < byteToCheck.length ; q++)
		// {
		// byteToCheck[q] = data[i*1024*8 + q];
		// }
		// MessageDigest md = MessageDigest.getInstance("SHA1");
		// // FileInputStream fis = new FileInputStream(datafile);
		//
		// StringBuffer sb = new StringBuffer("");
		// md.update(byteToCheck);
		//
		// byte[] mdbytes = md.digest();
		// for (int j = 0; j < mdbytes.length; j++) {
		// sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
		// .substring(1));
		// }
		// System.out.println("Computed SHA" + sb.toString());
		// System.out.println("Given" + shaValue[i]);
		//
		// }
		
		return NoCorruption;
	}

}
