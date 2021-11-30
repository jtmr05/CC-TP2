import java.net.*;
import java.io.*;

public class Server {

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        try{
            ServerSocket server = new ServerSocket(port);
            System.out.println("listening on port: "+port);
            Socket socket = server.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String received = in.readUTF().toUpperCase();
            out.writeUTF(received);
            socket.close();
            server.close();

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}