package org.drinkless;

import org.drinkless.user.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Start {

    private static final Properties appProperties = new Properties();

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
            appProperties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            file = new File(path, "reply-group.properties");
            User.replyGroupProperties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            file = new File(path, "reply-user.properties");
            User.replyUserProperties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws InterruptedException {
        String phone = appProperties.getProperty("主账号");
        String noListenerUser = appProperties.getProperty("不监听的用户","");
        Set<Long> noListener = new HashSet<>();
        if (noListenerUser != null && !noListenerUser.trim().equals("")) {
            for (String id : noListenerUser.split(",")) {
                noListener.add(Long.parseLong(id)) ;
            }
        }
        String receiveMsgGroup = appProperties.getProperty("接收消息提醒的群");
        if (phone == null || "".equals(phone)) {
            System.out.printf("必须配置主账号");
            return;
        }
        if (receiveMsgGroup == null || "".equals(receiveMsgGroup)) {
            System.out.printf("必须配置接收消息提醒的group");
            return;
        }
        String checkInterval = appProperties.getProperty("每个用户消息监听间隔", "10");
        new User(phone, noListener, receiveMsgGroup, Integer.parseInt(checkInterval)).start();
    }

}
