package tcp;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Date;

import packet.Packet;
import udp.FileTracker;
import utils.Utils;



public class TCP_Handler implements Runnable {

    private final Socket socket;
    private final File path;
    private final FileTracker f;

    public TCP_Handler(Socket s,File path,FileTracker f){
        this.socket = s;
        this.path = path;
        this.f=f;
    }

    @Override
    public void run() {
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            //GET /files HTTP/1.1
            //GET / HTTP/1.1
             
            Utils u = new Utils();
            String s;
            StringBuilder body = new StringBuilder();
            while((s = in.readLine()) != null && !s.isEmpty()){
                String[] argumentos = s.split(" ");
                System.out.println("\n"+"\n"+argumentos.toString()+"\n"+"\n");
                switch (argumentos[1]) {
                    case "/":
                        
                        String protocolDescription = u.readProtocol();///home/diogobarbosa/3º ano/CC/CC-TP2/src/utils/ProtocolDescription.txt
                        
                        body.append("<html><title>FFSync</title><span>");
                        body.append(protocolDescription);
                        body.append("<span></html>");
                        
                        break;
                    
                    case "/files"://falta ver se é diretoria em vez de file/função auxiliar/usar o tipo file
                    

                        body.append("<html><title>Files</title><span>");
                        for(File file : path.listFiles()){
                            body.append(file+"<br>");
                        }
                        body.append("<span></html>");

                        break;
                    case "/logs":
                        
                        body.append("<html><title>Logs</title><span>");
                        for(String log:this.f.getLogs()){
                            body.append(log);
                        }
                        body.append("<span></html>");

                        break;

                    case "/missing":
                        
                        body.append("<html><title>Missing</title><span>");
                        for(Packet p : this.f.toSendSet()){
                            body.append(p.getFilename()+"<br>");
                        }
                        body.append("<span></html>");
                        
                        break;    

                    default:
                        body.append("Page not Found");
                        break;
                }

                StringBuilder header = new StringBuilder(); 
                    header.append("HTTP/1.1 200 OK\n").
                    append("Server: localhost\n").
                    append("Date: ").append(new Date()).append("\n").
                    append("Content-type: text/html\n").
                    append("Content-length: ").append(body.length()).append("\n").
                    append("Connection: closed\n\n\n");
                out.write(header.append(body).toString());
                out.flush();
                System.out.println(s);
                       
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
}
