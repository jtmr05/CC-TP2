package udp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

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

    public byte[] nextChunk() throws AllChunksReadException {
        if(!this.finished){
            var data = new byte[DATA_SIZE];
            int bread = 0;
            try{
                bread = this.reader.read(data, off, data.length);
                this.off += bread;

                if(bread != -1) 
                    Arrays.fill(data, bread, data.length, (byte) 0);
                else
                    data = null;
            }
            catch(IOException e){
                data = null;
                this.finished = true;
            }
            this.finished = bread < data.length;
            return data;
        }
        else
            throw new AllChunksReadException();
    }
}
