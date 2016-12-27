package node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import protocols.ControllerRespondingToReadRequest;
import protocols.ControllerSendsChunkServerList;
import protocols.ControllerSendsRegistartionStatus;
import protocols.Protocol;
import transport.TCPSender;
import transport.TCPServerThread;
import util.RequestForReadHandler;

public class Controller implements Node {

	public Map<Integer, ArrayList<String>> hostAddressAndPortOfChunkServer = new HashMap<Integer, ArrayList<String>>();
	public int totalCS;
	public Map<Integer, ArrayList<String>> hostAddressAndPortOfClient = new HashMap<Integer, ArrayList<String>>();
	public final int DATA_SHARD = 9;
	public List<String> filesHolder = new ArrayList<String>();

	public Map<Integer, TCPSender> clientSenders = new HashMap<Integer, TCPSender>();

	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> FileToChunksToShardToNode = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();

	public Map<Integer, Long> numberOfChunksOnEachChunk = new HashMap<Integer, Long>();

	private Controller(String stringPortNumber) {
		// TODO Auto-generated constructor stub
		TCPServerThread serverThread = new TCPServerThread(stringPortNumber,
				this);
		Thread threadServerRunning = new Thread(serverThread);
		threadServerRunning.start();

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
	public void onEvent(byte[] rawData, Socket s) throws Exception {
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
			String IP = new String(IPAddress);

			// System.out.println(IP.toString());

			String portNumberOfChunk = String.valueOf(din.readInt());
			// System.out.println(portNumberOfChunk);
			int ID;
			synchronized (hostAddressAndPortOfChunkServer) {
				ID = hostAddressAndPortOfChunkServer.size() + 1;
				ArrayList<String> values = new ArrayList<String>();
				values.add(IP);
				values.add(portNumberOfChunk);
				// System.out.println("Values" + values);
				hostAddressAndPortOfChunkServer.put(ID, values);
				totalCS++;
			}
			System.out.println("Chunk Sends Registration " + ID + " Name: "
					+ IP);

			ControllerSendsRegistartionStatus controllerSendsRegistartionStatusCS = new ControllerSendsRegistartionStatus(
					ID);
			TCPSender senderF = new TCPSender(s);
			senderF.sendData(controllerSendsRegistartionStatusCS.getByte());

			// System.out.println("ID " + ID);
			// int ID = hostAddressAndPortOfChunkServer.size() + 1;
			// numberOfChunksOnEachChunk.put(ID, (long) 0);
			// System.out.println(counterForChunks);
			// System.out.println(hostAddressAndPortOfChunkServer);
			// createConnectionToCS(ID, IP, portNumberOfChunk);

			break;
		case Protocol.CLIENT_SENDS_REGISTRATION:

			int IPAddressLengthOfClient = din.readInt();
			byte[] IPAddressClient = new byte[IPAddressLengthOfClient];
			din.readFully(IPAddressClient);
			InetAddress IPClient = InetAddress.getByAddress(IPAddressClient);

			// System.out.println(IP.toString());

			int portNumberOfClient = din.readInt();
			// System.out.println(portNumberOfChunk);

			ArrayList<String> hostAndPort = new ArrayList<String>();
			hostAndPort.add(IPClient.getHostAddress());
			hostAndPort.add(String.valueOf(portNumberOfClient));
			// hostAndPort.put(IPClient.toString(), portNumberOfClient);

			int IDOfClient = hostAddressAndPortOfClient.size() + 1;
			hostAddressAndPortOfClient.put(IDOfClient, hostAndPort);
			System.out.println("Client Sends Registration " + IDOfClient
					+ "Name: " + IPClient);

			TCPSender sender = new TCPSender(s);
			// Map<Integer, TCPSender> clientSenders = new HashMap<Integer,
			// TCPSender>();
			clientSenders.put(IDOfClient, sender);

			ControllerSendsRegistartionStatus controllerSendsRegistartionStatus = new ControllerSendsRegistartionStatus(
					IDOfClient);
			sender.sendData(controllerSendsRegistartionStatus.getByte());
			// hostChunkServer.put(ID, IP.toString());
			// hostAddressAndPortOfClient.put(IDOfClient, hostAndPort);
			//
			// ArrayList<ControllerSendsChunkServerList> importantArrayList =
			// new ArrayList<ControllerSendsChunkServerList>();
			// startAfterCounter.put(IDOfClient, 0);
			// chunkServerListFinal.put(IDOfClient, importantArrayList);
			// // System.out.println(counterForChunks);
			// // System.out.println(hostAddressAndPortOfChunkServer);
			// createConnectionToClient(IDOfClient, IPClient,
			// portNumberOfClient);
			break;
		case Protocol.CHUNK_SERVER_SENDS_MINOR_HEARTBEAT:
			// int IDGot = din.readInt();
			//
			// try {
			//
			// synchronized (numberOfChunksOnEachChunk) {
			// numberOfChunksOnEachChunk.replace(IDGot, din.readLong());
			// // System.out.println(numberOfChunksOnEachChunk);
			// }
			// } catch (Exception e) {
			// // TODO: handle exception
			// }
			break;

		case Protocol.CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT:
			// int IDGotMajor = din.readInt();

			break;

		case Protocol.CLIENT_REQUESTING_CHUNKSERVER:
			// System.out.println("Called?>");
			int reqClientID = din.readInt();

			int fileNameLength = din.readInt();
			byte[] fileNameInBytes = new byte[fileNameLength];
			din.readFully(fileNameInBytes);
			String fileNameInString = new String(fileNameInBytes);
			if (!filesHolder.contains(fileNameInString)) {
				filesHolder.add(fileNameInString);
				// HashMap<Integer, Integer> putInFileToChunks = new
				// HashMap<Integer, Integer>();
				// FileToChunks.put(fileNameInString, putInFileToChunks);
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
			System.out.println("For Chunk: " + chunkNumber);
			ArrayList<Integer> nodeLocation = new ArrayList<Integer>();
			for (int i = 1; i <= DATA_SHARD; i++) {

				int randomChunk = new Random().nextInt(totalCS);
				// System.out.println("random chunk"+(randomChunk + 1));
				nodeLocation.add(randomChunk);

			}
			System.out.println("File: " + fileNameInString);
			System.out.println("Chunk: " + chunkNumber);
			System.out.println("List" + nodeLocation);

			if (!FileToChunksToShardToNode.containsKey(fileNameInString)) {
				HashMap<Integer, HashMap<Integer, Integer>> putingInFile = new HashMap<Integer, HashMap<Integer, Integer>>();
				FileToChunksToShardToNode.put(fileNameInString, putingInFile);
			}

			int j = 1;
			HashMap<Integer, Integer> nodeLoc = new HashMap<Integer, Integer>();
			for (int i : nodeLocation) {

				nodeLoc.put(j, (i + 1));
				j++;

			}
			FileToChunksToShardToNode.get(fileNameInString).put(chunkNumber,
					nodeLoc);

			ControllerSendsChunkServerList chunkServerList = new ControllerSendsChunkServerList(
					this, chunkNumber, nodeLoc);
			clientSenders.get(reqClientID).sendData(chunkServerList.getByte());
			// System.out.println(FileToChunksToShardToNode);
			// System.out.println("Node: " + reqClientID + "File: "
			// + new String(fileNameInBytes) + "Chunk: " + chunkNumber);

			// System.out.println("Got Request");
			// getBest3Locations(reqClientID, fileNameInString, chunkNumber,
			// totalChunk);
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
				// Thread readHandler = new Thread(requestForReadHandler);
				requestForReadHandler.start();
			} else {
				System.out.println("[ERROR] File " + reqFileName
						+ "Does Not Belong To This Cluster");
				ControllerRespondingToReadRequest controllerRespondingToReadRequest = new ControllerRespondingToReadRequest();
				controllerRespondingToReadRequest.status = -1;
				// IDToConnnectionClient.get(clientID).sender
				// .sendData(controllerRespondingToReadRequest.getByte());
			}
			break;

		default:
			System.out.println("[INFO] UnWanted Message Is Coming Away");
			break;
		}
		din.close();
		baInputStream.close();
	}

