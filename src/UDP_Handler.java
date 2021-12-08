import java.util.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import packet.*;
import static packet.Consts.*;

public class UDP_Handler implements Runnable { 
    
    private final DatagramPacket in_packet;
    private final String path;
    private final InetAddress address;
    private final Map<String, Packet> fileInfo;

    public UDP_Handler(DatagramPacket dp, String path, InetAddress address, int port) throws IOException {            
        this.in_packet = dp;
        this.path = path;
        this.address = address;
        this.fileInfo = new HashMap<>();
        this.populate();
    }

    @Override
    public void run(){
        
        try {
            Packet p = Packet.deserialize(this.in_packet);
            
            switch(p.getOpcode()){    
                case FILE_META -> {
                    String key = p.getMD5Hash();

                }
            }
            

            
        }
        catch (IllegalOpCodeException e) {}
        
    }

    private void populate() throws IOException{

        File f = new File(this.path);

        if(f.isDirectory()){
            File[] files = (File[]) Arrays.stream(f.listFiles()).filter(x -> !x.isDirectory()).toArray();
            final int size = files.length;

            for(int i = 0; i < size; i++){
                File entry = files[i];
                BasicFileAttributes meta = Files.readAttributes(files[i].toPath(), BasicFileAttributes.class);
                String key = this.hashFileMetadata(entry, meta);
                boolean hasNext = !(i == (size-1));
                Packet p = new Packet(FILE_META, key, meta.lastModifiedTime().toMillis(), meta.creationTime().toMillis(), entry.getName(), hasNext);
                this.fileInfo.put(key, p);
            }
        }
    }

    private String hashFileMetadata(File file, BasicFileAttributes meta) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(file.getName()).append(meta.creationTime());

        try{
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            BigInteger number = new BigInteger(1, hash); 
            sb = new StringBuilder(number.toString(16));
            while (sb.length() < 32)
                sb.insert(0, '0');
            
            return sb.toString();
        }
        catch(NoSuchAlgorithmException e){
            return null;
        }
        
        //Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).setTimes(lastModifiedTime, lastAccessTime, createTime); 
        // O que não queremos alterar pomos null        
    }
}