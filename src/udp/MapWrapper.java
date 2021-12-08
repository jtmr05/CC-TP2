package udp;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.*;

import static packet.Consts.*;
import packet.*;


public class MapWrapper implements Closeable {

    private final Lock lock;
    private final Map<String, Packet> map;
    private final File dir;
    private final Thread t;

    /** monitor the directory for any changes */
    private class Monitor implements Runnable {

        private Monitor(){}

        @Override
        public void run() {
            
            try{
                int stride = 10;
                while(!Thread.interrupted()){
                    MapWrapper.this.populate();
                    for(int i = 0; i < 60000 && !Thread.interrupted(); i += stride) //dormir 1 min
                        Thread.sleep(stride);
                }
            }
            catch(IOException | InterruptedException e){}
        }
    }
    
    protected MapWrapper(File dir){
        this.lock = new ReentrantLock();
        this.map = new HashMap<>();
        this.dir = dir;
        this.t = new Thread(new Monitor()); 
        this.t.start();
    }

    protected Packet put(String key, Packet value) {
        this.lock.lock();
        Packet p = this.map.put(key, value);
        this.lock.unlock();
        return p;
    }

    protected boolean containsKey(Object key) {
        this.lock.lock();
        boolean b = this.map.containsKey(key);
        this.lock.unlock();
        return b;
    }

    protected Packet get(String key){
        this.lock.lock();
        Packet p = this.map.get(key);
        this.lock.unlock();
        return p;
    }

    protected Set<Entry<String, Packet>> entrySet(){
        this.lock.lock();
        var s = this.map.entrySet();
        this.lock.unlock();
        return s;
    }

    private void populate() throws IOException {

        if(this.dir.isDirectory()){
            File[] files = (File[]) Arrays.stream(this.dir.listFiles()).filter(File::isFile).toArray();
            int size = files.length;

            for(int i = 0; i < size; i++){
                BasicFileAttributes meta = Files.readAttributes(files[i].toPath(), BasicFileAttributes.class);
                String key = this.hashFileMetadata(files[i], meta);
                boolean hasNext = i != (size-1);
                Packet p = new Packet(FILE_META, key, meta.lastModifiedTime().toMillis(), 
                                      meta.creationTime().toMillis(), files[i].getName(), hasNext);
                this.put(key, p);
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
            sb.setLength(0); 
            sb.append(number.toString(16));
            while (sb.length() < 32)
                sb.insert(0, '0');
            
            return sb.toString();
        }
        catch(NoSuchAlgorithmException e){
            return null;
        }
    }

    @Override
    public void close(){
        this.t.interrupt();
    }
}
