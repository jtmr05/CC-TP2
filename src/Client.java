import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException{

        if(args.length >= 2){
            String serverIP = args[0];             // IP do Servidor
            int port = Integer.parseInt(args[1]);  // Port do Servidor
            System.out.println("Connecting to " + serverIP + ":" + port);
        }



    }
}