package packet;

/** This {@code Exception} is thrown if a {@code Packet} is instantiated with an unknown opcode,
 * or if the calculated HMAC differs from the expected. */
public class IllegalPacketException extends Exception {
    
    public IllegalPacketException(){
        super();
    }

    public IllegalPacketException(String msg){
        super(msg);
    }
}
