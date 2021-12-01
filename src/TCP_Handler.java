import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class TCP_Handler implements Runnable {

    private final Socket socket;

    public TCP_Handler(Socket s){
        this.socket = s;
    }

    @Override
    public void run() {
        try{
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            //fazer cenas aqui ig

        }
        catch(IOException e){
            e.printStackTrace();
        }
        
    }
    
}
