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
import java.util.stream.Collectors;

import static packet.Consts.*;
import packet.*;


public class FileTracker implements Closeable {

    private final MapWrapper local;    //what files I have
    private final MapWrapper remote;   //what files the other guy has
    private final File dir;
    private final Thread t;

    /** monitor the directory for any changes */
    private class Monitor implements Runnable {

        private Monitor(){}

        @Override
        public void run() {
            
            try{
                final int stride = 10, millis = SECONDS_OF_SLEEP * 1000;
                int i = 0;
                while(!Thread.interrupted()){
                    if(i == 0)
                        FileTracker.this.populate();
                    i += stride;
                    if(i >= millis)
                        i = 0;
                    Thread.sleep(stride);
                }
            }
            catch(IOException | InterruptedException e){}
        }
    }

    private class MapWrapper {

        private final Lock lock;
        private final Map<String, Packet> map;

        private MapWrapper(){
            this.lock = new ReentrantLock();
            this.map = new HashMap<>();
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
    
        protected Packet remove(Object key){
            this.lock.lock();
            Packet p = this.map.remove(key);
            this.lock.unlock();
            return p;
        }
    
        protected Set<Entry<String, Packet>> entrySet(){
            this.lock.lock();
            var s = this.map.entrySet();
            this.lock.unlock();
            return s;
        }
    }

    protected FileTracker(File dir){
        this.local = new MapWrapper();
        this.remote = new MapWrapper();
        this.dir = dir;
        (this.t = new Thread(new Monitor())).start(); 
    }

    private void populate() throws IOException {

        if(this.dir.isDirectory()){
            File[] files = (File[]) Arrays.stream(this.dir.listFiles()).filter(File::isFile).toArray();
            int size = files.length;

            for(int i = 0; i < size; i++){
                BasicFileAttributes meta = Files.readAttributes(files[i].toPath(), BasicFileAttributes.class);
                String key = this.hashFileMetadata(files[i], meta);
                
                if(!this.local.containsKey(key)){
                    boolean hasNext = i != (size-1);
                    Packet p = new Packet(FILE_META, key, meta.lastModifiedTime().toMillis(), 
                                          meta.creationTime().toMillis(), files[i].getName(), hasNext);    
                    this.local.put(key, p);
                }
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

    public boolean putInRemote(String key, Packet value){
        return this.remote.put(key, value) != null;
    }

    /**
     * Computes the {@link Set} of identifiers representing the missing files in the peer directory.
     * @return The set of identifiers
     */
    public Set<String> toSendSet(){
        var localIter = this.local.entrySet();  //to guarantee concurrency
        return localIter.stream().
                         map(Entry::getKey).
                         filter(k -> !this.remote.containsKey(k)).
                         collect(Collectors.toSet());
    }

    @Override
    public void close(){
        this.t.interrupt();
    }
}
