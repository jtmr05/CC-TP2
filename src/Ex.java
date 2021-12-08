import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Ex {
    
    public static void main(String[] args) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("md5");

        byte[] hash = md.digest("pila".getBytes(StandardCharsets.UTF_8));

        BigInteger number = new BigInteger(1, hash); 
        StringBuilder hexString = new StringBuilder(number.toString(16)); 
        while (hexString.length() < 32){ 
            hexString.insert(0, '0'); 
        } 
  
        System.out.println(hexString.toString()); 
    }
}
