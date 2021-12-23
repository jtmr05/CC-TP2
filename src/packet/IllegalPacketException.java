package packet;

public class IllegalPacketException extends Exception {
    
    public IllegalPacketException(){
        super();
    }

    public IllegalPacketException(String msg){
        super(msg);
    }
}
