package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import protocols.ControllerSendsSuretyCheck;
import protocols.RequestingChunkForReSend;
import node.Controller;

public class ReduceCounterThread implements Runnable {

	Controller mainController;

	public ReduceCounterThread(Controller controller) {
		// TODO Auto-generated constructor stub
		this.mainController = controller;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int newValue;
		int oldValue;
		while (true) {
			try {
				Thread.sleep(1000);
				// System.out.println(mainController.counterForMayBeDead);
				// System.out.println(mainController.mayBeDead);
				synchronized (mainController.counterForMayBeDead) {
					if (mainController.counterForMayBeDead.size() != 0) {
						for (int i = 0; i < mainController.counterForMayBeDead
								.size(); i++) {
							oldValue = mainController.counterForMayBeDead
									.get(i);
							if (oldValue != 0) {
								newValue = oldValue - 1000;
								mainController.counterForMayBeDead.set(i,
										newValue);
								if (newValue == 0) {
									System.out.println("[INFO] Chunk Server "
											+ mainController.mayBeDead.get(i)
											+ " Is Down For Sure.. .. ");

									startRemovingProcessOfNode(mainController.mayBeDead
											.get(i));

									// mainController.mayBeDead.add((i+1));
									// mainController.counterForMayBeDead.add(5000);
									// ControllerSendsSuretyCheck check = new
									// ControllerSendsSuretyCheck();
									// mainController.IDToConnnection.get((i+1)).sender.sendData(check.getByte());

								}
								// else
								// {
								//
								// }
							}
						}
						// System.out.println(mainController.counterForChunks);
					}
				}
				// System.out.println("[INFO] Reducing Counter");
				// List<Integer> surelyDead = new ArrayList<Integer>();
				synchronized (mainController.counterForChunks) {
					if (mainController.counterForChunks.size() != 0) {
						for (int i = 0; i < mainController.counterForChunks
								.size(); i++) {
							oldValue = mainController.counterForChunks.get(i);
							if (oldValue != 0) {
								newValue = oldValue - 1000;
								mainController.counterForChunks
										.set(i, newValue);
								if (newValue == 0) {
									System.out.println("[INFO] Chunk Server "
											+ (i + 1) + " May Be Down.. .. ");

									mainController.mayBeDead.add((i + 1));
									mainController.counterForMayBeDead
											.add(5000);
									ControllerSendsSuretyCheck check = new ControllerSendsSuretyCheck();
									mainController.IDToConnnection.get((i + 1)).sender
											.sendData(check.getByte());

								}
								// else
								// {
								//
								// }
							}
						}
						// System.out.println(mainController.counterForChunks);
					}
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void startRemovingProcessOfNode(int nodeID) throws Exception {
		// TODO Auto-generated method stub
		synchronized (mainController.counterForMayBeDead) {
			mainController.counterForMayBeDead.remove(mainController.mayBeDead
					.indexOf(nodeID));
		}
		synchronized (mainController.mayBeDead) {
			mainController.mayBeDead.remove(mainController.mayBeDead
					.indexOf(nodeID));
		}
		
		
//		public Map<String, HashMap<Integer, Integer>> FileToChunks = new HashMap<String, HashMap<Integer, Integer>>();
		// List Of CS
//		public Map<Integer, List<Integer>> OffsetToChunkServersList = new HashMap<Integer, List<Integer>>();

//		public Map<Integer, Map<String, List<Integer>>> chunkServerTochunks = new HashMap<Integer, Map<String, List<Integer>>>();

//		System.out.println("OffSet To ChunkServersList" + mainController.OffsetToChunkServersList);
//		System.out.println("Chunk To " + mainController.chunkServerTochunks);
//		
//		Map<String, List<Integer>> needToReplicate = mainController.chunkServerTochunks.get(nodeID);
		
		
		synchronized (mainController.hostChunkServer) {
//			System.out.println("FileToChunk" + mainController.FileToChunks);
//			System.out.println("OffSet To ChunkServersList" + mainController.OffsetToChunkServersList);
//			System.out.println("Chunk To " + mainController.chunkServerTochunks);
			
			Map<String, List<Integer>> needToReplicate = mainController.chunkServerTochunks.get(nodeID);
			Random random = new Random();
			int size = mainController.hostAddressAndPortOfChunkServer.size();
			for(String fileName : needToReplicate.keySet())
			{
				for(int chunkNumber : mainController.FileToChunks.get(fileName).keySet())
				{
					if(chunkNumber != 0)
					{
						if(mainController.OffsetToChunkServersList.get(mainController.FileToChunks.get(fileName).get(chunkNumber)).contains(nodeID))
						{
							
							
							boolean notFound = true;
							int newID = 0;
							while(notFound)
							{
								notFound = false;
								newID = random.nextInt(size);
								newID++;
								if(mainController.deadNodes.contains(newID))
								{
									notFound = true;
								}
								if(mainController.OffsetToChunkServersList.get(mainController.FileToChunks.get(fileName).get(chunkNumber)).contains(newID))
								{
									notFound = true;
								}
								
							}
							
							
							Set<String> IPA = mainController.hostAddressAndPortOfChunkServer.get(newID).keySet();
//							System.out.println("IPA" + IPA.toArray()[0]);
							String IP = ((String)IPA.toArray()[0]).substring(1);
							int Port = mainController.hostAddressAndPortOfChunkServer.get(newID).get(IPA.toArray()[0]);
							RequestingChunkForReSend chunkForReSend = new RequestingChunkForReSend(newID, IP, Port, fileName+"_chunk"+chunkNumber);
//							System.out.println(fileName + " Chunk: " + chunkNumber + " Send It To" + newID +" Offset Value: " + mainController.OffsetToChunkServersList.get(mainController.FileToChunks.get(fileName).get(chunkNumber)));
							
							mainController.OffsetToChunkServersList.get(mainController.FileToChunks.get(fileName).get(chunkNumber)).remove(mainController.OffsetToChunkServersList.get(mainController.FileToChunks.get(fileName).get(chunkNumber)).indexOf(nodeID));
							
							int resposibleSenderRand = random.nextInt(1);
							
							int responsibleChunk = mainController.OffsetToChunkServersList.get(mainController.FileToChunks.get(fileName).get(chunkNumber)).get(resposibleSenderRand);
							
							
							mainController.OffsetToChunkServersList.get(mainController.FileToChunks.get(fileName).get(chunkNumber)).add(newID);
//							System.out.println("Probable ID" + newID);
							if(!mainController.chunkServerTochunks.get(newID).containsKey(fileName))
							{
								ArrayList<Integer> listOfChunks = new ArrayList<Integer>();
								mainController.chunkServerTochunks.get(newID).put(fileName, listOfChunks);
							}
							mainController.chunkServerTochunks.get(newID).get(fileName).add(chunkNumber);
							
							System.out.println("[INFO] Resender: "  + responsibleChunk + " File: " + fileName + " Chunk: " + chunkNumber + " Send It To " + newID +" Offset Value: " + mainController.OffsetToChunkServersList.get(mainController.FileToChunks.get(fileName).get(chunkNumber)));
							try
							{
							mainController.IDToConnnection.get(responsibleChunk).sender.sendData(chunkForReSend.getByte());
//							System.out.println("Requesting Is Done..");
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						}	
					}
					
				}
			}
//			System.out.println("After FileToChunk" + mainController.FileToChunks);
//			System.out.println("After OffSet To ChunkServersList" + mainController.OffsetToChunkServersList);
//			System.out.println("After Chunk To " + mainController.chunkServerTochunks);
////			hostAddressAndPortOfChunkServer
			
			mainController.deadNodes.add(nodeID);
			System.out.println("[INFO] Removing Node: " + nodeID + " Host: "
					+ mainController.hostChunkServer.get(nodeID));
			System.out.println("[INFO] Dead Nodes" + mainController.deadNodes);
			mainController.hostChunkServer.remove(nodeID);
			synchronized (mainController.numberOfChunksOnEachChunk) {
				mainController.numberOfChunksOnEachChunk.remove(nodeID);
			}
			synchronized (mainController.freeSpaceOnEachChunk) {
				mainController.freeSpaceOnEachChunk.remove(nodeID);
				if (mainController.freeSpaceOnEachChunk.isEmpty()) {
					System.out
							.println("[INFO] All Chunk Servers Are Dead.... Restart All Chunk Server If Possible Or Contact System Administrator");
				}
			}
			
			mainController.chunkServerTochunks.remove(nodeID);
			
		}
	}

}
