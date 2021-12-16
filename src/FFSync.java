import java.net.*;
import java.io.*;

import tcp.TCP_Listener;
import udp.FileTracker;
import udp.UDP_Listener;

public class FFSync {

    public static void main(String[] args) throws IOException {

        if(args.length >= 3){
            String path = args[0];
            File dir = new File(path);

            if(dir.isDirectory()){
                int localPort = Integer.parseInt(args[1]);
                String[] addressAndPort = args[2].split(":");
                InetAddress address = InetAddress.getByName(addressAndPort[0]);
                int peerPort = Integer.parseInt(addressAndPort[1]);


                FileTracker f = new FileTracker(dir);

                var tcp_listener = new TCP_Listener(localPort, dir, f);
                var udp_listener = new UDP_Listener(localPort, address, peerPort, dir, f);
                Thread tcp_thread = new Thread(tcp_listener);
                Thread udp_thread = new Thread(udp_listener);

                tcp_thread.start();
                udp_thread.start();
            }
            else
                System.err.println("Not a directory. Please specify a path to a directory.");
        }
        else
            System.err.println("Usage: directory_path local_port peer_address:peer_port");
    }
}
/*
public class FFSync {

    public static void main(String[] args) throws IOException {

        if(args.length >= 2){
            String path = args[0];
            File dir = new File(path);

            if(dir.isDirectory()){
                int port = Integer.parseInt(args[1]);

                var tcp_listener = new TCP_Listener(port,path);

                Thread tcp_thread = new Thread(tcp_listener);

                tcp_thread.start();
            }
            else
                System.err.println("Not a directory. Please specify a path to a directory.");
        }
        else
            System.err.println("Usage: directory_path port_to_listen_on destination_address");
    }
}*/