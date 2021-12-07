import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Packet {
    
    private byte opcode;
    private byte[] metaDados;
    private long lastUpdated;
    private boolean hasNext;
    private short sequenceNumber;
    private String filename;
    private byte[] data;
    
    public Packet(byte opcode){
        this.opcode = opcode;
    }
            
    public Packet(byte opcode, byte[] metaDados, long lastUpdated, boolean hasNext){
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

    ///

    public static Packet deserialize(DatagramPacket dp){

        byte[] data = dp.getData();
        Packet p;
        int pos = 1;

        switch (data[0]) {
            case Consts.NEW_CONNECTION ->
                p = new Packet(Consts.NEW_CONNECTION);
                
            case Consts.FILE_META -> {    
                byte[] metaDados = new byte[Consts.METADATA_SIZE];
                System.arraycopy(data, pos, metaDados, 0, metaDados.length);
                pos += metaDados.length;

                byte[] lastUpdated = new byte[Consts.LAST_UP_SIZE];
                System.arraycopy(data, pos, lastUpdated, 0, lastUpdated.length);
                pos += lastUpdated.length;

                boolean hasNext = data[pos]==0 ? false : true;
                p = new Packet(Consts.FILE_META, metaDados, bytesToLong(lastUpdated), hasNext);
            }
            
            case Consts.DATA_TRANSFER -> {
                byte[] seqNum = new byte[Consts.SEQ_NUM_SIZE];
                System.arraycopy(data, pos, seqNum, 0, seqNum.length);
                pos += seqNum.length;

                byte[] strSize = new byte[Consts.STR_SIZE_SIZE];
                System.arraycopy(data, pos, strSize, 0, strSize.length);
                pos += strSize.length;

                byte[] filename = new byte[bytesToInt(strSize)];
                System.arraycopy(data, pos, filename, 0, filename.length);
                pos += filename.length;

                byte[] data__ = new byte[data.length - pos];
                System.arraycopy(data, pos, data__, 0, data__.length);

                p = new Packet(Consts.DATA_TRANSFER, bytesToShort(seqNum), new String(filename), data__);
            } 
                
            case Consts.ACK -> {
                byte[] seqNum = new byte[Consts.SEQ_NUM_SIZE];
                System.arraycopy(data, pos, seqNum, 0, seqNum.length);
                
                p = new Packet(Consts.ACK, bytesToShort(seqNum));
            }
            
            default ->
                p = null;
        }
        
        return p;
    }


    private static short bytesToShort(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getShort();        
    }

    private static long bytesToLong(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip 
        return buffer.getLong();
    }

    private static int bytesToInt(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getInt();
    }



    public DatagramPacket serialize(){
        
        byte[] data = new byte[Consts.MAX_PACKET_SIZE];
        data[0] = this.opcode;
        int pos=1;
        

        switch (this.opcode) {
            case Consts.NEW_CONNECTION:
                //data[0] = this.opcode;             
                break;

            case Consts.FILE_META:
                
          

                System.arraycopy(this.metaDados, 0, data, pos, this.metaDados.length);
                pos+=this.metaDados.length;

                byte[] longBytes = longToBytes(this.lastUpdated);
                System.arraycopy(longBytes, 0, data, pos, longBytes.length);
                pos+=longBytes.length;

                data[pos]=(byte) (this.hasNext?0:1);
                
                break;
            
            case Consts.DATA_TRANSFER:

                // data[0]= this.opcode;
                byte[] shortBytes = shortToBytes(this.sequenceNumber);
                System.arraycopy(shortBytes, 0, data, pos, shortBytes.length);
                pos+=shortBytes.length;

                byte[] intBytes = intToBytes(this.filename.length());
                System.arraycopy(intBytes, 0, data, pos, intBytes.length);
                pos+=intBytes.length;

                byte[] filename = this.filename.getBytes();
                System.arraycopy(filename, 0, data, pos, filename.length);
                pos+=filename.length;
                
                System.arraycopy(this.data, 0, data, pos, this.data.length);

                break;

            case Consts.ACK:

                byte[] shortBytes1 = shortToBytes(this.sequenceNumber);
                System.arraycopy(shortBytes1, 0, data, pos, shortBytes1.length);
                pos+=shortBytes1.length;

                break;

            default:
                break;
        }

        DatagramPacket dp = new DatagramPacket(data, Consts.MAX_PACKET_SIZE);

        return dp;

    }



    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public byte[] shortToBytes(short x) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(x);
        return buffer.array();
    }

    public byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }


}
