

import java.util.concurrent.Exchanger;

/**
 * @author 张丰博
 */
public class Test {
    public static void main(String[] args) {
        Exchanger<String> stringExchanger = new Exchanger<>();
        new Thread(() -> {
            try {
                System.out.println("BB"+stringExchanger.exchange("BBB"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            System.out.println("AA"+stringExchanger.exchange("AAA"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




    }
}
