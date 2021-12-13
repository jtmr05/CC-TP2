package udp;

import java.util.concurrent.locks.*;

public final class RTT {
    
    private RTT(){}
    
    protected static final int MILLIS_OF_SLEEP = 30000; //30s

    private static Lock lock = new ReentrantLock();
    private static long RTT_SUM = 0;
    private static long COUNT = 0;
    protected static long ESTIMATED_RTT = 20;         //20 ms of initial estimated RTT

    /**
     * Updates this classes' {@code RTT_SUM} and {@code COUNT}, thus calculating an 
     * estimate for round trip time. 
     * @param timestamp The packet's {@code timestamp}
     */
    protected static void add(long timestamp){
        long t = System.currentTimeMillis();
        lock.lock();
        RTT_SUM += (t - timestamp);
        COUNT++;
        ESTIMATED_RTT = (RTT_SUM / COUNT) + 5; //error interval
        lock.unlock();
    }
}
