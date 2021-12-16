package udp;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import packet.*;
import static packet.Consts.*;

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

    private void sendMetadata(){//ter os envios dos metadados no log???
        List<Packet> toSendMetadata = this.tracker.toSendMetadataList();
        final int size = toSendMetadata.size();
        try{
            int i = 0;
            while(i < size){
                Packet p = toSendMetadata.get(i);
                try{
                    this.outSocket.send(p.serialize(this.address, this.port));
                }
                catch(IllegalOpCodeException e){
                    continue;
                }

                if(this.isAlive)
                    i++;
    
                while(!this.isAlive)
                    Thread.sleep(10);
            }
        }
        catch(Exception e){}
    }

    private void sendData(){
        List<Packet> toSendData = new ArrayList<>(this.tracker.toSendSet());
        final int size = toSendData.size();
        
        try{
            for(int i = 0; i++ < size && this.isAlive;){
                short seqNum = INIT_SEQ_NUMBER;
                Packet p = toSendData.get(i);
                FileChunkReader fcr = new FileChunkReader(p.getFilename());
                int windowSize = 1;
                String hash = p.getMD5Hash();
                
                while((!fcr.isFinished() || !this.tracker.isEmpty(hash)) && this.isAlive){
                    short curr = this.tracker.getCurrentSequenceNumber(hash);
                    
                    if(seqNum == curr)
                        windowSize *= 2;    
                    else{
                        windowSize = 1;
                        seqNum = curr;
                    }

                    seqNum = this.send(windowSize, seqNum, hash, fcr);
                    Thread.sleep(15 * windowSize);   //wait for an ACK
                }
                fcr.close();
                this.tracker.getLogs().add(p.getFilename()+" foi enviada com sucesso");
            }
        }
        catch(Exception e){}
    }

    private DatagramPacket nextDatagramPacket(String hash, short seqNum, FileChunkReader fcr){
        var p = this.tracker.getCachedPacket(hash, seqNum);
        
        if(p == null){
            byte[] data = fcr.nextChunk();
            p = new Packet(DATA_TRANSFER, seqNum, hash, !fcr.isFinished(), data);
            this.tracker.addToSent(hash, seqNum, p);
        }
        
        DatagramPacket dp = null;
            try{
                dp = p.serialize(this.address, this.port);
            }
            catch(IllegalOpCodeException e){}
        return dp;
    }

    private short send(int windowSize, short seqNum, String hash, FileChunkReader fcr) throws IOException {
        for(int j = 0; j++ < windowSize && !fcr.isFinished(); seqNum++){
            var dp = this.nextDatagramPacket(hash, seqNum, fcr);
            this.outSocket.send(dp);
        }
        return seqNum;
    }

    protected void signal(){
        this.isAlive = true;
    }

    protected void interrupt(){
        this.isAlive = false;
    }

    @Override
    public void close() throws IOException {
        this.outSocket.close();   
    }
}