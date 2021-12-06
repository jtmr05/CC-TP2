public class Serializer {
    
    public Packet deserializer(byte[] arr){
        return new Packet();
    }

    public byte[] serializer(Packet p){

        

        return new byte[Constants.MAX_CHUNK_SIZE];
    }
}
