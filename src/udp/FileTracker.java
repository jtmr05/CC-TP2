package udp;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static packet.Consts.*;
import static udp.RTT.*;
import packet.*;
import utils.FileChunkWriter;
import utils.Utils;

/** This class is shared across multiple other project classes. It keeps track of regular files
* in the local and peer directories, sent chunks of data (and which of those were acknowledged), 
* received chunks and logs used for the HTTP requests.
*/
public class FileTracker {

    /** The mapping of the local directory's contents represented as a {@code FILE_META} {@code Packet}. */
    private final Map<String, Packet> local;
    /** The mapping of the peer directory's contents represented as a {@code FILE_META} {@code Packet}. */
    private final Map<String, Packet> remote;
    /** The mapping of {@linkplain AckTracker}s to keep track of what chunks were sent and acknowledged.*/
    private final Map<String, AckTracker> acks;
    /** The mapping of {@linkplain FileChunkWriter}s to write received
    chunks to according to the {@code Packet}'s md5hash.*/
    private final Map<String, FileChunkWriter> receivedChunks;
    /** The registry logs used for HTTP responses. */
    private final List<String> logs;

    /** The {@code Lock} associated to {@linkplain FileTracker#local}.  */
    private final Lock localLock;
    /** The {@code Lock} associated to {@linkplain FileTracker#remote}.  */
    private final Lock remoteLock;
    /** The {@code Lock} associated to {@linkplain FileTracker#acks}.  */
    private final Lock ackLock;
    /** The {@code Lock} associated to {@linkplain FileTracker#receivedChunks}.  */
    private final Lock chunkLock;

    private final Lock logLock;

    /** The {@code Condition} associated to {@linkplain FileTracker#remoteLock}. 
    * A thread will suspend its execution upon this object when there is still metadata to be 
    * sent by the peer directory (as according to {@linkplain FileTracker#hasNext}).*/
    private final Condition remoteCond;
    /** The {@code Condition} associated to {@linkplain FileTracker#ackLock}. 
    * A thread will suspend its execution upon this object when there are still to be received. */
    private final Condition ackCond;

    /** Indicates if there is still metadata to be received. */
    private boolean hasNext;

    /** The directory {@code File} object. */
    private final File dir;

    /** {@linkplain Runnable} target that will monitor a directory for its contents, invoking the outter object's
     * {@linkplain FileTracker#populate} method.
     * The folder is scanned for contents each {@linkplain RTT#MILLIS_OF_SLEEP}.
     * 
     */
    private class Monitor implements Runnable {

        private Monitor(){}

        @Override
        public void run() {
            try{
                final int stride = 10, millis = MILLIS_OF_SLEEP;
                int i = 0;
                while(!Thread.interrupted()){
                    if(i == 0){
                        System.out.println("Populating...");
                        FileTracker.this.populate();
                    }
                    i += stride;
                    if(i >= millis)
                        i = 0;
                    Thread.sleep(stride);
                }
            }
            catch(InterruptedException e){}
        }
    }

    /** Maps the sequence numbers to the respective packets and indicates the next expected sequence number
    * to be acknowledged. */
    private class AckTracker {

        /** The mapping of sequence numbers to sent packets. */
        private final Map<Short, Packet> sent;
        /** Next expected sequence number. */
        private short currentSequenceNumber;

        protected AckTracker(){
            this.sent = new HashMap<>();
            this.currentSequenceNumber = INIT_SEQ_NUMBER;
        }
    }

    /**
     * Acknowledge a packet from this object's mapping of {@code AckTracker}s. Its {@code currentSequenceNumber}
     * is updated accordingly.
     *  @param id
            The file's unique identifier
        @param seqNum
            The sequence number to be acknowledged.
        @param timestamp
            The {@code Packet}'s timestamp used for logging purposes.
     */
    protected void ack(String id, short seqNum, long timestamp){
        this.ackLock.lock();
        AckTracker at = this.acks.get(id);
        Utils u = new Utils();
        String ldt = u.getInstant(timestamp);
        this.ackCond.signalAll();

        this.log(ldt + " -> recebendo ack " + seqNum);

        if(seqNum == at.currentSequenceNumber){
            at.sent.remove(seqNum);
            at.currentSequenceNumber++;
        }

        this.ackLock.unlock();
    }

     /**
     * Add a packet to this object's mapping of {@code AckTracker}s. Its {@code currentSequenceNumber}
     * is updated accordingly.
     *  @param id
            The file's unique identifier
        @param seqNum
            The sequence number to be acknowledged.
        @param timestamp
            The {@code Packet}'s timestamp used for logging purposes.
     */
    protected void addToSent(String id, short seqNum, Packet p){
        this.ackLock.lock();
        AckTracker at = this.acks.get(id);
        at.sent.put(seqNum, p);
        this.ackLock.unlock();
    }

    protected short getCurrentSequenceNumber(String id){
        this.ackLock.lock();
        AckTracker at = this.acks.get(id);
        short s = at.currentSequenceNumber;
        this.ackLock.unlock();
        return s;
    }

