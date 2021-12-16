package tcp;

import java.io.*;
import java.net.*;

import udp.FileTracker;

public class TCP_Listener implements Runnable, AutoCloseable {
    
    private final int port;
    private final ServerSocket server_socket;
    private final File path;
    private final FileTracker f;

    public TCP_Listener(int port,File path,FileTracker f) throws IOException {
        this.port = port;
        this.server_socket = new ServerSocket(port);
        this.path = path;
        this.f=f;
    }

    @Override
    public void run(){

        try{
            System.out.println("listening on port: "+port);

            while(true){
                Socket socket = this.server_socket.accept();
                Thread t = new Thread(new TCP_Handler(socket,path,f));
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

    //public static void main(String[] args) throws IOException {
    //    TCP_Listener listener = new TCP_Listener(8080);
    //    Socket socket = listener.server_socket.accept();
    //    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    //    System.out.println(in.readLine());
//
    //    
    //}
}
