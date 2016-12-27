package node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
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

import protocols.ChunkReplyToSuretyCheck;
import protocols.ClientRequestingChunkServerForRead;
import protocols.Protocol;
import protocols.SendingDataToChunkServer;
import protocols.SendingHealedFile;
import transport.TCPClientThread;
import transport.TCPConnection;
import transport.TCPSender;
import transport.TCPServerThread;
import util.ChunkServerSendsDataToClient;
import util.HeartBeat;
import util.StartHealing;

public class ChunkServer implements Node {

	public TCPConnection connectionToController;
	public String myPort;
	public int myID;
	public long numberOfChunks;
	public long freeSpaceAvailable;
	public File rootToStore = new File("/tmp/data");
	public String defaultLocation = "/tmp/data";
	public File fileInfo = new File("/tmp/data/fileInformations.txt");
	public List<String> listOfFilesStored = new ArrayList<String>();
	public Map<String, ArrayList<Integer>> listOfChunks = new HashMap<String, ArrayList<Integer>>();
	public Map<String, String> metaDetaOfChunks = new HashMap<String, String>();
	
	public ArrayList<String> newlyAddedChunks = new ArrayList<String>();
	
	public Map<Integer, TCPConnection> connectionToClient = new HashMap<Integer, TCPConnection>();
	public Object locking = new Object();
	public ChunkServer(String host, String portNumber, String i) throws IOException {
		numberOfChunks = 0;
		this.myPort = i;
		if (!rootToStore.exists()) {
			rootToStore.mkdirs();
			fileInfo.createNewFile();
		} else {
			if (!fileInfo.exists())
				fileInfo.createNewFile();
		}
		// TODO Auto-generated constructor stub
		TCPClientThread clientThreadThread = new TCPClientThread(host,
				portNumber, this);
		Thread threadClientRunning = new Thread(clientThreadThread);
		threadClientRunning.start();

		HeartBeat heartBeat = new HeartBeat(this);
		Thread heartThread = new Thread(heartBeat);
		heartThread.start();

	}

	public static void main(String[] argumentsOfCunkServer) throws IOException {
		if (argumentsOfCunkServer.length == 3) {
			
			ChunkServer chunkServer = new ChunkServer(argumentsOfCunkServer[0],
					argumentsOfCunkServer[1], (argumentsOfCunkServer[2]));
			

		} else {
			System.out
					.println("Some Problem In Argument.. Give Appropriate HostID & HostPortNumber");
		}

	}

