package udp;

import java.net.*;

import utils.Utils;

import java.io.*;

import static packet.Consts.*;

public class UDP_Listener implements Runnable, Closeable {

    private final int port;
    private final InetAddress address;
    private final DatagramSocket inSocket;
    private final File dir;
    private final FileTracker tracker;

    private UDP_Sender udpSender;
    private Thread senderThread;

    public UDP_Listener(int port, InetAddress address, File dir, FileTracker f) throws SocketException {
        this.port = port;
        this.address = address;
        this.dir = dir;
        this.inSocket = new DatagramSocket(this.port);
        this.tracker = f;
    }

    @Override
    public void run(){
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);

        try{
            int peerPort = this.init();

            this.udpSender = new UDP_Sender(this.address, peerPort, this.tracker);
            (this.senderThread = new Thread(this.udpSender)).start();

            while(true){
                this.inSocket.receive(inPacket);

                Thread handlerThread = new Thread(new UDP_Handler(inPacket, this.dir, this.tracker, peerPort));
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

    private int init() throws IOException {
        Utils u = new Utils();

        var s = new DatagramSocket(this.port);

        var buff = u.intToBytes(Ports.get());
        var out = new DatagramPacket(buff, buff.length, this.address, this.port);
        s.send(out);

        var in = new DatagramPacket(new byte[buff.length], buff.length);
        s.receive(in);

        s.close();
        return u.bytesToInt(in.getData());
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