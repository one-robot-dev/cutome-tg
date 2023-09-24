package org.drinkless;

import org.drinkless.tdlib.TdApi;
import org.drinkless.user.MainUser;
import org.drinkless.user.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Start {

    public static Properties appProperties;

    public static Set<String> specGroupReply = Collections.emptySet();

    public static Set<String> likeMatchGroupReply = Collections.emptySet();

    public static Set<String> likeMatchUserReply = Collections.emptySet();

    public static Set<String> specUserReply = Collections.emptySet();

    private static final MainUser mainUser = new MainUser();

    private static final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(16);

    private final static Map<String, User> loginPhoneUserMap = new ConcurrentHashMap<>();

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) throws Exception {
        //加载配置
        boolean success = refreshProperties(mainUser);
        if (!success)  {
            return;
        }
        //加载保持登录状态的账号
        loadLoginUser();
        boolean pass = SafeUtil.checkKey(appProperties.getProperty("key"));
        if (!pass) {
            System.out.println("机器码: " + SafeUtil.getMachineCode());
            System.out.print("输入任意字符退出：");
            new Scanner(System.in).nextLine();
            return;
        }
        //登陆主用户
        mainUser.start();
        //启动定时任务
        startSchedule();
        while (!mainUser.canQuit) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        }
        loginPhoneUserMap.values().forEach(User::quit);
        for (User user : loginPhoneUserMap.values()) {
            while (!user.canQuit) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            }
        }
        System.exit(0);
    }

    public static boolean refreshProperties(MainUser mainUser) {
        try {
            loadProperties();
            String phone = appProperties.getProperty("主账号", "");
            String noListenerUserId = appProperties.getProperty("不监听的用户id","");
            String noListenerUserName = appProperties.getProperty("不监听的用户名","");
            String receiveMsgGroup = appProperties.getProperty("接收消息提醒的群", "");
            String checkInterval = appProperties.getProperty("每个用户消息监听间隔", "10");
            String specGroupReply = appProperties.getProperty("群聊针对回复关键词", "");
            String likeMatchGroupReply = appProperties.getProperty("群聊模糊匹配关键词", "");
            String specUserReply = appProperties.getProperty("私聊针对回复关键词", "");
            String likeMatchUserReply = appProperties.getProperty("私聊模糊匹配关键词", "");
            Start.specGroupReply = Arrays.stream(specGroupReply.split(",")).filter(s -> !s.trim().equals("")).collect(Collectors.toSet());
            Start.likeMatchGroupReply = Arrays.stream(likeMatchGroupReply.split(",")).filter(s -> !s.trim().equals("")).collect(Collectors.toSet());
            Start.specUserReply = Arrays.stream(specUserReply.split(",")).filter(s -> !s.trim().equals("")).collect(Collectors.toSet());
            Start.likeMatchUserReply = Arrays.stream(likeMatchUserReply.split(",")).filter(s -> !s.trim().equals("")).collect(Collectors.toSet());
            Set<Long> noListenUserId = new HashSet<>();
            if (!noListenerUserId.trim().equals("")) {
                for (String id : noListenerUserId.split(",")) {
                    noListenUserId.add(Long.parseLong(id)) ;
                }
            }
            if (phone.trim().equals("")) {
                System.out.print("必须配置主账号");
                return false;
            }
            Set<String> noListerName = new HashSet<>(Arrays.asList(noListenerUserName.split(",")));
            mainUser.phoneNumber = mainUser.phoneNumber == null ? phone : mainUser.phoneNumber;
            mainUser.noListenUserId = noListenUserId;
            mainUser.noListenUserName = noListerName;
            mainUser.receiveGroupName = receiveMsgGroup;
            mainUser.checkInterval = Integer.parseInt(checkInterval);
            System.out.println("=========================================配置加载成功====================================================");
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadProperties() throws Exception{
        String path = "./";
        File file = new File(path,"app.properties");
        appProperties = new Properties();
        appProperties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        file = new File(path, "reply-group.properties");
        Properties replyGroupProperties = new Properties();
        replyGroupProperties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        file = new File(path, "reply-user.properties");
        Properties replyUserProperties = new Properties();
        replyUserProperties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        MainUser.replyGroupProperties = replyGroupProperties;
        MainUser.replyUserProperties = replyUserProperties;
    }

    private static void loadLoginUser() {
        String loginUser =  appProperties.getProperty("保持登录状态的账号");
        if (loginUser == null || loginUser.trim().equals("")) {
            return;
        }
        Set<String> loginUserSet = new HashSet<>(Arrays.asList(loginUser.split(",")));
        loginUserSet.remove(appProperties.getProperty("主账号"));
        for (String phone : loginUserSet) {
            User user = new User();
            new Thread(() -> {
                user.phoneNumber = phone;
                try {
                    user.start();
                    while (!user.canQuit) {
                        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
                    }
                } catch (InterruptedException e) {
                    System.out.println(phone + "账号失败退出了");
                    user.haveAuthorization = true;
                }
            }).start();
            try {
                while (!user.haveAuthorization) {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                }
                loginPhoneUserMap.put(phone, user);
            } catch (InterruptedException e) {
                System.out.println(phone + "账号等待登录异常");
            }
        }
    }

    private static void startSchedule() {
        //主账号发送心跳
        schedule.scheduleWithFixedDelay(() -> {
            mainUser.client.send(new TdApi.SetOption("online", new TdApi.OptionValueBoolean(true)), obj -> {
                if (!(obj instanceof TdApi.Ok)) {
                    mainUser.print(mainUser.phoneNumber + "主-账号设置登录状态失败！！！！！！！！！！！！！！！！！！！！！");
                }
            });
        }, 1, 2, TimeUnit.MINUTES);

        //保持登录的账号发送心跳
        for (User user : loginPhoneUserMap.values()) {
            schedule.scheduleWithFixedDelay(() -> {
                user.client.send(new TdApi.SetOption("online", new TdApi.OptionValueBoolean(true)), obj -> {
                    if (!(obj instanceof TdApi.Ok)) {
                        mainUser.print(user.phoneNumber + "子-账号设置登录状态失败！！！！！！！！！！！！！！！！！！！！！");
                    }
                });
            }, 1, 5, TimeUnit.MINUTES);
        }
    }
}