    protected boolean isEmpty(String id){
        this.ackLock.lock();
        AckTracker at = this.acks.get(id);
        boolean b = at.sent.isEmpty();
        this.ackLock.unlock();
        return b;
    }

    protected Packet getCachedPacket(String id, short seqNum){
        this.ackLock.lock();
        AckTracker at = this.acks.get(id);
        Packet p = at.sent.get(seqNum);
        this.ackLock.unlock();
        return p;
    }

    public FileTracker(File dir){
        this.local = new HashMap<>();
        this.remote = new HashMap<>();
        this.acks = new HashMap<>();
        this.receivedChunks = new HashMap<>();

        this.localLock = new ReentrantLock();
        this.remoteLock = new ReentrantLock();
        this.remoteCond = remoteLock.newCondition();
        this.ackLock = new ReentrantLock();
        this.chunkLock = new ReentrantLock();
        this.ackCond = this.ackLock.newCondition();
        this.logLock = new ReentrantLock();
        this.hasNext = false;

        this.logs = new ArrayList<>();

        this.dir = dir;
        new Thread(new Monitor()).start();
    }

    protected FileChunkWriter getChunkWriter(String id){
        this.chunkLock.lock();
        var fcw = this.receivedChunks.get(id);
        this.chunkLock.unlock();
        return fcw;
    }

    protected void putChunkWriter(String id, FileChunkWriter fcw){
        this.chunkLock.lock();
        this.receivedChunks.putIfAbsent(id, fcw);
        this.chunkLock.unlock();
    }

    protected FileChunkWriter removeChunkWriter(String id){
        this.chunkLock.lock();
        var fcw = this.receivedChunks.remove(id);
        if(fcw != null)
            fcw.close();
        this.chunkLock.unlock();
        return fcw;
    }

    public void log(String msg){
        this.logLock.lock();
        this.logs.add(msg);
        this.logLock.unlock();
    }

    public Iterator<String> logsIter(){
        this.logLock.lock();
        List<String> l = new ArrayList<>(this.logs);
        this.logLock.unlock();
        return l.iterator();
    }

    public String getRemoteFilename(String id){
        this.remoteLock.lock();
        Packet p = this.remote.get(id);
        String filename;
        if(p!=null)
            filename = p.getFilename();
        else filename = null;
        this.remoteLock.unlock();
        return filename;
    }

    private void populate(){

        if(this.dir.isDirectory()){
            List<File> files = Arrays.stream(this.dir.listFiles()).
                                      filter(File::isFile).
                                      collect(Collectors.toList());
            final int size = files.size();


            try{
                this.localLock.lock();
                this.local.clear();        //empty the map before adding new info

                for(int i = 0; i < size; i++){
                    File f = files.get(i);
                    var meta = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                    String key = this.hashFileMetadata(f, meta);

                    if(!this.local.containsKey(key)){
                        boolean hasNext = i != (size-1);
                        Packet p = new Packet(FILE_META, key, f.getName(), hasNext);

                        this.local.put(key, p);
                    }
                }
                this.localLock.unlock();
            }
            catch(IOException | IllegalPacketException e){
                this.localLock.unlock();
            }
        }
    }

    private String hashFileMetadata(File file, BasicFileAttributes meta) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(file.getName());

        try{
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] hash = md.digest(sb.toString().getBytes(UTF_8));

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

    protected boolean putInRemote(String key, Packet value){
        this.remoteLock.lock();

        //if(!this.hasNext)
        //    this.remote.clear();        //clear the map when a new batch of Packets arrives

        this.hasNext = value.getHasNext();

        if(!this.hasNext)
            this.remoteCond.signalAll();

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
        this.remoteLock.lock();
        this.localLock.unlock();

        try{

            while(this.hasNext)
                this.remoteCond.await();


            var ret = localSet.stream().
                               filter(e -> !this.remote.containsKey(e.getKey())).
                               map(Entry::getValue).
                               collect(Collectors.toCollection(HashSet::new));

            this.ackLock.lock();
            this.remoteLock.unlock();

            while(this.acks.values().stream().anyMatch(at -> !at.sent.isEmpty()))
                this.ackCond.await();

            this.acks.clear();

            ret.forEach(p -> this.acks.put(p.getMD5Hash(), new AckTracker()));
            this.ackLock.unlock();
            return ret;
        }
        catch(InterruptedException e){
            return null;
        }
    }

    protected List<Packet> toSendMetadataList(){
        this.localLock.lock();
        var localSet = this.local.entrySet();
        this.remoteLock.lock();
        this.localLock.unlock();

        var list = localSet.stream().
                            map(Entry::getValue).
                            collect(Collectors.toList());

        this.remoteLock.unlock();
        list.sort((p1, p2) -> Boolean.compare(p2.getHasNext(), p1.getHasNext())); //false comes last
        return list;
    }
}