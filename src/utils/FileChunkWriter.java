package utils;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileChunkWriter implements Closeable {

    private final BufferedOutputStream writer;
    private final Map<Integer, byte[]> map;
    private int offset;
    private final Lock lock;
    private final long begin;

    private FileChunkWriter(File f) throws FileNotFoundException {
        this.writer = new BufferedOutputStream(new FileOutputStream(f));
        this.map = new HashMap<>();
        this.offset = 0;
        this.lock = new ReentrantLock();
        this.begin = System.currentTimeMillis();
    }

    public static FileChunkWriter factory(String filename) throws IOException {
        File f = new File(filename);

        if(!f.exists())
            f.createNewFile();

        return new FileChunkWriter(f);
    }

    public void writeChunk(byte[] data, int off) throws IOException {

        this.lock.lock();
        if(this.offset == off){
            this.writer.write(data);
            this.offset += data.length;

            while(this.map.containsKey(this.offset)){
                var tmp = this.map.remove(this.offset);
                this.writer.write(tmp);
                this.offset += tmp.length;
            }
            this.writer.flush();
        }
        else
            this.map.put(off, data);
        
        this.lock.unlock();
    }

    public boolean isEmpty(){
        return this.map.isEmpty();
    }

    public double getTransferTime(){
        return (double) (System.currentTimeMillis() - this.begin) / 1000; //seconds
    }

    public double getDebit(){
        double time = this.getTransferTime();
        return (double) this.offset / time;
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