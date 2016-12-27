package protocols;

public class Protocol {
	public static final byte CHUNK_SERVER_SENDS_REGISTRATION = 2;

	public static final byte CONTROLLER_SENDS_REGISTRATION_STATUS = 3;

	public static final byte CHUNK_SERVER_SENDS_MINOR_HEARTBEAT = 4;

	public static final byte CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT = 5;

	public static final byte CONTROLLER_SENDS_SURETY_CHECK = 6;

	public static final byte CHUNK_REPLY_TO_SURETY_CHECK = 7;

	public static final byte CLIENT_SENDS_REGISTRATION = 8;

	public static final byte CLIENT_REQUESTING_CHUNKSERVER = 9;

	public static final byte CONTROLLER_SENDS_CHUNKSERVERLIST = 10;

	public static final byte SENDING_DATA_TO_CHUNKSERVER = 11;

	public static final byte REQUESTING_READ_TO_CONTROLLER = 12;

	public static final byte CONTROLLER_RESPODING_TO_READ_REQUEST = 13;

	public static final byte CLIENT_REQUESTING_CHUNKSERVER_FOR_READ = 14;

	public static final byte CHUNKSERVER_DATA_TO_CLIENT = 15;

	public static final byte CHUNKSERVER_REQUESTS_CONTROLLER_FOR_HEALING = 16;

	public static final byte CONTROLLER_TELLS_CHUNKSERVER_TO_HEAL = 17;

	public static final byte SENDING_HEALED_FILE = 18;

	public static final byte INCONSISTENCY_LEVEL_INCREASED = 19;

}
