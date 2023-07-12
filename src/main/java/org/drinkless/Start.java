package org.drinkless;

import org.drinkless.user.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Start {

    private static final Properties properties = new Properties();

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
        try {
            String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            System.out.printf(path);
            File file = new File(path,"app.properties");
            properties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws InterruptedException {
        String phone = properties.getProperty("主账号");
        Set<String> noListener = new HashSet<>(Arrays.asList(properties.getProperty("不监听的用户").split(",")));
        String receiveMsgGroup = properties.getProperty("接收消息提醒的群");
        if (phone == null || "".equals(phone)) {
            System.out.printf("必须配置主账号");
            return;
        }
        if (receiveMsgGroup == null || "".equals(receiveMsgGroup)) {
            System.out.printf("必须配置接收消息提醒的group");
            return;
        }
        new User(phone, noListener, receiveMsgGroup).start();
    }

}
