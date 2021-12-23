package tcp;

import java.io.*;
import java.net.Socket;
import java.util.Date;

import udp.FileTracker;
import utils.Utils;

/** A {@linkplain Runnable} target which will handle an HTTP request sent to {@linkplain TCP_Listener}. 
* An instance of this class will send HTML formated text to the underlying {@code Socket}.<p>
* There are three different requests that are supported:
* <li>{@code /} - Displays the structure of the FFSync protocol built over UDP.
* <li>{@code /logs} - Displays the log registry of the FFSync protocol 
at the moment which it is requested.
* <li>{@code /files} - Lists all the current files located 
in the folder which is being synchronized.
* </ul>
*/
public class TCP_Handler implements Runnable {

    /** The socket to send responses to. */
    private final Socket socket;
    /** The directory's path used to retrieve its files. */
    private final File path;
    /** The protocol's {@code FileTracker} instance shared across multiple classes. */
    private final FileTracker tracker;

    public TCP_Handler(Socket socket, File path, FileTracker tracker){
        this.socket = socket;
        this.path = path;
        this.tracker = tracker;
    } 

    @Override
    public void run() {
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));

            Utils u = new Utils();
            String s;
            StringBuilder body = new StringBuilder();
            while((s = in.readLine()) != null && !s.isEmpty()){
                String[] args = s.split(" ");

                switch (args[1]){
                    case "/":

                        String protocolDescription = u.readProtocol();
                        body.append("<html><title>FFSync</title><span>");
                        body.append(protocolDescription);
                        body.append("<span></html>");

                        break;

                    case "/files":

                        body.append("<html><title>Files</title><span>");
                        var listOfFiles = this.path.listFiles();//when size == 0, this would throw an exception
                        int size = listOfFiles.length;
                        for(int i = 0; i < size; i++)
                            body.append(listOfFiles[i] + "<br>");

                        body.append("<span></html>");

                        break;

                    case "/logs":

                        body.append("<html><title>Logs</title><span>");
                        
                        var iter = this.tracker.logsIter();
                        while(iter.hasNext())
                            body.append(iter.next() + "<br>");

                        body.append("<span></html>");

                        break;

                    default:
                        body.append("Page not Found");
                        body.append("<span></html>");
                        break;
                }

                StringBuilder header = new StringBuilder().
                       append("HTTP/1.1 200 OK\n").
                       append("Server: localhost\n").
                       append("Date: ").append(new Date()).append("\n").
                       append("Content-type: text/html\n").
                       append("Content-length: ").append(body.length()).append("\n").
                       append("Connection: closed\n\n\n");
                out.write(header.append(body).toString());
                out.flush();
            }
            this.socket.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}