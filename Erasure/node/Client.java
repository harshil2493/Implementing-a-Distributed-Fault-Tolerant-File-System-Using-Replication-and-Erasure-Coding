package node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import erasure.ReedSolomon;
import protocols.ClientRequestingChunkServerForRead;
import protocols.ClientSendsRegistration;
import protocols.Protocol;
import protocols.SendingDataToChunkServer;
import transport.TCPSender;
import util.InteractiveCommandParser;

public class Client implements Node {
	public int myIDAsClient;
	public InetAddress myIP;
	public int myPortNumber;
	public String controllerHostName;
	public String controllerPortNumber;
	
	
	
	public static final int DATA_SHARDS = 6;
    public static final int PARITY_SHARDS = 3;
    public static final int TOTAL_SHARDS = 9;

    public static final int BYTES_IN_INT = 4;

	

	// public int totalNumberOfChunk = 0;
	public Map<Integer, byte[]> fileIntoChunkIntoShards = new HashMap<Integer, byte[]>();
	public String fileName = "";
	public boolean accessToDoOperations = true;

	// public Map<Integer, TCPConnection> knownID = new HashMap<Integer,
	// TCPConnection>();
	public Map<Integer, TCPSender> knownSender = new HashMap<Integer, TCPSender>();
	// public Map<Integer, List<String>> knownIPPort = new HashMap<Integer,
	// List<String>>();

	public TCPSender senderOfControllerSender;

	public String fileNameToRead = "";
	public int counterToStartMerging = 0;
	public Map<Integer, byte[][]> dataOfChunks = new HashMap<Integer, byte[][]>();
	public Map<Integer, ArrayList<byte[]>> dataRec = new HashMap<Integer, ArrayList<byte[]>>();

	public Map<Integer, ArrayList<Integer>> dataFalse = new HashMap<Integer, ArrayList<Integer>>();
	public Map<Integer, ArrayList<Integer>> dataGet= new HashMap<Integer, ArrayList<Integer>>();
	public Map<Integer, byte[]> generatedFile = new HashMap<Integer, byte[]>();
	// public Map<Integer, Integer> counterOfBadChunks = new HashMap<Integer,
	// Integer>();

	public Client(String hostName, String portNumber) {
		// TODO Auto-generated constructor stub

		this.controllerHostName = hostName;
		this.controllerPortNumber = portNumber;

		System.out.println("[INFO] Controller Is Running On "
				+ this.controllerHostName + " On Port: "
				+ this.controllerPortNumber);
		// TCPServerThread serverClientThread = new TCPServerThread("0", this);
		// Thread threadServerClientRunning = new Thread(serverClientThread);
		// threadServerClientRunning.start();

		InteractiveCommandParser interactiveCommandParser = new InteractiveCommandParser(
				this);
		Thread threadConsoleIO = new Thread(interactiveCommandParser);
		threadConsoleIO.start();

		// TCPClientThread clientAsClient = new TCPClientThread(
		// this.controllerHostName, this.controllerPortNumber, this);
		// Thread clientAsThread = new Thread(clientAsClient);
		// clientAsThread.start();

		// Thread startSenderT = new Thread(this);
		// startSenderT.start();
	}

	public void startConnectionionWithController() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("[INFO] Client Is Sending Registration Request");

		Socket socketToController = new Socket(controllerHostName,
				Integer.parseInt(controllerPortNumber));
		TCPSender senderController = new TCPSender(socketToController);
		// senderController.

		ClientSendsRegistration clientSendsRegistration = new ClientSendsRegistration(
				socketToController.getLocalAddress(),
				socketToController.getLocalPort());

		myIP = socketToController.getLocalAddress();
		myPortNumber = socketToController.getLocalPort();

		System.out.println("MyIP: " + socketToController.getLocalAddress()
				+ "Port: " + socketToController.getLocalPort());
		senderController.sendData(clientSendsRegistration.getByte());

