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
        this.opcode = opcode;
        this.sequenceNumber = sequenceNumber;
        this.md5hash = md5hash;
        this.timestamp = System.currentTimeMillis();

        this.lastUpdated = this.creationDate = -1;
        this.hasNext = false;
        this.filename = (String) (Object) (this.data = null);
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

        byte[] autodata = dp.getData();
        Packet p;
        int pos = 1;
        Utils u = new Utils();

        switch (autodata[0]){

            case FILE_META -> {
                byte[] md5hash = new byte[HASH_SIZE];
                System.arraycopy(autodata, pos, md5hash, 0, md5hash.length);
                pos += md5hash.length;

                byte[] lastUpdated = new byte[LAST_UP_SIZE];
                System.arraycopy(autodata, pos, lastUpdated, 0, lastUpdated.length);
                pos += lastUpdated.length;

                byte[] creationDate = new byte[CREATION_SIZE];
                System.arraycopy(autodata, pos, creationDate, 0, creationDate.length);
                pos += creationDate.length;

                byte[] nameSize = new byte[NAME_SIZE_SIZE];
                System.arraycopy(autodata, pos, nameSize, 0, nameSize.length);
                pos += nameSize.length;

                byte[] filename = new byte[u.bytesToInt(nameSize)];
                System.arraycopy(autodata, pos, filename, 0, filename.length);
                pos += filename.length;

                boolean hasNext = autodata[pos] != 0;

                p = new Packet(FILE_META, new String(md5hash, UTF_8), u.bytesToLong(lastUpdated),
                               u.bytesToLong(creationDate), new String(filename, UTF_8), hasNext);
            }

            case DATA_TRANSFER -> {
                byte[] seqNum = new byte[SEQ_NUM_SIZE];
                System.arraycopy(autodata, pos, seqNum, 0, seqNum.length);
                pos += seqNum.length;

                byte[] md5hash = new byte[HASH_SIZE];
                System.arraycopy(autodata, pos, md5hash, 0, md5hash.length);
                pos += md5hash.length;

                boolean hasNext = autodata[pos] != 0;
                pos++;

                byte[] dataSize = new byte[DATA_SIZE_SIZE];
                System.arraycopy(autodata, pos, dataSize, 0, dataSize.length);
                pos += dataSize.length;

                System.out.println("\tdata__ size is "+u.bytesToShort(dataSize)+" | pos is "+pos);
                System.out.println("\tdata__ size_size is "+dataSize.length);
                byte[] data__ = new byte[(int) u.bytesToShort(dataSize)];
                System.arraycopy(autodata, pos, data__, 0, data__.length);

                p = new Packet(DATA_TRANSFER, u.bytesToShort(seqNum), new String(md5hash, UTF_8),
                               hasNext, data__);
            }

            case ACK -> {
                byte[] seqNum = new byte[SEQ_NUM_SIZE];
                System.arraycopy(autodata, pos, seqNum, 0, seqNum.length);
                pos += seqNum.length;

                byte[] md5hash = new byte[HASH_SIZE];
                System.arraycopy(autodata, pos, md5hash, 0, md5hash.length);
                pos += md5hash.length;

                byte[] timestamp = new byte[TIMESTAMP_SIZE];
                System.arraycopy(autodata, pos, timestamp, 0, timestamp.length);

                p = new Packet(ACK, u.bytesToShort(seqNum), new String(md5hash, UTF_8),
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

        byte[] autodata = new byte[MAX_PACKET_SIZE];
        autodata[0] = this.opcode;
        int pos = 1;
        Utils u = new Utils();

        switch(this.opcode){

            case FILE_META -> {

                byte[] strToBytes = this.md5hash.getBytes(UTF_8);
                System.arraycopy(strToBytes, 0, autodata, pos, this.md5hash.length());
                pos += this.md5hash.length();

                byte[] longBytes = u.longToBytes(this.lastUpdated);
                System.arraycopy(longBytes, 0, autodata, pos, longBytes.length);
                pos += longBytes.length;

                byte[] longBytes_ = u.longToBytes(this.creationDate);
                System.arraycopy(longBytes_, 0, autodata, pos, longBytes_.length);
                pos += longBytes_.length;

                byte[] intBytes = u.intToBytes(this.filename.length());
                System.arraycopy(intBytes, 0, autodata, pos, intBytes.length);
                pos += intBytes.length;

                byte[] filename = this.filename.getBytes(UTF_8);
                System.arraycopy(filename, 0, autodata, pos, filename.length);
                pos += filename.length;

                autodata[pos] = (byte) (this.hasNext ? 1 : 0);
                pos++;

                Arrays.fill(autodata, pos, autodata.length, (byte) 0);
            }

            case DATA_TRANSFER -> {

                byte[] shortBytes = u.shortToBytes(this.sequenceNumber);
                System.arraycopy(shortBytes, 0, autodata, pos, shortBytes.length);
                pos += shortBytes.length;

                byte[] hash = this.md5hash.getBytes(UTF_8);
                System.arraycopy(hash, 0, autodata, pos, hash.length);
                pos += hash.length;

                autodata[pos] = (byte) (this.hasNext ? 1 : 0);
                pos++;

                byte[] shortBytes_ = u.shortToBytes((short) this.data.length);
                System.arraycopy(shortBytes_, 0, autodata, pos, shortBytes_.length);
                pos += shortBytes_.length;

                System.arraycopy(this.data, 0, autodata, pos, this.data.length);
                pos += this.data.length;

                Arrays.fill(autodata, pos, autodata.length, (byte) 0);
            }

            case ACK -> {
                byte[] shortBytes = u.shortToBytes(this.sequenceNumber);
                System.arraycopy(shortBytes, 0, autodata, pos, shortBytes.length);
                pos += shortBytes.length;

                byte[] hash = this.md5hash.getBytes(UTF_8);
                System.arraycopy(hash, 0, autodata, pos, hash.length);
                pos += hash.length;

                byte[] timestamp = u.longToBytes(this.timestamp);
                System.arraycopy(timestamp, 0, autodata, pos, timestamp.length);
                pos += timestamp.length;

                Arrays.fill(autodata, pos, autodata.length, (byte) 0);
            }

            default ->
                throw new IllegalOpCodeException();
        }
        return new DatagramPacket(autodata, MAX_PACKET_SIZE, ip, port);
    }
}