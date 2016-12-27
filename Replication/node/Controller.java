package node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import protocols.ClientRequestingChunkServerForRead;
import protocols.ControllerRespondingToReadRequest;
import protocols.ControllerSendsChunkServerList;
import protocols.ControllerSendsRegistartionStatus;
import protocols.ControllerTellsChunkServerToHeal;
import protocols.Protocol;
import transport.TCPConnection;
import transport.TCPServerThread;
import util.ReduceCounterThread;
import util.RequestForReadHandler;

public class Controller implements Node {

	public Map<Integer, Map<String, Integer>> hostAddressAndPortOfChunkServer = new HashMap<Integer, Map<String, Integer>>();
	public Map<Integer, String> hostChunkServer = new HashMap<Integer, String>();
	public Map<Integer, TCPConnection> IDToConnnection = new HashMap<Integer, TCPConnection>();
	
	public List<Integer> deadNodes = new ArrayList<Integer>();
	
	public Map<Integer, Map<String, Integer>> hostAddressAndPortOfClient = new HashMap<Integer, Map<String, Integer>>();
	// public Map<Integer, String> hostChunkServer = new HashMap<Integer,
	// String>();
	public Map<Integer, TCPConnection> IDToConnnectionClient = new HashMap<Integer, TCPConnection>();

	public List<Integer> mayBeDead = new ArrayList<Integer>();
	public List<Integer> counterForMayBeDead = new ArrayList<Integer>();
	public List<Integer> counterForChunks = new ArrayList<Integer>();
	public Map<Integer, Set<String>> filesListOnChunks = new HashMap<Integer, Set<String>>();
	public Map<Integer, Integer> startAfterCounter = new HashMap<Integer, Integer>();
	public Map<Integer, ArrayList<ControllerSendsChunkServerList>> chunkServerListFinal = new HashMap<Integer, ArrayList<ControllerSendsChunkServerList>>();
	// public int chunkSize = - 1024*64;
	// public Map<Integer, String> IDToFileName = new HashMap<Integer,
	// String>();
	// Number Of Chunks, ChunkIDForOffset

	// public Map<String, Integer> FileID = new HashMap<String, Integer>();
	public List<String> filesHolder = new ArrayList<String>();
	public Map<String, HashMap<Integer, Integer>> FileToChunks = new HashMap<String, HashMap<Integer, Integer>>();
	// List Of CS
	public Map<Integer, List<Integer>> OffsetToChunkServersList = new HashMap<Integer, List<Integer>>();

	public Map<Integer, Map<String, List<Integer>>> chunkServerTochunks = new HashMap<Integer, Map<String, List<Integer>>>();

	int timeLimit = 40000;

	public Map<Integer, Long> numberOfChunksOnEachChunk = new HashMap<Integer, Long>();
	public Map<Integer, Long> freeSpaceOnEachChunk = new HashMap<Integer, Long>();

	public Map<Integer, ArrayList<Integer>> clientKnowsCS = new HashMap<Integer, ArrayList<Integer>>();

	public int replicationLevel = 3;
	public int chunkSize = 1024 * 64;

	public Object object = new Object();

	private Controller(String stringPortNumber) {
		// TODO Auto-generated constructor stub
		TCPServerThread serverThread = new TCPServerThread(stringPortNumber,
				this);
		Thread threadServerRunning = new Thread(serverThread);
		threadServerRunning.start();

		ReduceCounterThread counterThread = new ReduceCounterThread(this);
		Thread threadToKeepTimer = new Thread(counterThread);
		threadToKeepTimer.start();

	}

	public static void main(String[] argumentsOfController) {
		if (argumentsOfController.length == 1) {
			Controller controller = new Controller(argumentsOfController[0]);

		} else {
			System.out
					.println("Some Problem In Argument.. Give Appropriate PortNumber");
		}

	}