		DataInputStream dataInputStream = new DataInputStream(
				socketToController.getInputStream());
		senderOfControllerSender = senderController;
		int dataLength;
		dataLength = dataInputStream.readInt();
		byte[] data = new byte[dataLength];
		dataInputStream.readFully(data, 0, dataLength);
		this.onEvent(data, socketToController);

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
	public static void main(String[] argumentOfClient) throws Exception {
		if (argumentOfClient.length == 2) {
			Client client = new Client(argumentOfClient[0],
					(argumentOfClient[1]));
			client.startConnectionionWithController();

		} else {
			System.out
					.println("Some Problem In Argument.. Give Appropriate HostID & HostPortNumber Of Controller");
		}
	}

	@Override
	public void onEvent(byte[] rawData, Socket gotSocket) {
		// TODO Auto-generated method stub
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(
					rawData);
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
				// System.out.println("Got List");

				int chunkNumber = din.readInt();
				ArrayList<Integer> nodeListFinal = new ArrayList<Integer>();
				for (int i = 1; i <= 9; i++) {
					nodeListFinal.add(din.readInt());
					int IPL = din.readInt();
					byte[] IPInB = new byte[IPL];
					din.readFully(IPInB);
					int portS = din.readInt();
					// System.out.println("IP" + new String(IPInB));
					Socket temporary = new Socket(new String(IPInB), portS);
					TCPSender sender = new TCPSender(temporary);
					SendingDataToChunkServer chunkServer = new SendingDataToChunkServer(
							fileName, chunkNumber, i,
							dataOfChunks.get(chunkNumber)[i - 1]);
					sender.sendData(chunkServer.getByte());

					temporary.close();
				}
				// synchronized (din) {

				// int chunkGot = din.readInt();
				// int sizeOfChunkAllocated = din.readInt();
				// System.out.println("Chunk -- " + chunkGot);
				// ArrayList<Integer> listToSend = new ArrayList<Integer>();
				// int primaryNode = 0;
				// for (int i = 0; i < sizeOfChunkAllocated; i++) {
				//
				// int chunkID = din.readInt();
				// if (i == 0)
				// primaryNode = chunkID;
				// listToSend.add(chunkID);
				// System.out.println(chunkID);
				//
				// }
				//
				// int neededID = din.readInt();
				// // System.out.println("needed" + neededID);
				// synchronized (knownIPPort) {
				// for (int i = 0; i < neededID; i++) {
				// int needIDToStore = din.readInt();
				// // System.out.println("ID " + needIDToStore);
				//
				// int ipLength = din.readInt();
				// byte[] ipAddress = new byte[ipLength];
				// din.readFully(ipAddress);
				//
				// String ipString = new String(ipAddress);
				// int port = din.readInt();
				//
				// ArrayList<String> IPPort = new ArrayList<String>();
				// IPPort.add(ipString);
				// IPPort.add(String.valueOf(port));
				//
				// knownIPPort.put(needIDToStore, IPPort);
				//
				//
				// // System.out.println("Till IPPort");
				// // System.out.println(ipString +" " +port);
				// Socket s = new Socket(InetAddress.getByName(ipString
				// .substring(1)), port);
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
				// System.out.println("[INFO] Primary Node: "
				// + knownIPPort.get(primaryNode));
				// }
				//
				// // System.out.println("chunk Got: " + chunkGot);
				// sendDataToChunks(chunkGot, listToSend);
				// // }

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

					for (int i = 1; i <= totalChunksOfFile; i++) {
						for (int j = 1; j <= 9; j++) {
							int shardHolder = din.readInt();

							// System.out.println("Chunk: " + i + " Shard: " + j
							// + " Holder: " + shardHolder);

							int IPL = din.readInt();
							byte[] IPInB = new byte[IPL];
							din.readFully(IPInB);
							int portS = din.readInt();
							// System.out.println("IP" + new String(IPInB));
							// System.out.println(portS);
							// System.out.println(fileNameToRead);
							Socket temporaryNEW = new Socket(new String(IPInB),
									portS);
							TCPSender sender = new TCPSender(temporaryNEW);

							ClientRequestingChunkServerForRead chunkServerForRead = new ClientRequestingChunkServerForRead(
									myIDAsClient, myIP, myPortNumber, fileName,
									i, j);
							sender.sendData(chunkServerForRead.getByte());
							// SendingDataToChunkServer chunkServer = new
							// SendingDataToChunkServer(fileName, chunkNumber,
							// i, dataOfChunks.get(chunkNumber)[i-1]);
							// sender.sendData(chunkServer.getByte());

							DataInputStream dataInputStream = new DataInputStream(
									temporaryNEW.getInputStream());

							int dataLength;
							dataLength = dataInputStream.readInt();
							byte[] data = new byte[dataLength];
							dataInputStream.readFully(data, 0, dataLength);
							this.onEvent(data, temporaryNEW);

							temporaryNEW.close();
						}
						Reconstruct(i);
					}
					accessToDoOperations = true;
					
					
					fileNameToRead = "";
					counterToStartMerging = 0;
					dataOfChunks = new HashMap<Integer, byte[][]>();
					dataRec = new HashMap<Integer, ArrayList<byte[]>>();

					dataFalse = new HashMap<Integer, ArrayList<Integer>>();
					dataGet= new HashMap<Integer, ArrayList<Integer>>();
					generatedFile = new HashMap<Integer, byte[]>();
					
					System.out.println("Merging Done...");
//					GenerateFile(totalChunksOfFile, fileNameToRead);
				}
				//
				// this.counterToStartMerging = totalChunksOfFile;
				//
				// // System.out.println("Total Chunk Read");
				// int loopForNeededID = din.readInt();
				// // System.out.println("Loop Needed Read");
				// // System.out.println("needed" + neededID);
				// synchronized (knownIPPort) {
				//
				//
				// for (int i = 0; i < loopForNeededID; i++) {
				// int needIDToStore = din.readInt();
				// // System.out.println("ID " + needIDToStore);
				// // System.out.println(needIDToStore);
				// int ipLength = din.readInt();
				// byte[] ipAddress = new byte[ipLength];
				// din.readFully(ipAddress);
				//
				// String ipString = new String(ipAddress);
				// // System.out.println(ipString);
				// int port = din.readInt();
				// // System.out.println(port);
				// ArrayList<String> IPPort = new ArrayList<String>();
				// IPPort.add(ipString);
				// IPPort.add(String.valueOf(port));
				// knownIPPort.put(needIDToStore, IPPort);
				// // System.out.println("Till IPPort");
				// // System.out.println(ipString +" " +port);
				// Socket s = new Socket(InetAddress.getByName(ipString
				// .substring(1)), port);
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
				// }
				// // System.out.println(totalChunksOfFile);
				// ArrayList<Integer> chunksListToRead = new
				// ArrayList<Integer>();
				// for (int i = 0; i < totalChunksOfFile; i++) {
				//
				// chunksListToRead.add(din.readInt());
				// }
				//
				// System.out
				// .println("[INFO] Client Has Recieved All Relevant ChunkServer Info From Controller For Reading Operation: \n[INFO] FileName: "
				// + fileNameToRead);
				//
				// StartReadingBySendingChunkServerRequest
				// startReadingBySendingChunkServerRequest = new
				// StartReadingBySendingChunkServerRequest(
				// this, fileNameToRead, totalChunksOfFile,
				// chunksListToRead);
				// Thread tSender = new Thread(
				// startReadingBySendingChunkServerRequest);
				// tSender.start();
				// // startReadingBySendingChunkServerRequest(fileNameToRead,
				// // totalChunksOfFile, chunksListToRead);
				// // System.out.println("CS Read");
				// // System.out.println("File: " + fileNameToRead);
				// // System.out.println("Chunks: " + totalChunksOfFile);
				// // System.out.println(knownID);
				// // System.out.println(chunksListToRead);
				// }
				break;
			case Protocol.CHUNKSERVER_DATA_TO_CLIENT:

