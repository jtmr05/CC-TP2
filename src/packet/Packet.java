package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

import static packet.Consts.*;
import utils.*;

public class Packet {

    /** The operation code as defined in {@linkplain Consts} */
    private final byte opcode;
    /** md5 hash produced from the file's metadata (CreationDate and Filename) */
    private final String md5hash;
    /** Epoch time of the last time file was updated*/
    private final long lastUpdated;
    /** Epoch time of the files creation date */
    private final long creationDate;
    /** This attribute tells us if this is the last file*/
    private final boolean hasNext;
    /** The chunk sequence number*/
    private final short sequenceNumber;
    /** The filename */
    private final String filename;
    /** The raw data being sent/received*/
    private final byte[] data;
    /** Timestamp used to calculate RTT */
    private final long timestamp;


    //FILE_META
    public Packet(byte opcode, String md5hash, long lastUpdated, long creationDate, String filename,
                  boolean hasNext){
        this.opcode = opcode;
        this.md5hash = md5hash;
        this.lastUpdated = lastUpdated;
        this.creationDate = creationDate;
        this.hasNext = hasNext;
        this.filename = filename;

        this.timestamp = this.sequenceNumber = -1;
        this.data = null;
    }

    //DATA_TRANSFER
    public Packet(byte opcode, short sequenceNumber, String md5hash, boolean hasNext, byte[] data){
        this.opcode = opcode;
        this.sequenceNumber = sequenceNumber;
        this.md5hash = md5hash;
        this.hasNext = hasNext;
        this.data = data;

        this.lastUpdated = this.creationDate = this.timestamp = -1;
        this.filename = null;
    }

    //ACK
    public Packet(byte opcode, short sequenceNumber, String md5hash, long timestamp){
        this.opcode = opcode;
        this.sequenceNumber = sequenceNumber;
        this.md5hash = md5hash;
        this.timestamp = timestamp;

        this.lastUpdated = this.creationDate = -1;
        this.hasNext = false;
        this.filename = (String) (Object) (this.data = null);
    }

    public Packet(byte opcode, short sequenceNumber, String md5hash){
        this(opcode, sequenceNumber, md5hash, System.currentTimeMillis());
    }

    public byte getOpcode(){
        return this.opcode;
    }

    public String getMD5Hash(){
        return this.md5hash;
    }

    public long getLastUpdated(){
        return this.lastUpdated;
    }

    public long getCreationDate(){
        return this.creationDate;
    }

    public boolean getHasNext(){
        return this.hasNext;
    }

    public short getSequenceNumber(){
        return this.sequenceNumber;
    }

    public String getFilename(){
        return this.filename;
    }

    public byte[] getData(){
        byte[] ret = new byte[this.data.length];
        System.arraycopy(this.data, 0, ret, 0, ret.length);
        return ret;
    }

    public long getTimestamp(){
        return this.timestamp;
    }

    /**
     * Returns a new packet from the given {@code dp}.
     *
     * @param   dp
     *          The DatagramPacket to read data from
     *
     * @return  The new Packet instance
     *
     * @throws  IllegalOpCodeException
     *          If the read op code is unknown
     */
    public static Packet deserialize(DatagramPacket dp) throws IllegalOpCodeException {

        byte[] data = dp.getData();
        Packet p;
        int pos = 1;
        Utils u = new Utils();

        switch (data[0]){

            case FILE_META -> {
                byte[] md5hash = new byte[HASH_SIZE];
                System.arraycopy(data, pos, md5hash, 0, md5hash.length);
                pos += md5hash.length;

                byte[] lastUpdated = new byte[LAST_UP_SIZE];
                System.arraycopy(data, pos, lastUpdated, 0, lastUpdated.length);
                pos += lastUpdated.length;

                byte[] creationDate = new byte[CREATION_SIZE];
                System.arraycopy(data, pos, creationDate, 0, creationDate.length);
                pos += creationDate.length;

                byte[] nameSize = new byte[NAME_SIZE_SIZE];
                System.arraycopy(data, pos, nameSize, 0, nameSize.length);
                pos += nameSize.length;

                byte[] filename = new byte[u.bytesToInt(nameSize)];
                System.arraycopy(data, pos, filename, 0, filename.length);
                pos += filename.length;

                boolean hasNext = data[pos] != 0;

                p = new Packet(FILE_META, u.bytesToHexStr(md5hash), u.bytesToLong(lastUpdated),
                               u.bytesToLong(creationDate), new String(filename, UTF_8), hasNext);
            }

            case DATA_TRANSFER -> {
                byte[] seqNum = new byte[SEQ_NUM_SIZE];
                System.arraycopy(data, pos, seqNum, 0, seqNum.length);
                pos += seqNum.length;

                byte[] md5hash = new byte[HASH_SIZE];
                System.arraycopy(data, pos, md5hash, 0, md5hash.length);
                pos += md5hash.length;

                boolean hasNext = data[pos] != 0;
                pos++;

                byte[] dataSize = new byte[DATA_SIZE_SIZE];
                System.arraycopy(data, pos, dataSize, 0, dataSize.length);
                pos += dataSize.length;

                byte[] data__ = new byte[(int) u.bytesToShort(dataSize)];
                System.arraycopy(data, pos, data__, 0, data__.length);

                p = new Packet(DATA_TRANSFER, u.bytesToShort(seqNum), u.bytesToHexStr(md5hash),
                               hasNext, data__);
            }

            case ACK -> {
                byte[] seqNum = new byte[SEQ_NUM_SIZE];
                System.arraycopy(data, pos, seqNum, 0, seqNum.length);
                pos += seqNum.length;

                byte[] md5hash = new byte[HASH_SIZE];
                System.arraycopy(data, pos, md5hash, 0, md5hash.length);
                pos += md5hash.length;

                byte[] timestamp = new byte[TIMESTAMP_SIZE];
                System.arraycopy(data, pos, timestamp, 0, timestamp.length);

                p = new Packet(ACK, u.bytesToShort(seqNum), u.bytesToHexStr(md5hash),
                               u.bytesToLong(timestamp));
            }

            default ->
                throw new IllegalOpCodeException();
        }
        return p;
    }

