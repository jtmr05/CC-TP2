package udp;

import java.io.*;
import java.net.*;

import packet.*;
import static packet.Consts.*;

public class UDP_Handler implements Runnable { 
    
    private final DatagramPacket dp;
    private final File dir;
    private final InetAddress address;
    private final MapWrapper fileInfo;

    public UDP_Handler(DatagramPacket dp, File dir, InetAddress address, int port, MapWrapper fileInfo){
        this.dp = dp;
        this.dir = dir;
        this.address = address;
        this.fileInfo = fileInfo;
    }

    @Override
    public void run(){
        
        try {
            Packet p = Packet.deserialize(this.dp);
            
            switch(p.getOpcode()){    
                case FILE_META -> {
                    String key = p.getMD5Hash();
                    this.fileInfo.put(key, p);
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