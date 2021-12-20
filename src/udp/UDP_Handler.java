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
                    var fcw = this.tracker.getChunkWriter(key);
                    try{
                        short seqNum = p.getSequenceNumber();
                        if(fcw == null){ //nova file
                            System.out.println("\tDENTRO DO IF: "+seqNum);
                            System.out.println("-\t>>>>>>>>>>>>>>>>>>"+key);
                            String filename = this.tracker.getRemoteFilename(key);
                            String dirPath = this.dir.getAbsolutePath();
                            String filePath = new StringBuilder(dirPath).append("/").append(filename).toString();
                            fcw = FileChunkWriter.factory(filePath);
                            this.tracker.putChunkWriter(key, fcw);
                            this.tracker.log(filename + " was received and saved");
                        }

                        System.out.println("---->"+seqNum+"\t"+Thread.currentThread().getName());
                        int off = (seqNum - INIT_SEQ_NUMBER) * DATA_SIZE;
                        
                        fcw.writeChunk(p.getData(), off);
                        this.sendAck(new Packet(ACK, seqNum, key));

                        if(!p.getHasNext() && fcw.isEmpty())
                            this.tracker.removeChunkWriter(key);

                        this.tracker.log(p.getFilename() + " was updated");
                    }
                    catch(IOException e){}
                }

                case ACK -> {
                    RTT.add(p.getTimestamp());
                    this.tracker.ack(key, p.getSequenceNumber());
                }

                default -> {}
            }
        }
        catch (IllegalOpCodeException e){} //ignore any other opcode
    }


    private void sendAck(Packet p) throws IOException, IllegalOpCodeException {
        var ds = new DatagramSocket();
        ds.send(p.serialize(this.address, this.peerPort));
        System.out.println("sending ack");
        ds.close();
    }
}