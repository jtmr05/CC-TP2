package udp;

import java.io.*;
import java.net.*;

import packet.*;
import static packet.Consts.*;

import utils.AllChunksReadException;
import utils.FileChunkReader;

public class UDP_Sender implements Runnable, Closeable {

    private final DatagramSocket outSocket;
    private final InetAddress address;
    private final int port;
    private final FileTracker tracker;
    private boolean isAlive;

    protected UDP_Sender(InetAddress address, int port, FileTracker tracker, boolean isAlive) 
              throws SocketException {
        this.outSocket = new DatagramSocket();
        this.address = address;
        this.port = port;
        this.tracker = tracker;
        this.isAlive = isAlive;
    }

    protected UDP_Sender(InetAddress address, int port, FileTracker tracker) throws SocketException {
        this(address, port, tracker, false);
    }

    @Override
    public void run(){
        try{
            final int stride = 10, millis = SECONDS_OF_SLEEP * 1000;
            int i = 0;
            while(!Thread.interrupted()){
                if(i == 0){
                    this.sendMetadata();
                    this.sendData();
                }
                i += stride;
                if(i >= millis)
                    i = 0;
                Thread.sleep(stride);
            }
        }
        catch(InterruptedException e){}
    }

    private void sendMetadata(){
        var toSendMetadataIter = this.tracker.toSendMetadataList().iterator();
        try{
            while(toSendMetadataIter.hasNext()){
                try{
                    var dp = toSendMetadataIter.next().serialize(this.address, this.port);
                    this.outSocket.send(dp);
                    while(!this.isAlive)
                        Thread.sleep(10);
                }
                catch(IllegalOpCodeException e){
                    continue;
                }
            }
        }
        catch(Exception e){}
    }

    private void sendData(){
        var toSendIter = this.tracker.toSendSet().iterator();
        short seqNum = 0;

        try{
            while(toSendIter.hasNext()){
                Packet p = toSendIter.next();
                var fcr = new FileChunkReader(p.getFilename());
                
                do{
                    byte[] data = fcr.nextChunk();
                    Packet toSend = new Packet(DATA_TRANSFER, seqNum, p.getMD5Hash(), !fcr.isFinished(), data);
                    seqNum++;
                    this.outSocket.send(toSend.serialize(this.address, this.port));    
                } 
                while(!fcr.isFinished());
            }
        }
        catch(IllegalOpCodeException | IOException | AllChunksReadException e){}
    }

    protected void signal(){
        this.isAlive = true;
    }

    @Override
    public void close() throws IOException {
        this.outSocket.close();   
    }
}