package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import protocols.ControllerRespondingToReadRequest;
import node.Controller;

public class RequestForReadHandler {

	int cID;
	String fileToRead;
	Controller c;

	public RequestForReadHandler(int clientID, String reqFileName,
			Controller controller) {
		// TODO Auto-generated constructor stub
		this.cID = clientID;
		this.fileToRead = reqFileName;
		this.c = controller;
	}

	public void start() {
		// TODO Auto-generated method stub
		// System.out.println("File Name " + fileToRead);
		System.out.println("In Start");
		ControllerRespondingToReadRequest controllerRespondingToReadRequest = new ControllerRespondingToReadRequest();

		controllerRespondingToReadRequest.status = 1;
		controllerRespondingToReadRequest.controller = c;
		controllerRespondingToReadRequest.file = fileToRead;

		try {
			// System.out.println("Sending Exe");
			c.clientSenders.get(cID).sendData(
					controllerRespondingToReadRequest.getByte());
			// System.out.println("Sending Done");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println("Known" + knowID);
	}

}
