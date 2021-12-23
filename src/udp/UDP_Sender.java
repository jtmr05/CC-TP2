package udp;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.*;

import packet.*;
import static packet.Consts.*;
import static udp.RTT.*;

import utils.FileChunkReader;

public class UDP_Sender implements Runnable, Closeable {

    private final DatagramSocket outSocket;
    private final InetAddress address;
    private final int peerPort;
    private final FileTracker tracker;
    private final File dir;
    private boolean isAlive;
    private final Lock lock;

    protected UDP_Sender(InetAddress address, int peerPort, FileTracker tracker, File dir, boolean isAlive)
              throws SocketException {
        this.outSocket = new DatagramSocket();
        this.address = address;
        this.peerPort = peerPort;
        this.tracker = tracker;
        this.dir = dir;
        this.isAlive = isAlive;
        this.lock = new ReentrantLock();        
    }

    protected UDP_Sender(InetAddress address, int peerPort, FileTracker tracker, File dir) throws SocketException {
        this(address, peerPort, tracker, dir, false);
    }

    @Override
    public void run(){
        try{
            Thread.sleep(1000);
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

    private void sendMetadata() throws InterruptedException{
        List<Packet> toSendMetadata = this.tracker.toSendMetadataList();
        int size = toSendMetadata.size();
        
        if(size == 0)
        try{
            toSendMetadata.add(new Packet(ACK, (short) -1, "abcdef0123456789abcdef0123456789"));
            size++;
        } 
        catch(IllegalPacketException e){}
        
        System.out.println("about to start to send metadata ("+size+" item(s))");
        try{
            int i = 0;
            while(i < size){
                Packet p = toSendMetadata.get(i);
                System.out.println("\t"+p.getMD5Hash());
                if(i==(size-1)) Thread.sleep(2000);
                else Thread.sleep(100);
                try{
                    
                    this.outSocket.send(p.serialize(this.address, this.peerPort));
                }
                catch(IllegalPacketException e){
                    continue;
                }

                if(!this.isAlive)
                    //i++;
                //else
                    this.timeout();
                i++;
            }
        }
        catch(IOException e){}
    }

    private void sendData(){
        List<Packet> toSendData = new LinkedList<>(this.tracker.toSendSet());
        final int size = toSendData.size();
        System.out.println("about to start to send data ("+size+" item(s))");

        try{
            Thread.sleep(2000);
            for(int i = 0; i < size; i++){
                short seqNum = INIT_SEQ_NUMBER;
                Packet p = toSendData.get(0);
                toSendData.remove(0);
                String filename = p.getFilename(), hash = p.getMD5Hash();
                FileChunkReader fcr = new FileChunkReader(filename, this.dir);
                int numOfTries = 0;
                System.out.println("\t"+hash);

                while((!fcr.isFinished()) || (!this.tracker.isEmpty(hash))){
                    this.timeout();
                    short curr = this.tracker.getCurrentSequenceNumber(hash);
                    System.out.println("\t\texpected: "+seqNum+" got: "+curr);

                    if(seqNum == curr)
                        numOfTries = 0;
                    else
                        seqNum = curr;
                    
                    short temp = seqNum;
                    seqNum = this.send(seqNum, hash, fcr);
                    if(seqNum == temp)
                        break;

                    numOfTries++;

                    if(numOfTries == 3){
                        this.interrupt();
                        numOfTries = 0;
                    }
                    else
                        Thread.sleep(ESTIMATED_RTT);   //wait for ACKs
                }
                fcr.close();
                this.tracker.log("<b>"+filename + " foi enviado com sucesso </b>");
            }
        }
        catch(IOException | InterruptedException e){}
    }

    private short send(short seqNum, String hash, FileChunkReader fcr) throws IOException {
        if(!fcr.isFinished()){
            var dp = this.nextDatagramPacket(hash, seqNum, fcr);
            if(dp != null){
                this.outSocket.send(dp);
                this.tracker.log("Pacote enviado com numero de sequencia: "+seqNum);
                System.out.println("sending "+hash+" packet with "+seqNum);
                seqNum++;
            }
        }
        return seqNum;
    }

    private DatagramPacket nextDatagramPacket(String hash, short seqNum, FileChunkReader fcr){
        var p = this.tracker.getCachedPacket(hash, seqNum);

        if(p == null){
            byte[] data = fcr.nextChunk();
            if(data != null){
                System.out.println("\t\tsize is "+data.length);
                p = new Packet(DATA_TRANSFER, seqNum, hash, !fcr.isFinished(), data);
                this.tracker.addToSent(hash, seqNum, p);
            }
            else
                return null;
        }

        DatagramPacket dp = null;
            try{
                dp = p.serialize(this.address, this.peerPort);
            }
            catch(IllegalPacketException e){}
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
        for(System.out.println("zzzz...");!this.isAlive;)
            try{
                Thread.sleep(10);
            }
            catch(InterruptedException e){}
        System.out.println("woke up wwoooooooOOOOO");
    }

    @Override
    public void close() throws IOException {
        this.outSocket.close();
    }
}