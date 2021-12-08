import java.net.*;
import java.io.*;

import tcp.TCP_Listener;
import udp.UDP_Listener;

public class FFSync {

    public static void main(String[] args) throws IOException {
        
        if(args.length >= 3){
            String path = args[0];
            File dir = new File(path);

            if(dir.isDirectory()){
                int port = Integer.parseInt(args[1]); 
                InetAddress address = InetAddress.getByName(args[2]);

                Thread tcp_listener = new Thread(new TCP_Listener(port));
                Thread udp_listener = new Thread(new UDP_Listener(port, dir, address));

                tcp_listener.start();
                udp_listener.start();
            }
            else
                System.err.println("Path is a regular file.");
        }
        else
            System.err.println("Usage: directory_path port_to_listen_on destination_address");
    }
}