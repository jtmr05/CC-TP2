package udp;

import java.net.*;
import java.io.*;

import static packet.Consts.*;

public class UDP_Listener implements Runnable, AutoCloseable {
    
    private final int port;
    private final InetAddress address;
    private final DatagramSocket inSocket;
    private final File dir;
    private final FileTracker tracker;
    
    private final UDP_Sender udp_sender;
    private final Thread senderThread;
   
    public UDP_Listener(int port, InetAddress address, File dir) throws SocketException {
        this.port = port;
        this.address = address;
        this.dir = dir;
        this.inSocket = new DatagramSocket(this.port);
        this.tracker = new FileTracker(this.dir);

        this.udp_sender = new UDP_Sender(this.address, this.port, this.tracker);
        (this.senderThread = new Thread(this.udp_sender)).start();
    }

    @Override
    public void run(){
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
        
        try{
            while(true){
                this.inSocket.receive(inPacket);
                
                Thread t2 = new Thread(new UDP_Handler(inPacket, this.dir, address, port, this.tracker));
                t2.start();
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
            this.senderThread.interrupt();
            this.udp_sender.close();
            this.inSocket.close();
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