import java.net.*;
import java.io.*;

public class Server implements Runnable{

    private static final int BUFFER_SIZE = 1500;

    private final DatagramSocket data_socket;
    private final int port;

    public Server(int port) throws SocketException{
        this.port = port;
        this.data_socket = new DatagramSocket(this.port);
    }

    @Override
    public void run(){
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        
        try{
            while(true){
                this.data_socket.receive(dp);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            this.data_socket.close();
        }        
    }

    public static void listen_tcp(int port) throws IOException{
        ServerSocket server_socket = new ServerSocket(port);

        try{
            System.out.println("listening on port: "+port);

            while(true){
                Socket socket = server_socket.accept();
                Thread t = new Thread(new TCP_Handler(socket));
                t.start();
            }
        }
        finally{
            server_socket.close();
        }

    }
    public static void main(String[] args) throws IOException {
        
        if(args.length >= 1){
            int port = Integer.parseInt(args[0]); //pode ser hard coded?

            Thread udp_listener = new Thread(new Server(port));
            udp_listener.start();

            Server.listen_tcp(port);
        }
       
    }
}