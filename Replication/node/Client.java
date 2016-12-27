package node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.ietf.jgss.Oid;

import protocols.ClientRequestingChunkServerForRead;
import protocols.ClientSendsRegistration;
import protocols.Protocol;
import protocols.SendingDataToChunkServer;
import transport.TCPClientThread;
import transport.TCPConnection;
import transport.TCPServerThread;
import util.InteractiveCommandParser;
import util.StartReadingBySendingChunkServerRequest;

public class Client implements Node {
	public int myIDAsClient;
	public InetAddress myIP;
	public int myPortNumber;
	public String controllerHostName;
	public String controllerPortNumber;
	public TCPConnection connectionToController;

	public int totalNumberOfChunk = 0;
	public List<byte[]> fileIntoChunk = new ArrayList<byte[]>();
	public String fileName = "";
	public boolean accessToDoOperations = true;

	public Map<Integer, TCPConnection> knownID = new HashMap<Integer, TCPConnection>();
	public Map<Integer, Socket> knownSocket = new HashMap<Integer, Socket>();
	public Map<Integer, List<String>> knownIPPort = new HashMap<Integer, List<String>>();

	private Queue<DataInputStream> relayQueue = new LinkedList<DataInputStream>();

	public String fileNameToRead = "";
	public int counterToStartMerging = 0;
	public Map<Integer, byte[]> dataOfChunks = new HashMap<Integer, byte[]>();
//	public Map<Integer, Integer> counterOfBadChunks = new HashMap<Integer, Integer>();

	private Object object = new Object();

	public Client(String hostName, String portNumber) {
		// TODO Auto-generated constructor stub

		this.controllerHostName = hostName;
		this.controllerPortNumber = portNumber;

		System.out.println("[INFO] Controller Is Running On "
				+ this.controllerHostName + " On Port: "
				+ this.controllerPortNumber);
		TCPServerThread serverClientThread = new TCPServerThread("0", this);
		Thread threadServerClientRunning = new Thread(serverClientThread);
		threadServerClientRunning.start();

		InteractiveCommandParser interactiveCommandParser = new InteractiveCommandParser(
				this);
		Thread threadConsoleIO = new Thread(interactiveCommandParser);
		threadConsoleIO.start();

		TCPClientThread clientAsClient = new TCPClientThread(
				this.controllerHostName, this.controllerPortNumber, this);
		Thread clientAsThread = new Thread(clientAsClient);
		clientAsThread.start();

		// Thread startSenderT = new Thread(this);
		// startSenderT.start();
	}

