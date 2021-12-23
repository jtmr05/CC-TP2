package utils;

import java.io.*;

import static packet.Consts.*;

public class FileChunkReader implements Closeable {

    private final BufferedInputStream reader;
    private boolean finished;

    public FileChunkReader(String filename, File dir) throws FileNotFoundException {
        String path = dir.getAbsolutePath()+"/"+filename;
        this.reader = new BufferedInputStream(new FileInputStream(new File(path)));
        this.finished = false;
    }

    public boolean isFinished(){
        return this.finished;
    }

    public byte[] nextChunk() throws AllChunksReadException {
        if(!this.isFinished()){
            byte[] data, buffer = new byte[DATA_SIZE];
            try{
                final int bread = this.reader.read(buffer);

                if(bread != -1){
                    if(bread < DATA_SIZE){
                        data = new byte[bread];
                        System.arraycopy(buffer, 0, data, 0, bread);
                    }
                    else
                        data = buffer;
                }
                else
                    data = null;

                this.finished = bread < DATA_SIZE;
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

    @Override
    public void close(){
        try{
            this.reader.close();
        }
        catch(IOException e){}
    }
}