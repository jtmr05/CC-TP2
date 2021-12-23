package tcp;

import java.io.*;
import java.net.*;

import udp.FileTracker;

/** A class to listen for HTTP requests. Upon receiving such a request, a new {@code Thread} with a 
* {@linkplain Runnable} target {@code TCP_Handler} is started.
 */
public class TCP_Listener implements Runnable, Closeable {

    /** The socket to listen for requests. */
    private final ServerSocket server_socket;
    /** The directory's path used to instantiate {@code TCP_Handler}. */
    private final File path;
    /**The protocol's {@code FileTracker} instance shared across multiple classes 
    * used to instantiate {@code TCP_Handler}. */
    private final FileTracker tracker;

    public TCP_Listener(int port, File path, FileTracker tracker) throws IOException {
        this.server_socket = new ServerSocket(port);
        this.path = path;
        this.tracker = tracker;
    }

    @Override
    public void run(){

        try{
            while(true){
                Socket socket = this.server_socket.accept();
                Thread t = new Thread(new TCP_Handler(socket, path, tracker));
                t.start();
            }
        }
        catch(IOException e){}
        finally{
            this.close();
        }
    }

    @Override
    public void close(){
        try{
            this.server_socket.close();
        }
        catch(IOException e){}
    }
}