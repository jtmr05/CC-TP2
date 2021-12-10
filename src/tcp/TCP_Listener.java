package tcp;

import java.io.*;
import java.net.*;

public class TCP_Listener implements Runnable, AutoCloseable {
    
    private final int port;
    private final ServerSocket server_socket;

    public TCP_Listener(int port) throws IOException {
        this.port = port;
        this.server_socket = new ServerSocket(port);
    }

    @Override
    public void run(){

        try{
            System.out.println("listening on port: "+port);

            while(true){
                Socket socket = this.server_socket.accept();
                Thread t = new Thread(new TCP_Handler(socket));
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
