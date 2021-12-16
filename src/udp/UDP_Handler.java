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

    protected UDP_Handler(DatagramPacket received, File dir, FileTracker tracker){
        this.received = received;
        this.dir = dir;
        this.tracker = tracker;
        this.map = new HashMap<>();
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
                            this.tracker.getLogs().add(p.getFilename()+" was received and saved");
                        }
                        if(fcw != null){
                            this.map.put(key, fcw);
                            int off = p.getSequenceNumber() - INIT_SEQ_NUMBER;
                            fcw.writeChunk(p.getData(), off);
                            this.tracker.getLogs().add(p.getFilename()+" was updated");
                        }
                        
                    }
                    catch(IOException e){}
                }

                case ACK -> 
                    this.tracker.ack(key, p.getSequenceNumber());

                default -> {}
            }
        }
        catch (IllegalOpCodeException e){

        } //ignore any other opcode
    }
}