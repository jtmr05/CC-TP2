package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static packet.Consts.*;

public class FileChunkReader {
    
    private final BufferedInputStream reader;
    private int off;
    private boolean finished;

    public FileChunkReader(String filename) throws FileNotFoundException {
        var fileInStream = new FileInputStream(new File(filename));
        this.reader = new BufferedInputStream(fileInStream);
        this.off = 0;
        this.finished = false;
    }

    public boolean isFinished(){
        return this.finished;
    }

    public byte[] nextChunk() throws AllChunksReadException {
        if(!this.isFinished()){
            byte[] data, buffer = new byte[DATA_SIZE];
            try{
                final int bread = this.reader.read(buffer, off, buffer.length);
                
                if(bread != -1){
                    this.off += bread;
                    data = new byte[bread];
                    System.arraycopy(buffer, 0, data, 0, bread); 
                }
                else
                    data = null;
                
                this.finished = bread < buffer.length;
            }
            catch(IOException e){
                data = null;
                this.finished = true;
            }
            return data;
        }
        else
            throw new AllChunksReadException();
    }

    public byte[] indexedChunk(short sequenceNumber){
        int off__ = (sequenceNumber - 1) * DATA_SIZE;
        byte[] data, buffer = new byte[DATA_SIZE];
        try{
            final int bread = this.reader.read(buffer, off__, buffer.length);

            if(bread != -1){
                data = new byte[bread];
                System.arraycopy(buffer, 0, data, 0, bread);
            }
            else
                data = null;
            
        }
        catch(IOException e){
            data = null;
        }
        return data;
    }
}
