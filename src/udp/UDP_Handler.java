package udp;

import java.io.*;
import java.net.*;
import java.util.*;

import packet.*;
import utils.*;

import static packet.Consts.*;

/** Handles the received {@linkplain DatagramPacket}. */
public class UDP_Handler implements Runnable {

    private final DatagramPacket received;
    private final File dir;
    private final FileTracker tracker;
    private final Map<String, FileChunkWriter> map;
    private final int localPort;

    protected UDP_Handler(DatagramPacket received, File dir, FileTracker tracker, int localPort){
        this.received = received;
        this.dir = dir;
        this.tracker = tracker;
        this.map = new HashMap<>();
        this.localPort = localPort;
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
                    var fcw = this.map.get(key);
                    try{
                        if(fcw == null){ //nova file
                            String filename = this.tracker.getRemoteFilename(key);
                            String dirPath = this.dir.getAbsolutePath();
                            String filePath = new StringBuilder(dirPath).append("/").append(filename).toString();
                            fcw = FileChunkWriter.factory(filePath, this.tracker.getRemoteCreationTime(key));
                            this.map.put(key, fcw);
                            this.tracker.log(filename + " was received and saved");
                        }

                        short seqNum = p.getSequenceNumber();
                        int off = (seqNum - INIT_SEQ_NUMBER) * DATA_SIZE;
                        fcw.writeChunk(p.getData(), off);
                        this.sendAck(new Packet(ACK, seqNum, key));

                        if(!p.getHasNext() && fcw.isEmpty()){
                            this.map.remove(key);
                            fcw.close();
                        }

                        this.tracker.log(p.getFilename()+" was updated");
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
        var ds = new DatagramSocket(this.localPort);
        ds.send(p.serialize(this.received.getAddress(), this.received.getPort()));
        ds.close();
    }
}