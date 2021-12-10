package udp;

import java.net.*;
import java.io.*;

import static packet.Consts.*;

public class UDP_Listener implements Runnable, AutoCloseable {
    
    private final DatagramSocket dataSocket;
    private final int port;
    private final InetAddress address;
    private final File dir;
    private final FileTracker tracker;
   
    public UDP_Listener(int port, File dir, InetAddress address) throws SocketException {
        this.port = port;
        this.address = address;
        this.dir = dir;
        this.dataSocket = new DatagramSocket(this.port);
        this.tracker = new FileTracker(dir);
    }

    @Override
    public void run(){
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
        
        try{

            //aaa

            while(true){
                this.dataSocket.receive(inPacket);
                //aaa
                Thread t = new Thread(new UDP_Handler(inPacket, this.dir, address, port, this.tracker));
                t.start();
            } 
        }
        catch(IOException e){}
        finally{
            this.close();
        }
    }

    @Override
    public void close(){
        try{
            this.dataSocket.close();
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