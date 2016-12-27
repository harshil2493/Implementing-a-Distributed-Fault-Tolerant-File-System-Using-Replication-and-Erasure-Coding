package util;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import protocols.ClientRequestingChunkServer;
import protocols.RequestingReadToController;
import node.Client;
import node.Node;

public class InteractiveCommandParser implements Runnable {
	public Node node;

	public InteractiveCommandParser(Node nodeThreaded) {
		// TODO Auto-generated constructor stub
		node = nodeThreaded;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		System.out
				.println("[INFO] Interactive Command Parser Is Running For Client..");
		while (true) {
			Scanner reader = new Scanner(System.in);

			String command = reader.nextLine();
			Client c = (Client) node;
			if (c.accessToDoOperations) {
				c.accessToDoOperations = false;

				if (command.contains("store")) {
					System.out.println("[INFO] Storing Initiated..");
					// System.out.println("char At" + command.charAt(5));
					if (command.charAt(5) == ' ') {

						String needed = ((command.substring(6)));
						File absoluteFile = new File(needed);
						needed = absoluteFile.getAbsolutePath();

						if (needed.length() == 0) {
							c.accessToDoOperations = true;
							System.out
									.println("You Must Provide File Name.. Start Again");
						} else {

							MergeSplit mergeSplit = new MergeSplit(
									(Client) node);
							try {
								if (mergeSplit.splitFile(new File(needed))) {
									// c.accessToDoOperations = false;
									// System.out.println(c.fileIntoChunk.size());
									System.out.println("[INFO] Chunks Created");
									System.out
											.println("[INFO] Files Store In ChunksServer Too..");

									c.fileName = needed;
									c.accessToDoOperations = true;
									// StartRequestingController
									// startRequestingController = new
									// StartRequestingController(
									// c, c.totalNumberOfChunk, needed);
									// // startRequestingController.chunkNumber
									// =
									// // i+1;
									// // startRequestingController.FileName =
									// // needed;
									// // startRequestingController.reqClient =
									// c;
									// // byte[] x = {(byte) 1};
									// //
									// c.connectionToController.sender.sendData(x);
									// startRequestingController.startSending();

									// ClientRequestingChunkServer
									// clientRequestingChunkServer = new
									// ClientRequestingChunkServer(c, needed,
									// (i+1));
									// try {
									//
									// c.connectionToController.sender.sendData(clientRequestingChunkServer.getByte());
									// System.out.println("Called? Run");
									// } catch (Exception e) {
									// // TODO Auto-generated catch block
									// e.printStackTrace();9
									// }

									// c.accessToDoOperations = false;

								} else {
									c.accessToDoOperations = true;
									System.out
											.println("[ERROR] Some Error Happened While Chunking File");
								}
								// System.out.println((Client) node.);
							} catch (IOException e) {
								c.accessToDoOperations = true;
								System.out
										.println("[ERROR] File Chunking Error");

								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {
						c.accessToDoOperations = true;
						System.out
								.println("Put Space Between Filename And store");
					}
					// MergeSplit mergeSplit = new MergeSplit()
				} else if (command.contains("read")) {
					System.out.println("[INFO] Retrieving Initiated..");

					// System.out.println("[INFO] Storing Initiated..");
					// System.out.println("char At" + command.charAt(5));
					if (command.charAt(4) == ' ') {
						String needToRead = ((command.substring(5)));
						if (needToRead.length() == 0) {
							c.accessToDoOperations = true;
							System.out
									.println("[ERROR] Give Name Of File And Try Again");
						} else {
							try {
								startRequestingController(c, c.myIDAsClient,
										needToRead);
							} catch (Exception e) {
								c.accessToDoOperations = true;

							}
						}

					} else {
						c.accessToDoOperations = true;
						System.out
								.println("[ERROR] Put Space Between Command And FileName");
					}

				} else if (command.contains("update")) {
					System.out.println("[INFO] Updating Initiated..");

				} else {
					c.accessToDoOperations = true;
					System.out.println("[ERROR] Some Problem With Command.. ");
					System.out.println("[CONT] Can You Try Again?.");
				}
			} else {
				System.out
						.println("[ERROR] Sorry... This Client Has Already Started Doing Some Operation With FileSystem..");
				System.out
						.println("[CONT] Once It Is Done.. You Will Be Notified And You Will Be Able To Access Other Functionality");
				System.out.println("[CONT] Thank You");
			}
			// System.out.println("Out Of Interactive");
		}

	}

	private void startRequestingController(Client c, int myIDAsClient,
			String needToRead) throws Exception {
		// TODO Auto-generated method stub
		RequestingReadToController requestingReadToController = new RequestingReadToController(
				myIDAsClient, needToRead);
		c.senderOfControllerSender.sendData(requestingReadToController
				.getByte());

		int dataLength;
		DataInputStream dataInputStream = new DataInputStream(
				c.senderOfControllerSender.socket.getInputStream());
		dataLength = dataInputStream.readInt();
		byte[] data = new byte[dataLength];
		dataInputStream.readFully(data, 0, dataLength);
		c.onEvent(data, c.senderOfControllerSender.socket);
	}

}
