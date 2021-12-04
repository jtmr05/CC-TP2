public class Serializer {
    
    public Packet deserializer(byte[] arr){
        return new Packet();
    }

    public byte[] serializer(Packet p){

        

        return new byte[Packet.MAX_SIZE];
    }
}
