import java.net.*;

import packet.Consts;

import java.io.*;

public class UDP_Listener implements Runnable {
    
    private final DatagramSocket data_socket;
    private final int port;
    private final InetAddress address;
    private final String path;

    public UDP_Listener(int port, String path, InetAddress address) throws SocketException {
        this.port = port;
        this.address = address;
        this.path = path;
        this.data_socket = new DatagramSocket(this.port);
    }

    @Override
    public void run(){
        byte[] buffer = new byte[Consts.MAX_PACKET_SIZE];
        DatagramPacket in_packet = new DatagramPacket(buffer, buffer.length);
        
        try{
            while(true){
                this.data_socket.receive(in_packet);
                Thread t = new Thread(new UDP_Handler(in_packet, this.path));
                t.start();
            } 
        }
        catch(IOException e){}
        finally{
            this.close();
        }
    }

    public void close(){
        try{
            this.data_socket.close();
        }
        catch(Exception e){}
    }
}