	// private void getBest3Locations(int reqClientID, String fileNameInString,
	// int chunkNumber, int totalChunk) throws Exception {
	// // System.out.println("Hello");
	// // System.out.println(freeSpaceOnEachChunk);
	// // TreeMap<Integer, Long> sortedMap = SortByValue(freeSpaceOnEachChunk);
	// List<Long> chunksorted = new ArrayList<Long>(
	// freeSpaceOnEachChunk.values());
	// Collections.sort(chunksorted);
	// // System.out.println(chunksorted);
	// ArrayList<Integer> probableList = new ArrayList<Integer>();
	// int maxSize = chunksorted.size();
	//
	// if (maxSize >= replicationLevel + 3) {
	// try {
	// synchronized (freeSpaceOnEachChunk) {
	// for (Entry<Integer, Long> entry : freeSpaceOnEachChunk
	// .entrySet()) {
	// if (entry.getValue().equals(
	// chunksorted.get(maxSize - 1))) {
	// probableList.add(entry.getKey());
	// } else if (entry.getValue().equals(
	// chunksorted.get(maxSize - 2))) {
	// probableList.add(entry.getKey());
	// } else if (entry.getValue().equals(
	// chunksorted.get(maxSize - 3))) {
	// probableList.add(entry.getKey());
	// } else if (entry.getValue().equals(
	// chunksorted.get(maxSize - 4))) {
	// probableList.add(entry.getKey());
	// } else if (entry.getValue().equals(
	// chunksorted.get(maxSize - 5))) {
	// probableList.add(entry.getKey());
	// } else if (entry.getValue().equals(
	// chunksorted.get(maxSize - 6))) {
	// probableList.add(entry.getKey());
	// }
	// }
	// }
	//
	// Collections.shuffle(probableList);
	// // System.out.println("Store" + chunkNumber + " On " +
	// // probableList.subList(0, replicationLevel));
	//
	// // ArrayList<Long> changeList = new ArrayList<Long>();
	// // for(int i : probableList)
	// // {
	// // changeList.add(freeSpaceOnEachChunk.get(i) - chunkSize);
	// // }
	// ArrayList<Integer> mainList = new ArrayList<Integer>();
	// ArrayList<Integer> needToSendIPPort = new ArrayList<Integer>();
	// if (clientKnowsCS.get(reqClientID) == null) {
	// ArrayList<Integer> IDs = new ArrayList<Integer>();
	// clientKnowsCS.put(reqClientID, IDs);
	// }
	// for (int i = 0; i < replicationLevel; i++) {
	// mainList.add(probableList.get(i));
	// if (!clientKnowsCS.get(reqClientID).contains(
	// probableList.get(i))) {
	// clientKnowsCS.get(reqClientID).add(probableList.get(i));
	// needToSendIPPort.add(probableList.get(i));
	// // clientKnowsCS.replace(reqClientID, value)
	// }
	// }
	// for (int i : mainList) {
	// freeSpaceOnEachChunk.replace(i, freeSpaceOnEachChunk.get(i)
	// - chunkSize);
	// }
	//
	// // System.out.println("Executed");
	// try {
	//
	// OffsetToChunkServersList.put(
	// OffsetToChunkServersList.size(), mainList);
	// FileToChunks.get(fileNameInString).put(chunkNumber,
	// OffsetToChunkServersList.size() - 1);
	// for (int cs : mainList) {
	// // List<Integer> listOfFile;
	// if (!chunkServerTochunks.get(cs).containsKey(
	// fileNameInString)) {
	// // listOfFile =
	// // chunkServerTochunks.get(cs).get(fileNameInString);
	// List<Integer> listOfFile = new ArrayList<Integer>();
	// chunkServerTochunks.get(cs).put(fileNameInString,
	// listOfFile);
	// }
	// chunkServerTochunks.get(cs).get(fileNameInString)
	// .add(chunkNumber);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// ControllerSendsChunkServerList chunkServerList = new
	// ControllerSendsChunkServerList(
	// this, chunkNumber, mainList, needToSendIPPort);
	// // System.out.println("Wait..");
	// chunkServerListFinal.get(reqClientID).add(chunkServerList);
	// if (startAfterCounter.get(reqClientID) + 1 == totalChunk) {
	// FileToChunks.get(fileNameInString).put(0, totalChunk);
	// // System.out.println(FileToChunks);
	// // System.out.println(OffsetToChunkServersList);
	// // System.out.println(chunkServerTochunks);
	// for (ControllerSendsChunkServerList i : chunkServerListFinal
	// .get(reqClientID)) {
	// IDToConnnectionClient.get(reqClientID).sender
	// .sendData(i.getByte());
	// }
	// startAfterCounter.replace(reqClientID, 0);
	// ArrayList<ControllerSendsChunkServerList> newImp = new
	// ArrayList<ControllerSendsChunkServerList>();
	// chunkServerListFinal.replace((reqClientID), newImp);
	// } else {
	// startAfterCounter.replace(reqClientID,
	// (startAfterCounter.get(reqClientID) + 1));
	// // System.out.println(startAfterCounter);
	// }
	//
	// // if()
	// // System.out.println(clientKnowsCS);
	// // System.out.println(IDToConnnectionClient.get(reqClientID));
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// } else {
	// System.out
	// .println("[ERROR] Minimum Replication Will Not Work Efficiently.. Add More ChunkServer And Try Again");
	// }
	// // Collections.sort(chunksorted);
	// // System.out.println(chunksorted);
	// // TODO Auto-generated method stub
	//
	// // Collections collections =
	// // Collections.s(freeSpaceOnEachChunk.values());
	//
	// // IDToConnnectionClient.get(reqClientID).sender.sendData(data);
	// // System.out.println("Got?");
	// // System.out.println(IDToConnnectionClient.get(reqClientID - 1));
	//
	// }

