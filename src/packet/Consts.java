package packet;

public final class Consts {
    
    private Consts(){}

    public static final int MAX_PACKET_SIZE = 1472; //1500 - (20+8)

    public static final byte NEW_CONNECTION = 1;
    public static final byte FILE_META = 2;
    public static final byte DATA_TRANSFER = 3;
    public static final byte ACK = 4;

    public static final int METADATA_SIZE = 16;
    public static final int LAST_UP_SIZE = Long.BYTES;  
    public static final int STR_SIZE_SIZE = Integer.BYTES;  
    public static final int SEQ_NUM_SIZE = Short.BYTES;
}