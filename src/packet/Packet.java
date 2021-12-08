package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

import static java.nio.charset.StandardCharsets.UTF_8;

import static packet.Consts.*;
import utils.*;

public class Packet {
    
    /** The operation code as defined in {@linkplain Consts} */
    private byte opcode;

    /** md5 hash produced from the file's metadata */
    private String metaDados;
    
    private long lastUpdated;
    
    private boolean hasNext;
    
    private short sequenceNumber;
    
    private String filename;
    
    private byte[] data;
    


    public Packet(byte opcode){
        this.opcode = opcode;
    }
            
    public Packet(byte opcode, String metaDados, long lastUpdated, boolean hasNext){
        this.opcode = opcode;
        this.metaDados = metaDados;
        this.lastUpdated = lastUpdated;
        this.hasNext = hasNext;
    }

    public Packet(byte opcode, short sequenceNumber, String filename, byte[] data){
        this.opcode = opcode;
        this.sequenceNumber = sequenceNumber;
        this.filename = filename;
        this.data = data;
    }

    public Packet(byte opcode, short sequenceNumber){
        this.opcode = opcode;
        this.sequenceNumber = sequenceNumber;
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
                byte[] metaDados = new byte[METADATA_SIZE];
                System.arraycopy(data, pos, metaDados, 0, metaDados.length);
                pos += metaDados.length;

                byte[] lastUpdated = new byte[LAST_UP_SIZE];
                System.arraycopy(data, pos, lastUpdated, 0, lastUpdated.length);
                pos += lastUpdated.length;

                boolean hasNext = data[pos] == 0 ? false : true;

                p = new Packet(FILE_META, new String(metaDados, UTF_8), u.bytesToLong(lastUpdated), hasNext);
            }
            
            case DATA_TRANSFER -> {
                byte[] seqNum = new byte[SEQ_NUM_SIZE];
                System.arraycopy(data, pos, seqNum, 0, seqNum.length);
                pos += seqNum.length;

                byte[] strSize = new byte[STR_SIZE_SIZE];
                System.arraycopy(data, pos, strSize, 0, strSize.length);
                pos += strSize.length;

                byte[] filename = new byte[u.bytesToInt(strSize)];
                System.arraycopy(data, pos, filename, 0, filename.length);
                pos += filename.length;

                byte[] data__ = new byte[data.length - pos];
                System.arraycopy(data, pos, data__, 0, data__.length);

                p = new Packet(DATA_TRANSFER, u.bytesToShort(seqNum), new String(filename, UTF_8), data__);
            } 
                
            case ACK -> {
                byte[] seqNum = new byte[SEQ_NUM_SIZE];
                System.arraycopy(data, pos, seqNum, 0, seqNum.length);
                
                p = new Packet(ACK, u.bytesToShort(seqNum));
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
                byte[] strToBytes = this.metaDados.getBytes(UTF_8);
                System.arraycopy(strToBytes, 0, data, pos, this.metaDados.length());
                pos += this.metaDados.length();

                byte[] longBytes = u.longToBytes(this.lastUpdated);
                System.arraycopy(longBytes, 0, data, pos, longBytes.length);
                pos += longBytes.length;

                data[pos] = (byte) (this.hasNext ? 0 : 1);
            }
            
            case DATA_TRANSFER -> {
                byte[] shortBytes = u.shortToBytes(this.sequenceNumber);
                System.arraycopy(shortBytes, 0, data, pos, shortBytes.length);
                pos += shortBytes.length;

                byte[] intBytes = u.intToBytes(this.filename.length());
                System.arraycopy(intBytes, 0, data, pos, intBytes.length);
                pos += intBytes.length;

                byte[] filename = this.filename.getBytes(UTF_8);
                System.arraycopy(filename, 0, data, pos, filename.length);
                pos += filename.length;
                
                System.arraycopy(this.data, 0, data, pos, this.data.length);
            }

            case ACK -> {
                byte[] shortBytes = u.shortToBytes(this.sequenceNumber);
                System.arraycopy(shortBytes, 0, data, pos, shortBytes.length);
                pos += shortBytes.length;

            }

            default ->
                throw new IllegalOpCodeException();
        }
        return new DatagramPacket(data, MAX_PACKET_SIZE, ip, port);
    }
}