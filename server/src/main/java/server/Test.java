package server;

import com.alibaba.fastjson.JSON;
import com.graphbuilder.math.func.EFunction;
import entity.MailProps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Test {
    public static void main(String[] args) {
//        System.out.println(fun(6));


        ConcurrentHashMap<Integer, Integer> hashMap = new ConcurrentHashMap<>();

        for (int i = 0; i < 10000; i++) {
            hashMap.put(i,i+1);
            System.out.println("添加的数据: "+i+" : "+(i+1));
        }

        new Thread(()->{

//                Thread.sleep(50);

            int i = 10000;
            while (true){
                hashMap.put(i++,i);
                System.out.println("添加的数据: "+(i-1)+" : "+i);
            }
        }).start();

        System.out.println("开始移除数据;");
        Iterator<Map.Entry<Integer, Integer>> iterator = hashMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Integer, Integer> next = iterator.next();
            iterator.remove();
            System.out.println("移除的数据: "+next.getKey()+" : "+next.getValue());
        }



//        try {
//            Thread.sleep(5);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


    }

    static int fun(int n) {
        if (n <= 1){
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