				int fsize = din.readInt();
				byte[] fName = new byte[fsize];
				din.readFully(fName);

				// dout.writeInt(sizeByteFile);
				// dout.write(fileNameByte);

				int chunkID = din.readInt();
				int shard = din.readInt();
				if (!dataRec.containsKey(chunkID)) {
					ArrayList<byte[]> dataI = new ArrayList<byte[]>();
 					dataRec.put(chunkID, dataI);
					ArrayList<Integer> badShards = new ArrayList<Integer>();
					
					dataFalse.put(chunkID, badShards);
					
					ArrayList<Integer> yes = new ArrayList<Integer>();
					
					dataGet.put(chunkID, yes);
				}
				int flag = din.readInt();

				if (flag == 1) {
//					System.out.println("Here .. " + chunkID + shard);
					int dataSize = din.readInt();
					byte[] byteData = new byte[dataSize];
					din.readFully(byteData);

					dataRec.get(chunkID).add(byteData);
					dataGet.get(chunkID).add(shard);

				} else if(flag == -1) {
					int eSize = din.readInt();
					byte[] eB = new byte[eSize];
					din.readFully(eB);
					System.out.println(new String(eB));

					int dataSize = din.readInt();
					byte[] byteData = new byte[dataSize];
					din.readFully(byteData);

					dataRec.get(chunkID).add(byteData);
//					dataFalse.replace(chunkID, (dataFalse.get(chunkID) + 1));
					dataFalse.get(chunkID).add(shard);
					dataGet.get(chunkID).add(shard);
				}
				else
				{
//					byte[] falseArray = new byte[];
//					dataRec.get(chunkID).add(falseArray);
//					dataFalse.replace(chunkID, (dataFalse.get(chunkID) + 1));
					dataFalse.get(chunkID).add(shard);
					
				}
				// dout.writeInt(chunkID);
				// dout.writeInt(shard);
				//
				// if (SHAResult) {
				// int flag = 1;
				// dout.writeInt(flag);
				// int dataReadSize = dataRead.length;
				// dout.writeInt(dataReadSize);
				// // System.out.println("Puting" + dataReadSize);
				// dout.write(dataRead);

