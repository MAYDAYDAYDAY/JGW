package com.example.utils;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class MailCodeCache {
    private static HashMap<String, String> codeCaches = new HashMap<>();

    public static void setCache(String mail, String code){
        codeCaches.put(mail, code);
    }

    public static boolean validateCode(String mail, String code){
        //mail 去除空格
        mail = mail.replaceAll(" ", "");
        return codeCaches.containsKey(mail) && codeCaches.get(mail).equals(code);
    }

    @Scheduled(fixedRate = 1000*60*5) //每5分钟清理一次
    public void task(){
        long currentTimeMillis = System.currentTimeMillis();
        Iterator<Map.Entry<String, String>> iterator = codeCaches.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String mail = entry.getKey();
            String code = entry.getValue();

            if (currentTimeMillis - Long.parseLong(code) > 1000 * 120) {
                iterator.remove(); // 使用迭代器安全地删除元素
            }
        }
    }
}
