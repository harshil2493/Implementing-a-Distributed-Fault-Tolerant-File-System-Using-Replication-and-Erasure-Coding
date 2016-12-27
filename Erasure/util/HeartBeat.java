package util;

import protocols.ChunkServerSendsMajorHeartBeat;
import protocols.ChunkServerSendsMinorHeartBeat;
import node.ChunkServer;

public class HeartBeat implements Runnable {
	ChunkServer ChunkServerAssociated;
	int flag = 0;
	int timeLimitToSend = 30000;

	public HeartBeat(ChunkServer chunkServer) {
		// TODO Auto-generated constructor stub

		this.ChunkServerAssociated = chunkServer;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("[INFO] HeartBeatThread Started..");
		ChunkServerSendsMajorHeartBeat majorHeartBeat = new ChunkServerSendsMajorHeartBeat(
				this.ChunkServerAssociated);
		ChunkServerSendsMinorHeartBeat minorHeartBeat = new ChunkServerSendsMinorHeartBeat(
				this.ChunkServerAssociated);
		while (true) {
			try {
				Thread.sleep(timeLimitToSend);
				flag++;
				if (flag % 10 == 0) {
					flag = 0;
					// System.out.println("[INFO] Sending Major HeartBeat");

					this.ChunkServerAssociated.senderToController
							.sendData(majorHeartBeat.getByte());
				} else {
					// System.out.println("[INFO] Sending Minor HeartBeat");

					this.ChunkServerAssociated.senderToController
							.sendData(minorHeartBeat.getByte());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
