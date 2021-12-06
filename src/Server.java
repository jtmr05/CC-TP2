import java.net.*;
import java.io.*;

public class Server {

    public static void main(String[] args) throws IOException {
        
        if(args.length >= 2){
            String path = args[0];
            String[] ipAndPort = args[1].split(":");
            InetAddress address = InetAddress.getByName(ipAndPort[0]);
            int port = Integer.parseInt(ipAndPort[1]); 

            Thread tcp_listener = new Thread(new TCP_Listener(port));
            Thread udp_listener = new Thread(new UDP_Listener(port, path, address));

            tcp_listener.start();
            udp_listener.start();
        }
        else
            System.out.println("Usage: java class_name path ip_address:port");
    }
}