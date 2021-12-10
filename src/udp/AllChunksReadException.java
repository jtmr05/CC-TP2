package udp;

public class AllChunksReadException extends Exception {

    public AllChunksReadException(){
        super();
    }

    public AllChunksReadException(String msg){
        super(msg);
    }
}
