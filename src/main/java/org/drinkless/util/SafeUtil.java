package org.drinkless.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SafeUtil {

    private static final String PASS_WORD = "guan888";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("请输入密码：");
            String passWord = scanner.nextLine().trim();
            if (PASS_WORD.equals(passWord)) {
                break;
            }
            System.out.println("密码不正确");
        }
        while (true) {
            System.out.print("输入机器码：");
            String machineCode = scanner.nextLine().trim();
            System.out.print("输入截止时间：");
            String endTime = scanner.nextLine().trim();
            System.out.println();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long time = format.parse(endTime).getTime();
            SafeUserInfo info = new SafeUserInfo();
            info.setMachineCode(machineCode);
            info.setEndTime(time);
            String key = encodeUserInfo(info);
            System.out.println(key);
            System.out.println("输入1继续，输入其他退出：");
            String flag = scanner.nextLine().trim();
            if (!"1".equals(flag)) {
                break;
            }
        }
    }

    public static boolean checkKey(String key) throws Exception {
        try {
            if (System.currentTimeMillis() - getNetWorkTime() > TimeUnit.HOURS.toMillis(1)) {
                System.out.println("系统时间与网络时间不匹配");
                return false;
            }
            if (key == null) {
                System.out.println("密钥未配置！！！");
                return false;
            }
            SafeUserInfo info = decodeUserInfo(key);
            if (!getMachineCode().equals(info.getMachineCode())) {
                System.out.println("密钥与本机不匹配！！！");
                return false;
            }
            if (System.currentTimeMillis() > info.getEndTime()) {
                System.out.println("密钥已过期！！！");
                return false;
            }
        } catch (Exception e) {
            System.out.println("密钥异常！！！");
            return false;
        }
        return true;
    }

    public static String getMachineCode() throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] { "wmic", "cpu", "get", "ProcessorId"});
        process.getOutputStream().close();
        StringBuilder code = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
            String line;
            while ((line = reader.readLine()) != null) {
                code.append(line);
            }
        }
        return DESUtil.encryptByDES(code.toString());
    }

    public static String encodeUserInfo(SafeUserInfo info) throws Exception {
        return DESUtil.encryptByDES(info.toKey());
    }

    public static SafeUserInfo decodeUserInfo(String key) throws Exception {
        return SafeUserInfo.fromKey(DESUtil.decryptByDES(key));
    }

    public static long getNetWorkTime() throws IOException {
        // 创建URL对象
        URL url = new URL("http://www.baidu.com");
        // 打开连接
        URLConnection conn = url.openConnection();
        // 获取服务器时间
        return conn.getDate();
    }
}
