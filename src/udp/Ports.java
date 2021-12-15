package udp;

public class Ports{

    private static final int LOWER_BOUND = 40000;
    private static final int UPPER_BOUND = 45000;
    private static int current = LOWER_BOUND;

    public static int get(){
        current++;
        current = (current < UPPER_BOUND) ? current : LOWER_BOUND;
        return current;
    }
}