				// System.out.println("Here------------");
				//
				//
				//
				// System.out.println("Here");
				// int fileNameSizeOfByte = din.readInt();
				// byte[] fileToBeRead = new byte[fileNameSizeOfByte];
				// din.readFully(fileToBeRead);
				// // System.out.println("Till File Read");
				// int chunkNumberA = din.readInt();
				// // System.out.println(chunkNumberA);
				// // System.out.println("Till Chunk");
				// int doStore = din.readInt();
				// // System.out.println(doStore);
				// // System.out.println("Till Do Store");
				// if (doStore == 1) {
				// // synchronized (object) {
				// // try
				// // {
				// //
				// // counterToStartMerging--;
				// // System.out.println("Counter" + counterToStartMerging);
				// // }b
				// //
				// // catch(Exception e)
				// // {
				// // e.printStackTrace();
				// // }
				// // }
				// // System.out.println("Till Chunk" + chunkNumberA);
				// int dataSize = din.readInt();
				// byte[] dataReadInByte = new byte[dataSize];
				// din.readFully(dataReadInByte);
				// synchronized (dataOfChunks) {
				// // System.out.println(dataOfChunks.size());
				// // dataOfChunks.put(chunkNumberA, dataReadInByte);
				// }
				// synchronized (dataOfChunks) {
				// if (counterToStartMerging == dataOfChunks.size()) {
				// System.out.println("[INFO] Starting Merging");
				// // try {
				// // // String filename= "MyFile.txt";
				// // System.out.println("File Reading Initiated");
				// // // System.out.println(fileNameToRead);
				// String[] fNameToGetStore = fileNameToRead.split("\\.");
				// // File f = new File(fNameToGetStore[0] + "_merge"
				// // + fNameToGetStore[1]);
				// File f = new
				// File(fNameToGetStore[0]+"_merge"+fNameToGetStore[1]);
				// System.out.println();
				// if (f.exists()) {
				// System.out.println("[INFO] Merged File Is Already There..");
				// System.out.println("[CONT} Deleting Old Merged Version");
				// f.delete();
				// }
				// // System.out.println("File Created " + fileNameToRead +
				// // "_merge" +f.createNewFile());
				// FileWriter fw = new FileWriter(f, true); // the true
				// // // will
				// // // append
				// // // the new
				// // // data
				// for (int i : dataOfChunks.keySet()) {
				// // fw.write(new String(dataOfChunks.get(i)));// appends
				// // the
				// // string
				// // to
				// // System.out.println(new
				// // String(dataOfChunks.get(i)));
				// } // the file
				// fw.close();
				// // } catch (IOException ioe) {
				// // System.err.println("IOException: " +
				// // ioe.getMessage());
				// // }
				// accessToDoOperations = true;
				// fileNameToRead = "";
				// counterToStartMerging = 0;
				// // dataOfChunks = new HashMap<Integer, byte[]>();
				// System.out
				// .println("[INFO] Client Has Got All Chunks.. Merging Is Done..");
				// System.out
				// .println("[CONT] You Can See File In Default Location");
				// // }
				// }
				// }
				//
				// // System.out.println("Read File");
				// // System.out.println("");
				// // System.out.println("File: " + new String(fileToBeRead) +
				// // " Chunk: " + chunkNumberA + "\nData: " + new
				// // String(dataReadInByte));
				// } else if (doStore == -1) {
				//
				// int errorSize = din.readInt();
				// byte[] error = new byte[errorSize];
				// din.readFully(error);
				// System.out.println(new String(error));
				// System.out.println("[CONT] Wait For Other Chunk Server To Send You Data..");
				// System.out.println("[CONT] Sorry Again..");
				//
				// }
				// System.out.println("SomeTHing Happened");
				break;
			default:
				System.out.println("UnUsual Activity In OnEvent");
				break;
			}
			din.close();
			baInputStream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void GenerateFile(int totalChunksOfFile, String fileNameToRead2) throws IOException {
		accessToDoOperations = true;
		System.out.println("File: Merged .. Location: " + fileNameToRead2+"_merged");
		FileWriter fw = new FileWriter(new File(fileNameToRead2+"_merged"));
		for(int loop = 1; loop<=totalChunksOfFile;loop++)
		{
			fw.append(new String(generatedFile.get(loop)));
		}
		fw.close();
	}

	private void Reconstruct(int i) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Merging Started For Chunk " + i);
		
		final byte [] [] shards = new byte [TOTAL_SHARDS] [];
        final boolean [] shardPresent = new boolean [TOTAL_SHARDS];
        int shardSize = 0;
        int shardCount = 0;
