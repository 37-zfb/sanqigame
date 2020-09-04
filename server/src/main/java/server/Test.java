package server;

import com.alibaba.fastjson.JSON;
import com.graphbuilder.math.func.EFunction;
import entity.MailProps;

import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        System.out.println(fun(6));
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
