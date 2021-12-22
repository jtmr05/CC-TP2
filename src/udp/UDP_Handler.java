package udp;

import java.io.*;
import java.net.*;

import packet.*;
import utils.*;

import static packet.Consts.*;

/** Handles the received {@linkplain DatagramPacket}. */
public class UDP_Handler implements Runnable {

    private final DatagramPacket received;
    private final File dir;
    private final FileTracker tracker;
    private final int peerPort;
    private final InetAddress address;

    protected UDP_Handler(DatagramPacket received, File dir, FileTracker tracker, int peerPort, InetAddress address){
        this.received = received;
        this.dir = dir;
        this.tracker = tracker;
        this.peerPort = peerPort;
        this.address = address;
    }

    @Override
    public void run(){

        try{
            Packet p = Packet.deserialize(this.received);
            String key = p.getMD5Hash();

            switch(p.getOpcode()){

                case FILE_META ->
                    this.tracker.putInRemote(key, p);
                    
                case DATA_TRANSFER -> {
                    this.tracker.log("packet received with sequence number "+p.getSequenceNumber());
                    var fcw = this.tracker.getChunkWriter(key);
                    try{
                        short seqNum = p.getSequenceNumber();
                        if(fcw == null){ //nova file
                            //System.out.println("\tDENTRO DO IF: "+seqNum);
                            //System.out.println("-\t>>>>>>>>>>>>>>>>>>"+key);
                            String filename = this.tracker.getRemoteFilename(key);
                            if(filename==null) break;
                            String dirPath = this.dir.getAbsolutePath();
                            String filePath = new StringBuilder(dirPath).append("/").append(filename).toString();
                            fcw = FileChunkWriter.factory(filePath);
                            this.tracker.putChunkWriter(key, fcw);
                            
                        }

                        System.out.println("---->"+seqNum+"\t"+Thread.currentThread().getName());
                        int off = (seqNum - INIT_SEQ_NUMBER) * DATA_SIZE;
                        
                        fcw.writeChunk(p.getData(), off);
                        Packet ack = new Packet(ACK, seqNum, key);
                        this.sendAck(ack);

                        if(!p.getHasNext() && fcw.isEmpty()){
                            this.tracker.removeChunkWriter(key);
                            this.tracker.log("<b>"+this.tracker.getRemoteFilename(key) + " was received and saved </b>");
                        }
                    }
                    catch(IOException e){}
                }

                case ACK -> {
                    long timestamp = p.getTimestamp(); 
                    RTT.add(timestamp);
                    this.tracker.ack(key, p.getSequenceNumber(), timestamp);
                }

                default -> {}
            }
        }
        catch (IllegalPacketException e){
            System.out.println("\t\tEXECECAO MANERAAA");
        } //ignore any other opcode
 
    }


    private void sendAck(Packet p) throws IOException, IllegalPacketException {
        var ds = new DatagramSocket();
        ds.send(p.serialize(this.address, this.peerPort));
        Utils u = new Utils();
        String ldt = u.getInstant(p.getTimestamp());
        this.tracker.log(ldt + " -> sending ack " + p.getSequenceNumber());
        System.out.println("sending ack");
        ds.close();
    }
}