package packet;

public final class Consts {

    private Consts(){}

    public static final int MAX_PACKET_SIZE = 1472; //1500 - (20+8)

    public static final byte FILE_META = 1;
    public static final byte DATA_TRANSFER = 2;
    public static final byte ACK = 3;

    static final int HASH_SIZE = 16;
    static final int SEQ_NUM_SIZE = Short.BYTES;
    static final int NAME_SIZE_SIZE = Integer.BYTES;
    static final int DATA_SIZE_SIZE = Short.BYTES;
    static final int TIMESTAMP_SIZE = Long.BYTES;
    static final int HMAC_SIZE = 20;
    public static final int DATA_SIZE = MAX_PACKET_SIZE - (1 + SEQ_NUM_SIZE + HASH_SIZE + 1 + DATA_SIZE_SIZE);
    
    public static final short INIT_SEQ_NUMBER = 0;//Short.MIN_VALUE;

    static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    static final String KEY = "bomdia123";
}