//        for (int i = 0; i < TOTAL_SHARDS; i++) {
//            
//                shards[i] = new byte [shardSize];
//                
//        }
		int Q = 0;
		
		for(int yes : dataGet.get(i))
		{
			shards[yes-1] = dataRec.get(i).get(Q);
			shardSize = dataRec.get(i).get(Q).length ;
			Q++;
			shardCount++;
		}
//		for(byte[] data : dataRec.get(i))
//		{
////			System.out.println("data in shard? " + data.length);
//			
////			if(dataFalse.get(i) == (Q))
//			
//			shards[Q] = data;
//			shardSize = data.length ;
//			shardCount++;
//			Q++;
//		}
		for(int a = 0; a<TOTAL_SHARDS;a++)
		{
			shardPresent[a] = true;
		}
		System.out.println("DataFalse: " + dataFalse);
		for(int j : dataFalse.get(i))
		{
			System.out.println("Called.. Setting " + i + "Chunk" + j + "Shard");
			shardPresent[j-1] = false;
			shards[j-1] = new byte [shardSize];
		}
		
//		for (int j = 0; j < TOTAL_SHARDS; j++) {
//            if (!dataFalse.get(i).contains((j+1))) {
//                shards[j] = new byte [shardSize];
//            }
//        }
		if(dataFalse.get(i).size()<=PARITY_SHARDS)
		{
			
		}
		else
		{
			System.out.println("[ERROR] File Wont Be Converged Properly..");
			System.out.println("[ERROR] Chunk " + i + " Is Totally Corrupted..");
		}
		ReedSolomon reedSolomonM = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
		reedSolomonM.decodeMissing(shards, shardPresent, 0, shardSize);

        // Combine the data shards into one buffer for convenience.
        // (This is not efficient, but it is convenient.)
        byte [] allBytes = new byte [shardSize * DATA_SHARDS];
        for (int a = 0; a < DATA_SHARDS; a++) {
            System.arraycopy(shards[a], 0, allBytes, shardSize * a, shardSize);
        }

        // Extract the file length
        int fileSize = ByteBuffer.wrap(allBytes).getInt();
