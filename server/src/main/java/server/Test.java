package server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Test {
    public static void main(String[] args) {
//        System.out.println(fun(6));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date zero = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:ss");
        System.out.println(dateFormat.format(zero));
    }


    static int fun(int n) {
        if (n <= 1) {
            return n;
        }

        int result = 3;
        n = n - 1;
        while (n > 1) {
            result = (result % 2) * 3 + (result / 2) * 5;
            n = n - 1;
        }
        return result;
    }

}