	@Override
	public void onEvent(byte[] rawData) throws Exception {
		// TODO Auto-generated method stub
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(rawData);
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
//			System.out.println("Called?");
			numberOfChunks++;
			int fileNameLength = din.readInt();
			byte[] fileNameInBytes = new byte[fileNameLength];
			din.readFully(fileNameInBytes);

			String fileNameAbsolute = new String(fileNameInBytes);
			int chunkNumber = din.readInt();

			if (!listOfFilesStored.contains(fileNameAbsolute)) {
				ArrayList<Integer> listOfChunkNumbers = new ArrayList<Integer>();
				synchronized (listOfChunks) {
					listOfChunks.put(fileNameAbsolute, listOfChunkNumbers);

				}
				listOfFilesStored.add(fileNameAbsolute);
				String fileToStore = defaultLocation + fileNameAbsolute;

				File f = new File(fileToStore.substring(0,
						fileToStore.lastIndexOf("/")));
				if (!f.exists())
					f.mkdirs();

				try {
					// String filename= "MyFile.txt";
					FileWriter fw = new FileWriter(fileInfo, true); // the true
																	// will
																	// append
																	// the new
																	// data
					fw.write(fileNameAbsolute + "\n");// appends the string to
														// the file
					fw.close();
				} catch (IOException ioe) {
					System.err.println("IOException: " + ioe.getMessage());
				}

			}
			synchronized (listOfChunks) {
				listOfChunks.get(fileNameAbsolute).add(chunkNumber);

			}
			// temporary.add(chunkNumber);
			// listOfChunks.replace(fileNameAbsolute, temporary);
//			System.out.println("Store.. " + fileNameAbsolute
//					+ " Chunk Number: " + chunkNumber);
			// System.out.println(listOfChunks);
			int byteDataLength = din.readInt();
			byte[] dataInBytes = new byte[byteDataLength];
			din.readFully(dataInBytes);
			try {
				String file = defaultLocation + fileNameAbsolute + "_chunk"
						+ chunkNumber;
				synchronized (newlyAddedChunks) {
					newlyAddedChunks.add(file);
//					System.out.println("New Chunks" + newlyAddedChunks);

				}
				String metaFile = defaultLocation + "/"
						+ fileNameAbsolute.replace("/", "-").substring(1)
						+ "_chunk" + chunkNumber;
				String SHA = generateSHA(dataInBytes);
				// String SHA = "";
				// System.out.println(metaFile);
				// System.out.println(SHA);
				File fileM = new File(metaFile);
				FileOutputStream fosM = new FileOutputStream(fileM);
				String metaString = "V:\n" + String.valueOf(1) + "\n" + "S:\n"
						+ chunkNumber + "\n" + "T:\n"
						+ new Timestamp(new Date().getTime()) + "\nSHA:\n"
						+ SHA;
				synchronized (metaDetaOfChunks) {
					metaDetaOfChunks.put(metaFile, metaString);
				}
				
				// System.out.println(metaString);
				// metaString = metaString + T
				byte[] metaDataInByte = metaString.getBytes();
				fosM.write(metaDataInByte);
				fosM.close();

				File fileS = new File(file);
				FileOutputStream fos = new FileOutputStream(fileS);
				fos.write(dataInBytes);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			int sizeToSendForward = din.readInt();
			// System.out.println("Size To Send For: " + sizeToSendForward);
			if (sizeToSendForward != 0) {
				int nodeToSend = din.readInt();
				int neededIPLength = din.readInt();
				byte[] neededIPByte = new byte[neededIPLength];
				din.readFully(neededIPByte);
				String IP = new String(neededIPByte);
				int port = din.readInt();
				//
				// System.out.println("Next ID: " + nodeToSend);
				// System.out.println("next IP: " + IP);
				// System.out.println("Next Port:" + port);
				ArrayList<Integer> newList = new ArrayList<Integer>();
				Map<Integer, List<String>> newMap = new HashMap<Integer, List<String>>();
				for (int i = 0; i < sizeToSendForward - 1; i++) {
					int id = din.readInt();
					// System.out.println("next" + id);
					// newList.
					newList.add(id);
					int IPLength = din.readInt();
					byte[] IPByte = new byte[IPLength];
					din.readFully(IPByte);
					String IPS = new String(IPByte);
					String portS = String.valueOf(din.readInt());
					List<String> attachingList = new ArrayList<String>();
					attachingList.add(IPS);
					attachingList.add(portS);
					newMap.put(id, attachingList);

				}
				// System.out.println("Sending List: " + newList);
				// System.out.println("Map: " + newMap);
				SendingDataToChunkServer dataToChunkServer = new SendingDataToChunkServer(
						newMap, fileNameAbsolute, chunkNumber, dataInBytes,
						newList);

				Socket s = new Socket(InetAddress.getByName(IP.substring(1)),
						port);

				TCPConnection connectionFirst = new TCPConnection(this, s);
				connectionFirst.sender.sendData(dataToChunkServer.getByte());
				// DataOutputStream dataOutputStream = new
				// DataOutputStream(s.getOutputStream());
				// dataOutputStream.write(dataToChunkServer.getByte());

				s.close();
			}
			break;
		case Protocol.CONTROLLER_SENDS_SURETY_CHECK:
			ChunkReplyToSuretyCheck chunkReplyToSuretyCheck = new ChunkReplyToSuretyCheck(
					myID);
			connectionToController.sender.sendData(chunkReplyToSuretyCheck
					.getByte());
			break;
		case Protocol.CLIENT_REQUESTING_CHUNKSERVER_FOR_READ:
			synchronized (locking) {
				
			
			try
			{
			int clientID = din.readInt();

			int sizeOfIP = din.readInt();
			byte[] clientIPInBytes = new byte[sizeOfIP];
			din.readFully(clientIPInBytes);
			String clientIPInS = new String(clientIPInBytes);
			int portC = din.readInt();
			// System.out.println("Client: " + clientID + " IP: " + clientIPInS
			// + " Port: " + portC);
			TCPConnection connectClient;
			synchronized (connectionToClient) {
				if (connectionToClient.containsKey(clientID)) {
					connectClient = connectionToClient.get(clientID);
				} else {
					Socket s = new Socket(InetAddress.getByName(clientIPInS), portC);
					connectClient = new TCPConnection(this, s);
					connectionToClient.put(clientID, connectClient);
				}
			}
			
			int fileSizeForRead = din.readInt();
			byte[] fileNameInByte = new byte[fileSizeForRead];
			din.readFully(fileNameInByte);
			String fileReadingRequest = new String(fileNameInByte);
			// System.out.println("Request: " + fileReadingRequest);
			int chunkNo = din.readInt();
			// System.out.println(fileReadingRequest + " Chunk: " + chunkNo);

			String actualLocation = defaultLocation + fileReadingRequest
					+ "_chunk" + chunkNo;

			ChunkServerSendsDataToClient chunkServerSendsDataToClient = new ChunkServerSendsDataToClient(
					clientID, fileReadingRequest, chunkNo, actualLocation, connectClient,
					this, 0);
			// Thread sendingDataToClient = new
			// Thread(chunkServerSendsDataToClient);
//			chunkServerSendsDataToClient.inCon = 0;
			chunkServerSendsDataToClient.start();
//			System.out.println("[INFO] Client: "+clientID+" Requesting: "+fileReadingRequest + " Chunk: " + chunkNo);
			
			}
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			}
			// System.out.println("");
			break;
		case Protocol.CONTROLLER_TELLS_CHUNKSERVER_TO_HEAL:
			
			int nodeID = din.readInt();
			System.out.println(nodeID);
			int IPSize = din.readInt();
			byte[] IPInByte = new byte[IPSize];
			din.readFully(IPInByte);
			int Port = din.readInt();
			String IPInString = new String(IPInByte).substring(1);
			System.out.println("IPInString" + IPInString);
			int fileBadSize = din.readInt();
			
			byte[] fileBadInBytes = new byte[fileBadSize];
			din.readFully(fileBadInBytes);
			String fileBad = new String(fileBadInBytes);
			System.out.println(fileBad);
			int effectedChunk = din.readInt();
			System.out.println(effectedChunk);
			
			
//			File fileToCheckAgain = new File(defaultLocation + fileBad+"_chunk"+effectedChunk);
//			System.out.println("String: " + defaultLocation + fileBad+"_chunk"+effectedChunk);
//			FileInputStream fin = new FileInputStream(fileToCheckAgain);
//			byte fileContentFinal[] = new byte[(int) fileToCheckAgain.length()];
//			fin.read(fileContentFinal);
//			fin.close();
//			// try
//			// {
//			boolean doSHACheck = doSHA(fileContentFinal, fileBad, effectedChunk);
//			System.out.println("[INFO] Do I Have Real Copy?: " + doSHACheck);
			// if(!doSHACheck)
			// {
			// //StartHealing
			// }
			// System.out.println("File: " + chunk + "\n");
			// System.out.println(new String(fileContent));
//			
			
			int badSlices = din.readInt();
			ArrayList<Integer> listOfBadSlices = new ArrayList<Integer>();
			ArrayList<byte[]> lostData = new ArrayList<byte[]>();
			String fileLocation = defaultLocation + fileBad + "_chunk"
					+ effectedChunk;
			File f = new File(fileLocation);
			if(!f.exists()) f.createNewFile();
			int sliceAva = (int) Math.ceil(new File(fileLocation).length()
					* 1.0 / (1024 * 8));
			RandomAccessFile raf = new RandomAccessFile(fileLocation, "r");
			for (int i = 0; i < badSlices; i++) {
				int offset = din.readInt();
				listOfBadSlices.add(offset);

				raf.seek((offset - 1) * 1024 * 8);
				int defaultSize = 1024 * 8;
				if (offset == sliceAva)
					defaultSize = (int) (new File(fileLocation).length() - (offset - 1) * 1024 * 8);
//				else if(offset<sliceAva) defaultSize = 1024 * 8;
//				else defaultSize = 0;

				byte[] bytes = new byte[defaultSize];
				raf.read(bytes);
				// System.out.println("While Creating: Size: " + defaultSize);
				// System.out.println("ForSlice " + offset + " Data: " + new
				// String(bytes));
				lostData.add(bytes);

			}

			System.out.println("Node: " + nodeID + " IP: " + IPInString
					+ " PORT: " + Port + " BadFile: " + fileBad + " Chunk: "
					+ effectedChunk + " BadSlice: " + listOfBadSlices);
			raf.close();
			
			
			int clientNeedFile = din.readInt();
			
			int IPOfClientSize = din.readInt();
			byte[] IPOfClient = new byte[IPOfClientSize];
			din.readFully(IPOfClient);
			InetAddress INetOfIPClient = InetAddress.getByAddress(IPOfClient);
			
			
			int portOfClient = din.readInt();
			int inConsistencyLevel = din.readInt();
//			if(doSHACheck)
//			{
			
			Socket sTemp = new Socket(InetAddress.getByName(IPInString), Port);
			TCPConnection connectionTemporary = new TCPConnection(this, sTemp);

			SendingHealedFile sendingHealedFile = new SendingHealedFile(
					sliceAva, fileBadInBytes, effectedChunk, listOfBadSlices,
					lostData);
			connectionTemporary.sender.sendData(sendingHealedFile.getByte());

			sTemp.close();
//			System.out.println("[CONT] PORT: " + portOfClient);
//			Socket sTemporary = new Socket(INetOfIPClient, portOfClient);
//			TCPSender tempConnect = new TCPSender(sTemporary);
//			System.out.println("[INFO] Sending File To: " + INetOfIPClient+"\n[CONT] PORT: " + portOfClient);
			ClientRequestingChunkServerForRead controllerToRequest = new ClientRequestingChunkServerForRead(clientNeedFile, INetOfIPClient, portOfClient, fileBad, effectedChunk);
			this.onEvent(controllerToRequest.getByte());
			System.out.println("Sending");
//			sTemporary.close();
//			}
//			else
//			{
//				String fileLoc = defaultLocation + fileBad + "_chunk"
//						+ effectedChunk;
//				ChunkServerSendsDataToClient chunkServerSendsDataToClientI = new ChunkServerSendsDataToClient(
//						clientNeedFile, fileBad, effectedChunk, this, fileLoc, inConsistencyLevel);
////				// Thread sendingDataToClient = new
////				// Thread(chunkServerSendsDataToClient);
//////				chunkServerSendsDataToClientI.inCon = inConsistencyLevel;
//////				File fileStream = new File(fileLoc);
//////				FileInputStream finStream = new FileInputStream(fileStream);
//////				byte fileContent[] = new byte[(int) fileStream.length()];
//////				fin.read(fileContent);
////				// try
////				// {
//				chunkServerSendsDataToClientI.doSHA(fileContentFinal);
////
////				// if(!doSHACheck)
////				// {
////				// //StartHealing
////				// }
////				// System.out.println("File: " + chunk + "\n");
////				// System.out.println(new String(fileContent));
////				fin.close();
////				
////				chunkServerSendsDataToClientI.start();
//				System.out.println("[ERROR] Sorry.. I Do Have Invalid Copy Too");
////				
//			}
			break;
		case Protocol.SENDING_HEALED_FILE:
			byte[] fileToChangeByte = new byte[din.readInt()];
			din.readFully(fileToChangeByte);
			String fileToChange = new String(fileToChangeByte);
			int badChunk = din.readInt();
			System.out.println("[INFO] File " + fileToChange + " Chunk: "
					+ badChunk + " Will Be Updated..");
			int badSlicesFound = din.readInt();
			ArrayList<Integer> badS = new ArrayList<Integer>();
			ArrayList<byte[]> dataS = new ArrayList<byte[]>();
			for (int slice = 0; slice < badSlicesFound; slice++) {
				badS.add(din.readInt());
				int sizeOfD = din.readInt();
//				System.out.println("Read Size: " + sizeOfD);
				byte[] data = new byte[sizeOfD];
				din.readFully(data);
				// System.out.println("Slice: " + slice + " Data: " + new
				// String(data));
				dataS.add(data);
			}
			// System.out.println(badS);

			String chunkLocation = defaultLocation + fileToChange + "_chunk"
					+ badChunk;
			ArrayList<byte[]> finalDataOfSlice = new ArrayList<byte[]>();

			File chunkFile = new File(chunkLocation);
			
			if(!chunkFile.exists()) chunkFile.createNewFile();
			int slice = din.readInt();
			// System.out.println("Slices: " + slice);
			RandomAccessFile accessFile = new RandomAccessFile(chunkFile, "r");
			for (int i = 1; i <= slice; i++) {
				if (badS.contains(i)) {
					finalDataOfSlice.add(dataS.get(badS.indexOf(i)));
				} else {
					byte[] temporary = new byte[1024 * 8];
					accessFile.seek((i - 1) * 1024 * 8);
					accessFile.read(temporary);
					finalDataOfSlice.add(temporary);

				}
			}

			accessFile.close();
			chunkFile.renameTo(new File(chunkLocation+"_old_corrupted"));
			// chunkFile.delete();

			// chunkFile.createNewFile();
			FileWriter fw = new FileWriter(chunkLocation,
					true); // the true
			// will
			// append
			// the new
			// data
			for (byte[] dataToWriteBytes : finalDataOfSlice) {
				fw.write(new String(dataToWriteBytes));// appends the string to
				// the file
			}
			fw.close();
			System.out.println("[INFO] Healing Completed For :" + fileToChange
					+ " Chunk: " + badChunk);
			break;
		case Protocol.REQUESTING_CHUNKSERVER_TO_RESEND_DATA:
//			System.out.println("CALLED");
			int nodeIDToResend = din.readInt();
//			System.out.println("NODE: " + nodeIDToResend);
			byte[] IPString = new byte[din.readInt()];
			din.readFully(IPString);
//			System.out.println("IP" + new String(IPString));
			int portOfResend = din.readInt();
//			System.out.println("PORT" + portOfResend);
			byte[] fileNameInBRS = new byte[din.readInt()];
			din.readFully(fileNameInBRS);
			
			try
			{
			File fileRS = new File(defaultLocation + new String(fileNameInBRS));
			FileInputStream finRS = new FileInputStream(fileRS);
			byte fileContent[] = new byte[(int) fileRS.length()];
			finRS.read(fileContent);
			finRS.close();
			String[] fileD = new String(fileNameInBRS).split("_chunk");
			ArrayList<Integer> nullList = new ArrayList<Integer>();
			Map<Integer, List<String>> nullIPPort = new HashMap<Integer, List<String>>();
			SendingDataToChunkServer toChunkServer = new SendingDataToChunkServer(nullIPPort, fileD[0], Integer.parseInt(fileD[1]), fileContent, nullList);
			Socket sTemporaryFinal = new Socket(new String(IPString), portOfResend);
			TCPSender sender = new TCPSender(sTemporaryFinal);
			sender.sendData(toChunkServer.getByte());
			
			System.out.println("Request For Resend To " + nodeIDToResend + " File: " + fileD[0] + " Chunk: " +fileD[1]);
			}
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			break;
		default:
			System.out.println("UnUsual Activity");
			break;
		}
		din.close();
		baInputStream.close();
	}
	private boolean doSHA(byte[] data, String fileBad, int effectedChunk) throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		boolean NoCorruption = true;
		String chunkLocation = defaultLocation + "/"
				+ fileBad.replace("/", "-").substring(1) + "_chunk"
				+ effectedChunk;
		// System.out.println("MetaDeta" + c.metaDetaOfChunks);
//		System.out.println("Loc: " + chunkLocation + "Meta: " + c.metaDetaOfChunks);
//		System.out.println("Fetching  ...  Loc: " + chunkLocation + "Meta: " + " Info: " + c.metaDetaOfChunks.get(chunkLocation));
		String metadataInfo;
		synchronized (metaDetaOfChunks) {
			metadataInfo = metaDetaOfChunks.get(chunkLocation);
		}
//		String metadataInfo = c.metaDetaOfChunks.get(chunkLocation);
//		System.out.println(metadataInfo);
//		System.out.println("Loc: " + chunkLocation + "Meta: " + " Info: " + metadataInfo);
		metadataInfo = metadataInfo.substring(metadataInfo.indexOf("SHA:") + 5);
		String[] shaValue = metadataInfo.split("\n");
		int slices = (int) Math.ceil(data.length * 1.0 / (8 * 1024));
		System.out.println("slices: " + slices);
		MessageDigest md = MessageDigest.getInstance("SHA1");
		// FileInputStream fis = new FileInputStream(datafile);
		byte[] dataBytes = data;
//		int totalSlice = 0;
		// int slices = (int) Math.ceil((dataInBytes.length * 1.0) / (1024 *
		// 8));
		if (slices > 8) {
			slices = 8;
//			totalSlice = 8;
		}
		ArrayList<Integer> badSlice = new ArrayList<Integer>();
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
				break;
//				badSlice.add((j + 1));
//				System.out.println("File: " + fileBad + " Chunk: "
//						+ effectedChunk + " False Slice: " + (j + 1));
			}
			// return null;
			// System.out.println("Digest(in hex format):: " + sb.toString());
		}
//		totalSlice = slices;
//		if (slices < shaValue.length) {
//			for (int i = (slices + 1); i <= shaValue.length; i++) {
//				badSlice.add((i));
//			}
//			totalSlice = shaValue.length;
//		}
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
	private String generateSHA(byte[] dataInBytes)
			throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		MessageDigest md = MessageDigest.getInstance("SHA1");
		// FileInputStream fis = new FileInputStream(datafile);
		byte[] dataBytes = dataInBytes;
		StringBuffer sb = new StringBuffer("");
		int slices = (int) Math.ceil((dataInBytes.length * 1.0) / (1024 * 8));
		for (int j = 0; j < slices; j++) {
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
			sb.append("\n");
			// return null;
			// System.out.println("Digest(in hex format):: " + sb.toString());
		}
		return sb.toString();
	}

	public void startReceiver() {
		// TODO Auto-generated method stub
		// System.out.println(connectionToController);
		TCPServerThread serverChunkThread = new TCPServerThread(myPort, this);
		Thread threadServerChunkRunning = new Thread(serverChunkThread);
		threadServerChunkRunning.start();
	}

}
