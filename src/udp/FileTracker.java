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

    private final Map<String, Packet> local;    //what files I have
    private final Map<String, Packet> remote;   //what files the other guy has
    private final Lock localLock;
    private final Lock remoteLock;
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
            catch(InterruptedException e){}
        }
    }

   

    protected FileTracker(File dir){
        this.local = new HashMap<>();
        this.remote = new HashMap<>();
        this.localLock = new ReentrantLock();
        this.remoteLock = new ReentrantLock();
        this.dir = dir;
        (this.t = new Thread(new Monitor())).start();
    }

    private void populate(){

        if(this.dir.isDirectory()){
            File[] files = (File[]) Arrays.stream(this.dir.listFiles()).filter(File::isFile).toArray();
            final int size = files.length;

            try{
                this.localLock.lock();

                this.remoteLock.lock();
                this.remote.clear();
                this.remoteLock.unlock();

                this.local.clear();
                
                for(int i = 0; i < size; i++){
                    var meta = Files.readAttributes(files[i].toPath(), BasicFileAttributes.class);
                    String key = this.hashFileMetadata(files[i], meta);

                    if(!this.local.containsKey(key)){
                        boolean hasNext = i != (size-1);
                        Packet p = new Packet(FILE_META, key, meta.lastModifiedTime().toMillis(), 
                                              meta.creationTime().toMillis(), files[i].getName(), hasNext);   

                        this.local.put(key, p);
                    }
                }
                this.localLock.unlock();
            }
            catch(IOException e){
                this.localLock.unlock();
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
        this.remoteLock.lock();
        boolean b = this.remote.put(key, value) != null;
        this.remoteLock.unlock();
        return b;
    }

    /**
     * Computes the {@link Set} of packets representing the missing files in the peer directory.
     * @return The set of metadata packets
     */
    public Set<Packet> toSendSet(){
        this.localLock.lock();
        var localSet = this.local.entrySet();
        this.localLock.unlock();

        try{
            this.remoteLock.lock();
            return localSet.stream().
                            filter(e -> !this.remote.containsKey(e.getKey())).
                            map(Entry::getValue).
                            collect(Collectors.toSet());
        }
        finally{
            this.remoteLock.unlock();
        }
    }

    public List<Packet> toSendMetadataList(){
        this.localLock.lock();
        var localSet = this.local.entrySet();
        this.localLock.unlock();

        var list = localSet.stream().
                            map(Entry::getValue).
                            collect(Collectors.toList());

        list.sort((p1, p2) -> Boolean.compare(p2.getHasNext(), p1.getHasNext()));
        return list;
    }

    @Override
    public void close(){
        this.t.interrupt();
    }
}