    /**
     * Produces a new datagram packet from this object.
     *
     * @param   ip
     *          The destination address
     * @param   port
     *          The destination port number
     *
     * @return  The new datagram packet
     *
     * @throws  IllegalOpCodeException
     *          If this object's {@code opcode} is unknown
     */
    public DatagramPacket serialize(InetAddress ip, int port) throws IllegalOpCodeException {

        byte[] data = new byte[MAX_PACKET_SIZE];
        data[0] = this.opcode;
        int pos = 1;
        Utils u = new Utils();

        switch(this.opcode){

            case FILE_META -> {

                byte[] md5hash = u.hexStrToBytes(this.md5hash);
                System.arraycopy(md5hash, 0, data, pos, this.md5hash.length());
                pos += this.md5hash.length();

                byte[] lastUpdated = u.longToBytes(this.lastUpdated);
                System.arraycopy(lastUpdated, 0, data, pos, lastUpdated.length);
                pos += lastUpdated.length;

                byte[] creationDate = u.longToBytes(this.creationDate);
                System.arraycopy(creationDate, 0, data, pos, creationDate.length);
                pos += creationDate.length;

                byte[] filenameLength = u.intToBytes(this.filename.length());
                System.arraycopy(filenameLength, 0, data, pos, filenameLength.length);
                pos += filenameLength.length;

                byte[] filename = this.filename.getBytes(UTF_8);
                System.arraycopy(filename, 0, data, pos, filename.length);
                pos += filename.length;

                data[pos] = (byte) (this.hasNext ? 1 : 0);
                pos++;

                Arrays.fill(data, pos, data.length, (byte) 0);
            }

            case DATA_TRANSFER -> {

                byte[] seqNum = u.shortToBytes(this.sequenceNumber);
                System.arraycopy(seqNum, 0, data, pos, seqNum.length);
                pos += seqNum.length;

                byte[] md5hash = u.hexStrToBytes(this.md5hash);
                System.arraycopy(md5hash, 0, data, pos, md5hash.length);
                pos += md5hash.length;

                data[pos] = (byte) (this.hasNext ? 1 : 0);
                pos++;

                byte[] dataLength = u.shortToBytes((short) this.data.length);
                System.arraycopy(dataLength, 0, data, pos, dataLength.length);
                pos += dataLength.length;

                System.arraycopy(this.data, 0, data, pos, this.data.length);
                pos += this.data.length;

                Arrays.fill(data, pos, data.length, (byte) 0);
            }

            case ACK -> {
                byte[] seqNum = u.shortToBytes(this.sequenceNumber);
                System.arraycopy(seqNum, 0, data, pos, seqNum.length);
                pos += seqNum.length;

                byte[] md5hash = u.hexStrToBytes(this.md5hash);
                System.arraycopy(md5hash, 0, data, pos, md5hash.length);
                pos += md5hash.length;

                byte[] timestamp = u.longToBytes(this.timestamp);
                System.arraycopy(timestamp, 0, data, pos, timestamp.length);
                pos += timestamp.length;

                Arrays.fill(data, pos, data.length, (byte) 0);
            }

            default ->
                throw new IllegalOpCodeException();
        }
        return new DatagramPacket(data, MAX_PACKET_SIZE, ip, port);
    }
}