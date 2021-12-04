import java.net.DatagramPacket;

public class UDP_Handler implements Runnable {

    private final DatagramPacket in_packet;
    private final int MAX_SIZE;

    public UDP_Handler(DatagramPacket dp, int size) {            
        this.in_packet = dp;
        this.MAX_SIZE = size;
    }

    @Override
    public void run() {
        
        byte[] in = this.in_packet.getData();
        this.parser(in);

         
    }
   
    private void parser(byte[] buffer){
        int opcode = (int) buffer[0];
        
        switch(opcode){
            case 1:
        
        }


    }

}
/**
 * +-------+
 * |       | parking lot
 * +-------+
 *
 *
 */