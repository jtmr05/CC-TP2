package packet;

/** This class cannot be instantiated. 
 * It provides the necessary constants for the implementation of this protocol.
 */
public final class Consts {

    private Consts(){}

    /** The max data size is equal to the max UDP data size ({@code 1500}) 
     * minus network and transport layers' overheads ({@code 20} + {@code 8}).*/
    public static final int MAX_PACKET_SIZE = 1472;

    /** This {@code Packet}'s opcode indicates a transfer of file metadata. */
    public static final byte FILE_META = 1;

    /** This {@code Packet}'s opcode indicates a transfer of a chunk of file data. */
    public static final byte DATA_TRANSFER = 2;

    /** This {@code Packet}'s ocpode indicates an acknowledgement. */
    public static final byte ACK = 3;



    /** The size of the md5 hash computed from the file's metadata (filename). */
    static final int HASH_SIZE = 16;
    /** The size of the sequence number. */
    static final int SEQ_NUM_SIZE = Short.BYTES;
    /** The size of a {@code String}'s size ({@code 4} bytes). */
    static final int NAME_SIZE_SIZE = Integer.BYTES;
    /** The size of the length of the byte array of raw data. 
     * Since its max value is under {@code 1472}, a {@code short} is enough. */
    static final int DATA_SIZE_SIZE = Short.BYTES;
    /** The size of a timestamp (a {@code long} representing milliseconds since epoch time). */
    static final int TIMESTAMP_SIZE = Long.BYTES;
    /** The size of the HMAC computed from the {@code Packet}'s parameters accordingly. */
    static final int HMAC_SIZE = 20;
    /** The length of the byte array of raw data. It is equal to the {@code MAX_PACKET_SIZE} 
     * minus the other parameters of the {@code DATA_TRANSFER} {@code Packet}. */
    public static final int DATA_SIZE = MAX_PACKET_SIZE - (1 + SEQ_NUM_SIZE + HASH_SIZE + 1 + DATA_SIZE_SIZE);
    
    /** Initial sequence number. */
    public static final short INIT_SEQ_NUMBER = 0; 

    /** Algorithm used for computing a HMAC value.  */
    static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    /** The secret key for computing a HMAC value. */
    static final String KEY = "bomdia123";
}