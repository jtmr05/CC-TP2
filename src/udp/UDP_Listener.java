package udp;

import java.net.*;
import java.io.*;

import static packet.Consts.*;

public class UDP_Listener implements Runnable, Closeable {

    private final int localPort;
    private final InetAddress address;
    private final DatagramSocket inSocket;
    private final File dir;
    private final FileTracker tracker;
    private final int peerPort;
    private final UDP_Sender udpSender;
    private final Thread senderThread;

    public UDP_Listener(int localPort, InetAddress address, int peerPort, File dir, FileTracker tracker)
           throws SocketException {
        this.localPort = localPort;
        this.address = address;
        this.peerPort = peerPort;
        this.dir = dir;
        this.inSocket = new DatagramSocket(this.localPort);
        this.tracker = tracker;
        this.udpSender = new UDP_Sender(this.address, this.peerPort, this.tracker, this.dir);
        (this.senderThread = new Thread(this.udpSender)).start();
        //this.senderThread = new Thread(this.udpSender);
        System.out.println("will now send stuff");
    }

    @Override
    public void run(){
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);

        try{

            while(true){

                this.inSocket.receive(inPacket);
                this.udpSender.signal(); //it's important that the received packet is treated

                System.out.println("i hab gift");

                Thread handlerThread = new Thread(
                                       new UDP_Handler(inPacket, this.dir, this.tracker,
                                       this.localPort, this.peerPort, this.address));
                handlerThread.start();

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