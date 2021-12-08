import java.io.*;
import java.net.Socket;
import java.util.Date;

public class TCP_Handler implements Runnable {

    private final Socket socket;

    public TCP_Handler(Socket s){
        this.socket = s;
    }

    @Override
    public void run() {
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));

            
            String body = "<html><title>It's just a string</title></html>"; 
            StringBuilder header = new StringBuilder(); 
            header.append("HTTP/1.1 200 OK\n").
                   append("Server: localhost\n").
                   append("Date: ").append(new Date()).append("\n").
                   append("Content-type: text/html\n").
                   append("Content-length: ").append(body.length()).append("\n").
                   append("Connection: closed\n\n\n");
            
            String s;
            while((s = in.readLine()) != null && !s.isEmpty()){
                System.out.println(s);
            }
            out.write(header.append(body).toString());
            out.flush();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
}
