import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Packet {
    
    private byte opcode;
    private byte[] metaDados;
    private long lastUpdated;
    private boolean hasNext;
    private int sequenceNumber;
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

    public Packet(byte opcode, int sequenceNumber, String filename, byte[] data){
        this.opcode = opcode;
        this.sequenceNumber = sequenceNumber;
        this.filename = filename;
        this.data = data;
    }


    public Packet(byte opcode, int sequenceNumber){
        this.opcode = opcode;
        this.sequenceNumber = sequenceNumber;
    }

    ///

    public static Packet deserialize(DatagramPacket dp){

        byte[] data = dp.getData();
        Packet p;
        int pos = 1;

        switch (data[0]) {
            case Constants.NEW_CONNECTION ->
                p = new Packet(Constants.NEW_CONNECTION);
                
            case Constants.FILE_META ->

                byte[] metaDados = new byte[Constants.METADATA_SIZE];
                System.arraycopy(data, pos, metaDados, 0, metaDados.length);
                pos += metaDados.length;

                byte[] lastUpdated = new byte[Constants.LAST_UP_SIZE];
                System.arraycopy(data, pos, lastUpdated, 0, lastUpdated.length);
                pos += lastUpdated.length;

                byte hasNext = data[pos];
                p = new Packet(Constants.FILE_META, metaDados, 
                               bytesToLong(lastUpdated), bytesToBoolean(hasNext));
            
            case Constants.DATA_TRANSFER ->

                byte[] seqNum = new byte[Constants.SEQ_NUM_SIZE];
                System.arraycopy(data, pos, seqNum, 0, seqNum.length);
                pos += seqNum.length;

                byte[] strSize = new byte[Constants.STR_SIZE_SIZE];
                System.arraycopy(data, pos, strSize, 0, strSize.length);
                pos += strSize.length;

                byte[] filename = new byte[bytesToInt(strSize)];
                System.arraycopy(data, pos, filename, 0, filename.length);
                pos += filename.length;

                byte[] data__ = new byte[data.length - pos];
                System.arraycopy(data, pos, data__, 0, data__.length);

                p = new Packet(Constants.DATA_TRANSFER, bytesToShort(seqNum),
                               new String(filename), data__);

            case ACK ->
                
                byte[] seqNum = new byte[Constants.SEQ_NUM_SIZE];
                System.arraycopy(data, pos, seqNum, 0, seqNum.length);
                
                p = new Packet(Constants.ACK, bytesToShort(seqNum));

            default ->
                p = null;
        }
        
        return p;
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

    private static boolean bytesToBoolean(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.allocate(Boolean.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getBoolean();
    }





    public DatagramPacket serialize(){
        
        byte[] data = new byte[MAX_PACKET_SIZE];
        DatagramPacket dp = new DatagramPacket(data, MAX_PACKET_SIZE);

        switch (this.opcode) {
            case 1:
                
                break;
        
            default:
                break;
        }



        return dp;

    }
}
/**
 * Tipo de Packets do protocolo

|  OPCODE  |     Operação                               
|         1         |    NEW_CONNECTION      
|         2         |    FILE_META    
|         3         |    DATA_TRANSFER                    
|         4         |    ACK                                      
                
NEW_CONNECTION

  1 byte                 
  ----------------
 | Opcode     |     
 -----------------

FILE_META

   1 byte             16 bytes               8 bytes          1 byte
  --------------------------------------------------------------------------------
 | Opcode |  md5Hash MetaDados | LastUpdated   | hasNext  | ----------------------------------------------------------------------------------
md5Hash MetaDados: Vamos atribuir 16 bytes visto ser o espaço ocupado pelo md5.


DATA_TRANSFER  

  1 byte            2 bytes            4 bytes       StringSize bytes       n bytes        
  ----------------------------------------------------------------------------------------------------
 | Opcode |  Sequence Number | StringSize    |     Filename         |    Data      |
 -----------------------------------------------------------------------------------------------------

String: Vamos atribuir 4 bytes para o tamanho da String (espaço que ocupa um inteiro)


ACK 

  1 byte            2 bytes  
  ----------------------------------------------
 | Opcode |     Sequence Number    |
 -----------------------------------------------
O Ack é um acknowledgment dos pacotes de DATA, sendo feito de pacote a pacote.


Protocolo de conexão inicial para ler um ficheiro
Host A envia um NEW_CONNECTION ao Host B.
Host B envia um FILE_META para o Host A com os metaDados (filename e data de criação) relevantes em md5.



O tamanho do Packet foi decidido pelo grupo de trabalho como 1472 bytes, no máximo.
Como o header do UDP tem um overhead de 8 bytes e o header IP tem um overhead de 20 bytes, a soma destes valores com o tamanho do Packet corresponde a 1500 bytes, que será o maior tamanho da mensagem do nosso protocolo.




 */