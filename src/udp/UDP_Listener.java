package udp;

import java.net.*;
import java.io.*;

import packet.Consts;

public class UDP_Listener implements Runnable {
    
    private final DatagramSocket data_socket;
    private final int port;
    private final InetAddress address;
    private final File dir;
    private final MetadataTracker mapWrapper;
   
    public UDP_Listener(int port, File dir, InetAddress address) throws SocketException {
        this.port = port;
        this.address = address;
        this.dir = dir;
        this.data_socket = new DatagramSocket(this.port);
        this.mapWrapper = new MetadataTracker(dir);
    }

    @Override
    public void run(){
        byte[] buffer = new byte[Consts.MAX_PACKET_SIZE];
        DatagramPacket in_packet = new DatagramPacket(buffer, buffer.length);
        
        try{

            //aaa

            while(true){
                this.data_socket.receive(in_packet);
                //aaa
                Thread t = new Thread(new UDP_Handler(in_packet, this.dir, address, port, this.mapWrapper));
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


// try {
//     String s = CompletableFuture.supplyAsync(() -> br.readLine()).get(1, TimeUnit.SECONDS);
// } catch (TimeoutException e) {
//     System.out.println("Time out has occurred");
// } catch (InterruptedException | ExecutionException e) {
//     // Handle
// }