	// private void createConnectionToClient(int IDOfClient, InetAddress
	// IPClient,
	// int portNumberOfClient) throws Exception {
	// // TODO Auto-generated method stub
	// Socket socketToSend = new Socket(IPClient, portNumberOfClient);
	// TCPConnection connectionToC = new TCPConnection(this, socketToSend);
	// if (IDOfClient != -1) {
	// IDToConnnectionClient.put(IDOfClient, connectionToC);
	// System.out
	// .println("[INFO] Client Registration. . . Successful. ID: "
	// + IDOfClient);
	// } else {
	// System.out.println("[INFO] Registration. . . Unsuccessful. ");
	//
	// }
	// ControllerSendsRegistartionStatus controllerSendsRegistartionStatus = new
	// ControllerSendsRegistartionStatus(
	// IDOfClient);
	// connectionToC.sender.sendData(controllerSendsRegistartionStatus
	// .getByte());
	// }

	// private void createConnectionToCS(int ID, InetAddress IP,
	// int portNumberOfChunk) throws Exception {
	// // TODO Auto-generated method stub
	// // Map<String, Integer> IPAH = hostAddressAndPortOfChunkServer.get(ID);
	// // Object[] IPSet = IPAH.keySet().toArray();
	// // String IPAddress = (String) IPSet[0];
	// // System.out.println(IPAddress);
	// //
	// // int Port = IPAH.get(IPAddress);
	// // System.out.println(Port + "" + IPAddress);
	//
	// InetAddress IPToCS = IP;
	// int portOfCS = portNumberOfChunk;
	// Socket socketToSend = new Socket(IPToCS, portOfCS);
	// TCPConnection connectionToCS = new TCPConnection(this, socketToSend);
	// if (ID != -1) {
	// IDToConnnection.put(ID, connectionToCS);
	// System.out
	// .println("[INFO] Registration. . . Successful. ID: " + ID);
	// } else {
	// System.out
	// .println("[INFO] Registration. . . Unsuccessful. Same Host Requesting Another ChunkServer.. "
	// + IPToCS.toString());
	//
	// }
	// ControllerSendsRegistartionStatus controllerSendsRegistartionStatus = new
	// ControllerSendsRegistartionStatus(
	// ID);
	// connectionToCS.sender.sendData(controllerSendsRegistartionStatus
	// .getByte());
	// }
}