	@Override
	public void onEvent(byte[] rawData) throws Exception {
		// System.out.println("Event???");
		// TODO Auto-generated method stub
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(rawData);
		DataInputStream din = new DataInputStream(new BufferedInputStream(
				baInputStream));
		byte type = din.readByte();
		switch (type) {
		case Protocol.CHUNK_SERVER_SENDS_REGISTRATION:

			int IPAddressLength = din.readInt();
			byte[] IPAddress = new byte[IPAddressLength];
			din.readFully(IPAddress);
			InetAddress IP = InetAddress.getByAddress(IPAddress);

			// System.out.println(IP.toString());

			int portNumberOfChunk = din.readInt();
			// System.out.println(portNumberOfChunk);
			if (!hostChunkServer.containsValue(IP.toString())) {

				Map<String, Integer> hostAndPort = new HashMap<String, Integer>();
				hostAndPort.put(IP.toString(), portNumberOfChunk);

				Map<String, List<Integer>> importantToCS = new HashMap<String, List<Integer>>();
				int ID;
				synchronized (hostAddressAndPortOfChunkServer) {
					ID = hostAddressAndPortOfChunkServer.size() + 1;
				}
				// int ID = hostAddressAndPortOfChunkServer.size() + 1;
				chunkServerTochunks.put(ID, importantToCS);
				hostChunkServer.put(ID, IP.toString());
				hostAddressAndPortOfChunkServer.put(ID, hostAndPort);
				Set<String> filesWillBe = new HashSet<String>();
				filesListOnChunks.put(ID, filesWillBe);
				try {
					synchronized (counterForChunks) {
						counterForChunks.add(timeLimit);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				freeSpaceOnEachChunk.put(ID, (long) 0);
				// System.out.println(freeSpaceOnEachChunk);
				numberOfChunksOnEachChunk.put(ID, (long) 0);
				// System.out.println(counterForChunks);
				// System.out.println(hostAddressAndPortOfChunkServer);
				createConnectionToCS(ID, IP, portNumberOfChunk);
			} else {
				createConnectionToCS(-1, IP, portNumberOfChunk);
			}
			break;
		case Protocol.CLIENT_SENDS_REGISTRATION:
			int IPAddressLengthOfClient = din.readInt();
			byte[] IPAddressClient = new byte[IPAddressLengthOfClient];
			din.readFully(IPAddressClient);
			InetAddress IPClient = InetAddress.getByAddress(IPAddressClient);

			// System.out.println(IP.toString());

			int portNumberOfClient = din.readInt();
			// System.out.println(portNumberOfChunk);

			Map<String, Integer> hostAndPort = new HashMap<String, Integer>();
			hostAndPort.put(IPClient.toString(), portNumberOfClient);

			int IDOfClient = hostAddressAndPortOfClient.size() + 1;

			// hostChunkServer.put(ID, IP.toString());
			hostAddressAndPortOfClient.put(IDOfClient, hostAndPort);

			ArrayList<ControllerSendsChunkServerList> importantArrayList = new ArrayList<ControllerSendsChunkServerList>();
			startAfterCounter.put(IDOfClient, 0);
			chunkServerListFinal.put(IDOfClient, importantArrayList);
			// System.out.println(counterForChunks);
			// System.out.println(hostAddressAndPortOfChunkServer);
			createConnectionToClient(IDOfClient, IPClient, portNumberOfClient);
			break;
		case Protocol.CHUNK_SERVER_SENDS_MINOR_HEARTBEAT:
			int IDGot = din.readInt();
			if (mayBeDead.contains(IDGot)) {
				counterForMayBeDead.remove(mayBeDead.indexOf(IDGot));

				mayBeDead.remove(mayBeDead.indexOf(IDGot));

			}
			// System.out.println("[INFO] Minor HeartBeat From " + IDGot);
			try {
				synchronized (counterForChunks) {
					counterForChunks.set(IDGot - 1, timeLimit);
				}
				synchronized (freeSpaceOnEachChunk) {
					// freeSpaceOnEachChunk.put(IDGot, din.readLong());
					freeSpaceOnEachChunk.replace(IDGot, din.readLong());
					// System.out.println(freeSpaceOnEachChunk);
				}
				synchronized (numberOfChunksOnEachChunk) {
					numberOfChunksOnEachChunk.replace(IDGot, din.readLong());
					// System.out.println(numberOfChunksOnEachChunk);
				}
				synchronized (filesListOnChunks) {
					int loop = din.readInt();
					for(int i = 0 ; i < loop ; i++)
					{
						int size = din.readInt();
						byte[] newF = new byte[size];
						din.readFully(newF);
						filesListOnChunks.get(IDGot).add(new String(newF));
					}
//					System.out.println("File: " + filesListOnChunks);
				}
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			break;

		case Protocol.CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT:
			int IDGotMajor = din.readInt();
			if (mayBeDead.contains(IDGotMajor)) {
				counterForMayBeDead.remove(mayBeDead.indexOf(IDGotMajor));

				mayBeDead.remove(mayBeDead.indexOf(IDGotMajor));

			}
			// System.out.println("[INFO] Major HeartBeat From " + IDGotMajor);
			try {
				synchronized (counterForChunks) {
					counterForChunks.set(IDGotMajor - 1, timeLimit);
				}
				
				synchronized (freeSpaceOnEachChunk) {
					// freeSpaceOnEachChunk.put(IDGot, din.readLong());
					freeSpaceOnEachChunk.replace(IDGotMajor, din.readLong());
					// System.out.println(freeSpaceOnEachChunk);
				}
				synchronized (numberOfChunksOnEachChunk) {
					numberOfChunksOnEachChunk.replace(IDGotMajor, din.readLong());
					// System.out.println(numberOfChunksOnEachChunk);
				}
				
//				int loopFirst = dout.writeInt(chunkServer.listOfChunks.size());
//				for(String f : chunkServer.listOfChunks.keySet())
//				{
//					byte[] fB = f.getBytes();
//					dout.writeInt(fB.length);
//					dout.write(fB);
//					dout.writeInt(chunkServer.listOfChunks.get(f).size());
//					for(int chunk : chunkServer.listOfChunks.get(f))
//					{
//						dout.writeInt(chunk);
//					}
//				}
				int loopFirst = din.readInt();
				for(int outer = 0; outer<loopFirst;outer++)
				{
					int fileNameSize = din.readInt();
					byte[] fileNameB = new byte[fileNameSize];
					din.readFully(fileNameB);
					
					int loopSecond = din.readInt();
					for(int inner = 0 ; inner < loopSecond ; inner++)
					{
						int chunkNumber = din.readInt();
						
				//		System.out.println("ID: " + IDGotMajor + "FileName: " + new String(fileNameB) + "Chunk: " + chunkNumber);
					}
							
				}
				
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			break;

		case Protocol.CHUNK_REPLY_TO_SURETY_CHECK:
			int notDown = din.readInt();
			System.out.println("[INFO] May Be, Node " + notDown
					+ " Is Not Down");

			counterForMayBeDead.remove(mayBeDead.indexOf(notDown));
			mayBeDead.remove(mayBeDead.indexOf(notDown));
			break;
		case Protocol.CLIENT_REQUESTING_CHUNKSERVER:
			// System.out.println("Called?>");
			int reqClientID = din.readInt();
			int totalChunk = din.readInt();

			int fileNameLength = din.readInt();
			byte[] fileNameInBytes = new byte[fileNameLength];
			din.readFully(fileNameInBytes);
			String fileNameInString = new String(fileNameInBytes);
			if (!filesHolder.contains(fileNameInString)) {
				filesHolder.add(fileNameInString);
				HashMap<Integer, Integer> putInFileToChunks = new HashMap<Integer, Integer>();
				FileToChunks.put(fileNameInString, putInFileToChunks);
				// public Map<String, HashMap<Integer, Integer>> FileToChunks =
				// new HashMap<String, HashMap<Integer, Integer>>();
				// List Of CS
				// public Map<Integer, List<Integer>> OffsetToChunkServersList =
				// new HashMap<Integer, List<Integer>>();
				//
				// public Map<Integer, Map<String, HashMap<Integer, Integer>>>
				// chunkServerTochunks = new HashMap<Integer,
				// Map<String,HashMap<Integer,Integer>>>();

			}
			int chunkNumber = din.readInt();
			// System.out.println("Node: " + reqClientID + "File: "
			// + new String(fileNameInBytes) + "Chunk: " + chunkNumber);

			// System.out.println("Got Request");
			getBest3Locations(reqClientID, fileNameInString, chunkNumber,
					totalChunk);
			// System.out.println("Node: " + reqClientID +"File: " + new
			// String(fileNameInBytes) + "Chunk: " + chunkNumber);
			break;
		case Protocol.REQUESTING_READ_TO_CONTROLLER:
			int clientID = din.readInt();
			int sizeFile = din.readInt();
			byte[] fileByte = new byte[sizeFile];
			din.readFully(fileByte);
			String reqFileName = new String(fileByte);
			System.out.println("[INFO] Client: " + clientID + " Requested: "
					+ reqFileName);

			// public List<String> filesHolder = new ArrayList<String>();
			// public Map<String, HashMap<Integer, Integer>> FileToChunks = new
			// HashMap<String, HashMap<Integer, Integer>>();
			// //List Of CS
			// public Map<Integer, List<Integer>> OffsetToChunkServersList = new
			// HashMap<Integer, List<Integer>>();

			if (filesHolder.contains(reqFileName)) {
				RequestForReadHandler requestForReadHandler = new RequestForReadHandler(
						clientID, reqFileName, this);
				Thread readHandler = new Thread(requestForReadHandler);
				readHandler.start();
			} else {
				System.out.println("[ERROR] File " + reqFileName
						+ "Does Not Belong To This Cluster");
				ControllerRespondingToReadRequest controllerRespondingToReadRequest = new ControllerRespondingToReadRequest();
				controllerRespondingToReadRequest.status = -1;
				IDToConnnectionClient.get(clientID).sender
						.sendData(controllerRespondingToReadRequest.getByte());
			}
			break;
		case Protocol.CHUNKSERVER_REQUESTS_CONTROLLER_FOR_HEALING:
			int nodeID = din.readInt();

			int fileBadSize = din.readInt();
			byte[] fileBadInBytes = new byte[fileBadSize];
			din.readFully(fileBadInBytes);
			String fileBad = new String(fileBadInBytes);

			int effectedChunk = din.readInt();

			int badSlices = din.readInt();
			ArrayList<Integer> listOfBadSlices = new ArrayList<Integer>();
			for (int i = 0; i < badSlices; i++) {
				listOfBadSlices.add(din.readInt());
			}
			ArrayList<Integer> shuffle = new ArrayList<Integer>();
			shuffle.add(0);
			shuffle.add(1);
			shuffle.add(2);
			ArrayList<Integer> needed = (ArrayList<Integer>) OffsetToChunkServersList
					.get(FileToChunks.get(fileBad).get(effectedChunk));
//			shuffle.remove(needed.indexOf(nodeID));
			int nextIndex =  needed.indexOf(nodeID) + 1;
			if(nextIndex == 3) nextIndex = 0;
			// Collections.shuffle(shuffle);
			int responseCS = needed.get(shuffle.get(nextIndex));
			Set<String> IPToNodeID = hostAddressAndPortOfChunkServer
					.get(nodeID).keySet();
			String IPToString = (String) IPToNodeID.toArray()[0];
			int portOfNode = hostAddressAndPortOfChunkServer.get(nodeID).get(
					IPToString);

//			System.out.println("IP: " + IPToString + " Port: " + portOfNode);
			System.out.println("[INFO] Requesting: " + responseCS + " For Healing Of " + fileBad + " Chunk: " + effectedChunk);
			
			
			
			int clientNeededFile = din.readInt();
			
			int inLevel = din.readInt() + 1;
			System.out.println("[INFO] Inconsistency Level : " + inLevel);
			Object[] IPObjects = hostAddressAndPortOfClient.get(clientNeededFile).keySet().toArray();
			String IPOfClient = (String) IPObjects[0];
//			System.out.println("Client: " + IPOfClient);
//			System.out.println("PORT: " + hostAddressAndPortOfClient.get(clientNeededFile).get(IPOfClient));
			InetAddress IPClientInet = InetAddress.getByName(IPOfClient.substring(1));
			int PortOfClientNeed = hostAddressAndPortOfClient.get(clientNeededFile).get(IPOfClient);
			
			ControllerTellsChunkServerToHeal chunkServerToHeal = new ControllerTellsChunkServerToHeal(
					nodeID, IPToString, portOfNode, fileBad, effectedChunk,
					listOfBadSlices, clientNeededFile, IPClientInet, PortOfClientNeed, inLevel);
			IDToConnnection.get(responseCS).sender.sendData(chunkServerToHeal
					.getByte());
//			int clientNeededFile = din.readInt();
			
			
			
//			ResendingBadFileToClient resendingBadFileToClient = new ResendingBadFileToClient(clientNeededFile, hostAddressAndPortOfClient, fileBad, effectedChunk);
			
			
			// List Of CS
			
			int responseCSToSendCorrectToClient = needed.get(shuffle.get(nextIndex));
//			IDToConnnection.get(responseCSToSendCorrectToClient).sender.sendData(resendingBadFileToClient
//					.getByte());
//			System.out.println("Responsible" + responseCSToSendCorrectToClient);
//			System.out.println(hostAddressAndPortOfClient.get(clientNeededFile));
//			Object[] IPObjects = hostAddressAndPortOfClient.get(clientNeededFile).keySet().toArray();
//			String IPOfClient = (String) IPObjects[0];
////			System.out.println("Client: " + IPOfClient);
////			System.out.println("PORT: " + hostAddressAndPortOfClient.get(clientNeededFile).get(IPOfClient));
//			InetAddress IPClientInet = InetAddress.getByName(IPOfClient.substring(1));
//			System.out.println("INet: " + IPClientInet);
			
//			ClientRequestingChunkServerForRead controllerToRequest = new ClientRequestingChunkServerForRead(clientNeededFile, IPClientInet, hostAddressAndPortOfClient.get(clientNeededFile).get(IPOfClient), fileBad, effectedChunk);
//			IDToConnnection.get(responseCSToSendCorrectToClient).sender.sendData(controllerToRequest
//					.getByte());
			
			System.out.println("[INFO] Requesting "+responseCSToSendCorrectToClient + " For Sending Correct Copy Of " + fileBad + " Chunk: " + effectedChunk +" To Client Too.. Client ID: " + clientNeededFile);


			break;
		default:
			System.out.println("[INFO] UnWanted Message Is Coming Away");
			break;
		}
		din.close();
		baInputStream.close();
	}

	private void getBest3Locations(int reqClientID, String fileNameInString,
			int chunkNumber, int totalChunk) throws Exception {
		// System.out.println("Hello");
		// System.out.println(freeSpaceOnEachChunk);
		// TreeMap<Integer, Long> sortedMap = SortByValue(freeSpaceOnEachChunk);
		List<Long> chunksorted = new ArrayList<Long>(
				freeSpaceOnEachChunk.values());
		Collections.sort(chunksorted);
		// System.out.println(chunksorted);
		ArrayList<Integer> probableList = new ArrayList<Integer>();
		int maxSize = chunksorted.size();

		if (maxSize >= replicationLevel + 3) {
			try {
				synchronized (freeSpaceOnEachChunk) {
					for (Entry<Integer, Long> entry : freeSpaceOnEachChunk
							.entrySet()) {
						if (entry.getValue().equals(
								chunksorted.get(maxSize - 1))) {
							probableList.add(entry.getKey());
						} else if (entry.getValue().equals(
								chunksorted.get(maxSize - 2))) {
							probableList.add(entry.getKey());
						} else if (entry.getValue().equals(
								chunksorted.get(maxSize - 3))) {
							probableList.add(entry.getKey());
						} else if (entry.getValue().equals(
								chunksorted.get(maxSize - 4))) {
							probableList.add(entry.getKey());
						} else if (entry.getValue().equals(
								chunksorted.get(maxSize - 5))) {
							probableList.add(entry.getKey());
						} else if (entry.getValue().equals(
								chunksorted.get(maxSize - 6))) {
							probableList.add(entry.getKey());
						}
					}
				}

				Collections.shuffle(probableList);
				// System.out.println("Store" + chunkNumber + " On " +
				// probableList.subList(0, replicationLevel));

				// ArrayList<Long> changeList = new ArrayList<Long>();
				// for(int i : probableList)
				// {
				// changeList.add(freeSpaceOnEachChunk.get(i) - chunkSize);
				// }
				ArrayList<Integer> mainList = new ArrayList<Integer>();
				ArrayList<Integer> needToSendIPPort = new ArrayList<Integer>();
				if (clientKnowsCS.get(reqClientID) == null) {
					ArrayList<Integer> IDs = new ArrayList<Integer>();
					clientKnowsCS.put(reqClientID, IDs);
				}
				for (int i = 0; i < replicationLevel; i++) {
					mainList.add(probableList.get(i));
					if (!clientKnowsCS.get(reqClientID).contains(
							probableList.get(i))) {
						clientKnowsCS.get(reqClientID).add(probableList.get(i));
						needToSendIPPort.add(probableList.get(i));
						// clientKnowsCS.replace(reqClientID, value)
					}
				}
				for (int i : mainList) {
					freeSpaceOnEachChunk.replace(i, freeSpaceOnEachChunk.get(i)
							- chunkSize);
				}

				// System.out.println("Executed");
				try {

					OffsetToChunkServersList.put(
							OffsetToChunkServersList.size(), mainList);
					FileToChunks.get(fileNameInString).put(chunkNumber,
							OffsetToChunkServersList.size() - 1);
					for (int cs : mainList) {
						// List<Integer> listOfFile;
						if (!chunkServerTochunks.get(cs).containsKey(
								fileNameInString)) {
							// listOfFile =
							// chunkServerTochunks.get(cs).get(fileNameInString);
							List<Integer> listOfFile = new ArrayList<Integer>();
							chunkServerTochunks.get(cs).put(fileNameInString,
									listOfFile);
						}
						chunkServerTochunks.get(cs).get(fileNameInString)
								.add(chunkNumber);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				ControllerSendsChunkServerList chunkServerList = new ControllerSendsChunkServerList(
						this, chunkNumber, mainList, needToSendIPPort);
				// System.out.println("Wait..");
				chunkServerListFinal.get(reqClientID).add(chunkServerList);
				if (startAfterCounter.get(reqClientID) + 1 == totalChunk) {
					FileToChunks.get(fileNameInString).put(0, totalChunk);
					// System.out.println(FileToChunks);
					// System.out.println(OffsetToChunkServersList);
					// System.out.println(chunkServerTochunks);
					for (ControllerSendsChunkServerList i : chunkServerListFinal
							.get(reqClientID)) {
						IDToConnnectionClient.get(reqClientID).sender
								.sendData(i.getByte());
					}
					startAfterCounter.replace(reqClientID, 0);
					ArrayList<ControllerSendsChunkServerList> newImp = new ArrayList<ControllerSendsChunkServerList>();
					chunkServerListFinal.replace((reqClientID), newImp);
				} else {
					startAfterCounter.replace(reqClientID,
							(startAfterCounter.get(reqClientID) + 1));
					// System.out.println(startAfterCounter);
				}

				// if()
				// System.out.println(clientKnowsCS);
				// System.out.println(IDToConnnectionClient.get(reqClientID));

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out
					.println("[ERROR] Minimum Replication Will Not Work Efficiently.. Add More ChunkServer And Try Again");
		}
		// Collections.sort(chunksorted);
		// System.out.println(chunksorted);
		// TODO Auto-generated method stub

		// Collections collections =
		// Collections.s(freeSpaceOnEachChunk.values());

		// IDToConnnectionClient.get(reqClientID).sender.sendData(data);
		// System.out.println("Got?");
		// System.out.println(IDToConnnectionClient.get(reqClientID - 1));

	}

	private void createConnectionToClient(int IDOfClient, InetAddress IPClient,
			int portNumberOfClient) throws Exception {
		// TODO Auto-generated method stub
		Socket socketToSend = new Socket(IPClient, portNumberOfClient);
		TCPConnection connectionToC = new TCPConnection(this, socketToSend);
		if (IDOfClient != -1) {
			IDToConnnectionClient.put(IDOfClient, connectionToC);
			System.out
					.println("[INFO] Client Registration. . . Successful. ID: "
							+ IDOfClient);
		} else {
			System.out.println("[INFO] Registration. . . Unsuccessful. ");

		}
		ControllerSendsRegistartionStatus controllerSendsRegistartionStatus = new ControllerSendsRegistartionStatus(
				IDOfClient);
		connectionToC.sender.sendData(controllerSendsRegistartionStatus
				.getByte());
	}

	private void createConnectionToCS(int ID, InetAddress IP,
			int portNumberOfChunk) throws Exception {
		// TODO Auto-generated method stub
		// Map<String, Integer> IPAH = hostAddressAndPortOfChunkServer.get(ID);
		// Object[] IPSet = IPAH.keySet().toArray();
		// String IPAddress = (String) IPSet[0];
		// System.out.println(IPAddress);
		//
		// int Port = IPAH.get(IPAddress);
		// System.out.println(Port + "" + IPAddress);

		InetAddress IPToCS = IP;
		int portOfCS = portNumberOfChunk;
		Socket socketToSend = new Socket(IPToCS, portOfCS);
		TCPConnection connectionToCS = new TCPConnection(this, socketToSend);
		if (ID != -1) {
			IDToConnnection.put(ID, connectionToCS);
			System.out
					.println("[INFO] Registration. . . Successful. ID: " + ID);
		} else {
			System.out
					.println("[INFO] Registration. . . Unsuccessful. Same Host Requesting Another ChunkServer.. "
							+ IPToCS.toString());

		}
		ControllerSendsRegistartionStatus controllerSendsRegistartionStatus = new ControllerSendsRegistartionStatus(
				ID);
		connectionToCS.sender.sendData(controllerSendsRegistartionStatus
				.getByte());
	}
}
