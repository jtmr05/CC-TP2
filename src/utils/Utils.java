package utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {

    public Utils(){}

    private static final String PROTOCOL_PATH = "utils/ProtocolDescription.txt";

    public short bytesToShort(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getShort();
    }

    public long bytesToLong(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    public int bytesToInt(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getInt();
    }

    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public byte[] shortToBytes(short x) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(x);
        return buffer.array();
    }

    public byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }

    public String readProtocol() throws IOException {
        return Files.readString(Paths.get(PROTOCOL_PATH), UTF_8);
    }

    /*
    public String readProtocol() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(PROTOCOL_PATH, UTF_8));
        String temp;
        StringBuilder sb = new StringBuilder();

        while((temp = br.readLine()) != null)
            sb.append(temp).append("\n");

        br.close();
        return sb.toString();
    }
    */
}