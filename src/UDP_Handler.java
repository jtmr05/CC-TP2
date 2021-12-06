import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class UDP_Handler implements Runnable { 
    
    private final DatagramPacket in_packet;
    private final String path;

    public UDP_Handler(DatagramPacket dp, String path) {            
        this.in_packet = dp;
        this.path = path;
    }

    @Override
    public void run(){
        
        byte[] in = this.in_packet.getData();
        this.parser(in);
    }
   
    private void parser(byte[] buffer){
        byte opcode = buffer[0];
        
        switch(opcode){
            case 1:
        
        }
    }

    private int hashFileMetadata() throws IOException {
        File file = new File(this.path);
        BasicFileAttributes meta = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        int code = Objects.hash(meta.lastModifiedTime(), meta.size(), meta.creationTime());
        
        return code;
    }
}
    


/**
 * +-------+
 * |       | parking lot
 * +-------+
 *
 *
 */