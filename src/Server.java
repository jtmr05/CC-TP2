import java.net.*;
import java.io.*;

public class Server implements Runnable{

    private static final int BUFFER_SIZE = 1500;

    private final DatagramSocket data_socket;
    private final int port;
    private final InetAddress address;
    private final String path;

    public Server(int port, String path, InetAddress address) throws SocketException {
        this.port = port;
        this.address = address;
        this.path = path;
        this.data_socket = new DatagramSocket(this.port);
    }

    @Override
    public void run(){
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket in_packet = new DatagramPacket(buffer, buffer.length);
        
        try{
            while(true){
                this.data_socket.receive(in_packet);
                Thread t = new Thread(new UDP_Handler(in_packet, BUFFER_SIZE));
            } 
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            this.data_socket.close();
        }
    }

    public static void listen_tcp(int port) throws IOException {
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
        
        if(args.length >= 2){
            String path = args[0];
            InetAddress address = InetAddress.getByName(args[1].split(":")[0]);
            int port = Integer.parseInt(args[1].split(":")[1]); //pode ser hard coded?

            Thread udp_listener = new Thread(new Server(port, path, address));
            udp_listener.start();

            Server.listen_tcp(port);
        }
        else{
            System.out.println("Usage: java path IP_address:port");
        }
    }
}