//        System.out.println("File Size: " + fileSize);
//        System.out.println("All BySize: " + allBytes.length);
//        System.out.println("All ByteInInt" + BYTES_IN_INT);
//        byte[] recoveredData = new byte[fileSize - BYTES_IN_INT];
//         System.arraycopy(allBytes, BYTES_IN_INT, recoveredData, 0, fileSize);
//        FileWriter fw = new FileWriter(new File(fileNameToRead+"_merged"));
        String mergedLocation = fileNameToRead;
        String[] useful = mergedLocation.split("\\.");
        mergedLocation = useful[0]+"_merged"+"."+useful[1];
		File f = new File(mergedLocation);
		if(f.exists()) f.delete(); 
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f, true);
//		fw.append(new String(generatedFile.get(loop)));
		fos.write(allBytes, BYTES_IN_INT, fileSize);
		fos.close();
		
		
//		fw.close();
//        generatedFile.put(i, allBytes);
//         byte[] recoveredData = Arrays.copyOf(allBytes, allBytes.length);
//         System.out.println("Chunk: " + i + "Data: " + new String(recoveredData));
		
	}

	// private synchronized void sendDataToChunks(int chunkGot,
	// ArrayList<Integer> listToSend) throws Exception {
	// // TODO Auto-generated method stub
	//
	// String fileGot = fileName;
	// int chunkNumber = chunkGot;
	// // System.out.println("chunkNumberGot" + chunkNumber);
	// byte[] dataToSend = fileIntoChunk.get(chunkGot - 1);
	// TCPConnection connectionFirst = knownID.get(listToSend.get(0));
	// listToSend.remove(0);
	// // System.out.println(fileName + "  " +chunkGot);
	// // totalNumberOfChunk--;
	// // System.out.println("Chunk Remaining .." + totalNumberOfChunk);
	// SendingDataToChunkServer dataToChunkServer = new
	// SendingDataToChunkServer(
	// knownIPPort, fileName, chunkNumber, dataToSend, listToSend);
	// connectionFirst.sender.sendData(dataToChunkServer.getByte());
	// totalNumberOfChunk--;
	// // System.out.println("Chunk Remaining .." + totalNumberOfChunk);
	// // totalNumberOfChunk--;
	// // System.out.println("Chunk Remaining .." + totalNumberOfChunk);
	// // totalNumberOfChunk--;
	// // System.out.println("Chunk Remaining .." + totalNumberOfChunk);
	// // System.out.println(totalNumberOfChunk);
	// if (totalNumberOfChunk == 0) {
	//
	// fileIntoChunk = new ArrayList<byte[]>();
	// fileName = "";
	// accessToDoOperations = true;
	// System.out
	// .println("[INFO] Client Has Successfully Send Data To Respective ChunkServers..");
	// System.out
	// .println("[INFO] Client Can Fire Up Further Requests....");
	// }
	//
	// }

}
