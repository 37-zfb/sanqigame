import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author 张丰博
 */
public class Test {
    public static void main(String[] args) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();
        Date time = calendar.getTime();
        System.out.println(format.format(time));

        calendar.set(Calendar.DATE,calendar.get(Calendar.DATE)-20);
        Date time1 = calendar.getTime();
        System.out.println(format.format(time1));

    }
}
