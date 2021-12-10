package packet;

public final class Consts {
    
    private Consts(){}

    public static final int MAX_PACKET_SIZE = 1472; //1500 - (20+8)

    public static final byte FILE_META = 1;
    public static final byte DATA_TRANSFER = 2;
    public static final byte ACK = 3;

    static final int HASH_SIZE = 16;
    static final int LAST_UP_SIZE = Long.BYTES;  
    static final int CREATION_SIZE = Long.BYTES;  
    static final int SEQ_NUM_SIZE = Short.BYTES;
    static final int NAME_SIZE_SIZE = Integer.BYTES;
    static final int DATA_SIZE_SIZE = Integer.BYTES;  
    public static final int DATA_SIZE = MAX_PACKET_SIZE - (1 + SEQ_NUM_SIZE + HASH_SIZE + DATA_SIZE_SIZE);

    public static final int SECONDS_OF_SLEEP = 60; 
}