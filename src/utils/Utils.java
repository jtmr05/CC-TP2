package utils;

import java.nio.ByteBuffer;

public class Utils {

    public Utils(){}
    
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
}