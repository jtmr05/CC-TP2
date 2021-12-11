package udp;

import java.io.*;
import java.net.*;

import packet.*;

import static packet.Consts.*;

/** Handles the received {@linkplain DatagramPacket}. */
public class UDP_Handler implements Runnable { 
    
    private final DatagramPacket received;
    private final File dir;
    private final InetAddress address;
    private final FileTracker tracker;

    protected UDP_Handler(DatagramPacket received, File dir, InetAddress address, int port, FileTracker tracker){
        this.received = received;
        this.dir = dir;
        this.address = address;
        this.tracker = tracker;
    }

    @Override
    public void run(){
        
        try {
            Packet p = Packet.deserialize(this.received);
            
            switch(p.getOpcode()){
                case FILE_META -> {
                    String key = p.getMD5Hash();
                    boolean hasNext = p.getHasNext();
                    this.tracker.putInRemote(key, p);
                    if(!hasNext){
                        this.tracker.toSendSet();
                        //TODO
                    }
                }

                case DATA_TRANSFER -> {}

                case ACK -> {}

                default -> {}
            }
        }
        catch (IllegalOpCodeException e){}
    }
    
    //Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).setTimes(lastModifiedTime, 
    //lastAccessTime, createTime); 
    // O que não queremos alterar pomos null        
}