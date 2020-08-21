import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张丰博
 */
public class Test {
    public static void main(String[] args) {
        Map<Integer,Integer> map = new ConcurrentHashMap<>();

        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 2);
        map.put(4, 2);
        map.put(5, 2);
        map.put(6, 2);
        map.put(7, 2);
        map.put(8, 2);
        map.put(9, 2);
        map.put(10, 2);
        map.put(11, 2);
        map.put(12, 2);
        map.put(13, 2);

    }
}
