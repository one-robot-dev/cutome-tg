package org.drinkless;

import org.drinkless.user.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Start {

    private static Properties appProperties;

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) throws Exception {
        User user = new User();
        boolean success = refreshProperties(user);
        if (!success)  {
            return;
        }
        user.start();
    }

    private static void loadProperties() throws Exception{
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        File file = new File(path,"app.properties");
        appProperties = new Properties();
        appProperties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        file = new File(path, "reply-group.properties");
        Properties replyGroupProperties = new Properties();
        replyGroupProperties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        file = new File(path, "reply-user.properties");
        Properties replyUserProperties = new Properties();
        replyUserProperties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        User.replyGroupProperties = replyGroupProperties;
        User.replyUserProperties = replyUserProperties;
    }

    public static boolean refreshProperties(User user) {
        try {
            loadProperties();
            String phone = appProperties.getProperty("主账号");
            String noListenerUserId = appProperties.getProperty("不监听的用户id","");
            String noListenerUserName = appProperties.getProperty("不监听的用户名","");
            String receiveMsgGroup = appProperties.getProperty("接收消息提醒的群");
            String checkInterval = appProperties.getProperty("每个用户消息监听间隔", "10");
            Set<Long> noListenUserId = new HashSet<>();
            if (noListenerUserId != null && !noListenerUserId.trim().equals("")) {
                for (String id : noListenerUserId.split(",")) {
                    noListenUserId.add(Long.parseLong(id)) ;
                }
            }
            if (phone == null || "".equals(phone)) {
                System.out.print("必须配置主账号");
                return false;
            }
            if (receiveMsgGroup == null || "".equals(receiveMsgGroup)) {
                System.out.print("必须配置接收消息提醒的group");
                return false;
            }
            Set<String> noListerName = new HashSet<>(Arrays.asList(noListenerUserName.split(",")));
            user.phoneNumber = user.phoneNumber == null ? phone : user.phoneNumber;
            user.noListenUserId = noListenUserId;
            user.noListenUserName = noListerName;
            user.receiveGroupName = receiveMsgGroup;
            user.checkInterval = Integer.parseInt(checkInterval);
            System.out.println("=========================================配置加载成功====================================================");
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
