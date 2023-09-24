package org.drinkless.util;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class DESUtil {

    public static final String algorithm = "DES";

    // 这是默认模式
     public static final String transformation = "DES/ECB/PKCS5Padding";
    // 使用CBC模式, 在初始化Cipher对象时, 需要增加参数, 初始化向量IV : IvParameterSpec iv = new IvParameterSpec(key.getBytes());
//     public static final String transformation = "DES/CBC/PKCS5Padding";
    // NOPadding: 使用NOPadding模式时, 原文长度必须是8byte的整数倍
//    public static final String transformation = "DES/ECB/NOPadding";
    public static final String key = "sdgasdga";

    // QO2klVpoYT8=
    // QO2klVpoYT8=
    public static void main(String[] args) throws Exception {

        String encryptByDES = encryptByDES("sgasdhgasdhasdhasdhasdgasdgasd");

        System.out.println(encryptByDES);
        String decryptByDES = decryptByDES(encryptByDES);
        System.out.println(decryptByDES);
    }

    public static String encryptByDES(String content) throws Exception {
        // 获取Cipher
        Cipher cipher = Cipher.getInstance(transformation);

        // 指定密钥规则
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);

        // 指定模式(加密)和密钥
        // 创建初始向量
        IvParameterSpec iv = new IvParameterSpec(key.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        //  cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        // 加密
        byte[] bytes = cipher.doFinal(content.getBytes());
        // 输出加密后的数据
        // com.sun.org.apache.xml.internal.security.utils.Base64
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String decryptByDES(String encrypted) throws Exception {
        // 获取Cipher
        Cipher cipher = Cipher.getInstance(transformation);

        // 指定密钥规则
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);

        // 指定模式(解密)和密钥
        // 创建初始向量
        IvParameterSpec iv = new IvParameterSpec(key.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        //  cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        // 解码密文
        // com.sun.org.apache.xml.internal.security.utils.Base64
        byte[] decode = Base64.getDecoder().decode(encrypted);
        // 解密
        byte[] bytes = cipher.doFinal(decode);
        // 输出解密后的数据
        return new String(bytes);
    }

}
