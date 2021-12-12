package utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.util.*;

public class FileChunkWriter implements Closeable {

    private final BufferedOutputStream writer;
    private final Map<Integer, byte[]> map;
    private int offset;
    
    private FileChunkWriter(File f) throws FileNotFoundException {
        this.writer = new BufferedOutputStream(new FileOutputStream(f));
        this.map = new HashMap<>();
        this.offset = 0;
    }

    public static FileChunkWriter factory(String filename, long remoteCreationDate) throws IOException {
        File f = new File(filename);
        FileChunkWriter fcw = null;

        if(f.exists()){
            var meta = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            long localCreationDate = meta.creationTime().toMillis();
            if(remoteCreationDate > localCreationDate)     //most recent wins
                fcw = new FileChunkWriter(f);
        }
        else{
            f.createNewFile();
            Files.getFileAttributeView(f.toPath(), BasicFileAttributeView.class).
                  setTimes(null, null, FileTime.fromMillis(remoteCreationDate)); 
            fcw = new FileChunkWriter(f);
        }
        return fcw;
    }

    public void writeChunk(byte[] data, int off) throws IOException {

        if(this.offset == off){
            this.writer.write(data);
            this.offset += data.length;

            while(this.map.containsKey(this.offset)){
                var tmp = this.map.remove(this.offset);
                this.writer.write(tmp);
                this.offset += tmp.length;
            }
        }
        else
            this.map.put(off, data);   
    }

    @Override
    public void close(){
        try{
            this.writer.flush();
            this.writer.close();
        }
        catch(IOException e){}
    }
}