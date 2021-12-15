package udp;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.*;

import packet.*;
import static packet.Consts.*;
import static udp.RTT.*;

import utils.FileChunkReader;

public class UDP_Sender implements Runnable, Closeable {

    private final DatagramSocket outSocket;
    private final InetAddress address;
    private final FileTracker tracker;
    private boolean isAlive;
    private final Lock lock;

    protected UDP_Sender(InetAddress address, FileTracker tracker, boolean isAlive)
              throws SocketException {
        this.outSocket = new DatagramSocket();
        this.address = address;
        this.tracker = tracker;
        this.isAlive = isAlive;
        this.lock = new ReentrantLock();
    }

    protected UDP_Sender(InetAddress address, FileTracker tracker) throws SocketException {
        this(address, tracker, false);
    }

    @Override
    public void run(){
        try{
            final int stride = 10, millis = MILLIS_OF_SLEEP;
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
        List<Packet> toSendMetadata = this.tracker.toSendMetadataList();
        final int size = toSendMetadata.size();
        try{
            int i = 0;
            while(i < size){
                Packet p = toSendMetadata.get(i);
                try{
                    this.outSocket.send(p.serialize(this.address, Ports.get()));
                }
                catch(IllegalOpCodeException e){
                    continue;
                }

                if(this.isAlive)
                    i++;
                else
                    this.timeout();
            }
        }
        catch(Exception e){}
    }

    private void sendData(){
        List<Packet> toSendData = new ArrayList<>(this.tracker.toSendSet());
        final int size = toSendData.size();

        try{
            for(int i = 0; i < size; i++){
                short seqNum = INIT_SEQ_NUMBER;
                Packet p = toSendData.get(i);
                FileChunkReader fcr = new FileChunkReader(p.getFilename());
                int windowSize = 1;
                String hash = p.getMD5Hash();
                int numOfTries = 0;

                while((!fcr.isFinished()) || (!this.tracker.isEmpty(hash))){
                    this.timeout();
                    short curr = this.tracker.getCurrentSequenceNumber(hash);

                    if(seqNum == curr){
                        windowSize *= 2;
                        numOfTries = 0;
                    }
                    else{
                        windowSize = 1;
                        seqNum = curr;
                    }

                    seqNum = this.send(windowSize, seqNum, hash, fcr);
                    numOfTries++;
                    Thread.sleep(ESTIMATED_RTT * windowSize);   //wait for ACKs

                    if(numOfTries == 3){
                        this.interrupt();
                        numOfTries = 0;
                    }
                }
                fcr.close();
            }
        }
        catch(Exception e){}
    }

    private short send(int windowSize, short seqNum, String hash, FileChunkReader fcr) throws IOException {
        for(int j = 0; j < windowSize && !fcr.isFinished(); j++){
            var dp = this.nextDatagramPacket(hash, seqNum, fcr);
            this.outSocket.send(dp);
            seqNum++;
        }
        return seqNum;
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
                dp = p.serialize(this.address, Ports.get());
            }
            catch(IllegalOpCodeException e){}
        return dp;
    }

    protected void signal(){
        this.lock.lock();
        this.isAlive = true;
        this.lock.unlock();
    }

    private void interrupt(){
        this.lock.lock();
        this.isAlive = false;
        this.lock.unlock();
    }

    private void timeout(){
        while(!this.isAlive)
            try{
                Thread.sleep(10);
            }
            catch(InterruptedException e){}
    }

    @Override
    public void close() throws IOException {
        this.outSocket.close();
    }
}