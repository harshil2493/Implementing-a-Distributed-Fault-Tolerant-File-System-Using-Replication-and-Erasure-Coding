package node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import protocols.ChunkServerDataToClient;
import protocols.Protocol;
import protocols.SendingDataToChunkServer;
import transport.TCPConnection;
import transport.TCPSender;
import transport.TCPServerThread;
import util.HeartBeat;

public class ChunkServer implements Node {

	public TCPSender senderToController;
	public int myID;
	public long numberOfShards;
	public Map<String, String> metaDetaOfChunks = new HashMap<String, String>();

	public File rootToStore = new File("/tmp/data_erasure");
	public String defaultLocation = "/tmp/data_erasure";
	public File fileInfo = new File("/tmp/data_fileInformations.txt");

	public Map<String, Map<Integer, ArrayList<Integer>>> maindataStructureOfChunkServer = new HashMap<String, Map<Integer, ArrayList<Integer>>>();

	// public int numberOfShards = 0;

	public String hostAddressOfController;
	public int portOfController;

	public ChunkServer(String host, String portNumber) throws IOException {
		this.hostAddressOfController = host;
		this.portOfController = Integer.parseInt(portNumber);

		numberOfShards = 0;
		if (!rootToStore.exists()) {
			rootToStore.mkdirs();
			fileInfo.createNewFile();
		} else {
			if (!fileInfo.exists())
				fileInfo.createNewFile();
		}
		// TODO Auto-generated constructor stub

		HeartBeat heartBeat = new HeartBeat(this);
		Thread heartThread = new Thread(heartBeat);
		// heartThread.start();
		startReceiver();
	}

	public static void main(String[] argumentsOfChunkServer) throws IOException {
		if (argumentsOfChunkServer.length == 2) {
			ChunkServer chunkServer = new ChunkServer(
					argumentsOfChunkServer[0], argumentsOfChunkServer[1]);

		} else {
			System.out
					.println("Some Problem In Argument.. Give Appropriate HostID & HostPortNumber");
		}

	}

