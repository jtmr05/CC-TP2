package udp;

import java.io.*;
import java.net.*;

import packet.*;

import static packet.Consts.*;

public class UDP_Handler implements Runnable { 
    
    private final DatagramPacket dp;
    private final File dir;
    private final InetAddress address;
    private final FileTracker tracker;

    public UDP_Handler(DatagramPacket dp, File dir, InetAddress address, int port, FileTracker tracker){
        this.dp = dp;
        this.dir = dir;
        this.address = address;
        this.tracker = tracker;
    }

    @Override
    public void run(){
        
        try {
            Packet p = Packet.deserialize(this.dp); //received
            
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