	public void startConnectionionWithController() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("[INFO] Client Is Sending Registration Request");
		ClientSendsRegistration clientSendsRegistration = new ClientSendsRegistration(
				this.myIP, this.myPortNumber);
		connectionToController.sender.sendData(clientSendsRegistration
				.getByte());

	}

	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// System.out.println("Sending Thread Is ON");
	// while (true) {
	// DataInputStream relayMsg;
	// synchronized (relayQueue) {
	// relayMsg = relayQueue.poll();
	// }
	//
	// if (relayMsg != null) {
	// try {
	// int chunkGot = relayMsg.readInt();
	// int sizeOfChunkAllocated = relayMsg.readInt();
	// // System.out.println("Chunk -- " + chunkGot);
	// ArrayList<Integer> listToSend = new ArrayList<Integer>();
	//
	// for(int i = 0 ; i < sizeOfChunkAllocated ; i++)
	// {
	// int chunkID = relayMsg.readInt();
	// listToSend.add(chunkID);
	// // System.out.println(chunkID);
	// }
	// int neededID = relayMsg.readInt();
	// // System.out.println("needed" + neededID);
	// for(int i = 0 ; i<neededID;i++)
	// {
	// int needIDToStore = relayMsg.readInt();
	// // System.out.println("ID " + needIDToStore);
	//
	// int ipLength = relayMsg.readInt();
	// byte[] ipAddress = new byte[ipLength];
	// relayMsg.readFully(ipAddress);
	//
	// String ipString = new String(ipAddress);
	// int port = relayMsg.readInt();
	//
	// ArrayList<String> IPPort = new ArrayList<String>();
	// IPPort.add(ipString);
	// IPPort.add(String.valueOf(port));
	// knownIPPort.put(needIDToStore, IPPort);
	// // System.out.println("Till IPPort");
	// // System.out.println(ipString +" " +port);
	// Socket s = new Socket(InetAddress.getByName(ipString.substring(1)),
	// port);
	// knownSocket.put(needIDToStore, s);
	// // System.out.println("Till Socket");
	//
	// TCPConnection connection = new TCPConnection(this, s);
	// knownID.put(needIDToStore, connection);
	// // System.out.println("Till Connection");
	// // byte[] data = {(byte) 1};
	// // connection.sender.sendData(data);
	// // System.out.println(ipString + port);
	// }
	// sendDataToChunks(chunkGot, listToSend);
	//
	// } catch (Exception e) {
	// System.err.println(e.getMessage());
	// }
	// }
	// }
	// }
	public static void main(String[] argumentOfClient) {
		if (argumentOfClient.length == 2) {
			Client client = new Client(argumentOfClient[0],
					(argumentOfClient[1]));

		} else {
			System.out
					.println("Some Problem In Argument.. Give Appropriate HostID & HostPortNumber Of Controller");
		}
	}

	@Override
	public void onEvent(byte[] rawData) throws Exception {
		// TODO Auto-generated method stub
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(rawData);
		DataInputStream din = new DataInputStream(new BufferedInputStream(
				baInputStream));
		byte type = din.readByte();
		// System.out.println(type);
		switch (type) {
		case Protocol.CONTROLLER_SENDS_REGISTRATION_STATUS:
			this.myIDAsClient = din.readInt();
			System.out.println("[INFO] Allocated ID: " + this.myIDAsClient);
			break;

		case Protocol.CONTROLLER_SENDS_CHUNKSERVERLIST:
			// synchronized (din) {

			int chunkGot = din.readInt();
			int sizeOfChunkAllocated = din.readInt();
			System.out.println("Chunk -- " + chunkGot);
			ArrayList<Integer> listToSend = new ArrayList<Integer>();
			int primaryNode = 0;
			for (int i = 0; i < sizeOfChunkAllocated; i++) {

				int chunkID = din.readInt();
				if (i == 0)
					primaryNode = chunkID;
				listToSend.add(chunkID);
				System.out.println(chunkID);

			}

			int neededID = din.readInt();
			// System.out.println("needed" + neededID);
			synchronized (knownIPPort) {
			for (int i = 0; i < neededID; i++) {
				int needIDToStore = din.readInt();
				// System.out.println("ID " + needIDToStore);

				int ipLength = din.readInt();
				byte[] ipAddress = new byte[ipLength];
				din.readFully(ipAddress);

				String ipString = new String(ipAddress);
				int port = din.readInt();

				ArrayList<String> IPPort = new ArrayList<String>();
				IPPort.add(ipString);
				IPPort.add(String.valueOf(port));
				
					knownIPPort.put(needIDToStore, IPPort);

				
				// System.out.println("Till IPPort");
				// System.out.println(ipString +" " +port);
				Socket s = new Socket(InetAddress.getByName(ipString
						.substring(1)), port);
				knownSocket.put(needIDToStore, s);
				// System.out.println("Till Socket");

				TCPConnection connection = new TCPConnection(this, s);
				knownID.put(needIDToStore, connection);
				// System.out.println("Till Connection");
				// byte[] data = {(byte) 1};
				// connection.sender.sendData(data);
				// System.out.println(ipString + port);
			}
			System.out.println("[INFO] Primary Node: "
					+ knownIPPort.get(primaryNode));
			}
			
			// System.out.println("chunk Got: " + chunkGot);
			sendDataToChunks(chunkGot, listToSend);
			// }

			break;
		case Protocol.SENDING_DATA_TO_CHUNKSERVER:
			System.out.println("Something Recieved");
			break;
		case Protocol.CONTROLLER_RESPODING_TO_READ_REQUEST:
			// System.out.println("----");
			if (din.readInt() == -1) {
				int errorSize = din.readInt();
				byte[] errorB = new byte[errorSize];
				din.readFully(errorB);
				System.out.println(new String(errorB));
				accessToDoOperations = true;
			} else {
				int fileNameSize = din.readInt();
				byte[] fileB = new byte[fileNameSize];
				din.readFully(fileB);
				this.fileNameToRead = new String(fileB);
				// System.out.println("File Read");
				int totalChunksOfFile = din.readInt();

				this.counterToStartMerging = totalChunksOfFile;

				// System.out.println("Total Chunk Read");
				int loopForNeededID = din.readInt();
				// System.out.println("Loop Needed Read");
				// System.out.println("needed" + neededID);
				synchronized (knownIPPort) {
					
				
				for (int i = 0; i < loopForNeededID; i++) {
					int needIDToStore = din.readInt();
					// System.out.println("ID " + needIDToStore);
					// System.out.println(needIDToStore);
					int ipLength = din.readInt();
					byte[] ipAddress = new byte[ipLength];
					din.readFully(ipAddress);

					String ipString = new String(ipAddress);
					// System.out.println(ipString);
					int port = din.readInt();
					// System.out.println(port);
					ArrayList<String> IPPort = new ArrayList<String>();
					IPPort.add(ipString);
					IPPort.add(String.valueOf(port));
					knownIPPort.put(needIDToStore, IPPort);
					// System.out.println("Till IPPort");
					// System.out.println(ipString +" " +port);
					Socket s = new Socket(InetAddress.getByName(ipString
							.substring(1)), port);
					knownSocket.put(needIDToStore, s);
					// System.out.println("Till Socket");

					TCPConnection connection = new TCPConnection(this, s);
					knownID.put(needIDToStore, connection);
					// System.out.println("Till Connection");
					// byte[] data = {(byte) 1};
					// connection.sender.sendData(data);
					// System.out.println(ipString + port);
				}
				}
				// System.out.println(totalChunksOfFile);
				ArrayList<Integer> chunksListToRead = new ArrayList<Integer>();
				for (int i = 0; i < totalChunksOfFile; i++) {

					chunksListToRead.add(din.readInt());
				}

				System.out
						.println("[INFO] Client Has Recieved All Relevant ChunkServer Info From Controller For Reading Operation: \n[INFO] FileName: "
								+ fileNameToRead);

				StartReadingBySendingChunkServerRequest startReadingBySendingChunkServerRequest = new StartReadingBySendingChunkServerRequest(
						this, fileNameToRead, totalChunksOfFile,
						chunksListToRead);
//				Thread tSender = new Thread(
//						startReadingBySendingChunkServerRequest);
				startReadingBySendingChunkServerRequest.run();
				// startReadingBySendingChunkServerRequest(fileNameToRead,
				// totalChunksOfFile, chunksListToRead);
				// System.out.println("CS Read");
				// System.out.println("File: " + fileNameToRead);
				// System.out.println("Chunks: " + totalChunksOfFile);
				// System.out.println(knownID);
				// System.out.println(chunksListToRead);
			}
			break;
		case Protocol.CHUNKSERVER_DATA_TO_CLIENT:
			// System.out.println("Here------------");
			//
			//
			//
			// System.out.println("Here");
			int fileNameSizeOfByte = din.readInt();
			byte[] fileToBeRead = new byte[fileNameSizeOfByte];
			din.readFully(fileToBeRead);
			// System.out.println("Till File Read");
			int chunkNumberA = din.readInt();
			// System.out.println(chunkNumberA);
			// System.out.println("Till Chunk");
			int doStore = din.readInt();
			// System.out.println(doStore);
			// System.out.println("Till Do Store");
			if (doStore == 1) {
				// synchronized (object) {
				// try
				// {
				//
				// counterToStartMerging--;
				// System.out.println("Counter" + counterToStartMerging);
				// }b
				//
				// catch(Exception e)
				// {
				// e.printStackTrace();
				// }
				// }
				// System.out.println("Till Chunk" + chunkNumberA);
				int dataSize = din.readInt();
				byte[] dataReadInByte = new byte[dataSize];
				din.readFully(dataReadInByte);
//				synchronized (dataOfChunks) {
////					System.out.println(dataOfChunks.size());
//					
//				}
				synchronized (dataOfChunks) {
					dataOfChunks.put(chunkNumberA, dataReadInByte);
					System.out.println("[INFO] Chunk Got: " + dataOfChunks.size());
					if (counterToStartMerging == dataOfChunks.size()) {
						System.out.println("[INFO] Starting Merging");
						// try {
						// // String filename= "MyFile.txt";
						// System.out.println("File Reading Initiated");
						// // System.out.println(fileNameToRead);
						String[] fNameToGetStore = fileNameToRead.split("\\.");
//						File f = new File(fNameToGetStore[0] + "_merge"
//								+ fNameToGetStore[1]);
						File f = new File(fNameToGetStore[0]+"_merge"+"."+fNameToGetStore[1]);
						System.out.println();
						if (f.exists()) {
							System.out.println("[INFO] Merged File Is Already There..");
							System.out.println("[CONT] Deleting Old Merged Version");
							f.delete();
						}
						// System.out.println("File Created " + fileNameToRead +
						// "_merge" +f.createNewFile());
						
						
						
//						if(f.exists()) f.delete(); 
							f.createNewFile();
							FileOutputStream fos = new FileOutputStream(f, true);
//							fw.append(new String(generatedFile.get(loop)));
							
						
						
//						FileWriter fw = new FileWriter(f, true); // the true
						// // will
						// // append
						// // the new
						// // data
						for (int i : dataOfChunks.keySet()) {
							
							fos.write(dataOfChunks.get(i));
//							fos.close();
							
//							fw.write(new String(dataOfChunks.get(i)));// appends
																		// the
																		// string
																		// to
							// System.out.println(new
							// String(dataOfChunks.get(i)));
						} // the file
						fos.close();
						// } catch (IOException ioe) {
						// System.err.println("IOException: " +
						// ioe.getMessage());
						// }
						accessToDoOperations = true;
						fileNameToRead = "";
						counterToStartMerging = 0;
						dataOfChunks = new HashMap<Integer, byte[]>();
						System.out
								.println("[INFO] Client Has Got All Chunks.. Merging Is Done..");
						System.out
								.println("[CONT] You Can See File In Default Location");
						// }
					}
				}

				// System.out.println("Read File");
				// System.out.println("");
				// System.out.println("File: " + new String(fileToBeRead) +
				// " Chunk: " + chunkNumberA + "\nData: " + new
				// String(dataReadInByte));
			} else if (doStore == -1) {

				int errorSize = din.readInt();
				byte[] error = new byte[errorSize];
				din.readFully(error);
				System.out.println(new String(error));
				System.out.println("[CONT] Wait For Other Chunk Server To Send You Data..");
				System.out.println("[CONT] Sorry Again..");
//				int requestOffset = 0;
//				if(counterOfBadChunks.containsKey(chunkNumberA))
//				{
//					
//					requestOffset = counterOfBadChunks.get(chunkNumberA) + 1;
//					
//					counterOfBadChunks.replace(chunkNumberA, requestOffset);
//					
//					
//				}
//				else
//				{
//					requestOffset ++;
//					counterOfBadChunks.put(chunkNumberA, requestOffset);
//				}
//				
//				if(requestOffset >= 3)
//				{
//					System.out.println("[ERROR] Given File Will Not Be Recover.. Ever..");
//					System.out.println("[ERROR] Cluster Has All False Copies For File: " + fileNameToRead + " Chunk: " + chunkNumberA);
//				}
//				else
//				{
////					counterOfBadChunks.replace(chunkNumberA, requestOffset);
//				}
			}
			else
			{
				System.out.println("[ERROR] File Might Be Deleted.. Requesting Other Server..");
			}
			// synchronized (dataOfChunks) {
			// // System.out.println(dataOfChunks.size());
			//
			//
			// // System.out.println("counter: " + counterToStartMerging);
			// // synchronized (object) {
			//
			//
			// }

			break;
		default:
			System.out.println("UnUsual Activity In OnEvent");
			break;
		}
		din.close();
		baInputStream.close();
	}

	// private void startReadingBySendingChunkServerRequest(
	// String fileNameToReadFinal, int totalChunksOfFile,
	// ArrayList<Integer> chunksListToRead) throws Exception {
	// // TODO Auto-generated method stub
	//
	// for(int i = 1; i<=totalChunksOfFile ; i++)
	// {
	// ClientRequestingChunkServerForRead clientRequestingChunkServerForRead =
	// new ClientRequestingChunkServerForRead(myIDAsClient, myIP, myPortNumber,
	// fileNameToReadFinal, i);;
	// knownID.get(chunksListToRead.get(i-1)).sender.sendData(clientRequestingChunkServerForRead.getByte());
	// }
	//
	// }

	private synchronized void sendDataToChunks(int chunkGot,
			ArrayList<Integer> listToSend) throws Exception {
		// TODO Auto-generated method stub

		String fileGot = fileName;
		int chunkNumber = chunkGot;
		// System.out.println("chunkNumberGot" + chunkNumber);
		byte[] dataToSend = fileIntoChunk.get(chunkGot - 1);
		TCPConnection connectionFirst = knownID.get(listToSend.get(0));
		listToSend.remove(0);
		// System.out.println(fileName + "  " +chunkGot);
		// totalNumberOfChunk--;
		// System.out.println("Chunk Remaining .." + totalNumberOfChunk);
		SendingDataToChunkServer dataToChunkServer = new SendingDataToChunkServer(
				knownIPPort, fileName, chunkNumber, dataToSend, listToSend);
		connectionFirst.sender.sendData(dataToChunkServer.getByte());
		totalNumberOfChunk--;
		// System.out.println("Chunk Remaining .." + totalNumberOfChunk);
		// totalNumberOfChunk--;
		// System.out.println("Chunk Remaining .." + totalNumberOfChunk);
		// totalNumberOfChunk--;
		// System.out.println("Chunk Remaining .." + totalNumberOfChunk);
		// System.out.println(totalNumberOfChunk);
		if (totalNumberOfChunk == 0) {

			fileIntoChunk = new ArrayList<byte[]>();
			fileName = "";
			accessToDoOperations = true;
			System.out
					.println("[INFO] Client Has Successfully Send Data To Respective ChunkServers..");
			System.out
					.println("[INFO] Client Can Fire Up Further Requests....");
		}

	}
}
