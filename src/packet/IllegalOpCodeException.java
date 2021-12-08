package packet;

public class IllegalOpCodeException extends Exception {
    
    public IllegalOpCodeException(){
        super();
    }

    public IllegalOpCodeException(String msg){
        super(msg);
    }
}