	@Override
	public void onEvent(byte[] rawData, Socket s) throws Exception {
		try {
			// TODO Auto-generated method stub
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(
					rawData);
			DataInputStream din = new DataInputStream(new BufferedInputStream(
					baInputStream));
			byte type = din.readByte();
			// System.out.println("Type:  " + type);
			switch (type) {
			case Protocol.CONTROLLER_SENDS_REGISTRATION_STATUS:

				myID = din.readInt();
				if (myID != -1) {
					System.out.println("[INFO] Allocated ID: " + myID);
				} else {
					System.out
							.println("[ERROR] Another ChunkServer Is Running On Same Machine.. Exiting Program");
					System.exit(0);
				}
				break;
			case Protocol.SENDING_DATA_TO_CHUNKSERVER:
				// System.out.println("Data Coming In");

				int fileNameLength = din.readInt();
				byte[] fileNameInBytes = new byte[fileNameLength];
				din.readFully(fileNameInBytes);

				String fileNameAbsolute = new String(fileNameInBytes);
				int chunkNumber = din.readInt();
				int shardNumber = din.readInt();

				String directory = defaultLocation
						+ fileNameAbsolute.substring(0,
								fileNameAbsolute.lastIndexOf("/") + 1);
				// System.out.println(directory);
				File f = new File(directory);
				if (!f.exists()) {
					// System.out.println("executed?");
					f.mkdirs();

				}
				new File(fileNameAbsolute).createNewFile();
				// if (!listOfFilesStored.contains(fileNameAbsolute)) {
				// ArrayList<Integer> listOfChunkNumbers = new
				// ArrayList<Integer>();
				// synchronized (listOfChunks) {
				// listOfChunks.put(fileNameAbsolute, listOfChunkNumbers);
				//
				// }
				// listOfFilesStored.add(fileNameAbsolute);
				// String fileToStore = defaultLocation + fileNameAbsolute;
				//
				// File f = new File(fileToStore.substring(0,
				// fileToStore.lastIndexOf("/")));
				// if (!f.exists())
				// f.mkdirs();
				//
				// try {
				// // String filename= "MyFile.txt";
				// FileWriter fw = new FileWriter(fileInfo, true); // the true
				// // will
				// // append
				// // the new
				// // data
				// fw.write(fileNameAbsolute + "\n");// appends the string to
				// // the file
				// fw.close();
				// } catch (IOException ioe) {
				// System.err.println("IOException: " + ioe.getMessage());
				// }
				//
				// }
				// synchronized (listOfChunks) {
				// listOfChunks.get(fileNameAbsolute).add(chunkNumber);
				//
				// }
				// temporary.add(chunkNumber);
				// listOfChunks.replace(fileNameAbsolute, temporary);
				// System.out.println("Store.. " + fileNameAbsolute
				// + " Chunk Number: " + chunkNumber);
				// System.out.println(listOfChunks);
				int byteDataLength = din.readInt();
				byte[] dataInBytes = new byte[byteDataLength];
				din.readFully(dataInBytes);
				try {
					String file = defaultLocation + fileNameAbsolute + "_chunk"
							+ chunkNumber + "_shard" + shardNumber;
					String metaFile = defaultLocation + "/"
							+ fileNameAbsolute.replace("/", "-").substring(1)
							+ "_chunk" + chunkNumber + "_shard" + shardNumber;
					String SHA = generateSHA(dataInBytes);
					// String SHA = "";
					// System.out.println(metaFile);
					// System.out.println(SHA);
					File fileM = new File(metaFile);
					FileOutputStream fosM = new FileOutputStream(fileM);
					String metaString = "V:\n" + String.valueOf(1) + "\n"
							+ "S:\n" + chunkNumber + "\n" + "T:\n"
							+ new Timestamp(new Date().getTime()) + "\nSHA:\n"
							+ SHA;
					synchronized (metaDetaOfChunks) {
						metaDetaOfChunks.put(metaFile, metaString);
					}
//					System.out.println("Meta: " + metaDetaOfChunks);
					// System.out.println(metaString);
					// metaString = metaString + T
					byte[] metaDataInByte = metaString.getBytes();
					fosM.write(metaDataInByte);
					fosM.close();

					File fileS = new File(file);
					FileOutputStream fos = new FileOutputStream(fileS);
					fos.write(dataInBytes);
					fos.close();
					numberOfShards++;
				} catch (Exception e) {
					e.printStackTrace();
				}

				// }
				break;

			case Protocol.CLIENT_REQUESTING_CHUNKSERVER_FOR_READ:
				// System.out.println("Request Came/");

				int requestedClient = din.readInt();
				byte[] addressOfIP = new byte[din.readInt()];
				din.readFully(addressOfIP);
				String IPInS = new String(addressOfIP);
				int portOfC = din.readInt();

				// System.out.println("IP: " + IPInS + "Port"+ portOfC);
				// dout.writeInt(ID);
				byte[] fileInB = new byte[din.readInt()];
				din.readFully(fileInB);
				String fileToRead = new String(fileInB);
				int chunk = din.readInt();
				int shard = din.readInt();

				String fileLoc = defaultLocation + fileToRead + "_chunk"
						+ chunk + "_shard" + shard;
				File file = new File(fileLoc);
				
				try {

					FileInputStream fin = new FileInputStream(file);
					byte fileContent[] = new byte[(int) file.length()];
					fin.read(fileContent);
					
					String metaInfo = defaultLocation
							+ "/"
							+ (fileToRead + "_chunk" + chunk + "_shard" + shard)
									.replace("/", "-").substring(1);
					// try
					// {
					boolean doSHACheck = checkSHA(fileContent, metaInfo);

					// if(!doSHACheck)
					// {
					// //StartHealing
					// }
					// System.out.println("File: " + chunk + "\n");
					// System.out.println(new String(fileContent));
					
					System.out.println("Boolean" + doSHACheck);
					ChunkServerDataToClient chunkServerDataToClient = new ChunkServerDataToClient(
							this, fileContent, fileToRead, chunk, shard,
							doSHACheck);
					// Socket sT = new Socket(IPInS, portOfC);
					TCPSender senderT = new TCPSender(s);
					senderT.sendData(chunkServerDataToClient.getByte());

					// sT.close();
					// System.out.println(fileNameActual + chunk+"--------");
					// dataOutputStream.write(b);

				} catch (IOException e) {
					System.out.println("IOE CALLED");
					byte[] fileE = new byte[0];
					ChunkServerDataToClient chunkServerDataToClientNF = new ChunkServerDataToClient(
							this, fileE, fileToRead, chunk, shard,
							true);
//					ChunkServerDataToClient chunkServerDataToClientNF = new ChunkServerDataToClient();
					chunkServerDataToClientNF.flagForNotFound = 2;
					TCPSender senderTEF = new TCPSender(s);
					senderTEF.sendData(chunkServerDataToClientNF.getByte());
					// TODO Auto-generated catch block
//					e.printStackTrace();
				} catch (Exception e) {
					System.out.println("WEIRD");
					byte[] fileE = new byte[0];
					ChunkServerDataToClient chunkServerDataToClientE = new ChunkServerDataToClient(
							this, fileE, fileToRead, chunk, shard,
							true);
//					ChunkServerDataToClient chunkServerDataToClientE = new ChunkServerDataToClient();
					chunkServerDataToClientE.flagForNotFound = 3;
					TCPSender senderTE = new TCPSender(s);
					senderTE.sendData(chunkServerDataToClientE.getByte());
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
				finally
				{
					
				}
				break;

			default:
				System.out.println("UnUsual Activity");
				break;
			}
			din.close();
			baInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkSHA(byte[] fileContent, String metadataInfo)
			throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		boolean NoCorruption = true;
		// System.out.println("Meta" + metadataInfo);
		String meta;
		synchronized (metaDetaOfChunks) {
			meta = metaDetaOfChunks.get(metadataInfo);
		}
		// String metadataInfo = c.metaDetaOfChunks.get(chunkLocation);
		// System.out.println(metadataInfo);
		// System.out.println("Loc: " + chunkLocation + "Meta: " + " Info: " +
		// metadataInfo);
		meta = meta.substring(meta.indexOf("SHA:") + 5);

		MessageDigest md = MessageDigest.getInstance("SHA1");
		StringBuffer sb = new StringBuffer("");
		md.update(fileContent);

		byte[] mdbytes = md.digest();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		// System.out.println("Given" + meta);
		// System.out.println("Computed" + sb);

		// if(sb.)
		return sb.toString().equals(meta);
	}

	private String generateSHA(byte[] dataInBytes)
			throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		MessageDigest md = MessageDigest.getInstance("SHA1");
		StringBuffer sb = new StringBuffer("");
		md.update(dataInBytes);

		byte[] mdbytes = md.digest();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}

		return sb.toString();
	}

	public void startReceiver() {
		// TODO Auto-generated method stub
		// System.out.println(connectionToController);
		TCPServerThread serverChunkThread = new TCPServerThread("0", this);
		Thread threadServerChunkRunning = new Thread(serverChunkThread);
		threadServerChunkRunning.start();
	}

}
