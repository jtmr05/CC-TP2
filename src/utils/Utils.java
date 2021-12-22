package utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;

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

    public String readProtocol() throws IOException{
        InputStream is = getClass().getResourceAsStream("ProtocolDescription.txt"); //check if name is right
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String temp;
        StringBuilder sb = new StringBuilder(br.readLine());


        while((temp = br.readLine()) != null)
            sb.append("\r\n").append(temp);

        br.close();
        return sb.toString();
    }

    public String bytesToHexStr(byte[] arr){
        int len = arr.length;

        char[] hexValues = "0123456789abcdef".toCharArray();
        char[] hexCharacter = new char[len * 2];

        for(int i = 0; i < len; i++){
            int aux = arr[i] + Byte.MAX_VALUE;
            int v = aux & 0xFF;
            hexCharacter[i * 2] = hexValues[v >>> 4];
            hexCharacter[i * 2 + 1] = hexValues[v & 0x0F];
        }
        return new String(hexCharacter);
    }

    public byte[] hexStrToBytes(String s){
        byte[] arr = new byte[s.length() / 2];

        for(int i = 0, j = 0; j < arr.length; i += 2, j++)
            arr[j] = (byte) (Short.parseShort(s.substring(i, i+2), 16) - Byte.MAX_VALUE);

        return arr;
    }

    public String getInstant(long t){
        String ldt = Instant.ofEpochMilli(t).
                             atZone(ZoneId.systemDefault()).
                             toLocalDateTime().
                             toString().
                             split("T")[1];
        return ldt;
    }
}