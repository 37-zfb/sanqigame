package server;

import com.alibaba.fastjson.JSON;
import server.model.MailProps;

import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        ArrayList<MailProps> mailProps = new ArrayList<>();
        mailProps.add(new MailProps(1,2));
        mailProps.add(new MailProps(1,2));
        mailProps.add(new MailProps(1,2));
        mailProps.add(new MailProps(1,2));
        mailProps.add(new MailProps(1,2));
        mailProps.add(new MailProps(1,2));
        mailProps.add(new MailProps(1,2));

        System.out.println(JSON.toJSONString(mailProps));

    }
}
