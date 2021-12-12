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

                var tcp_listener = new TCP_Listener(port);
                var udp_listener = new UDP_Listener(port, address, dir);

                Thread tcp_thread = new Thread(tcp_listener);
                Thread udp_thread = new Thread(udp_listener);
                tcp_thread.start();
                udp_thread.start();
            }
            else
                System.err.println("Not a directory. Please specify a path to a directory.");
        }
        else
            System.err.println("Usage: directory_path port_to_listen_on destination_address");
    }
}