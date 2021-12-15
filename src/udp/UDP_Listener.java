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

    private final UDP_Sender udpSender;
    private final Thread senderThread;

    public UDP_Listener(int port, InetAddress address, File dir) throws SocketException {
        this.port = port;
        this.address = address;
        this.dir = dir;
        this.inSocket = new DatagramSocket(this.port);
        this.tracker = new FileTracker(this.dir);

        this.udpSender = new UDP_Sender(this.address, this.tracker);
        (this.senderThread = new Thread(this.udpSender)).start();
    }

    @Override
    public void run(){
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);

        try{
            while(true){
                this.inSocket.receive(inPacket);

                Thread handlerThread = new Thread(new UDP_Handler(inPacket, this.dir, this.tracker));
                handlerThread.start();

                this.udpSender.signal(); //it's important that the received packet is treated
                                         //before signaling the sender
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
            this.udpSender.close();
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