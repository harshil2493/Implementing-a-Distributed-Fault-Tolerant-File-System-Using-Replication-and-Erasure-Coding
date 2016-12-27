package node;

import java.net.Socket;

public interface Node {
	public void onEvent(byte[] data, Socket socket) throws Exception;
}
